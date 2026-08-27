<script setup lang="ts">
import { onMounted,ref } from 'vue'
import { Clock3,Sparkles,Plus } from '@lucide/vue'
import { api,assetUrl } from '@/api/http'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import type { Category,Dish } from '@/types'
const emit=defineEmits(['open-ai','open-cart'])
const categories=ref<Category[]>([]),dishes=ref<Dish[]>([]),active=ref(''),adding=ref('')
const user=useUserStore(),cart=useCartStore()
async function select(id:string){active.value=id;dishes.value=await api<Dish[]>({url:`/user/dishes?categoryId=${id}`})}
async function add(id:string){adding.value=id;try{if(!user.loggedIn)await user.login();await cart.add(id)}finally{adding.value=''}}
function scrollMenu(){document.querySelector('#menu')?.scrollIntoView({behavior:'smooth'})}
onMounted(async()=>{categories.value=await api<Category[]>({url:'/user/categories'});if(categories.value[0])await select(categories.value[0].id)})
</script>
<template><div class="home"><section class="hero"><div class="hero-copy"><span class="eyebrow"><Sparkles :size="16"/>AI SMART DINING</span><h1>一顿好饭，<br/><em>从懂你开始。</em></h1><p>AI理解你的食材、口味和健康目标，也能从云膳真实菜单中直接推荐。</p><div><button class="primary" @click="emit('open-ai')">问问AI私厨</button><button class="secondary" @click="scrollMenu">浏览今日菜单</button></div></div><div class="hero-art"><div class="plate"><span>膳</span></div><div class="delivery"><Clock3 :size="18"/><b>约30分钟送达</b><small>营业中 · 新鲜现做</small></div></div></section><section id="menu" class="menu-section"><div class="section-head"><div><span>今日菜单</span><h2>认真做的每一道菜</h2></div><button @click="emit('open-cart')">查看购物车 · {{cart.count}}件</button></div><div class="category-tabs"><button v-for="c in categories" :key="c.id" :class="{active:c.id===active}" @click="select(c.id)">{{c.name}}</button></div><div class="dish-grid"><article v-for="dish in dishes" :key="dish.id" class="dish-card"><div class="dish-image"><img v-if="dish.image" :src="assetUrl(dish.image)"/><span v-else>云膳</span><i>库存 {{dish.stock}}</i></div><div class="dish-content"><h3>{{dish.name}}</h3><p>{{dish.description||'当日新鲜制作，认真对待每一口。'}}</p><footer><strong>¥{{dish.price}}</strong><button :disabled="!!adding||dish.stock<1" @click="add(dish.id)"><Plus :size="20"/>{{dish.stock<1?'售罄':'加入'}}</button></footer></div></article></div></section></div></template>
