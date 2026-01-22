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
// 宠物保险模块实例（新增）
const insuranceAxios = createAxiosInstance(BASE_URL)

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
    
    // 方案2：返回公共占位符URL（无需本地文件）
    // return 'https://via.placeholder.com/200x150?text=保险默认图';
  }
  // 已包含完整域名直接返回
  if (imgPath.startsWith('http://') || imgPath.startsWith('https://')) {
    return imgPath;
  }
  // 拼接基础URL（适配后端静态资源映射）
  return `${BASE_URL}${imgPath.startsWith('/') ? '' : '/'}${imgPath}`;
};