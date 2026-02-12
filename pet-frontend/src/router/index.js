// src/router/index.js
import { createRouter, createWebHashHistory } from 'vue-router'
import userRouterInstance from './UserRouter'
import merchantRoutes from './merchantRouter'

const userRoutes = userRouterInstance.options.routes

const routes = [
  // 移除重复的/merchant路由，直接使用merchantRoutes中的配置（避免路由冲突）
  ...merchantRoutes,
  ...userRoutes
]

// 创建路由实例（保留原有scrollBehavior）
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

// 路由守卫：整合「端口区分」+「商家登录状态校验」
router.beforeEach((to, from, next) => {
  const currentPort = window.location.port || '8081'

  // ========== 第一步：端口路由规则（保留你原有逻辑） ==========
  if (currentPort === '8082') {
    // 8082端口（商家端）：默认/跳/merchant，用户端页面跳/merchant
    if (to.path === '/') {
      next('/merchant')
      return
    }
    if (['/PetHome', '/guarantee', '/mall', '/my'].includes(to.path)) {
      next('/merchant')
      return
    }
  } else if (currentPort === '8081') {
    // 8081端口（用户端）：默认/跳/PetHome，商家端页面跳/PetHome
    if (to.path === '/') {
      next('/PetHome')
      return
    }
    if (to.path.startsWith('/merchant')) {
      next('/PetHome')
      return
    }
  }

  // ========== 第二步：商家登录状态校验（仅8082端口生效） ==========
  if (currentPort === '8082' && to.path.startsWith('/merchant')) {
    // 1. 获取本地存储的商家登录信息（登录成功后存储）
    const merchantInfo = localStorage.getItem('merchantInfo')
    const isMerchantLogin = !!merchantInfo

    // 2. 定义无需登录的商家页面（仅登录页）
    const noAuthPages = ['/merchant/login']

    // 3. 规则：需要登录但未登录 → 跳商家登录页
    if (!noAuthPages.includes(to.path) && !isMerchantLogin) {
      next('/merchant/login')
      return
    }

    // 4. 规则：已登录但访问登录页 → 跳商家后台首页
    if (to.path === '/merchant/login' && isMerchantLogin) {
      next('/merchant/insurance/list')
      return
    }
  }

  // 所有规则校验通过，正常放行
  next()
})

export default router