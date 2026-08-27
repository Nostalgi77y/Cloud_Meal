<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { Bot, ShoppingBag, UserRound, Settings } from "@lucide/vue";
import { useUserStore } from "@/stores/user";
import { useCartStore } from "@/stores/cart";
import CartDrawer from "@/components/CartDrawer.vue";
const router = useRouter(),
  route = useRoute(),
  user = useUserStore(),
  cart = useCartStore(),
  showCart = ref(false);
function requireLogin(path: string) {
  router.push(
    user.loggedIn ? path : { path: "/auth", query: { redirect: path } },
  );
}
async function openCart() {
  if (!user.loggedIn) {
    await router.push({ path: "/auth", query: { redirect: "/" } });
    return;
  }
  await cart.load();
  showCart.value = true;
}
function onExpired() {
  user.logout();
  cart.items = [];
  router.push("/auth");
}
onMounted(() => {
  window.addEventListener("cloud-auth-expired", onExpired);
  if (user.loggedIn) cart.load();
});
onBeforeUnmount(() =>
  window.removeEventListener("cloud-auth-expired", onExpired),
);
</script>
<template>
  <div class="app">
    <header class="site-header">
      <RouterLink to="/" class="brand"
        ><span>膳</span>
        <div><b>云膳外卖</b><small>CLOUD MEAL</small></div></RouterLink
      >
      <nav>
        <RouterLink to="/">在线点餐</RouterLink
        ><RouterLink to="/orders">我的订单</RouterLink
        ><RouterLink to="/settings">我的设置</RouterLink>
      </nav>
      <div class="header-actions">
        <button class="cart-button" @click="openCart">
          <ShoppingBag :size="18" />购物车 <em>{{ cart.count }}</em></button
        ><button
          class="user-button"
          @click="requireLogin(user.loggedIn ? '/settings' : '/auth')"
        >
          <component :is="user.loggedIn ? Settings : UserRound" :size="17" />{{
            user.loggedIn ? user.name : "登录 / 注册"
          }}
        </button>
      </div>
    </header>
    <main><RouterView @open-cart="openCart" /></main>
    <button
      v-if="route.path !== '/ai-chef' && route.path !== '/auth'"
      class="floating-ai"
      title="进入AI私厨"
      @click="requireLogin('/ai-chef')"
    >
      <Bot /><span>AI私厨</span></button
    ><CartDrawer v-model="showCart" />
  </div>
</template>
