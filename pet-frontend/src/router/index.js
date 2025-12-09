// src/router/index.js
import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/PetHome' }, // 默认跳转到新首页
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
    component: () => import('../views/user/UserCenter.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/policy-detail',
    component: () => import('../views/user/PolicyDetail.vue'), // 新增保单详情页路由
    meta: { showFooter: false }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  // 新增滚动行为配置：跳转页面后默认滚动到顶部
  scrollBehavior (to, from, savedPosition) {
    // savedPosition 为返回时的历史位置（如浏览器后退），优先保留
    if (savedPosition) {
      return savedPosition
    } else {
      // 正常跳转时，滚动到顶部
      return { top: 0 }
    }
  }
})

export default router
