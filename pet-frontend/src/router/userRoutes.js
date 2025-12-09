import PetHome from '@/views/user/PetHome.vue'
import PetGuarantee from '@/views/user/PetGuarantee.vue'
import PetMall from '@/views/user/PetMall.vue'
import UserCenter from '@/views/user/UserCenter.vue'

export default [
  { path: '/', redirect: '/home' },
  { path: '/home', component: PetHome },
  { path: '/guarantee', component: PetGuarantee },
  { path: '/mall', component: PetMall },
  { path: '/my', component: UserCenter },
  // 保留原宠物管理路由（可整合到UserCenter）
  { path: '/pet-list', component: () => import('@/views/user/PetList.vue') },
  { path: '/pet-edit/:id', component: () => import('@/views/user/PetEdit.vue') }
]
