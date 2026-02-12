import axios from 'axios'
import { BASE_URL } from '@/config/index.js'
import { ElMessage } from 'element-plus'

// 创建商家端axios实例
const createMerchantAxiosInstance = (baseURL) => {
  const instance = axios.create({
    baseURL: baseURL,
    timeout: 30000
  })

  instance.defaults.withCredentials = true

  instance.interceptors.response.use(
    (response) => response.data,
    (error) => {
      console.error('商家端请求异常：', error)
      ElMessage.error(error.response?.data?.msg || error.response?.data || '商家端请求失败，请重试')
      return Promise.reject({
        code: error.response?.status || 500,
        msg: error.message || '请求失败'
      })
    }
  )

  return instance
}

export const merchantAxios = createMerchantAxiosInstance(BASE_URL)

// 商家登录
export const merchantLogin = (data) => {
  return merchantAxios.post('/merchant/login', data)
}
// 商家注册
export const merchantRegister = (data) => {
  return merchantAxios.post('/merchant/register', data)
}
// 获取商家信息
export const getMerchantInfo = (merchantId) => {
  return merchantAxios.get(`/merchant/info/${merchantId}`)
}

// 修改商家密码
export const updateMerchantPassword = (merchantId, oldPassword, newPassword) => {
  return merchantAxios.post('/merchant/updatePassword', {
    merchantId,
    oldPassword,
    newPassword
  })
}