import axios from 'axios'
import type { ApiResponse } from '@/types'
export const http=axios.create({baseURL:import.meta.env.VITE_API_BASE_URL||'/api',timeout:75000})
http.interceptors.request.use(config=>{const token=localStorage.getItem('user_token');if(token)config.headers.Authorization=`Bearer ${token}`;return config})
http.interceptors.response.use(response=>{const body=response.data as ApiResponse<unknown>;if(body?.code&&body.code!=='SUCCESS')return Promise.reject(new Error(body.message));return response},error=>{if(error.response?.status===401){localStorage.removeItem('user_token');window.dispatchEvent(new Event('cloud-auth-expired'))}return Promise.reject(new Error(error.response?.data?.message||error.message||'网络请求失败'))})
export async function api<T>(config:Parameters<typeof http.request>[0]){const {data}=await http.request<ApiResponse<T>>(config);return data.data}
export function assetUrl(url?:string){
  if(!url)return ''
  if(/^https?:/i.test(url))return url
  const base=(import.meta.env.VITE_API_BASE_URL||'/api').replace(/\/$/,'')
  if(url.startsWith('/api/')){
    if(/^https?:/i.test(base))return `${new URL(base).origin}${url}`
    return url
  }
  return `${base}${url.startsWith('/')?'':'/'}${url}`
}
