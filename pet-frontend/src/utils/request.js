import axios from 'axios'
// 导入全局配置
import { BASE_URL } from '@/config'

const service = axios.create({
  // 替换硬编码：使用全局配置的后端地址
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 响应拦截器（统一处理后端返回格式）
service.interceptors.response.use(
  (response) => {
    // 若后端直接返回pet对象，需手动包装为code+data格式
    return {
      code: 200,
      data: response.data
    }
  },
  (error) => Promise.reject(error)
)

export default service
