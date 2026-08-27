import { createRouter,createWebHistory } from 'vue-router'
const router=createRouter({history:createWebHistory(),routes:[
  {path:'/',component:()=>import('@/views/HomeView.vue')},
  {path:'/auth',component:()=>import('@/views/AuthView.vue'),meta:{guest:true}},
  {path:'/orders',component:()=>import('@/views/OrdersView.vue'),meta:{requiresAuth:true}},
  {path:'/ai-chef',component:()=>import('@/views/AiChefView.vue'),meta:{requiresAuth:true}},
  {path:'/settings',component:()=>import('@/views/SettingsView.vue'),meta:{requiresAuth:true}}
]})
router.beforeEach(to=>{const loggedIn=!!localStorage.getItem('user_token');if(to.meta.requiresAuth&&!loggedIn)return{path:'/auth',query:{redirect:to.fullPath}};if(to.meta.guest&&loggedIn)return'/'})
export default router
