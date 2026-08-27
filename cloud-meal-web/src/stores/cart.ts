import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api } from '@/api/http'
import type { CartItem } from '@/types'
export const useCartStore=defineStore('web-cart',()=>{const items=ref<CartItem[]>([]);const count=computed(()=>items.value.reduce((sum,x)=>sum+x.quantity,0));const total=computed(()=>items.value.reduce((sum,x)=>sum+Number(x.unitPrice)*x.quantity,0))
async function load(){items.value=await api<CartItem[]>({url:'/user/cart'})}async function add(dishId:string){await api<void>({url:'/user/cart',method:'POST',data:{dishId,quantity:1}});await load()}async function clear(){await api<void>({url:'/user/cart',method:'DELETE'});items.value=[]}return{items,count,total,load,add,clear}})
