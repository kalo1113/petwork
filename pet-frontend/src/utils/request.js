import axios from 'axios'

const request = axios.create({
  baseURL: 'http://localhost:8080', // 必须和后端启动的地址一致（包括端口）
  timeout: 10000 // 仅保留超时配置
})

request.interceptors.response.use(
  response => response.data,
  error => Promise.reject(error)
)

export default request
