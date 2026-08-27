<script setup lang="ts">
import { ref } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { LockKeyhole,UserRound,ChefHat } from '@lucide/vue'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
const route=useRoute(),router=useRouter(),user=useUserStore(),cart=useCartStore()
const mode=ref<'login'|'register'>('login'),account=ref(''),password=ref(''),confirmPassword=ref(''),nickname=ref(''),loading=ref(false),error=ref('')
async function submit(){
  error.value=''
  if(mode.value==='register'&&!/^(?:[A-Za-z][A-Za-z0-9_]{2,29}|1[3-9]\d{9})$/.test(account.value.trim())){error.value='账号应为3-30位字母开头的用户名，或11位手机号';return}
  if(mode.value==='register'&&!/^(?=.*[A-Za-z])(?=.*\d).{8,32}$/.test(password.value)){error.value='密码应为8-32位且同时包含字母和数字';return}
  if(mode.value==='register'&&password.value!==confirmPassword.value){error.value='两次输入的密码不一致';return}
  loading.value=true
  try{
    if(mode.value==='login')await user.login(account.value.trim(),password.value)
    else await user.register(account.value.trim(),password.value,nickname.value.trim())
    await cart.load()
    const redirect=typeof route.query.redirect==='string'?route.query.redirect:'/'
    await router.replace(redirect)
  }catch(e){error.value=(e as Error).message}finally{loading.value=false}
}
function switchMode(value:'login'|'register'){mode.value=value;error.value='';password.value='';confirmPassword.value=''}
</script>
<template><section class="auth-page"><div class="auth-story"><span>MEMBERSHIP</span><ChefHat :size="50"/><h1>每位用户，<br/>都有自己的云膳记忆。</h1><p>你的订单、地址、优惠券和 AI 私厨历史，都只绑定到注册后的唯一账号。</p></div><div class="auth-card"><div class="auth-tabs"><button :class="{active:mode==='login'}" @click="switchMode('login')">登录</button><button :class="{active:mode==='register'}" @click="switchMode('register')">注册</button></div><div class="auth-heading"><h2>{{mode==='login'?'欢迎回来':'创建云膳账号'}}</h2><p>{{mode==='login'?'继续你的点餐与私厨会话':'用户名或手机号均可注册'}}</p></div><form @submit.prevent="submit"><label><span><UserRound :size="17"/>用户名或手机号</span><input v-model="account" required maxlength="30" autocomplete="username" placeholder="用户名或11位手机号"></label><label v-if="mode==='register'"><span>昵称</span><input v-model="nickname" maxlength="80" placeholder="选填，展示给你的称呼"></label><label><span><LockKeyhole :size="17"/>密码</span><input v-model="password" required type="password" minlength="8" maxlength="32" :autocomplete="mode==='login'?'current-password':'new-password'" placeholder="8-32位，包含字母和数字"></label><label v-if="mode==='register'"><span>确认密码</span><input v-model="confirmPassword" required type="password" minlength="8" maxlength="32" autocomplete="new-password" placeholder="再次输入密码"></label><p v-if="error" class="form-error">{{error}}</p><button class="auth-submit" :disabled="loading">{{loading?'处理中…':mode==='login'?'登录':'注册并登录'}}</button></form><small>注册即表示你同意仅将账户数据用于云膳点餐和 AI 私厨服务。</small></div></section></template>
