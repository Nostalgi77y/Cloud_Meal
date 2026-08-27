# AI 私厨接入执行与验收记录

## 执行结果

完成时间：2026-08-28（Asia/Shanghai）。

| 模块 | 验证 | 结果 |
| --- | --- | --- |
| 云膳后端 | `mvn test` | 8 项通过，新增正式账号注册与错误密码测试 |
| AI 私厨后端 | `mvn test` | 2 项通过 |
| 微信小程序 | `npm run build:mp-weixin` | 生产构建通过 |
| Vue3 用户网页 | `npm run build` | TypeScript 检查与 Vite 生产构建通过，0 漏洞 |
| Docker 全栈 | `docker compose ... up -d --build` | MySQL/Redis 健康，server/admin/web 均启动 |
| AI 用户隔离 | A 用户会话由 B 用户读取 | 返回 400“会话不存在或无权访问” |
| 正式账号 | 用户名/手机号注册、密码登录、资料查询 | 两种账号均可注册登录，JWT 中 userId 与数据库主键一致 |
| 地址隔离 | A 用户创建默认地址，B 用户查询 | A 为 1 条，B 为 0 条 |
| 真实 AI 链路 | 鸡胸肉/西兰花/米饭、少油高蛋白 | 约 26 秒返回 3 个菜谱和 1 个真实在售推荐 |
| 交易衔接 | 推荐 `dishId` 加入购物车 | 成功匹配“云膳香煎鸡排”，购物车可查询 |
| 会话记忆 | 分析后查询 turns | 当前用户新增 1 条，其他用户不可读 |
| 页面验收 | 应用内浏览器桌面视口 | 正式登录、独立 AI 页面、单一悬浮入口、设置/地址/优惠券/AI 历史正常 |

本轮联调使用两个临时正式账号完成跨用户验证；验收结束后按精确 userId 清理临时地址、AI 会话和账号，不触碰演示用户或业务数据。

## 发现并解决的问题

1. 两个 Spring Boot 服务均占用 8080：AI 服务调整为 8081。
2. AI 会话原先没有用户归属：新增 `user_id`、组合索引和用户级仓储查询。
3. AI 可生成不存在的菜：改为云膳读取实时菜单并在本地确定性匹配真实 `dishId`。
4. 模型调用曾超过 120 秒：确认基础百炼链路 6.2 秒可用，问题来自默认思考、重复搜索和双层重试；改为 Flash、关闭思考、单次搜索与单层超时后降到约 20 秒。
5. Spring 包装异常导致超时落成 `SYSTEM_ERROR`：扩展异常链识别并返回 AI 专用错误。
6. Vue 图片路径曾错误移除 `/api`：统一处理相对地址、`/api/...` 和绝对 URL，实际图片加载验证通过。
7. Docker Web 构建上下文约 97 MB：新增 `.dockerignore` 后降至 KB 级。
8. 一次 YAML 缩进错误只在容器启动时出现：修复并增加 `ApplicationYamlTest`，以后测试阶段即可拦截。
9. Web 体验登录不能形成真实用户边界：新增 Flyway V6、BCrypt 正式账号服务、资料接口和前端登录注册页。
10. 设置页原本会因 AI 服务异常整体失败：改为 AI 历史局部降级，地址和优惠券继续可用。

## 当前运行地址

- 商家管理端：`http://127.0.0.1/`
- Vue3 用户网页：`http://127.0.0.1:81/`
- 云膳 API：`http://127.0.0.1:8080/api`
- AI 私厨健康检查：`http://127.0.0.1:8081/api/health`
- 微信小程序产物：`cloud-meal-user/dist/build/mp-weixin`

## 启动顺序

```powershell
# 1. AI 私厨（RAG 8001 可选，未启动会自动降级）
cd "H:\AI开发\ai-private-chef\backend"
mvn spring-boot:run

# 2. 云膳全栈
cd "H:\杨家永作业\苍穹外卖"
docker compose -f deploy\docker-compose.full.yml up -d --build

# 3. 如需重新编译微信小程序
cd cloud-meal-user
npm run build:mp-weixin
```

微信开发者工具导入 `cloud-meal-user/dist/build/mp-weixin`。本地开发需开启“不校验合法域名”；真机/发布必须把云膳 API 部署为已备案且已配置业务域名的 HTTPS 地址。

## 尚需用户提供的生产条件

- 正式域名、HTTPS 证书与服务器；
- 小程序 AppID/AppSecret；
- 真实支付所需商户号、API v3 密钥、商户私钥/证书与 HTTPS 回调地址；
- 生产级 `CLOUD_MEAL_AI_SERVICE_TOKEN`；
- 短信供应商和微信开放平台配置（仅后续增加验证码登录或微信账号绑定时需要）。

这些条件不影响当前本地第一版运行，但缺少时不能进行小程序正式发布和真实微信支付验收。
