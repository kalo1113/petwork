import { createRouter, createWebHashHistory } from 'vue-router'

// 核心：动态获取当前端口，决定根路径跳转目标
const getRootRedirect = () => {
  // 获取浏览器地址栏的端口（默认8081为用户端）
  const currentPort = window.location.port || '8081'
  // 8081端口跳用户端首页，8082端口跳商家端首页
  return currentPort === '8081' ? '/PetHome' : '/merchant'
}

const routes = [
  // 根路径重定向改为动态计算，不再写死 /PetHome
  { path: '/', redirect: getRootRedirect() }, 
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
    path: '/user/claim',
    name: 'UserClaim',
    component: () => import('../views/user/my/Claim.vue')
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

// 额外：路由守卫（可选，强化端口隔离）
router.beforeEach((to, from, next) => {
  const currentPort = window.location.port || '8081'
  // 8082端口禁止访问用户端核心路径，强制跳商家端
  if (currentPort === '8082') {
    const userPaths = ['/PetHome', '/guarantee', '/mall', '/my']
    if (userPaths.includes(to.path)) {
      next('/merchant')
      return
    }
  }
  // 8081端口禁止访问商家端路径，强制跳用户端
  if (currentPort === '8081' && to.path.startsWith('/merchant')) {
    next('/PetHome')
    return
  }
  next()
})

export default router