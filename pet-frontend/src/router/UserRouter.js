import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/PetHome' }, 
  {
    path: '/PetHome',
    component: () => import('../views/user/PetHome.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/guarantee',
    component: () => import('../views/user/PetGuarantee.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/mall',
    component: () => import('../views/user/PetMall.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/my',
    component: () => import('../views/user/my/UserCenter.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/policy-detail',
    component: () => import('../views/user/PolicyDetail.vue'),
    meta: { showFooter: false }
  },
  {
    path: '/user/setting',
    name: 'UserSetting',
    component: () => import('../views/user/my/UserSetting.vue')
  },
  {
    path: '/pet-id-card',
    name: 'PetIDCard',
    component: () => import('../views/user/PetIDCard.vue')
  },
    {
    path: '/user/myorder',
    name: 'UserOrder',
    component: () => import('../views/user/my/MyOrder.vue')
  },
      {
    path: '/policy-detail-more',
    name: 'policy-more',
    component: () => import('../views/user/PolicyDetailmore.vue')
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior (to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router