<template>
  <div class="page-container">
    <div class="setting-header">
      <el-button type="text" class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <h3 class="setting-title">我的订单</h3>
    </div>
    <div class="order-page">
      <!-- 选项卡导航 -->
      <div class="order-tabs">
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'effective'}"
          @click="switchTab('effective')"
        >已生效</div>
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'wallet'}"
          @click="switchTab('wallet')"
        >我的钱包</div>
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'deliver'}"
          @click="switchTab('deliver')"
        >待发货</div>
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'receive'}"
          @click="switchTab('receive')"
        >待收货</div>
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'comment'}"
          @click="switchTab('comment')"
        >待评价</div>
      </div>

      <!-- 根据选中的选项卡展示对应内容 -->
      <div class="order-content">
        <!-- 已生效订单 -->
        <div v-if="activeTab === 'effective'">
          <div v-if="!effectiveOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无已生效订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 已生效订单列表渲染 -->
            <div class="order-item" v-for="order in effectiveOrders" :key="order.id">
              {{ order.title }} - {{ order.amount }}元
            </div>
          </div>
        </div>

        <!-- 我的钱包 -->
        <div v-if="activeTab === 'wallet'">
          <div class="wallet-content">
            <div class="wallet-balance-row">
              <span class="balance-label">钱包余额：¥{{ walletBalance }}</span>
              <!-- 放大后的充值按钮 -->
              <el-button 
                class="recharge-btn" 
                type="primary" 
                @click="handleRechargeClick"
                :disabled="!userId"
              >
                充值
              </el-button>
            </div>
            <div class="wallet-pending-row">
              <span class="balance-label">待入账金额：</span>
              <span class="balance-value">¥{{ pendingAmount }}</span>
            </div>
          </div>
        </div>

        <!-- 待发货订单 -->
        <div v-if="activeTab === 'deliver'">
          <div v-if="!deliverOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无待发货订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 待发货订单列表（一个订单号下展示多个商品） -->
            <div class="order-item" v-for="order in deliverOrders" :key="order.orderId">
              <!-- 订单头：订单号 + 状态 -->
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderId }}</span>
                <span class="order-status">待发货</span>
              </div>

              <!-- 商品列表（一个订单下的多个商品） -->
              <div class="order-product-list">
                <div 
                  class="product-item" 
                  v-for="(item, idx) in order.itemList" 
                  :key="idx"
                >
                  <!-- 商品图片 -->
                  <img 
                    :src="getImgUrl(item.productImgPath)" 
                    :alt="item.productTitle" 
                    class="product-img"
                    @error="(e) => handleImgError(e, item)"
                  >
                  <!-- 商品信息 -->
                  <div class="product-info">
                    <p class="product-title">{{ item.productTitle }}</p>
                    <p class="product-desc">{{ item.productDescription }}</p>
                  </div>
                  <!-- 商品价格 + 数量 -->
                  <div class="product-amount">
                    <span class="price">¥{{ item.itemAmount }}</span>
                    <span class="count">×{{ item.productCount }}</span>
                  </div>
                </div>
              </div>

              <!-- 实付款 -->
              <div class="order-footer">
                <span class="total-amount">实付款 ¥{{ order.totalAmount }}</span>
              </div>

              <!-- 操作按钮 -->
              <div class="order-operate">
                <!-- 修复：传递order参数给handleMoreCommand -->
                <el-dropdown @command="(cmd) => handleMoreCommand(cmd, order)" placement="top">
                  <el-button type="text" class="operate-btn">
                    更多
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="delete">删除订单</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>

                <el-button type="text" class="operate-btn">查看物流</el-button>
                <!-- 加入购物车按钮绑定事件 -->
                <el-button 
                  type="text" 
                  class="operate-btn"
                  @click="handleAddToCart(order)"
                >
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 待收货订单 -->
        <div v-if="activeTab === 'receive'">
          <div v-if="!receiveOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无待收货订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 待收货订单列表 -->
            <div class="order-item" v-for="order in receiveOrders" :key="order.id">
              {{ order.title }} - {{ order.amount }}元
            </div>
          </div>
        </div>

        <!-- 待评价订单 -->
        <div v-if="activeTab === 'comment'">
          <div v-if="!commentOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无待评价订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 待评价订单列表 -->
            <div class="order-item" v-for="order in commentOrders" :key="order.id">
              {{ order.title }} - {{ order.amount }}元
            </div>
          </div>
        </div>

        <!-- 全部订单 -->
        <div v-if="activeTab === 'all'">
          <div v-if="!allOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 全部订单列表 -->
            <div class="order-item" v-for="order in allOrders" :key="order.id">
              {{ order.title }} - {{ order.amount }}元
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog
      v-model="rechargeDialogVisible"
      title="钱包充值"
      width="400px"
      :close-on-click-modal="false"
      :before-close="handleDialogClose"
    >
      <el-form :model="rechargeForm" :rules="rechargeRules" ref="rechargeFormRef" label-width="80px">
        <el-form-item label="充值金额" prop="amount">
          <el-input
            v-model="rechargeForm.amount"
            type="number"
            placeholder="请输入充值金额（最低1元）"
            min="1"
            step="1"
            size="default"
            class="recharge-input"
          ></el-input>
        </el-form-item>
        <el-form-item label="快捷充值">
          <div class="quick-recharge-wrap">
            <el-button 
              v-for="amount in [10, 20, 50, 100, 200, 500]" 
              :key="amount"
              type="text" 
              @click="rechargeForm.amount = amount"
              :class="['quick-recharge-btn', {active: rechargeForm.amount === amount}]"
            >
              ¥{{ amount }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogClose">取消</el-button>
        <el-button type="primary" @click="handleRecharge" :loading="isRecharging">
          确认充值
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElIcon, ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
// 替换不存在的getOrderListByStatus，引入getOrderList
import { rechargeWallet, getUserAmountInfo, getOrderList, deleteOrder, addToCart, checkProductExist } from '@/api/user/index.js'

const router = useRouter()
const route = useRoute()

// 初始化选中的选项卡
const activeTab = ref(route.query.activeTab || 'effective')

// 订单数据
const effectiveOrders = ref([])
const deliverOrders = ref([])
const receiveOrders = ref([])
const commentOrders = ref([])
const allOrders = ref([])
// 新增：存储用户所有订单（作为待发货订单的数据源）
const allOrderList = ref([])

// 钱包金额（从接口获取）
const walletBalance = ref(0)
const pendingAmount = ref(0)

// 当前登录用户ID（优先从本地存储获取，无则提示登录）
const userId = ref('')
// 从 userData 中解析 userId
const initUserId = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const user = JSON.parse(userData)
    userId.value = user.userId || ''
  }
}

// 充值弹窗相关
const rechargeDialogVisible = ref(false)
const isRecharging = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = reactive({
  amount: ''
})

// 充值校验规则
const rechargeRules = reactive({
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', message: '充值金额必须为数字', trigger: 'blur' },
    { min: 1, message: '充值金额最低1元', trigger: 'blur' }
  ]
})

/** 切换选项卡（仅切换内容，不跳转路由） */
const switchTab = (tabKey) => {
  activeTab.value = tabKey
  // 仅请求对应数据，移除路由跳转逻辑
  fetchOrderData(tabKey)
}

// 处理图片路径（适配本地/服务器路径，兜底改为本地SVG图）
const getImgUrl = (imgPath) => {
  // 空路径直接返回本地默认图
  if (!imgPath || imgPath.trim() === '') {
    return new URL('@/assets/images/我的图标/查询.svg', import.meta.url).href;
  }
  
  // 适配Windows路径（替换反斜杠+截取productimg后的路径）
  const relativePath = imgPath.split('productimg\\')[1]?.replace(/\\/g, '/');
  
  // 有有效路径则拼接服务器地址，否则返回本地默认图
  return relativePath 
    ? `http://localhost:8080/product-images/${relativePath}` 
    : new URL('@/assets/images/我的图标/查询.svg', import.meta.url).href;
};

// 防无限报错：图片错误处理
const handleImgError = (e, item) => {
  if (item.imgErrorHandled) return;
  item.imgErrorHandled = true;
  // 改为本地默认SVG图
  e.target.src = new URL('@/assets/images/我的图标/查询.svg', import.meta.url).href;
  console.warn(`商品【${item.productTitle || '未知商品'}】图片加载失败，已替换为默认图`);
};

/** 请求数据（仅修改待发货订单逻辑，其他不变） */
const fetchOrderData = async (tabKey) => {
  // 无用户ID时提示登录
  if (!userId.value) {
    handleNoLogin()
    return
  }

  try {
    switch (tabKey) {
      case 'effective':
        // 保留原有逻辑，不修改
        break
      case 'wallet': {
        // 查询钱包金额（原有逻辑不变）
        const amountRes = await getUserAmountInfo(userId.value)
        if (amountRes.code === 200 && amountRes.data) {
          walletBalance.value = amountRes.data.accountBalance || 0
          pendingAmount.value = amountRes.data.pendingAmount || 0
        } else if (amountRes.code === 404) {
          ElMessage.warning(amountRes.msg || '用户信息不存在，请重新登录')
          localStorage.removeItem('userId') // 清除无效的用户ID
          userId.value = null
        } else {
          ElMessage.error(amountRes.msg || '查询钱包信息失败')
        }
        break
      }
      case 'deliver': {
        // 核心修改：先获取用户所有订单，再筛选状态为1（待发货）的订单
        const res = await getOrderList(userId.value)
        if (res.code === 200) {
          allOrderList.value = res.data || []
          // 筛选待发货订单（状态1）
          deliverOrders.value = allOrderList.value.filter(order => order.orderStatus === 1)
        } else {
          ElMessage.error(res.msg || '获取订单列表失败')
          deliverOrders.value = []
        }
        break
      }
      case 'receive':
        // 保留原有逻辑，不修改
        break
      case 'comment':
        // 保留原有逻辑，不修改
        break
      case 'all':
        // 保留原有逻辑，不修改
        break
      default:
        break
    }
  } catch (err) {
    console.error(`获取${tabKey}数据失败：`, err)
    ElMessage.error('网络异常，获取数据失败，请重试')
  }
}

// ========== 修复：删除订单（接收order参数） ==========
const handleMoreCommand = async (command, order) => {
  // 校验order是否存在
  if (!order || !order.orderId) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }

  if (command === 'delete') {
    // 1. 确认弹窗
    try {
      await ElMessageBox.confirm(
        '此操作将永久删除该订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' }
      )

      // 2. 调用删除接口（打印日志，方便调试）
      console.log('删除订单ID：', order.orderId)
      const res = await deleteOrder(order.orderId)
      if (res.code === 200) {
        ElMessage.success('订单删除成功！')
        // 3. 重新拉取订单列表（刷新页面）
        await fetchOrderData('deliver')
      } else {
        ElMessage.error(res.msg || '订单删除失败')
      }
    } catch (err) {
      // 取消删除的情况
      if (err !== 'cancel') {
        ElMessage.error('删除订单时发生异常：' + err.message)
      } else {
        ElMessage.info('已取消删除')
      }
    }
  }
}

// ========== 简化版：加入购物车（仅校验商品ID是否存在） ==========
const handleAddToCart = async (order) => {
  if (!userId.value) {
    handleNoLogin()
    return
  }

  // 校验订单商品
  if (!order || !order.itemList || order.itemList.length === 0) {
    ElMessage.error('订单无商品，无法加入购物车')
    return
  }

  let successCount = 0
  let failCount = 0
  const failProducts = []
  const notExistProducts = [] // 记录商品ID不存在的商品

  try {
    // 批量处理订单内的商品
    for (const item of order.itemList) {
      try {
        // 1. 基础校验：商品ID不能为空
        if (!item.productId) {
          failCount++
          failProducts.push(`${item.productTitle || '未知商品'}（无商品ID）`)
          continue
        }

        // 2. 核心：查询商品ID是否存在（调用新增的checkProductExist接口）
        let isProductExist = false
        try {
          const existRes = await checkProductExist(item.productId)
          // 后端返回布尔值true/false（或字符串"exist"/"not exist"）
          isProductExist = existRes === true || existRes === "exist"
        } catch (productErr) {
          failCount++
          failProducts.push(`${item.productTitle}（商品信息查询失败）`)
          console.error(`查询商品【${item.productId}】是否存在失败：`, productErr)
          continue
        }

        // 商品ID不存在（已删除/下架），跳过加购
        if (!isProductExist) {
          failCount++
          notExistProducts.push(item.productTitle)
          failProducts.push(`${item.productTitle}（商品不存在/已下架）`)
          continue
        }

        // 3. 商品ID存在，调用加购接口（适配修改后的addToCart API）
        const res = await addToCart(userId.value, item.productId, item.productCount)
        
        // 4. 匹配后端返回：纯字符串 "添加购物车成功"/"添加失败"
        if (res === "添加购物车成功") {
          successCount++
          console.log(`商品【${item.productTitle}】加购成功`)
        } else {
          failCount++
          failProducts.push(`${item.productTitle}（${res || '加购失败'}）`)
        }
      } catch (err) {
        failCount++
        failProducts.push(`${item.productTitle || '未知商品'}（加购接口异常）`)
        console.error(`加购商品异常：`, err)
      }
    }

    // 5. 精准提示逻辑
    if (successCount > 0 && failCount === 0) {
      ElMessage.success(` 全部${successCount}个商品加入购物车成功！`)
    } else if (successCount > 0 && failCount > 0) {
      let tip = `⚠️ 成功加购${successCount}个商品；`
      if (notExistProducts.length > 0) {
        tip += `${notExistProducts.length}个商品不存在（${notExistProducts.join('、')}）；`
      }
      tip += `剩余${failCount - notExistProducts.length}个商品加购失败`
      ElMessage.warning(tip)
    } else if (failCount > 0) {
      let tip = `❌ 所有商品加购失败：`
      if (notExistProducts.length > 0) {
        tip += `${notExistProducts.length}个商品不存在（${notExistProducts.join('、')}），`
      }
      tip += `其余${failCount - notExistProducts.length}个商品加购接口失败`
      ElMessage.error(tip)
    }
  } catch (err) {
    ElMessage.error('加入购物车失败：' + err.message)
  }
}

/** 返回上一页 */
const handleBack = () => {
  try {
    router.go(-1)
    setTimeout(() => {
      const currentPath = router.currentRoute.fullPath
      if (currentPath === '/my-orders') {
        router.push('/my').catch(err => {
          console.error('兜底跳转失败：', err)
          ElMessage.error('返回失败，请重试')
        })
      }
    }, 100)
  } catch (_) {
    console.error('返回失败：未知错误')
    router.push('/my').catch(() => {
      ElMessage.error('返回失败，请重试')
    })
  }
}

/** 未登录处理 */
const handleNoLogin = () => {
  ElMessageBox.confirm(
    '您还未登录，请先登录',
    '提示',
    {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 跳转到登录页，登录后返回当前页面
    router.push({
      path: '/login',
      query: { redirect: router.currentRoute.fullPath }
    })
  }).catch(() => {
    ElMessage.info('已取消登录')
  })
}

/** 点击充值按钮（先校验登录） */
const handleRechargeClick = () => {
  if (!userId.value) {
    handleNoLogin()
    return
  }
  rechargeDialogVisible.value = true
}

/** 重置充值表单 */
const resetRechargeForm = () => {
  rechargeForm.amount = ''
  if (rechargeFormRef.value) {
    rechargeFormRef.value.clearValidate()
  }
}

/** 关闭充值弹窗 */
const handleDialogClose = () => {
  rechargeDialogVisible.value = false
  resetRechargeForm()
}

/** 处理充值逻辑 */
const handleRecharge = async () => {
  // 1. 校验登录状态
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (!rechargeFormRef.value) return

    // 2. 手动前置校验（绕过element内置校验的坑）
    const amount = Number(rechargeForm.amount)
    if (isNaN(amount) || amount < 1) {
      ElMessage.error('充值金额必须是大于等于1的数字')
      return
    }

    // 3. 手动清除表单校验状态（避免残留错误提示）
    if (rechargeFormRef.value) {
      rechargeFormRef.value.clearValidate()
    }

    isRecharging.value = true

    // 4. 调用充值接口（确保传数字类型）
    const res = await rechargeWallet(userId.value, amount)
    
    if (res && res.code === 200) {
      ElMessage.success(`充值成功！已为您的钱包充值¥${amount}`)
      // 刷新钱包金额
      await fetchOrderData('wallet')
      // 关闭弹窗并重置表单
      handleDialogClose()
    } else {
      ElMessage.error(res?.msg || '充值失败，请稍后重试')
    }
  } catch (err) {
    // 5. 捕获接口异常（排除表单校验干扰）
    console.error('充值接口调用失败：', err)
    // 区分接口错误和其他错误
    const errMsg = err?.response?.data?.msg || err?.message || '充值失败，请重试'
    ElMessage.error(errMsg)
  } finally {
    // 6. 无论成功失败，都停止加载状态
    isRecharging.value = false
  }
}

// 页面加载时初始化
onMounted(() => {
  initUserId()
  fetchOrderData(activeTab.value)
})
</script>

<style scoped>
/* 基础样式 */
.page-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f9f9f9;
  /* 核心：占满屏幕高度，同时抵消padding导致的高度溢出 */
  min-height: 100vh; 
  box-sizing: border-box; /* 关键：让padding包含在100vh内，避免高度超出 */
}

.setting-header {
  display: flex;
  align-items: center;
}

.back-btn {
  font-size: 20px;
  color: #666;
  padding: 4px 8px;
  transition: all 0.2s;
}

.back-btn:hover {
  color: #2196f3;
  background-color: #f5f8ff;
  border-radius: 4px;
}

.setting-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 0 10px;
}

.order-page {
  padding: 15px;
  min-height: calc(100vh - 100px);
}

.order-tabs {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  overflow-x: auto;
  white-space: nowrap;
  scrollbar-width: none;
}

.order-tabs::-webkit-scrollbar {
  display: none;
}

.order-tab {
  padding: 8px 16px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.order-tab.active {
  color: #409eff;
  border-bottom-color: #409eff;
  font-weight: 500;
}

.order-tab:hover:not(.active) {
  color: #333;
}

.order-content {
  margin-top: 20px;
  width: 100%;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-item {
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

.order-item:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.no-order {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  text-align: center;
}

.no-order-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 20px;
  opacity: 0.6;
}

.no-order-text {
  font-size: 16px;
  color: #999;
}

/* 钱包样式 */
.wallet-content {
  padding: 30px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  font-size: 30px;
  color: #333;
  line-height: 2;
  height: 200px;
}

.wallet-balance-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f5f5f5;
  padding-bottom: 15px;
  margin-bottom: 15px;
}

.wallet-pending-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-label {
  font-size: 30px;
  color: #666;
}

.balance-value {
  font-size: 30px;
  font-weight: 600;
  color: #333;
}

/* 放大的充值按钮样式 */
.recharge-btn {
  padding: 15px 30px; /* 大幅增加内边距，放大按钮 */
  border-radius: 8px; /* 圆角适配大按钮 */
  font-size: 20px; /* 加大按钮字体 */
  background-color: #409eff;
  border-color: #409eff;
  transition: all 0.2s;
  min-width: 120px; /* 固定最小宽度，保证按钮大小 */
  height: 60px; /* 固定高度 */
  color: #f9f9f9;
}

.recharge-btn:hover {
  background-color: #3393f3;
  border-color: #3393f3;
}

.recharge-btn:disabled {
  background-color: #a0cfff;
  border-color: #a0cfff;
  cursor: not-allowed;
}

/* 充值弹窗样式 */
.recharge-input {
  height: 40px;
  border-radius: 6px;
}

.quick-recharge-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.quick-recharge-btn {
  width: 70px;
  height: 36px;
  line-height: 34px;
  text-align: center;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
  padding: 0;
  margin: 0;
}

.quick-recharge-btn:hover {
  border-color: #409eff;
  color: #409eff;
}

:deep(.quick-recharge-btn.active) {
  background-color: #409eff;
  color: #fff;
  border-color: #409eff;
}

:deep(.quick-recharge-btn.active:hover) {
  background-color: #3393f3;
  border-color: #3393f3;
}

/* 待发货订单样式 */
.order-header {
  display: flex;
  justify-content: space-between;
  padding: 12px 16px;
  font-size: 14px;
  border-bottom: 1px solid #f5f5f5;
}
.order-id {
  color: #666;
}
.order-status {
  color: #f56c6c;
  font-weight: 500;
}

.order-product-list {
  padding: 16px;
}
.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.product-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}
.product-info {
  flex: 1;
}
.product-title {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  margin-bottom: 4px;
}
.product-desc {
  font-size: 12px;
  color: #999;
}
.product-amount {
  text-align: right;
  min-width: 100px;
}
.price {
  font-size: 14px;
  color: #f56c6c;
  display: block;
}
.count {
  font-size: 12px;
  color: #999;
}

.order-footer {
  padding: 0 16px 12px;
  text-align: right;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.order-operate {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding: 12px 16px;
  border-top: 1px solid #f5f5f5;
}
.operate-btn {
  font-size: 14px;
  color: #666;
}
.order-operate .el-dropdown {
  display: inline-block;
}
.order-operate .el-dropdown .el-button {
  padding: 0;
  margin: 0;
}
/* 响应式适配 */
@media (max-width: 768px) {
  .order-tab {
    padding: 6px 12px;
    font-size: 13px;
  }

  .no-order-icon {
    width: 60px;
    height: 60px;
  }

  .no-order-text {
    font-size: 14px;
  }

  .wallet-content {
    padding: 20px;
    font-size: 20px;
    height: auto;
  }

  .recharge-btn {
    padding: 10px 20px;
    font-size: 16px;
    min-width: 100px;
    height: 50px;
  }

  .balance-label, .balance-value {
    font-size: 20px;
  }

  .quick-recharge-btn {
    width: 60px;
    height: 32px;
    line-height: 30px;
    font-size: 13px;
  }
}
</style>