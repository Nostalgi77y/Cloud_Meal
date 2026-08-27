import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api/http'
import type { LoginResult } from '@/types'
export const useUserStore=defineStore('web-user',()=>{const token=ref(localStorage.getItem('user_token')||'');const name=ref(localStorage.getItem('user_name')||'访客');const loggedIn=computed(()=>!!token.value)
async function login(){const result=await api<LoginResult>({url:'/user/auth/demo-login',method:'POST'});token.value=result.token;name.value=result.name;localStorage.setItem('user_token',token.value);localStorage.setItem('user_name',name.value)}
function logout(){token.value='';name.value='访客';localStorage.removeItem('user_token');localStorage.removeItem('user_name')}
return{token,name,loggedIn,login,logout}})
