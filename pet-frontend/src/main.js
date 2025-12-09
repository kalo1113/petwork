// 添加错误拦截逻辑
import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // 导入路由配置
import request from './utils/request' // 导入封装的axios实例
import { ElMessage, ElementPlus } from 'element-plus' // 单独导入ElMessage
import 'element-plus/dist/index.css'
// 引入 Vuetify 核心及样式
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

// 修复ResizeObserver错误拦截（优化写法）
window.addEventListener('error', (e) => {
  if (e.message?.includes('ResizeObserver loop completed with undelivered notifications')) {
    e.stopImmediatePropagation() // 更彻底阻止传播
    e.preventDefault()
  }
}, true)

// 创建 Vuetify 实例并配置主题
const vuetify = createVuetify({
  theme: {
    defaultTheme: 'light',
    themes: {
      light: {
        colors: {
          primary: '#2196f3', // 主色调，与你项目现有风格保持一致
          secondary: '#f57c00', // 辅助色，用于强调（如价格）
          background: '#f2f2f2', // 页面背景色
          surface: '#ffffff' // 卡片、表单等容器背景色
        }
      }
    }
  }
})

// -------------------------- 核心：Axios 全局配置 --------------------------
// 1. 适配代理配置：若vue.config.js配置了代理，此处改为相对路径（关键！）
// 若前端代理配置了 /user 转发到8080，baseURL设为''或'/api'（根据你的代理规则）
request.defaults.baseURL = '' // 优先用代理，避免跨域（替换原8080硬编码）
// 2. 设置请求超时时间
request.defaults.timeout = 10000
// 3. 允许跨域携带cookie（如需登录态保持）
request.defaults.withCredentials = true

// 4. 请求拦截器（添加token、处理请求前逻辑）
request.interceptors.response.use(
  (response) => {
    // 直接返回完整响应（包含data），不做任何拦截
    return response
  },
  (error) => {
    // 仅处理网络错误，业务错误交给组件处理
    if (!error.response) {
      ElMessage.error('网络异常，请检查后端服务是否启动！')
      return Promise.reject(error)
    }
    // 传递后端原始响应，不做任何处理
    return Promise.reject(error.response)
  }
)

// 5. 响应拦截器（适配后端Result类，精准处理错误码）
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回非200时，直接抛出错误（包含code和msg）
    if (res.code !== 200) {
      // 关键：将res作为错误抛出，确保前端能拿到code和msg
      return Promise.reject(res)
    }
    return res
  },
  (error) => {
    // 网络/接口错误处理（原有不变）
    if (error.response) {
      const status = error.response.status
      const errMsg = error.response.data?.msg || '请求失败！'
      switch (status) {
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
    // 关键：若后端返回了错误对象，直接抛出；否则抛出error
    return Promise.reject(error.response?.data || error)
  }
)

// -------------------------- 应用实例挂载 --------------------------
// 创建Vue应用实例
const app = createApp(App)

// 全局注册工具（方便组件内通过this.$xxx调用）
app.config.globalProperties.$request = request
app.config.globalProperties.$message = ElMessage

// 挂载插件（顺序：router → vuetify → ElementPlus）
app.use(router)
app.use(vuetify)
app.use(ElementPlus)

// 挂载到#app节点
app.mount('#app')
