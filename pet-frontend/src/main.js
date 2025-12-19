// main.js 完整配置
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import request from './utils/request'

// ========== Element Plus 配置 ==========
import { ElMessage, ElementPlus } from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// ========== Vuetify 配置 ==========
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

// ========== 全局错误拦截 ==========
window.addEventListener('error', (e) => {
  if (e.message?.includes('ResizeObserver loop completed with undelivered notifications')) {
    e.stopImmediatePropagation()
    e.preventDefault()
  }
}, true)

// ========== Vuetify 实例 ==========
const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#2196f3',
          secondary: '#f57c00',
          background: '#f2f2f2',
          surface: '#ffffff'
        }
      }
    }
  },
  defaults: {
    global: {
      zIndex: 2000 // 低于Element Plus的3000，避免覆盖
    }
  }
})

// ========== Axios 配置（适配代理） ==========
request.defaults.baseURL = '' // 空值：优先走vue.config.js的代理
request.defaults.timeout = 10000
request.defaults.withCredentials = true // 跨域携带Cookie

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可添加token（示例）
    // const token = localStorage.getItem('token')
    // if (token) config.headers['Authorization'] = `Bearer ${token}`
    return config
  },
  (error) => {
    ElMessage.error('请求参数错误！')
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.warning(res.msg || '操作失败！')
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const errMsg = error.response.data?.msg || '请求失败！'
      switch (status) {
        case 401:
          ElMessage.error('登录失效，请重新登录！')
          // router.push('/login') // 登录失效跳转
          break
        case 404:
          ElMessage.error(`接口不存在：${errMsg}`)
          break
        case 500:
          ElMessage.error(`服务器错误：${errMsg}`)
          break
        default:
          ElMessage.error(`请求失败 [${status}]：${errMsg}`)
      }
    } else if (error.request) {
      ElMessage.error('网络异常，请检查后端服务是否启动！')
    } else {
      ElMessage.error(`请求配置错误：${error.message}`)
    }
    return Promise.reject(error)
  }
)

// ========== Vue 应用实例 ==========
const app = createApp(App)

// 核心：声明el-为自定义元素
app.config.compilerOptions = {
  isCustomElement: tag => tag.startsWith('el-')
}

// 全局注册工具
app.config.globalProperties.$request = request
app.config.globalProperties.$message = ElMessage

// 注册Element Plus所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 插件注册顺序（关键！）
app.use(ElementPlus, { zIndex: 3000 })
app.use(router)
app.use(vuetify)

// 挂载应用
app.mount('#app')
