<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  UserRound,
  MapPin,
  Ticket,
  ChefHat,
  LogOut,
  Plus,
  Trash2,
  Star,
} from "@lucide/vue";
import { api } from "@/api/http";
import { useUserStore } from "@/stores/user";
import { useCartStore } from "@/stores/cart";
import type {
  Address,
  AiConversation,
  Coupon,
  UserCoupon,
  UserProfile,
} from "@/types";
type Tab = "profile" | "addresses" | "coupons" | "ai";
const route = useRoute(),
  router = useRouter(),
  user = useUserStore(),
  cart = useCartStore(),
  tab = ref<Tab>("profile"),
  loading = ref(true),
  message = ref("");
const profile = ref<UserProfile>(),
  addresses = ref<Address[]>([]),
  available = ref<Coupon[]>([]),
  mine = ref<UserCoupon[]>([]),
  conversations = ref<AiConversation[]>([]);
const profileForm = reactive({ nickname: "", avatar: "" });
const addressForm = reactive({
  id: "",
  consignee: "",
  phone: "",
  province: "",
  city: "",
  district: "",
  detail: "",
  isDefault: false,
});
async function loadAll() {
  loading.value = true;
  try {
    const [p, a, c, m] = await Promise.all([
      api<UserProfile>({ url: "/user/profile" }),
      api<Address[]>({ url: "/user/addresses" }),
      api<Coupon[]>({ url: "/user/coupons/available" }),
      api<UserCoupon[]>({ url: "/user/coupons/mine" }),
    ]);
    profile.value = p;
    Object.assign(profileForm, {
      nickname: p.nickname,
      avatar: p.avatar || "",
    });
    addresses.value = a;
    available.value = c;
    mine.value = m;
    try {
      conversations.value = await api<AiConversation[]>({
        url: "/user/ai/conversations",
      });
    } catch {
      conversations.value = [];
      message.value = "基础设置已加载，AI 历史服务暂不可用";
    }
  } catch (error) {
    message.value = (error as Error).message;
  } finally {
    loading.value = false;
  }
}
async function saveProfile() {
  const saved = await api<UserProfile>({
    url: "/user/profile",
    method: "PUT",
    data: profileForm,
  });
  profile.value = saved;
  user.rename(saved.nickname);
  message.value = "个人资料已保存";
}
function resetAddress() {
  Object.assign(addressForm, {
    id: "",
    consignee: "",
    phone: "",
    province: "",
    city: "",
    district: "",
    detail: "",
    isDefault: false,
  });
}
function editAddress(item: Address) {
  Object.assign(addressForm, { ...item, isDefault: item.isDefault === 1 });
}
async function saveAddress() {
  const data = { ...addressForm, id: undefined };
  if (addressForm.id)
    await api<Address>({
      url: `/user/addresses/${addressForm.id}`,
      method: "PUT",
      data,
    });
  else await api<Address>({ url: "/user/addresses", method: "POST", data });
  addresses.value = await api<Address[]>({ url: "/user/addresses" });
  resetAddress();
  message.value = "收货地址已保存";
}
async function deleteAddress(id: string) {
  if (!confirm("确定删除该收货地址吗？")) return;
  await api<void>({ url: `/user/addresses/${id}`, method: "DELETE" });
  addresses.value = await api<Address[]>({ url: "/user/addresses" });
}
async function setDefault(id: string) {
  await api<void>({ url: `/user/addresses/${id}/default`, method: "PUT" });
  addresses.value = await api<Address[]>({ url: "/user/addresses" });
}
async function receive(id: string) {
  try {
    await api<void>({ url: `/user/coupons/${id}/receive`, method: "POST" });
    mine.value = await api<UserCoupon[]>({ url: "/user/coupons/mine" });
    message.value = "优惠券领取成功";
  } catch (e) {
    message.value = (e as Error).message;
  }
}
function logout() {
  user.logout();
  cart.items = [];
  router.replace("/");
}
onMounted(async () => {
  const requested = route.query.tab;
  if (
    requested === "addresses" ||
    requested === "coupons" ||
    requested === "ai"
  )
    tab.value = requested;
  await loadAll();
});
</script>
<template>
  <section class="settings-page">
    <div class="settings-head">
      <span>MY CLOUD MEAL</span>
      <h1>我的设置</h1>
      <p>管理当前账号的资料、地址、优惠券与 AI 私厨历史。</p>
    </div>
    <div class="settings-layout">
      <aside>
        <button :class="{ active: tab === 'profile' }" @click="tab = 'profile'">
          <UserRound />个人资料</button
        ><button
          :class="{ active: tab === 'addresses' }"
          @click="tab = 'addresses'"
        >
          <MapPin />我的地址</button
        ><button
          :class="{ active: tab === 'coupons' }"
          @click="tab = 'coupons'"
        >
          <Ticket />我的优惠券</button
        ><button :class="{ active: tab === 'ai' }" @click="tab = 'ai'">
          <ChefHat />AI 历史</button
        ><button class="logout" @click="logout"><LogOut />退出登录</button>
      </aside>
      <main>
        <div v-if="loading" class="state">正在读取当前账号数据…</div>
        <template v-else
          ><p v-if="message" class="settings-message">{{ message }}</p>
          <section v-if="tab === 'profile'" class="settings-panel">
            <h2>个人资料</h2>
            <div class="account-id">
              <b>{{ profile?.nickname }}</b
              ><span>账号：{{ profile?.account }}</span
              ><small>云膳 userId：{{ profile?.userId }}</small>
            </div>
            <form class="settings-form" @submit.prevent="saveProfile">
              <label
                >昵称<input
                  v-model="profileForm.nickname"
                  required
                  maxlength="80" /></label
              ><label
                >头像地址<input
                  v-model="profileForm.avatar"
                  maxlength="500"
                  placeholder="可选，填写 HTTPS 图片地址" /></label
              ><button>保存资料</button>
            </form>
          </section>
          <section v-if="tab === 'addresses'" class="settings-panel">
            <div class="panel-title">
              <h2>我的地址</h2>
              <button @click="resetAddress"><Plus />新增地址</button>
            </div>
            <div class="address-grid">
              <article v-for="item in addresses" :key="item.id">
                <div>
                  <b>{{ item.consignee }} · {{ item.phone }}</b>
                  <p>
                    {{ item.province }}{{ item.city }}{{ item.district
                    }}{{ item.detail }}
                  </p>
                  <span v-if="item.isDefault === 1">默认地址</span>
                </div>
                <footer>
                  <button
                    v-if="item.isDefault !== 1"
                    @click="setDefault(item.id)"
                  >
                    <Star />设为默认</button
                  ><button @click="editAddress(item)">编辑</button
                  ><button @click="deleteAddress(item.id)">
                    <Trash2 />删除
                  </button>
                </footer>
              </article>
            </div>
            <form class="address-form" @submit.prevent="saveAddress">
              <h3>{{ addressForm.id ? "编辑地址" : "新增地址" }}</h3>
              <input
                v-model="addressForm.consignee"
                required
                maxlength="50"
                placeholder="收货人"
              /><input
                v-model="addressForm.phone"
                required
                pattern="1[3-9]\d{9}"
                placeholder="手机号"
              />
              <div>
                <input
                  v-model="addressForm.province"
                  maxlength="50"
                  placeholder="省"
                /><input
                  v-model="addressForm.city"
                  maxlength="50"
                  placeholder="市"
                /><input
                  v-model="addressForm.district"
                  maxlength="50"
                  placeholder="区/县"
                />
              </div>
              <input
                v-model="addressForm.detail"
                required
                maxlength="255"
                placeholder="详细地址"
              /><label
                ><input
                  v-model="addressForm.isDefault"
                  type="checkbox"
                />设为默认地址</label
              ><button>{{ addressForm.id ? "保存修改" : "添加地址" }}</button>
            </form>
          </section>
          <section v-if="tab === 'coupons'" class="settings-panel">
            <h2>我的优惠券</h2>
            <h3>已领取</h3>
            <div class="coupon-grid">
              <article v-for="item in mine" :key="item.userCouponId">
                <strong>¥{{ item.discountAmount }}</strong>
                <div>
                  <b>{{ item.name }}</b
                  ><span>满 ¥{{ item.thresholdAmount }} 可用</span
                  ><small>有效期至 {{ item.validUntil?.slice(0, 10) }}</small>
                </div>
                <em>{{
                  item.status === "UNUSED"
                    ? "未使用"
                    : item.status === "USED"
                      ? "已使用"
                      : "已过期"
                }}</em>
              </article>
              <p v-if="!mine.length">还没有领取优惠券。</p>
            </div>
            <h3>可领取</h3>
            <div class="coupon-grid">
              <article v-for="item in available" :key="item.id">
                <strong>¥{{ item.discountAmount }}</strong>
                <div>
                  <b>{{ item.name }}</b
                  ><span>满 ¥{{ item.thresholdAmount }} 可用</span
                  ><small>有效期至 {{ item.validUntil?.slice(0, 10) }}</small>
                </div>
                <button @click="receive(item.id)">领取</button>
              </article>
            </div>
          </section>
          <section v-if="tab === 'ai'" class="settings-panel">
            <div class="panel-title">
              <div>
                <h2>AI 私厨历史</h2>
                <p>
                  共 {{ conversations.length }} 个仅属于 userId
                  {{ profile?.userId }} 的会话。
                </p>
              </div>
            </div>
            <div class="ai-history-cards">
              <article v-for="item in conversations" :key="item.id">
                <ChefHat />
                <div>
                  <b>{{ item.title }}</b
                  ><small>{{
                    item.updatedAt?.replace("T", " ").slice(0, 16)
                  }}</small>
                </div>
              </article>
              <p v-if="!conversations.length">暂无 AI 历史会话。</p>
            </div>
          </section></template
        >
      </main>
    </div>
  </section>
</template>
