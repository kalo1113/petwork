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
    component: () => import('../views/user/my/UserCenter.vue'),
    meta: { showFooter: true }
  },
  {
    path: '/policy-detail',
    component: () => import('../views/user/PolicyDetail.vue'), // 新增保单详情页路由
    meta: { showFooter: false }
  },
  {
    path: '/user/setting', // 需与跳转的path一致
    name: 'UserSetting', // 若使用命名路由，name需匹配
    component: () => import('../views/user/my/UserSetting.vue') // 路径需正确
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  // 滚动行为配置：跳转页面后默认滚动到顶部
  scrollBehavior (to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router
