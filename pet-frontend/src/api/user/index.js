// src/api/user/index.js
import axios from 'axios'
// 导入全局配置（之前定义的BASE_URL）
import { BASE_URL } from '@/config/index.js'

// 创建用户模块的axios实例（可选，统一配置请求头/拦截器）
const userAxios = axios.create({
  baseURL: BASE_URL, // 统一基础地址
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 创建宠物模块的axios实例
const petAxios = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 登录接口
export const login = (data) => {
  return userAxios.post('/user/login', data)
}

// 注册接口
export const register = (data) => {
  return userAxios.post('/user/register', data)
}

// 头像上传接口
export const uploadAvatar = (userId, file) => {
  // 上传文件需用FormData
  const formData = new FormData()
  formData.append('userId', userId)
  formData.append('file', file)
  return userAxios.post('/user/uploadAvatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' } // 覆盖默认头
  })
}

// 后续可添加其他用户接口（如获取用户信息、修改信息等）
export const getUserInfo = (userId) => {
  return userAxios.get(`/user/${userId}`)
}
// 生成宠物身份证接口
export const createPetIDCard = (data) => {
  return petAxios.post('/pet/createIDCard', data)
}

// 后续可添加其他宠物接口（如获取宠物信息、修改宠物信息等）
export const getPetInfo = (petId) => {
  return petAxios.get(`/pet/${petId}`)
}
