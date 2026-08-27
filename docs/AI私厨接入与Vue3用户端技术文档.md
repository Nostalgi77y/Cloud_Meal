# AI 私厨接入与 Vue3 用户端技术文档

## 1. 交付范围

本次将 `H:\AI开发\ai-private-chef` 作为独立 AI 服务接入云膳外卖，并新增：

- 微信小程序全局 AI 私厨悬浮入口与独立分析页；
- Vue 3 + TypeScript + Pinia 响应式用户网页；
- 用户名或手机号 + 密码的正式注册登录、个人资料和路由鉴权；
- 与小程序数据一致的地址、优惠券和 AI 历史设置页；
- 云膳统一 AI BFF 接口、用户身份透传、会话隔离；
- AI 结果与云膳真实在售菜品的确定性匹配及一键加购。

## 2. 架构与端口

```text
微信小程序 / Vue3 Web(:81)
          │ JWT + /api/user/ai/**
          ▼
云膳 Spring Boot(:8080) ── MySQL / Redis / RabbitMQ
          │ X-Cloud-User-Id + X-Service-Token
          ▼
AI 私厨 Spring Boot(:8081) ── H2 会话库
          │                 ├── DashScope qwen3.8-flash
          │                 ├── Tavily 菜谱证据
          └─────────────────└── RAG API(:8001，可降级)
```

端口冲突已解除：云膳后端保留 `8080`，AI 私厨改为 `8081`，新用户网页 Docker 入口为 `81`。

## 3. 数据一致性边界

| 数据 | 唯一数据源 | 约束 |
| --- | --- | --- |
| 菜品、价格、库存、购物车、地址、优惠券、订单、支付 | 云膳 MySQL | AI 服务只读菜单上下文，不直接写交易表 |
| 菜单查询缓存 | 云膳 Redis | 菜品修改仍由原 ProductService 负责缓存失效 |
| AI 会话与对话轮次 | AI 私厨 H2 | 每条记录带 `user_id`，所有查询与删除同时校验用户 |
| AI 生成菜谱 | 请求结果/H2 会话 | 不能直接成为交易数据 |
| Web 账号与个人资料 | 云膳 MySQL `user` 表 | `username`/`phone` 唯一，密码仅保存 BCrypt 哈希 |

AI 返回后，`AiMenuMatcher` 只在 `status=1 && stock>0` 的当前数据库菜品中匹配，最终返回真实 `dishId`、实时价格、图片和库存。加入购物车仍走原 `CartService`，再次检查上架状态与库存；提交订单仍由原事务重新计算价格并原子扣减库存。因此 AI 不能绕过交易规则，也不会制造不存在的菜品或价格。

## 4. 统一外部接口

所有客户端只访问云膳 `/api`，响应统一为：

```json
{"code":"SUCCESS","message":"success","data":{}}
```

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/user/ai/analyze/text` | 文字食材/偏好分析 |
| POST | `/user/ai/analyze` | multipart 图片与文字分析，图片最大 5 MB |
| POST | `/user/ai/conversations` | 创建当前用户会话 |
| GET | `/user/ai/conversations` | 当前用户会话列表 |
| GET | `/user/ai/conversations/{id}/turns` | 当前用户会话详情 |
| DELETE | `/user/ai/conversations/{id}/memory` | 清空会话记忆 |
| DELETE | `/user/ai/conversations/{id}` | 删除会话 |
| POST | `/user/auth/register` | 用户名或手机号 + 密码注册并签发 JWT |
| POST | `/user/auth/login` | 正式账号登录 |
| GET/PUT | `/user/profile` | 查询或修改当前账号资料 |

常用错误码：`AI_INPUT_REQUIRED`、`AI_INPUT_TOO_LONG`、`AI_IMAGE_TOO_LARGE`、`AI_SERVICE_TIMEOUT`、`AI_SERVICE_UNAVAILABLE`、`AI_SERVICE_ERROR`。客户端显示 `message`，但不能在失败时自动提交购物车或订单。

## 5. 内部身份与安全

- 客户端 JWT 只由云膳校验，AI 服务不接触用户 JWT；
- Web 密码使用 BCrypt 单向哈希，登录失败不区分账号不存在和密码错误；
- Flyway V6 为 `user` 增加用户名、密码哈希及用户名/手机号唯一索引；
- 云膳从安全上下文取得用户 ID，以 `X-Cloud-User-Id` 传给 AI；
- 生产环境必须在两个项目配置同一个 `CLOUD_MEAL_AI_SERVICE_TOKEN`；
- AI 服务配置令牌后，缺失/错误令牌的内部请求返回 401；
- 会话仓储方法均以 `userId + conversationId` 作为访问条件，阻止跨用户读取和删除；
- 图片只接收 `image/*` 且最大 5 MB，文字与偏好分别限制 2000/1000 字。

## 6. 性能与降级策略

- 默认模型使用 `qwen3.8-flash`，关闭思考模式，单次模型超时 55 秒；
- 移除双层重试和重复搜索，避免最坏多轮 90 秒等待；
- 云膳 AI BFF 连接超时 3 秒、读取超时 70 秒，Web 请求超时 75 秒；
- RAG 服务不可用时返回空证据，Tavily 不可用时以 `SEARCH_UNAVAILABLE` 明确降级；
- 菜单上下文最多传 30 道当前在售菜品，降低 Token 与延迟；
- Vue 页面按路由拆包，Docker 忽略 `node_modules` 和 `dist`，构建上下文由约 97 MB 降至 KB 级。

## 7. 客户端实现

微信小程序在菜单、购物车、订单、我的四个主页面挂载 `AiChefFloat`，点击进入 `/pages/ai-chef/index`。页面支持拍摄/相册、文字偏好、“直接点”与“自己做”双模式，以及真实菜品一键加入购物车。

Vue3 网页位于 `cloud-meal-web`，使用 Vue 3、TypeScript、Pinia、Vue Router、Axios、Vite、Nginx。第一版包含正式注册登录、首页菜单、购物车、订单、独立 AI 页面和“我的设置”。设置页复用云膳现有接口管理个人资料、收货地址、优惠券和当前账号的 AI 历史。

全站仅保留一个 AI 私厨悬浮入口；进入 `/ai-chef` 后显示独立会话工作区，不再重复展示悬浮按钮。Pinia 仅保存当前 JWT、`userId` 和展示昵称；地址、优惠券、订单和 AI 会话全部以服务端当前 JWT 解析出的 `userId` 为准，客户端传入的用户 ID 不参与数据归属判断。

设置页对 AI 服务采用局部降级：AI 服务不可用时，个人资料、地址和优惠券仍可正常加载，只提示 AI 历史暂不可用。

## 8. 配置

云膳：`AI_CHEF_BASE_URL`、`CLOUD_MEAL_AI_SERVICE_TOKEN`、`AI_CHEF_CONNECT_TIMEOUT_MS`、`AI_CHEF_READ_TIMEOUT_MS`。

AI 私厨：`SERVER_PORT`、`DASHSCOPE_API_KEY`、`CHEF_MODEL_NAME`、`CHEF_MODEL_TIMEOUT_SECONDS`、`TAVILY_API_KEY`、`RAG_BASE_URL`、`CLOUD_MEAL_AI_SERVICE_TOKEN`。

真实密钥只放本地 `.env` 或部署平台密钥管理中，不得提交 Git。
