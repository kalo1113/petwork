import request from '@/utils/request' 

// ====================== 商家保险核心操作 ======================
// 创建保险（适配后端 /insurance/add 接口，纯JSON提交）
export const createInsurance = (data) => {
  return request({
    url: '/insurance/add',
    method: 'post',
    data
  })
}

// 获取保险列表（分页查询，适配后端 /insurance/page 接口）
export const getInsuranceList = (params = {}) => {
  return request({
    url: '/insurance/page',
    method: 'get',
    params
  })
}

// 获取保险详情（包含图片，适配后端 /insurance/detail/{id} 接口）
export const getInsuranceDetail = (id) => {
  return request({
    url: `/insurance/detail/${id}`,
    method: 'get'
  })
}

// 修改保险信息（适配后端 /insurance/update 接口）
export const updateInsurance = (data) => {
  return request({
    url: '/insurance/update',
    method: 'post',
    data
  })
}

// 删除保险（适配后端 /insurance/delete/{id} 接口）
export const deleteInsurance = (id) => {
  return request({
    url: `/insurance/delete/${id}`,
    method: 'post'
  })
}

// 更新保险状态（上架/下架，适配后端 /insurance/updateStatus 接口）
// 修正：改用data传参（POST请求推荐用body传参，与后端接口更匹配）
export const updateInsuranceStatus = (data) => {
  return request({
    url: '/insurance/updateStatus',
    method: 'post',
    data // 原params改为data，传 {id: 1, status: 1} 格式
  })
}

// ====================== 保险图片操作（完全匹配后端） ======================
// 上传保险图片（适配后端 /insurance/media/upload 接口，文件上传）
export const uploadInsuranceMedia = (formData) => {
  return request({
    url: '/insurance/media/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除保险图片（适配后端 /insurance/media/delete/{id} 接口，同时删文件+数据库）
export const deleteInsuranceMedia = (id) => {
  return request({
    url: `/insurance/media/delete/${id}`,
    method: 'post'
  })
}

// 获取保险关联的图片列表（适配后端 /insurance/media/list/{insuranceId} 接口）
export const getInsuranceMediaList = (insuranceId) => {
  return request({
    url: `/insurance/media/list/${insuranceId}`,
    method: 'get'
  })
}

// ====================== 商家端订单管理接口（新增，匹配后端 PetInsuranceOrderController） ======================
// 1. 创建保险订单（适配后端 /api/order/create 接口）
export const createInsuranceOrder = (data) => {
  return request({
    url: '/api/order/create',
    method: 'post',
    data
  })
}

// 2. 根据用户ID查询订单列表（适配后端 /api/order/user/{userId} 接口）
export const getOrderListByUserId = (userId) => {
  return request({
    url: `/api/order/user/${userId}`,
    method: 'get'
  })
}

// 3. 更新订单状态（适配后端 /api/order/updateStatus 接口）
export const updateOrderStatus = (data) => {
  return request({
    url: '/api/order/updateStatus',
    method: 'post',
    data // 传参格式：{orderId: 1, status: 0}
  })
}

// 4. 根据订单ID查询订单详情（适配后端 /api/order/{id} 接口）
export const getOrderDetail = (id) => {
  return request({
    url: `/api/order/${id}`,
    method: 'get'
  })
}

// 5. 一次性缴清剩余保费（适配后端 /api/order/payRemaining 接口）
export const payRemainingPremium = (data) => {
  return request({
    url: '/api/order/payRemaining',
    method: 'post',
    data // 传参格式：{orderId: 1, userId: 1}
  })
}

// 6. 【扩展】商家端查询所有订单（分页，需后端补充接口后启用）
export const getMerchantOrderList = (params = {}) => {
  return request({
    url: '/api/order/merchant/page',
    method: 'get',
    params // 传参：{pageNum: 1, pageSize: 10, status: 1, insuranceId: 1}
  })
}

// ====================== 保险订单管理接口（修改路径，匹配后端 /api/order/merchant/*） ======================
// 1. 获取待审核列表（适配后端 /api/order/merchant/page 接口）
export const getAuditList = (params = {}) => {
  return request({
    url: '/api/order/merchant/page', // 修改为后端实际路径
    method: 'get',
    params // 传参：{pageNum: 1, pageSize: 10, insuranceName: '', userName: ''}
  })
}
// 审核接口
export function auditOrder(data) {
  return request({
    url: '/api/order/merchant/audit', // 确认地址和后端一致
    method: 'post',
    data: data // 直接传{orderId, status, auditRemark}
  })
}

// 4. 新增：根据订单ID查询订单详情（含宠物信息）
export const getOrderDetailById = (orderId) => {
  return request({
    url: `/api/order/merchant/detail/${orderId}`, // 匹配后端详情接口路径
    method: 'get',
    // 无参数（参数拼在URL路径中）
  })
}

// ====================== 商家端商品订单管理接口（匹配后端 PetOrderController） ======================
// 1. 创建商品订单（适配后端 /order/create 接口）
export const createProductOrder = (params, itemList) => {
  return request({
    url: '/order/create',
    method: 'post',
    params: {
      ...params, // 包含userId, receiverName, receiverPhone, receiverAddress
    },
    data: itemList // 订单明细表数据列表
  })
}

// 2. 查询用户的商品订单列表（适配后端 /order/list 接口）
export const getProductOrderListByUserId = (userId) => {
  return request({
    url: '/order/list',
    method: 'get',
    params: { userId }
  })
}

// 3. 查询商品订单详情（含商品图片，适配后端 /order/detail 接口）
export const getProductOrderDetail = (orderId) => {
  return request({
    url: '/order/detail',
    method: 'get',
    params: { orderId }
  })
}

// 4. 更新商品订单状态（适配后端 /order/updateStatus 接口）
export const updateProductOrderStatus = (data) => {
  return request({
    url: '/order/updateStatus',
    method: 'post',
    params: {
      orderId: data.orderId,
      status: data.status
    }
  })
}

// 5. 删除商品订单（适配后端 /order/delete 接口）
export const deleteProductOrder = (orderId) => {
  return request({
    url: '/order/delete',
    method: 'post',
    params: { orderId }
  })
}

// 6. 商家端分页查询所有商品订单（核心接口，适配后端 /order/merchant/page 接口）
// 支持：精准查询(orderId/userId)、模糊查询(orderIdLike/userIdLike)、状态筛选(orderStatus)、分页
export const getMerchantProductOrderList = (params = {}) => {
  // 处理参数：空值过滤，避免传递无效参数给后端
  const queryParams = {}
  // 分页参数（必传，默认值由后端处理）
  if (params.pageNum !== undefined) queryParams.pageNum = params.pageNum
  if (params.pageSize !== undefined) queryParams.pageSize = params.pageSize
  
  // 订单状态筛选
  if (params.orderStatus !== undefined && params.orderStatus !== -1) {
    queryParams.orderStatus = params.orderStatus
  }
  
  // 精准查询（优先）
  if (params.orderId !== undefined && params.orderId !== null) {
    queryParams.orderId = params.orderId
  }
  if (params.userId !== undefined && params.userId !== null) {
    queryParams.userId = params.userId
  }
  
  // 模糊查询（补充）
  if (params.orderIdLike !== undefined && params.orderIdLike.trim() !== '') {
    queryParams.orderIdLike = params.orderIdLike.trim()
  }
  if (params.userIdLike !== undefined && params.userIdLike.trim() !== '') {
    queryParams.userIdLike = params.userIdLike.trim()
  }

  return request({
    url: '/order/merchant/page',
    method: 'get',
    params: queryParams
  })
}

// 7. 快捷查询方法（可选，简化前端调用）
/**
 * 商家端精准查询单个订单
 * @param {Number} orderId 订单ID
 * @returns {Promise}
 */
export const getMerchantOrderByOrderId = (orderId) => {
  return getMerchantProductOrderList({
    pageNum: 1,
    pageSize: 1,
    orderId: orderId
  })
}

/**
 * 商家端查询指定用户的所有订单
 * @param {Number} userId 用户ID
 * @param {Number} pageNum 页码
 * @param {Number} pageSize 每页条数
 * @returns {Promise}
 */
export const getMerchantOrderByUserId = (userId, pageNum = 1, pageSize = 10) => {
  return getMerchantProductOrderList({
    pageNum,
    pageSize,
    userId: userId
  })
}

/**
 * 商家端模糊搜索订单（支持订单号/用户ID模糊匹配）
 * @param {Object} searchParams 搜索参数 {orderIdLike, userIdLike, orderStatus, pageNum, pageSize}
 * @returns {Promise}
 */
export const searchMerchantOrder = (searchParams = {}) => {
  return getMerchantProductOrderList({
    pageNum: searchParams.pageNum || 1,
    pageSize: searchParams.pageSize || 10,
    orderStatus: searchParams.orderStatus,
    orderIdLike: searchParams.orderIdLike,
    userIdLike: searchParams.userIdLike
  })
}