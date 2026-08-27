import { createRouter,createWebHistory } from 'vue-router'
const router=createRouter({history:createWebHistory(),routes:[{path:'/',component:()=>import('@/views/HomeView.vue')},{path:'/orders',component:()=>import('@/views/OrdersView.vue')}]})
export default router
