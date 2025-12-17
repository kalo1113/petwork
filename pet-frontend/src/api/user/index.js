import axios from 'axios'
import { BASE_URL } from '@/config/index.js'

// ===================== 通用配置 =====================
// 创建基础axios实例（不预设Content-Type，避免覆盖）
const createAxiosInstance = (baseURL) => {
  const instance = axios.create({
    baseURL: baseURL,
    timeout: 30000 // 延长超时，适配图片上传
  })

  // 跨域请求携带凭证（可选，根据后端需求）
  instance.defaults.withCredentials = true

  // 响应拦截器（统一处理返回格式）
  instance.interceptors.response.use(
    (response) => {
      // 直接返回响应数据，简化前端调用
      return response.data
    },
    (error) => {
      console.error('请求异常：', error)
      // eslint-disable-next-line prefer-promise-reject-errors
      return Promise.reject({
        code: 500,
        msg: error.message || '请求失败'
      })
    }
  )

  return instance
}

// ===================== 实例创建 =====================
// 用户模块实例
const userAxios = createAxiosInstance(BASE_URL)
// 宠物模块实例（无默认Content-Type）
const petAxios = createAxiosInstance(BASE_URL)

// ===================== 用户接口 =====================
// 登录
export const login = (data) => {
  return userAxios.post('/user/login', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 注册
export const register = (data) => {
  return userAxios.post('/user/register', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 头像上传
export const uploadAvatar = (userId, file) => {
  const formData = new FormData()
  formData.append('userId', userId)
  formData.append('file', file)
  return userAxios.post('/user/uploadAvatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取用户信息
export const getUserInfo = (userId) => {
  return userAxios.get(`/user/${userId}`)
}

// 前端 getPetListByUserId 接口修正
export const getPetListByUserId = (userId) => {
  return petAxios.get('/pet/list', { // 确保URL以 / 开头
    params: { userId: userId }, // 自动拼接参数，避免手动写?userId=4
    headers: { 'Content-Type': 'application/json' }
  })
}

// ===================== 宠物接口（补充缺失的核心接口） =====================
// 新增宠物
export const addPet = (data) => {
  return petAxios.post('/pet/add', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 宠物图片上传（关键：适配multipart/form-data）
export const uploadPetImg = (formData) => {
  return petAxios.post('/pet/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' } // 强制覆盖请求头
  })
}

// 关联宠物图片（更新photo字段）
export const updatePetPhoto = (data) => {
  return petAxios.put('/pet/update-photo', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}
