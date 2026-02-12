// src/router/merchantRouter.js
export default [
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/merchant/login/index.vue'),
    meta: { hideSidebar: true }
  },
  {
    path: '/merchant',
    redirect: '/merchant/insurance/list', // 默认跳保险列表
    component: () => import('@/views/merchant/layout/index.vue'),
    children: [
      {
        path: 'insurance/list',
        name: 'MerchantInsuranceList',
        component: () => import('@/views/merchant/insurance/list.vue'),
        meta: { title: '保险列表' }
      },
      {
        path: 'insurance/create',
        name: 'MerchantInsuranceCreate',
        component: () => import('@/views/merchant/insurance/Create.vue'),
        meta: { title: '创建保险' }
      },
      {
        path: 'insurance/audit/:id?',
        name: 'MerchantInsuranceAudit',
        component: () => import('@/views/merchant/insurance/Audit.vue'),
        meta: { title: '保险订单管理' }
      },
      {
        path: 'order/shipping',
        name: 'MerchantOrderShipping',
        component: () => import('@/views/merchant/order/Shipping.vue'),
        meta: { title: '商品发货' }
      },
            {
        path: 'insurance/cheak',
        name: 'MerchantInsuranceCheak',
        component: () => import('@/views/merchant/insurance/cheak.vue'),
        meta: { title: '理赔订单审核' }
      }
    ]
  },
  // 兜底：未匹配的商家路由跳登录页
  {
    path: '/merchant/*',
    redirect: '/merchant/login'
  }
]