import { createApp } from 'vue'
import App from './App.vue'
import router from './router/UserRouter'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

// ========== 新增：全局后端基础地址配置（只改这一处即可） ==========
const BASE_URL = 'http://localhost:8080'

const vuetify = createVuetify()
const app = createApp(App)

// ========== 挂载全局属性：所有组件可通过 proxy.$BASE_URL 使用 ==========
app.config.globalProperties.$BASE_URL = BASE_URL

app.use(router)
app.use(ElementPlus)
app.use(vuetify)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')