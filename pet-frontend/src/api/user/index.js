import axios from 'axios'
import { BASE_URL } from '@/config/index.js'
import { ElMessage } from 'element-plus' // 按需引入消息提示

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
      // 直接返回响应数据，简化前端调用（注意：这里已经是response.data，后续接口无需再取.data）
      return response.data
    },
    (error) => {
      console.error('请求异常：', error)
      // 统一错误提示
      ElMessage.error(error.response?.data || '请求失败，请重试')
      // eslint-disable-next-line prefer-promise-reject-errors
      return Promise.reject({
        code: error.response?.status || 500,
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
// 商品模块实例（复用BASE_URL，统一管理）
const productAxios = createAxiosInstance(BASE_URL)
// 购物车模块实例
const cartAxios = createAxiosInstance(BASE_URL)
// 订单模块实例（新增）
const orderAxios = createAxiosInstance(BASE_URL)
// 收货地址模块实例（新增）
const addressAxios = createAxiosInstance(BASE_URL)

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

// ===================== 宠物接口 =====================
// 前端 getPetListByUserId 接口修正
export const getPetListByUserId = (userId) => {
  return petAxios.get('/pet/list', { // 确保URL以 / 开头
    params: { userId: userId }, // 自动拼接参数，避免手动写?userId=4
    headers: { 'Content-Type': 'application/json' }
  })
}

// 新增宠物
export const addPet = (data) => {
  return petAxios.post('/pet/add', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 【新增】根据宠物ID查询宠物信息（编辑模式回显）
export const getPetInfoById = (petId) => {
  return petAxios.get(`/pet/info/${petId}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 【新增】更新宠物基础信息（编辑模式提交）
export const updatePetInfo = (data) => {
  return petAxios.put('/pet/update', data, {
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

// ===================== 商品接口 =====================
// 获取商品列表（简化后的loadProducts）
export const getProductList = async () => {
  try {
    const data = await productAxios.get('/products/list', {
      headers: { 'Content-Type': 'application/json' }
    })
    return data || [] // 异常时返回空数组，避免调用处报错
  } catch (err) {
    console.error('加载商品失败：', err)
    return []
  }
}

// ===================== 购物车接口 =====================
// 1. 添加商品到购物车（修复参数校验逻辑）
export const addToCart = async (userId, productId, count = 1) => {
  // 直接使用传入的参数（页面层已校验过有效性）
  const userIdNum = Number(userId)
  const productIdNum = Number(productId)
  const countNum = Number(count)

  // 现在参数是有效的，直接请求（移除多余的错误抛出）
  return cartAxios.post('/cart/add', null, {
    params: { 
      userId: userIdNum, 
      productId: productIdNum,
      count: countNum
    }
  })
}

// 2. 获取购物车列表（匹配后端：userId）
export const getCartList = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法获取购物车')
    throw new Error('userId不是有效数字')
  }
  return cartAxios.get('/cart/list', {
    params: { userId: Number(userId) }
  })
}

// 3. 修改购物车商品数量（匹配后端：cartId, count）
export const updateCartCount = async (cartId, count) => {
  if (isNaN(Number(cartId)) || isNaN(Number(count))) {
    ElMessage.error('参数异常，无法修改数量')
    throw new Error('cartId或count不是有效数字')
  }
  return cartAxios.post('/cart/update/count', null, {
    params: { 
      cartId: Number(cartId),
      count: Number(count)
    }
  })
}

// 4. 删除购物车商品（匹配后端：cartId）
export const deleteCartItem = async (cartId) => {
  if (isNaN(Number(cartId))) {
    ElMessage.error('参数异常，无法删除商品')
    throw new Error('cartId不是有效数字')
  }
  return cartAxios.post('/cart/delete', null, {
    params: { cartId: Number(cartId) }
  })
}

// ===================== 订单接口（新增） =====================
// 1. 创建订单（核心：支持多商品）
export const createOrder = async (userId, itemList, receiverName, receiverPhone, receiverAddress) => {
  // 参数校验
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法创建订单')
    throw new Error('userId不是有效数字')
  }
  if (!itemList || itemList.length === 0) {
    ElMessage.error('请选择要购买的商品')
    throw new Error('商品列表为空')
  }
  // 发起请求（参数分两部分：URL参数 + 请求体）
  return orderAxios.post('/order/create', itemList, {
    params: {
      userId: Number(userId),
      receiverName: receiverName,
      receiverPhone: receiverPhone,
      receiverAddress: receiverAddress
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 获取用户订单列表
export const getOrderList = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法获取订单列表')
    throw new Error('userId不是有效数字')
  }
  return orderAxios.get('/order/list', {
    params: { userId: Number(userId) },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 获取订单详情（主表+商品明细）
export const getOrderDetail = async (orderId) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常，无法获取订单详情')
    throw new Error('orderId不是有效数字')
  }
  return orderAxios.get('/order/detail', {
    params: { orderId: Number(orderId) },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 更新订单状态（模拟付款/发货/收货/取消）
export const updateOrderStatus = async (orderId, status) => {
  if (isNaN(Number(orderId)) || isNaN(Number(status))) {
    ElMessage.error('参数异常，无法更新订单状态')
    throw new Error('orderId或status不是有效数字')
  }
  // 状态合法性校验（前端兜底）
  const validStatus = [0, 1, 2, 3, 4]
  if (!validStatus.includes(Number(status))) {
    ElMessage.error('订单状态不合法（0=待付款 1=待发货 2=待收货 3=已完成 4=已取消）')
    throw new Error('订单状态不合法')
  }
  return orderAxios.post('/order/updateStatus', null, {
    params: {
      orderId: Number(orderId),
      status: Number(status)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// ===================== 收货地址接口（新增） =====================
// 1. 添加收货地址
export const addReceiverAddress = async (addressData) => {
  // 参数校验
  if (isNaN(Number(addressData.userId))) {
    ElMessage.error('用户ID异常，无法添加地址')
    throw new Error('userId不是有效数字')
  }
  if (!addressData.receiverName || !addressData.receiverPhone) {
    ElMessage.error('收货人姓名和电话不能为空')
    throw new Error('收货人信息不完整')
  }
  if (!addressData.receiverProvince || !addressData.receiverCity || !addressData.receiverDistrict || !addressData.receiverDetailAddress) {
    ElMessage.error('请完整填写收货地址')
    throw new Error('收货地址不完整')
  }
  // 发起请求
  return addressAxios.post('/user/address/add', addressData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 获取用户的所有收货地址
export const getReceiverAddressList = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法获取地址列表')
    throw new Error('userId不是有效数字')
  }
  return addressAxios.get('/user/address/list', {
    params: { userId: Number(userId) },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 获取用户的默认收货地址
export const getDefaultReceiverAddress = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法获取默认地址')
    throw new Error('userId不是有效数字')
  }
  return addressAxios.get('/user/address/default', {
    params: { userId: Number(userId) },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 修改收货地址
export const updateReceiverAddress = async (addressData) => {
  // 参数校验
  if (isNaN(Number(addressData.id))) {
    ElMessage.error('地址ID异常，无法修改地址')
    throw new Error('地址ID不是有效数字')
  }
  if (isNaN(Number(addressData.userId))) {
    ElMessage.error('用户ID异常，无法修改地址')
    throw new Error('userId不是有效数字')
  }
  // 发起请求
  return addressAxios.put('/user/address/update', addressData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 5. 删除收货地址
export const deleteReceiverAddress = async (addressId, userId) => {
  // 参数校验
  if (isNaN(Number(addressId))) {
    ElMessage.error('地址ID异常，无法删除地址')
    throw new Error('addressId不是有效数字')
  }
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法删除地址')
    throw new Error('userId不是有效数字')
  }
  // 发起请求
  return addressAxios.delete(`/user/address/delete/${addressId}`, {
    params: { userId: Number(userId) },
    headers: { 'Content-Type': 'application/json' }
  })
}