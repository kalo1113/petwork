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
      // 优化错误提示：优先取后端返回的msg，再降级
      ElMessage.error(error.response?.data?.msg || error.response?.data || '请求失败，请重试')
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
// 宠物保险模块实例（新增）
const insuranceAxios = createAxiosInstance(BASE_URL)
// 宠物保险订单模块实例（新增，复用BASE_URL）
const insuranceOrderAxios = createAxiosInstance(BASE_URL)
// 宠物保险理赔模块实例（新增）
const claimAxios = createAxiosInstance(BASE_URL)

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

// ========== 新增：金额相关接口 ==========
// 钱包充值
export const rechargeWallet = async (userId, amount) => {
  // 参数校验
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法充值')
    throw new Error('userId不是有效数字')
  }
  if (isNaN(Number(amount)) || Number(amount) <= 0) {
    ElMessage.error('充值金额必须大于0')
    throw new Error('充值金额不合法')
  }
  // 发起充值请求
  return userAxios.post('/user/recharge', {
    userId: Number(userId),
    amount: Number(amount)
  }, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 查询用户金额信息（余额+待入账）
export const getUserAmountInfo = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法查询金额信息')
    throw new Error('userId不是有效数字')
  }
  return userAxios.get(`/user/amount/${userId}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 更新待入账金额（管理员/系统场景）
export const updatePendingAmount = async (userId, pendingAmount) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法更新待入账金额')
    throw new Error('userId不是有效数字')
  }
  if (isNaN(Number(pendingAmount))) {
    ElMessage.error('待入账金额不合法')
    throw new Error('pendingAmount不是有效数字')
  }
  return userAxios.post('/user/updatePendingAmount', {
    userId: Number(userId),
    pendingAmount: Number(pendingAmount)
  }, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 待入账金额转正式余额
export const pendingToBalance = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法转入金额')
    throw new Error('userId不是有效数字')
  }
  return userAxios.post(`/user/pendingToBalance/${userId}`, {}, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 钱包扣款（结算时扣减余额）
export const deductWalletBalance = async (userId, amount) => {
  // 前端参数前置校验
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法扣款')
    throw new Error('userId不是有效数字')
  }
  if (isNaN(Number(amount)) || Number(amount) <= 0) {
    ElMessage.error('扣款金额必须大于0')
    throw new Error('扣款金额不合法')
  }
  // 发起扣款请求
  return userAxios.post('/user/deductBalance', {
    userId: Number(userId),
    amount: Number(amount)
  }, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 修改昵称
export const updateNickname = async (userId, username) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法修改昵称')
    throw new Error('userId不是有效数字')
  }
  if (!username || username.trim().length === 0) {
    ElMessage.error('昵称不能为空')
    throw new Error('昵称不能为空')
  }
  return userAxios.post('/user/updateNickname', {
    userId: Number(userId),
    username: username.trim()
  }, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 修改密码
export const updatePassword = async (userId, oldPassword, newPassword) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法修改密码')
    throw new Error('userId不是有效数字')
  }
  if (!oldPassword || !newPassword) {
    ElMessage.error('原密码和新密码不能为空')
    throw new Error('密码不能为空')
  }
  return userAxios.post('/user/updatePassword', {
    userId: Number(userId),
    oldPassword: oldPassword,
    newPassword: newPassword
  }, {
    headers: { 'Content-Type': 'application/json' }
  })
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
// 获取用户月度补贴信息（每月限额、已用额度等）
export const getMonthlySubsidyInfo = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法查询补贴信息')
    throw new Error('userId不是有效数字')
  }
  // 后端接口路径，可根据实际情况调整
  return userAxios.get('/user/subsidy/monthly', {
    params: { userId: Number(userId) },
    headers: { 'Content-Type': 'application/json' }
  })
}
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

// 【新增】校验商品ID是否存在（核心：替代下架状态，仅查是否存在）
export const checkProductExist = async (productId) => {
  if (isNaN(Number(productId))) {
    ElMessage.error('商品ID异常，无法查询')
    throw new Error('productId不是有效数字')
  }
  // 关键修改：从 /product/checkExist 改为 /products/checkExist
  return productAxios.get('/products/checkExist', {
    params: { productId: Number(productId) },
    headers: { 'Content-Type': 'application/json' }
  })
}

// ===================== 购物车接口 =====================
// 1. 添加商品到购物车（适配后端：form-data参数 + 字符串返回）
export const addToCart = async (userId, productId, count = 1) => {
  // 参数校验
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法加入购物车')
    throw new Error('userId不是有效数字')
  }
  if (isNaN(Number(productId))) {
    ElMessage.error('商品ID异常，无法加入购物车')
    throw new Error('productId不是有效数字')
  }
  if (isNaN(Number(count)) || Number(count) <= 0) {
    ElMessage.error('商品数量必须大于0')
    throw new Error('count不合法')
  }

  // 适配后端：使用form-data格式传递参数（你的CartController要求）
  const formData = new FormData()
  formData.append('userId', Number(userId))
  formData.append('productId', Number(productId))
  formData.append('count', Number(count))

  // 发起请求（关键：后端返回纯字符串，这里直接返回）
  return cartAxios.post('/cart/add', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
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
// ========== 新增：确认收货接口 ==========
export const confirmReceiveOrder = async (orderId, userId) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常，无法确认收货')
    throw new Error('orderId无效')
  }
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法确认收货')
    throw new Error('userId无效')
  }
  return orderAxios.post('/order/confirmReceive', null, {
    params: {
      orderId: Number(orderId),
      userId: Number(userId)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}
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

// 5. 按状态查询用户订单（适配前端tab切换）
export const getOrderListByStatus = async (userId, status) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法获取订单列表')
    throw new Error('userId不是有效数字')
  }
  if (isNaN(Number(status)) || status < 0 || status > 4) {
    ElMessage.error('订单状态不合法（0=待付款 1=待发货 2=待收货 3=已完成 4=已取消）')
    throw new Error('订单状态不合法')
  }
  return orderAxios.get('/order/listByStatus', {
    params: { 
      userId: Number(userId),
      status: Number(status)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// ========== 新增：订单删除接口 ==========
export const deleteOrder = async (orderId) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常')
    throw new Error('orderId无效')
  }
  return orderAxios.post('/order/delete', null, {
    params: { orderId: Number(orderId) }
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

// ===================== 宠物保险接口（新增） =====================
// 1. 新增保险产品
export const addInsurance = async (insuranceData) => {
  // 参数校验
  if (!insuranceData.insuranceName || insuranceData.insuranceName.trim().length === 0) {
    ElMessage.error('保险名称不能为空')
    throw new Error('保险名称为空')
  }
  if (!insuranceData.discountPremium) {
    ElMessage.error('优惠保费不能为空')
    throw new Error('优惠保费为空')
  }
  // 发起请求
  return insuranceAxios.post('/insurance/add', insuranceData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 修改保险产品
export const updateInsurance = async (insuranceData) => {
  // 参数校验
  if (isNaN(Number(insuranceData.id))) {
    ElMessage.error('保险ID异常，无法修改')
    throw new Error('insuranceId不是有效数字')
  }
  // 发起请求
  return insuranceAxios.post('/insurance/update', insuranceData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 删除保险产品（级联删除关联图片）
export const deleteInsurance = async (insuranceId) => {
  if (isNaN(Number(insuranceId))) {
    ElMessage.error('保险ID异常，无法删除')
    throw new Error('insuranceId不是有效数字')
  }
  return insuranceAxios.post(`/insurance/delete/${insuranceId}`, {}, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 分页查询保险产品列表
export const getInsurancePage = async (pageNum = 1, pageSize = 10, insuranceName, petType, status) => {
  // 参数校验
  if (isNaN(Number(pageNum)) || pageNum < 1) {
    ElMessage.error('页码必须大于0')
    throw new Error('pageNum不合法')
  }
  if (isNaN(Number(pageSize)) || pageSize < 1 || pageSize > 100) {
    ElMessage.error('每页条数必须在1-100之间')
    throw new Error('pageSize不合法')
  }
  // 构建查询参数
  const params = {
    pageNum: Number(pageNum),
    pageSize: Number(pageSize)
  }
  if (insuranceName && insuranceName.trim().length > 0) {
    params.insuranceName = insuranceName.trim()
  }
  if (petType !== undefined && !isNaN(Number(petType))) {
    params.petType = Number(petType)
  }
  if (status !== undefined && !isNaN(Number(status))) {
    params.status = Number(status)
  }
  // 发起请求
  return insuranceAxios.get('/insurance/page', {
    params: params,
    headers: { 'Content-Type': 'application/json' }
  })
}

// 5. 查询保险产品详情（含关联媒体图片）
export const getInsuranceDetail = async (insuranceId) => {
  if (isNaN(Number(insuranceId))) {
    ElMessage.error('保险ID异常，无法查询详情')
    throw new Error('insuranceId不是有效数字')
  }
  return insuranceAxios.get(`/insurance/detail/${insuranceId}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 6. 更新保险产品状态（上架/下架）
export const updateInsuranceStatus = async (insuranceId, status) => {
  if (isNaN(Number(insuranceId))) {
    ElMessage.error('保险ID异常，无法更新状态')
    throw new Error('insuranceId不是有效数字')
  }
  if (![0, 1].includes(Number(status))) {
    ElMessage.error('状态值只能是0（下架）或1（上架）')
    throw new Error('status不合法')
  }
  return insuranceAxios.post('/insurance/updateStatus', null, {
    params: {
      id: Number(insuranceId),
      status: Number(status)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// ========== 保险媒体图片接口 ==========
// 1. 新增保险媒体图片
export const addInsuranceMedia = async (mediaData) => {
  // 参数校验
  if (isNaN(Number(mediaData.insuranceId))) {
    ElMessage.error('关联保险ID异常')
    throw new Error('insuranceId不是有效数字')
  }
  if (isNaN(Number(mediaData.contentType))) {
    ElMessage.error('内容类型异常')
    throw new Error('contentType不是有效数字')
  }
  if (!mediaData.imgPath && !mediaData.imgRemark) {
    ElMessage.error('图片路径和说明不能同时为空')
    throw new Error('媒体内容不完整')
  }
  // 发起请求
  return insuranceAxios.post('/insurance/media/add', mediaData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 修改保险媒体图片
export const updateInsuranceMedia = async (mediaData) => {
  if (isNaN(Number(mediaData.id))) {
    ElMessage.error('媒体图片ID异常，无法修改')
    throw new Error('mediaId不是有效数字')
  }
  return insuranceAxios.post('/insurance/media/update', mediaData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 删除保险媒体图片
export const deleteInsuranceMedia = async (mediaId) => {
  if (isNaN(Number(mediaId))) {
    ElMessage.error('媒体图片ID异常，无法删除')
    throw new Error('mediaId不是有效数字')
  }
  return insuranceAxios.post(`/insurance/media/delete/${mediaId}`, {}, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 根据保险ID查询关联的媒体图片
export const getInsuranceMediaList = async (insuranceId) => {
  if (isNaN(Number(insuranceId))) {
    ElMessage.error('保险ID异常，无法查询媒体图片')
    throw new Error('insuranceId不是有效数字')
  }
  return insuranceAxios.get(`/insurance/media/list/${insuranceId}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 5. 获取保险图片完整URL（适配静态资源映射）- 修复后版本
export const getInsuranceImgUrl = (imgPath) => {
  // 空路径返回空字符串（或通用占位符），避免引用不存在的图片
  if (!imgPath || imgPath.trim() === '') {
    // 方案1：返回空字符串（推荐，前端使用时可加默认图）
    return '';
  }
  // 已包含完整域名直接返回（加时间戳）
  if (imgPath.startsWith('http://') || imgPath.startsWith('https://')) {
    return `${imgPath}?t=${new Date().getTime()}`; // 核心：加时间戳
  }
  // 拼接基础URL（适配后端静态资源映射）+ 时间戳
  const fullUrl = `${BASE_URL}${imgPath.startsWith('/') ? '' : '/'}${imgPath}`;
  return `${fullUrl}?t=${new Date().getTime()}`; // 核心：加时间戳
};

// ===================== 宠物保险订单接口（新增核心，适配后端PetInsuranceOrderController） =====================
// 1. 创建宠物保险订单（严格匹配后端校验逻辑）
export const createInsuranceOrder = async (orderData) => {
  // 前端参数前置校验（和后端PetInsuranceOrderController完全一致）
  if (orderData.userId === null || orderData.userId === undefined) {
    ElMessage.error('数据错误：用户ID不能为空')
    throw new Error('用户ID不能为空')
  }
  if (orderData.petId === null || orderData.petId === undefined) {
    ElMessage.error('数据错误：宠物ID不能为空')
    throw new Error('宠物ID不能为空')
  }
  if (orderData.insuranceId === null || orderData.insuranceId === undefined) {
    ElMessage.error('数据错误：保险产品ID不能为空')
    throw new Error('保险产品ID不能为空')
  }
  if (!orderData.insuranceName || orderData.insuranceName.trim().length === 0) {
    ElMessage.error('数据错误：保险产品名称不能为空')
    throw new Error('保险产品名称不能为空')
  }
  if (orderData.paymentMethod === null || orderData.paymentMethod === undefined) {
    ElMessage.error('数据错误：缴费方式不能为空')
    throw new Error('缴费方式不能为空')
  } else if (orderData.paymentMethod !== "monthly" && orderData.paymentMethod !== "lump") {
    ElMessage.error('数据错误：缴费方式无效（仅支持monthly-分期/lump-全额）')
    throw new Error('缴费方式无效')
  }
  if (orderData.discountPremium === null || orderData.discountPremium === undefined) {
    ElMessage.error('数据错误：优惠保费不能为空')
    throw new Error('优惠保费不能为空')
  } else if (Number(orderData.discountPremium) <= 0) {
    ElMessage.error('数据错误：优惠保费不能为0或负数')
    throw new Error('优惠保费不合法')
  }
  if (orderData.guaranteeCycle === null || orderData.guaranteeCycle === undefined) {
    ElMessage.error('数据错误：保障周期不能为空')
    throw new Error('保障周期不能为空')
  } else if (Number(orderData.guaranteeCycle) <= 0) {
    ElMessage.error('数据错误：保障周期必须为正整数')
    throw new Error('保障周期不合法')
  }

  // 发起创建订单请求（后端路径：/api/order/create）
  return insuranceOrderAxios.post('/api/order/create', orderData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 根据用户ID查询宠物保险订单列表（后端路径：/api/order/user/{userId}）
export const getInsuranceOrderListByUserId = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法查询保险订单')
    throw new Error('userId不是有效数字')
  }
  // 严格匹配后端路径：/api/order/user/{userId}
  return insuranceOrderAxios.get(`/api/order/user/${Number(userId)}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 更新宠物保险订单状态（后端路径：/api/order/updateStatus）
export const updateInsuranceOrderStatus = async (orderId, status) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常，无法更新状态')
    throw new Error('orderId不是有效数字')
  }
  // 后端状态校验：0-已支付 1-已生效 2-已取消
  if (status < 0 || status > 2) {
    ElMessage.error('状态值无效（仅支持0-已支付 1-已生效 2-已取消）')
    throw new Error('status不合法')
  }
  // 后端参数：orderId + status（RequestParam）
  return insuranceOrderAxios.post('/api/order/updateStatus', null, {
    params: {
      orderId: Number(orderId),
      status: Number(status)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 根据订单ID查询宠物保险订单详情（后端路径：/api/order/{id}）
export const getInsuranceOrderDetail = async (orderId) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常，无法查询保险订单详情')
    throw new Error('orderId不是有效数字')
  }
  // 严格匹配后端路径：/api/order/{id}
  return insuranceOrderAxios.get(`/api/order/${Number(orderId)}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 5. 扩展：按状态筛选用户的宠物保险订单（前端封装）
export const getInsuranceOrderListByStatus = async (userId, status) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法查询保险订单')
    throw new Error('userId不是有效数字')
  }
  if (status < 0 || status > 2) {
    ElMessage.error('订单状态无效（仅支持0-已支付/1-已生效/2-已取消）')
    throw new Error('status不合法')
  }
  // 先获取用户所有保险订单，再前端筛选状态
  const allOrders = await getInsuranceOrderListByUserId(userId)
  return allOrders.filter(order => order.orderStatus === status)
}

// 6. 扩展：删除宠物保险订单（备用接口）
export const deleteInsuranceOrder = async (orderId) => {
  if (isNaN(Number(orderId))) {
    ElMessage.error('订单ID异常，无法删除保险订单')
    throw new Error('orderId不是有效数字')
  }
  // 注意：后端未提供删除接口，如需使用请先在后端添加 /api/order/delete/{id} 接口
  return insuranceOrderAxios.post(`/api/order/delete/${orderId}`, {}, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 7. 新增：一次性缴清剩余保费接口（核心补充）
export const payInsuranceOrderRemaining = async (orderId, userId) => {
  // 严格的参数校验（和后端保持一致）
  if (orderId === null || orderId === undefined || isNaN(Number(orderId)) || Number(orderId) <= 0) {
    ElMessage.error('数据错误：订单ID必须为正整数')
    throw new Error('订单ID不合法')
  }
  if (userId === null || userId === undefined || isNaN(Number(userId)) || Number(userId) <= 0) {
    ElMessage.error('数据错误：用户ID必须为正整数')
    throw new Error('用户ID不合法')
  }

  // 调用后端接口：/api/order/payRemaining（RequestParam传参）
  return insuranceOrderAxios.post('/api/order/payRemaining', null, {
    params: {
      orderId: Number(orderId),
      userId: Number(userId)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// ===================== 用户保险权益接口（新增，适配后端UserInsuranceBenefitController） =====================
// 1. 创建用户保险权益记录（购买保险后初始化）
export const createInsuranceBenefit = async (benefitData) => {
  // 参数校验（和后端完全对齐）
  if (benefitData.userId === null || benefitData.userId === undefined || isNaN(Number(benefitData.userId))) {
    ElMessage.error('用户ID异常，无法创建权益记录')
    throw new Error('userId不是有效数字')
  }
  if (benefitData.insuranceOrderId === null || benefitData.insuranceOrderId === undefined || isNaN(Number(benefitData.insuranceOrderId))) {
    ElMessage.error('保险订单ID异常，无法创建权益记录')
    throw new Error('insuranceOrderId不是有效数字')
  }
  if (benefitData.insuranceId === null || benefitData.insuranceId === undefined || isNaN(Number(benefitData.insuranceId))) {
    ElMessage.error('保险产品ID异常，无法创建权益记录')
    throw new Error('insuranceId不是有效数字')
  }
  if (benefitData.petId === null || benefitData.petId === undefined || isNaN(Number(benefitData.petId))) {
    ElMessage.error('宠物ID异常，无法创建权益记录')
    throw new Error('petId不是有效数字')
  }
  if (!benefitData.insuranceExpireTime) {
    ElMessage.error('保险到期时间不能为空')
    throw new Error('insuranceExpireTime为空')
  }
  
  // 发起创建请求
  return insuranceOrderAxios.post('/insurance/benefit/create', benefitData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 2. 根据保险订单ID查询用户保险权益
export const getInsuranceBenefitByOrderId = async (insuranceOrderId) => {
  if (isNaN(Number(insuranceOrderId))) {
    ElMessage.error('保险订单ID异常，无法查询权益')
    throw new Error('insuranceOrderId不是有效数字')
  }
  return insuranceOrderAxios.get(`/insurance/benefit/order/${Number(insuranceOrderId)}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 3. 根据用户ID查询所有保险权益
export const getInsuranceBenefitListByUserId = async (userId) => {
  if (isNaN(Number(userId))) {
    ElMessage.error('用户ID异常，无法查询权益列表')
    throw new Error('userId不是有效数字')
  }
  return insuranceOrderAxios.get(`/insurance/benefit/user/${Number(userId)}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 4. 根据宠物ID查询权益记录
export const getInsuranceBenefitListByPetId = async (petId) => {
  if (isNaN(Number(petId))) {
    ElMessage.error('宠物ID异常，无法查询权益列表')
    throw new Error('petId不是有效数字')
  }
  return insuranceOrderAxios.get(`/insurance/benefit/pet/${Number(petId)}`, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 5. 更新剩余保额（理赔后扣减）
export const updateRemainingInsuranceAmount = async (benefitId, amount) => {
  if (isNaN(Number(benefitId))) {
    ElMessage.error('权益ID异常，无法更新保额')
    throw new Error('benefitId不是有效数字')
  }
  if (isNaN(Number(amount)) || Number(amount) < 0) {
    ElMessage.error('剩余保额不能为负数')
    throw new Error('amount不合法')
  }
  return insuranceOrderAxios.post('/insurance/benefit/updateAmount', null, {
    params: {
      benefitId: Number(benefitId),
      amount: Number(amount)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 6. 更新月消费补贴余额（使用/发放后更新）
export const updateMonthlySubsidyBalance = async (benefitId, balance) => {
  if (isNaN(Number(benefitId))) {
    ElMessage.error('权益ID异常，无法更新补贴余额')
    throw new Error('benefitId不是有效数字')
  }
  if (isNaN(Number(balance)) || Number(balance) < 0) {
    ElMessage.error('补贴余额不能为负数')
    throw new Error('balance不合法')
  }
  return insuranceOrderAxios.post('/insurance/benefit/updateSubsidy', null, {
    params: {
      benefitId: Number(benefitId),
      balance: Number(balance)
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 7. 更新剩余赠送服务（使用后扣减次数）
export const updateFreeServiceRemaining = async (benefitId, serviceJson) => {
  if (isNaN(Number(benefitId))) {
    ElMessage.error('权益ID异常，无法更新赠送服务')
    throw new Error('benefitId不是有效数字')
  }
  // 校验JSON格式
  try {
    JSON.parse(serviceJson)
  } catch (err) {
    ElMessage.error('赠送服务格式错误（需为合法JSON字符串）')
    throw new Error('serviceJson不是合法JSON')
  }
  return insuranceOrderAxios.post('/insurance/benefit/updateService', null, {
    params: {
      benefitId: Number(benefitId),
      serviceJson: serviceJson
    },
    headers: { 'Content-Type': 'application/json' }
  })
}

// 8. 分页查询权益记录（管理员后台使用）
export const getInsuranceBenefitPage = async (pageNum = 1, pageSize = 10, userId) => {
  // 参数校验
  if (isNaN(Number(pageNum)) || pageNum < 1) {
    ElMessage.error('页码必须大于0')
    throw new Error('pageNum不合法')
  }
  if (isNaN(Number(pageSize)) || pageSize < 1 || pageSize > 100) {
    ElMessage.error('每页条数必须在1-100之间')
    throw new Error('pageSize不合法')
  }
  // 构建查询参数
  const params = {
    pageNum: Number(pageNum),
    pageSize: Number(pageSize)
  }
  if (userId !== undefined && !isNaN(Number(userId))) {
    params.userId = Number(userId)
  }
  // 发起请求
  return insuranceOrderAxios.get('/insurance/benefit/page', {
    params: params,
    headers: { 'Content-Type': 'application/json' }
  })
}

// 9. 逻辑删除权益记录
export const deleteInsuranceBenefit = async (benefitId) => {
  if (isNaN(Number(benefitId))) {
    ElMessage.error('权益ID异常，无法删除')
    throw new Error('benefitId不是有效数字')
  }
  return insuranceOrderAxios.post(`/insurance/benefit/delete/${Number(benefitId)}`, {}, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// ===================== 宠物保险理赔接口（新增） =====================
/**
 * 1. 根据ID查询理赔申请详情
 * GET /api/claim/{id}
 * @param {Number} id 理赔申请ID
 * @returns {Promise} 理赔详情
 */
export const getClaimById = async (id) => {
  if (isNaN(Number(id)) || Number(id) <= 0) {
    ElMessage.error('理赔申请ID必须为正整数')
    throw new Error('claimId不合法')
  }
  return claimAxios.get(`/api/claim/${Number(id)}`)
}

/**
 * 2. 根据理赔单号查询详情
 * GET /api/claim/by-no/{claimNo}
 * @param {String} claimNo 理赔单号
 * @returns {Promise} 理赔详情
 */
export const getClaimByClaimNo = async (claimNo) => {
  if (!claimNo || claimNo.trim() === '') {
    ElMessage.error('理赔单号不能为空')
    throw new Error('claimNo为空')
  }
  return claimAxios.get(`/api/claim/by-no/${claimNo.trim()}`)
}

/**
 * 3. 根据用户ID查询理赔申请列表
 * GET /api/claim/by-user/{userId}
 * @param {Number} userId 用户ID
 * @returns {Promise} 理赔列表
 */
export const getClaimListByUserId = async (userId) => {
  if (isNaN(Number(userId)) || Number(userId) <= 0) {
    ElMessage.error('用户ID必须为正整数')
    throw new Error('userId不合法')
  }
  return claimAxios.get(`/api/claim/by-user/${Number(userId)}`)
}

/**
 * 4. 根据保险订单ID查询理赔申请列表
 * GET /api/claim/by-order/{orderId}
 * @param {Number} orderId 保险订单ID
 * @returns {Promise} 理赔列表
 */
export const getClaimListByOrderId = async (orderId) => {
  if (isNaN(Number(orderId)) || Number(orderId) <= 0) {
    ElMessage.error('保险订单ID必须为正整数')
    throw new Error('orderId不合法')
  }
  return claimAxios.get(`/api/claim/by-order/${Number(orderId)}`)
}

/**
 * 5. 根据状态分页查询理赔申请列表
 * GET /api/claim/by-status/{status}?pageNum=1&pageSize=10
 * @param {Number} status 理赔状态
 * @param {Number} pageNum 页码（默认1）
 * @param {Number} pageSize 每页条数（默认10）
 * @returns {Promise} 分页理赔列表
 */
export const getClaimListByStatus = async (status, pageNum = 1, pageSize = 10) => {
  if (isNaN(Number(status))) {
    ElMessage.error('理赔状态必须为数字')
    throw new Error('status不合法')
  }
  if (isNaN(Number(pageNum)) || pageNum < 1) {
    ElMessage.error('页码必须大于0')
    throw new Error('pageNum不合法')
  }
  if (isNaN(Number(pageSize)) || pageSize < 1 || pageSize > 100) {
    ElMessage.error('每页条数必须在1-100之间')
    throw new Error('pageSize不合法')
  }
  return claimAxios.get(`/api/claim/by-status/${Number(status)}`, {
    params: {
      pageNum: Number(pageNum),
      pageSize: Number(pageSize)
    }
  })
}

/**
 * 6. 创建理赔申请
 * POST /api/claim
 * @param {Object} claimData 理赔申请数据
 * @returns {Promise} 创建结果
 */
export const createInsuranceClaim = async (claimData) => {
  // 基础参数校验（与后端一致）
  if (isNaN(Number(claimData.userId)) || Number(claimData.userId) <= 0) {
    ElMessage.error('用户ID必须为正整数')
    throw new Error('userId不合法')
  }
  if (isNaN(Number(claimData.insuranceOrderId)) || Number(claimData.insuranceOrderId) <= 0) {
    ElMessage.error('保险订单ID必须为正整数')
    throw new Error('insuranceOrderId不合法')
  }
  if (!claimData.petType || claimData.petType.trim() === '') {
    ElMessage.error('宠物种类不能为空')
    throw new Error('petType为空')
  }
  if (!claimData.petNickname || claimData.petNickname.trim() === '') {
    ElMessage.error('宠物昵称不能为空')
    throw new Error('petNickname为空')
  }
  if (!claimData.contactPhone || !/^1[3-9]\d{9}$/.test(claimData.contactPhone)) {
    ElMessage.error('请输入正确的联系电话')
    throw new Error('contactPhone不合法')
  }
  if (!claimData.realName || claimData.realName.trim() === '') {
    ElMessage.error('真实姓名不能为空')
    throw new Error('realName为空')
  }
  if (!claimData.userEmail || !/^[\w.-]+@[a-zA-Z0-9-]+\.[a-zA-Z]+$/.test(claimData.userEmail)) {
    ElMessage.error('请输入正确的邮箱')
    throw new Error('userEmail不合法')
  }
  if (claimData.isSurgery === '' || claimData.isSurgery === undefined) {
    ElMessage.error('请选择是否手术')
    throw new Error('isSurgery未选择')
  }
  if (!claimData.accidentTime) {
    ElMessage.error('出险时间不能为空')
    throw new Error('accidentTime为空')
  }
  if (claimData.hospitalType === '' || claimData.hospitalType === undefined) {
    ElMessage.error('请选择就诊医院类型')
    throw new Error('hospitalType未选择')
  }
  if (isNaN(Number(claimData.medicalCost)) || Number(claimData.medicalCost) <= 0) {
    ElMessage.error('就诊费用必须大于0')
    throw new Error('medicalCost不合法')
  }
  if (!claimData.illnessDesc || claimData.illnessDesc.trim().length < 10) {
    ElMessage.error('病情描述至少10个字')
    throw new Error('illnessDesc不合法')
  }

  // 图片URL字段兜底（避免后端接收null）
  const submitData = {
    ...claimData,
    petFrontPhotoUrl: claimData.petFrontPhotoUrl || '',
    petFullPhotoUrl: claimData.petFullPhotoUrl || '',
    medicalRecordUrl: claimData.medicalRecordUrl || '',
    inspectionReportUrl: claimData.inspectionReportUrl || '',
    costDetailUrl: claimData.costDetailUrl || '',
    medicalInvoiceUrl: claimData.medicalInvoiceUrl || '',
    treatmentPhotoUrl: claimData.treatmentPhotoUrl || ''
  }

  return claimAxios.post('/api/claim', submitData, {
    headers: { 'Content-Type': 'application/json' }
  })
}

/**
 * 7. 更新理赔申请状态
 * PUT /api/claim/{id}/status
 * @param {Number} id 理赔ID
 * @param {Number} status 目标状态
 * @param {Number} [auditorId] 审核人ID（可选）
 * @param {String} [auditRemark] 审核备注（可选）
 * @returns {Promise} 更新结果
 */
export const updateClaimStatus = async (id, status, auditorId, auditRemark) => {
  if (isNaN(Number(id)) || Number(id) <= 0) {
    ElMessage.error('理赔ID必须为正整数')
    throw new Error('claimId不合法')
  }
  if (isNaN(Number(status))) {
    ElMessage.error('理赔状态必须为数字')
    throw new Error('status不合法')
  }

  const params = { status }
  if (auditorId !== undefined && !isNaN(Number(auditorId))) {
    params.auditorId = Number(auditorId)
  }
  if (auditRemark !== undefined && auditRemark.trim() !== '') {
    params.auditRemark = auditRemark.trim()
  }

  return claimAxios.put(`/api/claim/${Number(id)}/status`, null, { params })
}

/**
 * 8. 删除理赔申请（逻辑删除）
 * DELETE /api/claim/{id}
 * @param {Number} id 理赔ID
 * @returns {Promise} 删除结果
 */
export const deleteClaim = async (id) => {
  if (isNaN(Number(id)) || Number(id) <= 0) {
    ElMessage.error('理赔ID必须为正整数')
    throw new Error('claimId不合法')
  }
  return claimAxios.delete(`/api/claim/${Number(id)}`)
}

/**
 * 9. 分页查询所有理赔申请（管理员接口）
 * GET /api/claim/page?pageNum=1&pageSize=10
 * @param {Number} pageNum 页码（默认1）
 * @param {Number} pageSize 每页条数（默认10）
 * @returns {Promise} 分页理赔列表
 */
export const getClaimPage = async (pageNum = 1, pageSize = 10) => {
  if (isNaN(Number(pageNum)) || pageNum < 1) {
    ElMessage.error('页码必须大于0')
    throw new Error('pageNum不合法')
  }
  if (isNaN(Number(pageSize)) || pageSize < 1 || pageSize > 100) {
    ElMessage.error('每页条数必须在1-100之间')
    throw new Error('pageSize不合法')
  }
  return claimAxios.get('/api/claim/page', {
    params: {
      pageNum: Number(pageNum),
      pageSize: Number(pageSize)
    }
  })
}

/**
 * 10. 上传理赔材料（兼容临时claimId=0）
 * POST /api/claim/upload-material
 * @param {File} file 上传文件
 * @param {Number} claimId 理赔ID（允许0作为临时标识）
 * @param {String} materialType 材料类型（petFrontPhoto/petFullPhoto/medicalRecord等）
 * @returns {Promise} 上传结果（含图片访问URL）
 */
export const uploadClaimMaterial = async (file, claimId, materialType) => {
  if (!file) {
    ElMessage.error('上传文件不能为空')
    throw new Error('file为空')
  }
  if (claimId === null || claimId === undefined) {
    ElMessage.error('理赔ID不能为空')
    throw new Error('claimId为空')
  }
  const validTypes = ['petFrontPhoto', 'petFullPhoto', 'medicalRecord', 'inspectionReport', 'costDetail', 'medicalInvoice', 'treatmentPhoto']
  if (!materialType || !validTypes.includes(materialType)) {
    ElMessage.error(`材料类型不合法，仅支持：${validTypes.join('、')}`)
    throw new Error('materialType不合法')
  }

  const formData = new FormData()
  formData.append('file', file)
  formData.append('claimId', Number(claimId))
  formData.append('materialType', materialType)

  return claimAxios.post('/api/claim/upload-material', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 11. 更新临时材料的关联claimId（将temp文件关联到真实理赔ID）
 * PUT /api/claim/update-material-claim-id
 * @param {Number} oldClaimId 旧理赔ID（临时ID=0）
 * @param {Number} newClaimId 新理赔ID（真实ID）
 * @param {Array<String>} materialUrls 上传成功的材料URL列表
 * @returns {Promise} 更新结果
 */
export const updateMaterialClaimId = async (oldClaimId, newClaimId, materialUrls) => {
  if (oldClaimId === null || oldClaimId === undefined) {
    ElMessage.error('旧理赔ID不能为空')
    throw new Error('oldClaimId为空')
  }
  if (isNaN(Number(newClaimId)) || Number(newClaimId) <= 0) {
    ElMessage.error('新理赔ID必须为正整数')
    throw new Error('newClaimId不合法')
  }
  if (!Array.isArray(materialUrls) || materialUrls.length === 0) {
    ElMessage.error('材料URL列表不能为空')
    throw new Error('materialUrls为空')
  }

  // 转逗号分隔字符串传递
  const materialUrlsStr = materialUrls.filter(url => url && url.trim() !== '').join(',')
  if (materialUrlsStr === '') {
    ElMessage.error('材料URL列表不能为空')
    throw new Error('materialUrls无有效URL')
  }

  return claimAxios.put('/api/claim/update-material-claim-id', null, {
    params: {
      oldClaimId: Number(oldClaimId),
      newClaimId: Number(newClaimId),
      materialUrls: materialUrlsStr
    }
  })
}

/**
 * 12. 更新理赔申请的图片URL
 * PUT /api/claim/{id}/urls
 * @param {Number} id 理赔ID
 * @param {Object} urlData 图片URL映射（key为URL字段名，value为URL值）
 * @returns {Promise} 更新结果
 */
export const updateClaimUrls = async (id, urlData) => {
  if (isNaN(Number(id)) || Number(id) <= 0) {
    ElMessage.error('理赔ID必须为正整数')
    throw new Error('claimId不合法')
  }
  if (typeof urlData !== 'object' || urlData === null) {
    ElMessage.error('URL数据必须为对象')
    throw new Error('urlData不合法')
  }

  const validUrlFields = [
    'petFrontPhotoUrl', 'petFullPhotoUrl', 'medicalRecordUrl',
    'inspectionReportUrl', 'costDetailUrl', 'medicalInvoiceUrl', 'treatmentPhotoUrl'
  ]
  // 过滤无效字段
  const validData = Object.keys(urlData).reduce((obj, key) => {
    if (validUrlFields.includes(key) && typeof urlData[key] === 'string') {
      obj[key] = urlData[key].trim()
    }
    return obj
  }, {})

  if (Object.keys(validData).length === 0) {
    ElMessage.error('无有效URL字段需要更新')
    throw new Error('validData为空')
  }

  return claimAxios.put(`/api/claim/${Number(id)}/urls`, validData, {
    headers: { 'Content-Type': 'application/json' }
  })
}