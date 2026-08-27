import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api/http'
import type { LoginResult } from '@/types'
export const useUserStore=defineStore('web-user',()=>{
  const token=ref(localStorage.getItem('user_token')||'')
  const userId=ref(localStorage.getItem('user_id')||'')
  const name=ref(localStorage.getItem('user_name')||'访客')
  const loggedIn=computed(()=>!!token.value)
  function applySession(result:LoginResult){token.value=result.token;userId.value=String(result.userId);name.value=result.name;localStorage.setItem('user_token',token.value);localStorage.setItem('user_id',userId.value);localStorage.setItem('user_name',name.value)}
  async function login(account:string,password:string){applySession(await api<LoginResult>({url:'/user/auth/login',method:'POST',data:{account,password}}))}
  async function register(account:string,password:string,nickname:string){applySession(await api<LoginResult>({url:'/user/auth/register',method:'POST',data:{account,password,nickname}}))}
  function rename(value:string){name.value=value;localStorage.setItem('user_name',value)}
  function logout(){token.value='';userId.value='';name.value='访客';localStorage.removeItem('user_token');localStorage.removeItem('user_id');localStorage.removeItem('user_name')}
  return{token,userId,name,loggedIn,login,register,rename,logout}
})
