<script setup lang="ts">
import { ref,onMounted } from 'vue'
import { RouterLink,RouterView } from 'vue-router'
import { Bot,ShoppingBag,UserRound } from '@lucide/vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import AiChefDrawer from '@/components/AiChefDrawer.vue'
import CartDrawer from '@/components/CartDrawer.vue'
const user=useUserStore(),cart=useCartStore(),showAi=ref(false),showCart=ref(false)
async function ensureLogin(){if(!user.loggedIn)await user.login();await cart.load()}
onMounted(()=>{window.addEventListener('cloud-auth-expired',user.logout);if(user.loggedIn)cart.load()})
</script>
<template><div class="app"><header class="site-header"><RouterLink to="/" class="brand"><span>膳</span><div><b>云膳外卖</b><small>CLOUD MEAL</small></div></RouterLink><nav><RouterLink to="/">在线点餐</RouterLink><RouterLink to="/orders">我的订单</RouterLink></nav><div class="header-actions"><button class="ghost" @click="showAi=true;ensureLogin()"><Bot :size="18"/>AI私厨</button><button class="cart-button" @click="showCart=true;ensureLogin()"><ShoppingBag :size="18"/>购物车 <em>{{cart.count}}</em></button><button class="user-button" @click="user.loggedIn?user.logout():ensureLogin()"><UserRound :size="17"/>{{user.loggedIn?user.name:'体验登录'}}</button></div></header><main><RouterView @open-ai="showAi=true;ensureLogin()" @open-cart="showCart=true;ensureLogin()"/></main><button class="floating-ai" @click="showAi=true;ensureLogin()"><Bot/><span>AI私厨</span></button><AiChefDrawer v-model="showAi"/><CartDrawer v-model="showCart"/></div></template>
