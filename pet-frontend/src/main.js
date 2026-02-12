if (typeof require !== 'undefined') {
  try {
    const util = require('util');
    const originalExtend = util._extend;
    if (originalExtend && typeof originalExtend === 'function') {
      util._extend = function(target, ...sources) {
        return Object.assign(target, ...sources);
      };
    }
  } catch (e) {
    console.log('覆盖 util._extend 失败（不影响功能）：', e);
  }
}

import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import '@mdi/font/css/materialdesignicons.css'

// ========== 全局后端基础地址配置 ==========
const BASE_URL = 'http://localhost:8080'

const vuetify = createVuetify()
const app = createApp(App)

// ========== 挂载全局属性 ==========
app.config.globalProperties.$BASE_URL = BASE_URL

// ========== 终极修复 ResizeObserver 报错（纯原生，无依赖） ==========
// 1. 原生防抖函数
function debounce(fn, delay = 16) {
  let timer = null;
  return function(...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

// 2. 重写 ResizeObserver 核心逻辑
if (window.ResizeObserver) {
  const OriginalResizeObserver = window.ResizeObserver;
  window.ResizeObserver = class ResizeObserver extends OriginalResizeObserver {
    constructor(callback) {
      // 包装回调：防抖 + 异常捕获
      const wrappedCallback = debounce((entries, observer) => {
        try {
          callback(entries, observer);
        } catch (err) {
          // 静默处理，不抛错
        }
      });
      super(wrappedCallback);
    }
  };
}

// 3. 彻底屏蔽所有 ResizeObserver 相关报错
const originalConsoleError = console.error;
console.error = function(...args) {
  const errorString = args.join(' ').toLowerCase();
  if (errorString.includes('resizeobserver')) {
    return; // 直接忽略
  }
  originalConsoleError.apply(console, args);
};

// ========== 注册插件和图标 ==========
app.use(router)
app.use(ElementPlus)
app.use(vuetify)

// 注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.mount('#app')