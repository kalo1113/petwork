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
  :class="{active: activeTab === 'received'}"
  @click="switchTab('received')"
>已收货</div>
        <div 
          class="order-tab" 
          :class="{active: activeTab === 'comment'}"
          @click="switchTab('comment')"
        >待评价</div>
      </div>

      <!-- 根据选中的选项卡展示对应内容 -->
      <div class="order-content">
        <!-- 已生效订单（保险订单） -->
        <div v-if="activeTab === 'effective'">
          <div v-if="!effectiveOrders.length" class="no-order">
            <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
            <p class="no-order-text">暂无保险订单</p>
          </div>
          <div v-else class="order-list">
            <!-- 已生效保险订单列表渲染 -->
            <div class="order-item insurance-order-item" v-for="order in effectiveOrders" :key="order.id">
              <!-- 订单头：订单号 + 状态 -->
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status insurance-status" 
                      :class="getStatusClass(order.orderStatus)">
                  {{ getStatusText(order.orderStatus) }}
                </span>
              </div>

              <!-- 保险订单详情 -->
              <div class="insurance-order-detail">
                <div class="insurance-info-row">
                  <span class="info-label">保险产品：</span>
                  <span class="info-value">{{ order.insuranceName }}</span>
                </div>
                <div class="insurance-info-row">
                  <span class="info-label">宠物名称：</span>
                  <span class="info-value">{{ order.petName || '未知宠物' }}</span>
                </div>
                <div class="insurance-info-row">
                  <span class="info-label">缴费方式：</span>
                  <span class="info-value">{{ order.paymentMethod === 'lump' ? '全额缴费' : '分期缴费' }}</span>
                </div>
                <div class="insurance-info-row">
                  <span class="info-label">已缴保费：</span>
                  <span class="info-value">¥{{ order.totalAmount }}</span>
                </div>
                <!-- 新增：列表页显示剩余待缴金额 -->
                <div class="insurance-info-row" v-if="getRemainingAmount(order) > 0">
                  <span class="info-label">剩余待缴：</span>
                  <span class="info-value" style="color: #f56c6c;">¥{{ getRemainingAmount(order).toFixed(2) }}</span>
                </div>
                <div class="insurance-info-row">
                  <span class="info-label">保障周期：</span>
                  <span class="info-value">{{ order.guaranteeCycle }}个月</span>
                </div>
                <div class="insurance-info-row">
                  <span class="info-label">创建时间：</span>
                  <span class="info-value">{{ formatTime(order.createTime) }}</span>
                </div>
              </div>

              <!-- 操作按钮 -->
              <div class="order-operate">
                <el-dropdown @command="(cmd) => handleInsuranceOrderCommand(cmd, order)" placement="top">
                  <el-button type="text" class="operate-btn">
                    更多
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <!-- 修改2：仅对未取消的订单显示取消选项 -->
                      <el-dropdown-item command="cancel" v-if="order.orderStatus === 0 || order.orderStatus === 1">取消订单</el-dropdown-item>
                      <el-dropdown-item command="delete">删除订单</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>

                <el-button type="text" class="operate-btn" @click="viewInsuranceOrderDetail(order)">查看详情</el-button>
              </div>
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
            <div class="order-item" v-for="order in receiveOrders" :key="order.orderId">
              <!-- 订单头：订单号 + 状态 -->
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderId }}</span>
                <span class="order-status">待收货</span>
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
                <el-dropdown @command="(cmd) => handleReceiveOrderCommand(cmd, order)" placement="top">
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
                <el-button 
                  type="primary" 
                  class="operate-btn confirm-receive-btn"
                  @click="handleConfirmReceive(order)"
                >
                  确认收货
                </el-button>
              </div>
            </div>
          </div>
        </div>
<!-- 已收货订单 -->
<div v-if="activeTab === 'received'">
  <div v-if="!receivedOrders.length" class="no-order">
    <img src="@/assets/images/我的图标/查询.svg" alt="暂无订单" class="no-order-icon" />
    <p class="no-order-text">暂无已收货订单</p>
  </div>
  <div v-else class="order-list">
    <!-- 已收货订单列表 -->
    <div class="order-item" v-for="order in receivedOrders" :key="order.orderId">
      <!-- 订单头：订单号 + 状态 -->
      <div class="order-header">
        <span class="order-id">订单号：{{ order.orderId }}</span>
        <span class="order-status">已收货</span>
      </div>

      <!-- 商品列表 -->
      <div class="order-product-list">
        <div 
          class="product-item" 
          v-for="(item, idx) in order.itemList" 
          :key="idx"
        >
          <img 
            :src="getImgUrl(item.productImgPath)" 
            :alt="item.productTitle" 
            class="product-img"
            @error="(e) => handleImgError(e, item)"
          >
          <div class="product-info">
            <p class="product-title">{{ item.productTitle }}</p>
            <p class="product-desc">{{ item.productDescription }}</p>
          </div>
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
        <el-dropdown @command="(cmd) => handleReceivedOrderCommand(cmd, order)" placement="top">
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
      </div>
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

    <!-- 保险订单详情弹窗 -->
    <el-dialog
      v-model="insuranceDetailVisible"
      title="保险订单详情"
      width="650px"
      :close-on-click-modal="false"
    >
      <div v-if="currentInsuranceOrder" class="insurance-detail-content">
        <!-- 订单基础信息 -->
        <div class="detail-row">
          <span class="detail-label">订单编号：</span>
          <span class="detail-value">{{ currentInsuranceOrder.orderNo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">保险产品：</span>
          <span class="detail-value">{{ currentInsuranceOrder.insuranceName }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">宠物名称：</span>
          <span class="detail-value">{{ currentInsuranceOrder.petName || '未填写' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">缴费方式：</span>
          <span class="detail-value">{{ currentInsuranceOrder.paymentMethod === 'lump' ? '全额缴费' : '分期缴费' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">已缴保费：</span>
          <span class="detail-value">¥{{ (currentInsuranceOrder.totalAmount || 0).toFixed(2) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">剩余待缴费用：</span>
          <span class="detail-value">
            ¥{{ remainingAmount.toFixed(2) }}
            <el-button 
              v-if="remainingAmount > 0 && currentInsuranceOrder.orderStatus !== 2"
              size="small" 
              type="primary" 
              style="margin-left: 10px"
              @click="payRemainingAmount"
              :loading="isPaying"
            >
              一次性缴清
            </el-button>
          </span>
        </div>
        <div class="detail-row">
          <span class="detail-label">保障周期：</span>
          <span class="detail-value">{{ currentInsuranceOrder.guaranteeCycle }}个月</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">订单状态：</span>
          <span 
            class="order-status insurance-status" 
            :class="getStatusClass(currentInsuranceOrder.orderStatus)"
          >
            {{ getStatusText(currentInsuranceOrder.orderStatus) }}
          </span>
        </div>

        <!-- 分割线 -->
        <div style="height: 1px; background: #eee; margin: 15px 0;"></div>

        <!-- ====================== 核心：按状态显示 ====================== -->
        <!-- 已生效：显示保险规则 + 等待期 + 权益 -->
        <div v-if="currentInsuranceOrder.orderStatus === 1">
          <!-- 保险产品核心规则 -->
          <div class="detail-row">
            <span class="detail-label">优惠保费：</span>
            <span class="detail-value">¥{{ (currentInsuranceOrder.insuranceDetail?.discountPremium || 0).toFixed(2) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">总保额：</span>
            <span class="detail-value">
              ¥{{ (currentInsuranceOrder.insuranceDetail?.totalGuarantee || 0).toFixed(2) }}
              <span style="color: #f56c6c; margin-left: 8px;">
                （剩余 ¥{{ (currentInsuranceOrder.remainingCoverage || currentInsuranceOrder.remainingInsuranceAmount || 0).toFixed(2) }}）
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">免赔额：</span>
            <span class="detail-value">¥{{ (currentInsuranceOrder.insuranceDetail?.deductible || 0).toFixed(2) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">门诊单次赔付上限：</span>
            <span class="detail-value">¥{{ (currentInsuranceOrder.insuranceDetail?.outpatientLimit || 0).toFixed(2) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">手术单次赔付上限：</span>
            <span class="detail-value">¥{{ (currentInsuranceOrder.insuranceDetail?.surgeryLimit || 0).toFixed(2) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">定点医院赔付比例：</span>
            <span class="detail-value">{{ formatRatio(currentInsuranceOrder.insuranceDetail?.inNetworkRatio) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">非定点医院赔付比例：</span>
            <span class="detail-value">{{ formatRatio(currentInsuranceOrder.insuranceDetail?.outNetworkRatio) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">月消费补贴：</span>
            <span class="detail-value">
              ¥{{ (currentInsuranceOrder.insuranceDetail?.monthlySubsidy || 0).toFixed(2) }}
              <span style="color: #f56c6c; margin-left: 8px;">
                （剩余 ¥{{ (currentInsuranceOrder.remainingSubsidy || currentInsuranceOrder.monthlySubsidyBalance || 0).toFixed(2) }}）
              </span>
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">赠送服务：</span>
            <span class="detail-value">{{ currentInsuranceOrder.insuranceDetail?.giftService || '无' }}</span>
          </div>

          <!-- 分割线 -->
          <div style="height: 1px; background: #eee; margin: 15px 0;"></div>

          <!-- 等待期计算 -->
          <div class="detail-row" style="font-weight: 600; color: #333;">
            <span class="detail-label">等待期校验：</span>
            <span class="detail-value">（生效时间：{{ formatTime(currentInsuranceOrder.updateTime) }}）</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">意外等待期：</span>
            <span class="detail-value" :style="{color: calculateWaitingPeriod(currentInsuranceOrder).accident.includes('已满足') ? 'green' : 'red'}">
              {{ calculateWaitingPeriod(currentInsuranceOrder).accident }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">先天/遗传疾病等待期：</span>
            <span class="detail-value" :style="{color: calculateWaitingPeriod(currentInsuranceOrder).disease.includes('已满足') ? 'green' : 'red'}">
              {{ calculateWaitingPeriod(currentInsuranceOrder).disease }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">一般疾病等待期：</span>
            <span class="detail-value" :style="{color: calculateWaitingPeriod(currentInsuranceOrder).common.includes('已满足') ? 'green' : 'red'}">
              {{ calculateWaitingPeriod(currentInsuranceOrder).common }}
            </span>
          </div>
        </div>

        <!-- 被驳回：只显示驳回理由 -->
        <div v-else-if="currentInsuranceOrder.orderStatus === 2" class="reject-section">
          <div class="detail-row">
            <span class="detail-label" style="color:#f56c6c;font-weight:600;">驳回理由：</span>
          </div>
          <div class="detail-row">
            <span class="detail-value" 
                  style="padding:12px; background:#fff2f2; border-radius:6px; border:1px solid #ffd4d4;">
              {{ currentInsuranceOrder.auditRemark 
                || currentInsuranceOrder.rejectReason 
                || currentInsuranceOrder.remark 
                || '未填写驳回理由' }}
            </span>
          </div>
        </div>

        <!-- 其他状态：仅提示 -->
        <div v-else class="tip-section">
          <div class="detail-row">
            <span class="detail-value" style="color:#999;">
              当前状态暂不展示权益与等待期信息
            </span>
          </div>
        </div>

      </div>

      <!-- 空数据兜底 -->
      <div v-else class="empty-tip" style="text-align: center; padding: 20px; color: #999;">
        暂无订单详情
      </div>

      <template #footer>
        <el-button @click="insuranceDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElIcon, ElMessage, ElMessageBox,  ElButton } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
// 引入所有需要的接口（新增确认收货接口）
import { 
  rechargeWallet, getUserAmountInfo, getOrderList, deleteOrder, addToCart, checkProductExist,
  getInsuranceOrderListByUserId, updateInsuranceOrderStatus, deleteInsuranceOrder, getInsuranceOrderDetail,
  getPetInfoById, getUserInfo, payInsuranceOrderRemaining, getInsuranceDetail,
  getInsuranceBenefitListByPetId, confirmReceiveOrder // 新增导入确认收货接口
} from '@/api/user/index.js'

const router = useRouter()
const route = useRoute()

// 初始化选中的选项卡
const activeTab = ref(route.query.activeTab || 'effective')

// 订单数据
const effectiveOrders = ref([])
const deliverOrders = ref([])
const receiveOrders = ref([]) // 待收货订单数据
const commentOrders = ref([])
const allOrders = ref([])
const userName = ref('')
const currentUserName = ref('')
const allOrderList = ref([])
const receivedOrders = ref([]) // 已收货订单
// 钱包金额
const walletBalance = ref(0)
const pendingAmount = ref(0)

// 当前登录用户ID
const userId = ref('')
const initUserId = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const user = JSON.parse(userData)
    userId.value = user.userId || ''
    userName.value = user.userName || ''
  }
}

// 订单状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    0: '已支付', 
    1: '已生效',
    2: '被驳回'
  }
  return statusMap[status] || '未知状态'
}

// 订单状态样式类映射
const getStatusClass = (status) => {
  const classMap = {
    0: 'status-pending',
    1: 'status-success',
    2: 'status-danger'
  }
  return classMap[status] || 'status-default'
}

// 充值弹窗相关
const rechargeDialogVisible = ref(false)
const isRecharging = ref(false)
const rechargeFormRef = ref(null)
const rechargeForm = reactive({
  amount: ''
})

// 保险订单详情弹窗
const insuranceDetailVisible = ref(false)
const currentInsuranceOrder = ref(null)
const isPaying = ref(false)

// 确认收货加载状态
const isConfirmReceiving = ref(false)

// 充值校验规则
const rechargeRules = reactive({
  amount: [
    { required: true, message: '请输入充值金额', trigger: 'blur' },
    { type: 'number', message: '充值金额必须为数字', trigger: 'blur' },
    { min: 1, message: '充值金额最低1元', trigger: 'blur' }
  ]
})

// 计算剩余待缴费用
const remainingAmount = computed(() => {
  if (!currentInsuranceOrder.value) return 0
  const discountPremium = Number(currentInsuranceOrder.value.discountPremium || currentInsuranceOrder.value.discount_premium || 0)
  const totalAmount = Number(currentInsuranceOrder.value.totalAmount || 0)
  const remaining = discountPremium - totalAmount
  return remaining > 0 ? remaining : 0
})

// 列表页计算单个订单剩余金额
const getRemainingAmount = (order) => {
  if (!order) return 0
  const discountPremium = Number(order.discountPremium || order.discount_premium || 0)
  const totalAmount = Number(order.totalAmount || 0)
  const remaining = discountPremium - totalAmount
  return remaining > 0 ? remaining : 0
}

/** 切换选项卡 */
const switchTab = (tabKey) => {
  activeTab.value = tabKey
  fetchOrderData(tabKey)
}

// 图片路径拼接
const getImgUrl = (imgPath) => {
  if (!imgPath || imgPath.trim() === '') {
    return new URL('@/assets/images/我的图标/查询.svg', import.meta.url).href;
  }
  if (imgPath.startsWith('http://') || imgPath.startsWith('https://')) {
    return imgPath;
  }
  const baseUrl = 'http://localhost:8080';
  const fullPath = imgPath.startsWith('/') 
    ? imgPath 
    : `/product-img/${imgPath}`;
  return `${baseUrl}${fullPath}`;
};

// 图片错误处理
const handleImgError = (e, item) => {
  if (item.imgErrorHandled) return;
  item.imgErrorHandled = true;
  e.target.src = new URL('@/assets/images/我的图标/查询.svg', import.meta.url).href;
};

// 时间格式化
const formatTime = (timeStr) => {
  if (!timeStr) return '未知时间'
  try {
    const date = new Date(timeStr)
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
  } catch (e) {
    return timeStr
  }
}

// 等待期计算
const calculateWaitingPeriod = (order) => {
  if (!order?.createTime || !order?.insuranceDetail) return {
    accident: '无法计算',
    disease: '无法计算',
    common: '无法计算'
  };

  const purchaseTime = new Date(order.createTime);
  const now = new Date();
  const daysDiff = Math.floor((now - purchaseTime) / (1000 * 60 * 60 * 24));

  const accidentWait = order.insuranceDetail.waitingPeriodAccident || 0;
  const diseaseWait = order.insuranceDetail.waitingPeriodDisease || 0;
  const commonWait = order.insuranceDetail.waitingPeriodCommon || 0;

  return {
    accident: daysDiff >= accidentWait ? `已满足（需${accidentWait}天，已过${daysDiff}天）` : `未满足（需${accidentWait}天，仅过${daysDiff}天）`,
    disease: daysDiff >= diseaseWait ? `已满足（需${diseaseWait}天，已过${daysDiff}天）` : `未满足（需${diseaseWait}天，仅过${daysDiff}天）`,
    common: daysDiff >= commonWait ? `已满足（需${commonWait}天，已过${daysDiff}天）` : `未满足（需${commonWait}天，仅过${daysDiff}天）`
  };
};

// 格式化赔付比例
const formatRatio = (ratio) => {
  return ratio ? `${ratio}%` : '0%';
};

// 批量查询宠物名称
const getPetNamesByIds = async (petIds) => {
  const uniquePetIds = Array.from(new Set(petIds)).filter(id => id && id !== '')
  if (uniquePetIds.length === 0) return {}
  const petNameMap = {}
  try {
    for (const petId of uniquePetIds) {
      try {
        const res = await getPetInfoById(petId)
        if (res && (res.code === 200 || res.id)) {
          const petInfo = res.data || res
          petNameMap[petId] = petInfo.petName || `宠物${petId}`
        } else {
          petNameMap[petId] = `宠物${petId}`
        }
      } catch (err) {
        console.error(`查询宠物${petId}信息失败：`, err)
        petNameMap[petId] = `宠物${petId}`
      }
    }
  } catch (err) {
    uniquePetIds.forEach(petId => {
      petNameMap[petId] = `宠物${petId}`
    })
  }
  return petNameMap
}

// 查询用户名
const getUserNameById = async (userId) => {
  if (!userId) return '未知用户'
  try {
    const res = await getUserInfo(userId)
    if (res && res.code === 200 && res.data) {
      return res.data.userName || `用户${userId}`
    }
    return `用户${userId}`
  } catch (err) {
    console.error(`查询用户${userId}名称失败：`, err)
    return `用户${userId}`
  }
}

// 获取权益表剩余金额
const getBenefitRemainingAmount = async (orderId, petId) => {
  const result = {
    remainingSubsidy: 0,
    remainingCoverage: 0
  }
  if (!orderId || !petId) {
    return result
  }
  try {
    const benefitRes = await getInsuranceBenefitListByPetId(petId)
    if (!benefitRes || benefitRes.code !== 200 || !Array.isArray(benefitRes.data)) {
      return result
    }
    const targetBenefit = benefitRes.data.find(item => 
      item.insuranceOrderId === orderId || 
      item.insurance_order_id === orderId ||
      item.orderId === orderId ||
      item.order_id === orderId
    )
    if (!targetBenefit) return result
    result.remainingSubsidy = Number(targetBenefit.monthlySubsidyBalance || targetBenefit.remainingSubsidy || targetBenefit.subsidy_balance || 0)
    result.remainingCoverage = Number(targetBenefit.remainingInsuranceAmount || targetBenefit.remainingCoverage || targetBenefit.insurance_balance || 0)
  } catch (err) {
    console.error('查询权益表失败：', err)
  }
  return result
}

/** 请求数据 */
const fetchOrderData = async (tabKey) => {
  if (!userId.value) {
    handleNoLogin()
    return
  }
  try {
    switch (tabKey) {
      case 'effective': {
        const res = await getInsuranceOrderListByUserId(userId.value)
        if (res && (res.code === 200 || Array.isArray(res))) {
          const insuranceOrders = Array.isArray(res) ? res : (res.data || [])
          const filteredOrders = insuranceOrders.filter(order => 
            order.orderStatus === 0 || order.orderStatus === 1 || order.orderStatus === 2
          )
          const petIds = filteredOrders.map(order => order.petId).filter(id => id)
          const petNameMap = await getPetNamesByIds(petIds)
          const orderListWithBenefit = []
          for (const order of filteredOrders) {
            const benefitAmount = await getBenefitRemainingAmount(order.id, order.petId)
            orderListWithBenefit.push({
              ...order,
              petName: petNameMap[order.petId] || `宠物${order.petId}`,
              remainingSubsidy: benefitAmount.remainingSubsidy,
              remainingCoverage: benefitAmount.remainingCoverage
            })
          }
          effectiveOrders.value = orderListWithBenefit
        } else {
          ElMessage.error(res?.msg || '获取保险订单失败')
          effectiveOrders.value = []
        }
        break
      }
      case 'wallet': {
        const amountRes = await getUserAmountInfo(userId.value)
        if (amountRes.code === 200 && amountRes.data) {
          walletBalance.value = amountRes.data.accountBalance || 0
          pendingAmount.value = amountRes.data.pendingAmount || 0
        } else if (amountRes.code === 404) {
          ElMessage.warning(amountRes.msg || '用户信息不存在，请重新登录')
          localStorage.removeItem('userId')
          userId.value = null
        } else {
          ElMessage.error(amountRes.msg || '查询钱包信息失败')
        }
        break
      }
      case 'deliver': {
        const res = await getOrderList(userId.value)
        if (res.code === 200) {
          allOrderList.value = res.data || []
          deliverOrders.value = allOrderList.value.filter(order => order.orderStatus === 1)
        } else {
          ElMessage.error(res.msg || '获取订单列表失败')
          deliverOrders.value = []
        }
        break
      }
      case 'receive': { // 新增待收货订单请求逻辑
        const res = await getOrderList(userId.value)
        if (res.code === 200) {
          allOrderList.value = res.data || []
          // 筛选出状态为2的订单（待收货）
          receiveOrders.value = allOrderList.value.filter(order => order.orderStatus === 2)
        } else {
          ElMessage.error(res.msg || '获取待收货订单失败')
          receiveOrders.value = []
        }
        break
      }
      case 'received': {
  const res = await getOrderList(userId.value)
  if (res.code === 200) {
    allOrderList.value = res.data || []
    receivedOrders.value = allOrderList.value.filter(order => order.orderStatus === 3)
  } else {
    ElMessage.error(res.msg || '获取已收货订单失败')
    receivedOrders.value = []
  }
  break
}
      case 'comment':
        break
      case 'all':
        break
      default:
        break
    }
  } catch (err) {
    console.error(`获取${tabKey}数据失败：`, err)
    ElMessage.error('网络异常，获取数据失败，请重试')
  }
}

// 确认收货处理函数
const handleConfirmReceive = async (order) => {
  if (!order || !order.orderId) {
    ElMessage.error('订单信息异常，无法确认收货')
    return
  }
  if (!userId.value) {
    handleNoLogin()
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确认已收到商品吗？确认后订单状态将变为已完成',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    isConfirmReceiving.value = true
    // 调用确认收货接口
    const res = await confirmReceiveOrder(order.orderId, userId.value)
    if (res && (res.code === 200 || res.success)) {
      ElMessage.success('确认收货成功！')
      // 重新请求待收货订单数据
      await fetchOrderData('receive')
      // 同时更新待评价订单数据（可选）
      // await fetchOrderData('comment')
    } else {
      ElMessage.error(res?.msg || '确认收货失败，请稍后重试')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('确认收货失败：', err)
      ElMessage.error('确认收货失败：' + (err.msg || err.message || '未知错误'))
    }
  } finally {
    isConfirmReceiving.value = false
  }
}

// 待收货订单操作处理（删除等）
const handleReceiveOrderCommand = async (command, order) => {
  if (!order || !order.orderId) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }
  
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm(
        '此操作将永久删除该待收货订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' 
        }
      )
      const res = await deleteOrder(order.orderId)
      if (res.code === 200) {
        ElMessage.success('订单删除成功！')
        await fetchOrderData('receive')
      } else {
        ElMessage.error(res.msg || '订单删除失败')
      }
    } catch (err) {
      if (err !== 'cancel') {
        ElMessage.error('删除订单时发生异常：' + err.message)
      } else {
        ElMessage.info('已取消删除')
      }
    }
  }
}

// 一次性缴清剩余费用
const payRemainingAmount = async () => {
  if (!currentInsuranceOrder.value) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }
  if (currentInsuranceOrder.value.orderStatus === 2) {
    ElMessage.warning('已取消的订单无法缴费')
    return
  }
  if (remainingAmount.value <= 0) {
    ElMessage.info('暂无剩余待缴费用')
    return
  }
  if (!userId.value) {
    handleNoLogin()
    return
  }
  try {
    await ElMessageBox.confirm(
      `确认一次性缴清剩余费用¥${remainingAmount.value.toFixed(2)}吗？`,
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    isPaying.value = true
    const res = await payInsuranceOrderRemaining(currentInsuranceOrder.value.id, userId.value)
    if (res && (res.code === 200 || res.success)) {
      ElMessage.success(res.msg || '剩余费用缴清成功！')
      await viewInsuranceOrderDetail(currentInsuranceOrder.value)
      await fetchOrderData('effective')
      await fetchOrderData('wallet')
    } else {
      ElMessage.error(res?.msg || '缴费失败，请稍后重试')
    }
  } catch (err) {
    if (err !== 'cancel') {
      console.error('缴清失败：', err)
      ElMessage.error('缴费失败：' + (err.msg || err.message || '未知错误'))
    }
  } finally {
    isPaying.value = false
  }
}

// 保险订单操作
const handleInsuranceOrderCommand = async (command, order) => {
  if (!order || !order.id) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }
  try {
    if (command === 'cancel') {
      await ElMessageBox.confirm(
        '此操作将取消该保险订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' 
        }
      )
      const res = await updateInsuranceOrderStatus(order.id, 2)
      if (res && (res.code === 200 || res.success)) {
        ElMessage.success('保险订单已取消！')
        await fetchOrderData('effective')
      } else {
        ElMessage.error(res?.msg || '取消订单失败')
      }
    } else if (command === 'delete') {
      await ElMessageBox.confirm(
        '此操作将永久删除该保险订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' 
        }
      )
      const res = await deleteInsuranceOrder(order.id)
      if (res && (res.code === 200 || res.success)) {
        ElMessage.success('保险订单已删除！')
        await fetchOrderData('effective')
      } else {
        ElMessage.error(res?.msg || '删除订单失败')
      }
    }
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('操作失败：' + err.message)
    } else {
      ElMessage.info('已取消操作')
    }
  }
}

// 查看保险订单详情
const viewInsuranceOrderDetail = async (order) => {
  try {
    const res = await getInsuranceOrderDetail(order.id)
    if (res && (res.code === 200 || res.id)) {
      let orderDetail = Array.isArray(res) ? res[0] : (res.data || res)
      const insuranceId = orderDetail.insuranceId || orderDetail.insurance_id
      if (insuranceId) {
        try {
          const insuranceRes = await getInsuranceDetail(insuranceId)
          orderDetail.insuranceDetail = insuranceRes?.data?.insurance || insuranceRes?.data || {}
        } catch (e) {
          console.error(`查询保险${insuranceId}详情失败：`, e)
          orderDetail.insuranceDetail = {}
        }
      } else {
        orderDetail.insuranceDetail = {}
      }
      const benefitAmount = await getBenefitRemainingAmount(orderDetail.id, orderDetail.petId)
      orderDetail.remainingSubsidy = benefitAmount.remainingSubsidy
      orderDetail.remainingCoverage = benefitAmount.remainingCoverage
      if (orderDetail.petId) {
        const petNameMap = await getPetNamesByIds([orderDetail.petId])
        orderDetail.petName = petNameMap[orderDetail.petId] || `宠物${orderDetail.petId}`
      } else {
        orderDetail.petName = '未知宠物'
      }
      currentUserName.value = await getUserNameById(orderDetail.userId)
      currentInsuranceOrder.value = orderDetail
      insuranceDetailVisible.value = true
    } else {
      ElMessage.error('获取订单详情失败')
    }
  } catch (err) {
    console.error('获取订单详情失败：', err)
    ElMessage.error('获取订单详情失败，请重试')
  }
}

// 删除订单
const handleMoreCommand = async (command, order) => {
  if (!order || !order.orderId) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm(
        '此操作将永久删除该订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' }
      )
      console.log('删除订单ID：', order.orderId)
      const res = await deleteOrder(order.orderId)
      if (res.code === 200) {
        ElMessage.success('订单删除成功！')
        await fetchOrderData('deliver')
      } else {
        ElMessage.error(res.msg || '订单删除失败')
      }
    } catch (err) {
      if (err !== 'cancel') {
        ElMessage.error('删除订单时发生异常：' + err.message)
      } else {
        ElMessage.info('已取消删除')
      }
    }
  }
}

// 加入购物车
const handleAddToCart = async (order) => {
  if (!userId.value) {
    handleNoLogin()
    return
  }
  if (!order || !order.itemList || order.itemList.length === 0) {
    ElMessage.error('订单无商品，无法加入购物车')
    return
  }
  let successCount = 0
  let failCount = 0
  const failProducts = []
  const notExistProducts = []
  try {
    for (const item of order.itemList) {
      try {
        if (!item.productId) {
          failCount++
          failProducts.push(`${item.productTitle || '未知商品'}（无商品ID）`)
          continue
        }
        let isProductExist = false
        try {
          const existRes = await checkProductExist(item.productId)
          isProductExist = existRes === true || existRes === "exist"
        } catch (productErr) {
          failCount++
          failProducts.push(`${item.productTitle}（商品信息查询失败）`)
          continue
        }
        if (!isProductExist) {
          failCount++
          notExistProducts.push(item.productTitle)
          failProducts.push(`${item.productTitle}（商品不存在/已下架）`)
          continue
        }
        const res = await addToCart(userId.value, item.productId, item.productCount)
        if (res === "添加购物车成功") {
          successCount++
        } else {
          failCount++
          failProducts.push(`${item.productTitle}（${res || '加购失败'}）`)
        }
      } catch (err) {
        failCount++
        failProducts.push(`${item.productTitle || '未知商品'}（加购接口异常）`)
      }
    }
    if (successCount > 0 && failCount === 0) {
      ElMessage.success(`全部${successCount}个商品加入购物车成功！`)
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
// 已收货订单操作处理
const handleReceivedOrderCommand = async (command, order) => {
  if (!order || !order.orderId) {
    ElMessage.error('订单信息异常，无法操作')
    return
  }
  
  if (command === 'delete') {
    try {
      await ElMessageBox.confirm(
        '此操作将永久删除该已收货订单，是否继续？',
        '提示',
        { 
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning' 
        }
      )
      const res = await deleteOrder(order.orderId)
      if (res.code === 200) {
        ElMessage.success('订单删除成功！')
        await fetchOrderData('received')
      } else {
        ElMessage.error(res.msg || '订单删除失败')
      }
    } catch (err) {
      if (err !== 'cancel') {
        ElMessage.error('删除订单时发生异常：' + err.message)
      } else {
        ElMessage.info('已取消删除')
      }
    }
  }
}
/** 返回上一页 */
const handleBack = () => {
  try {
    router.go(-1)
    setTimeout(() => {
      const currentPath = router.currentRoute.fullPath
      if (currentPath === '/my-orders') {
        router.push('/my').catch(() => {
          ElMessage.error('返回失败，请重试')
        })
      }
    }, 100)
  } catch (_) {
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
    router.push({
      path: '/my',
      query: { redirect: router.currentRoute.fullPath }
    })
  }).catch(() => {
    ElMessage.info('已取消登录')
  })
}

/** 点击充值按钮 */
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

/** 处理充值 */
const handleRecharge = async () => {
  if (!userId.value) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const amount = Number(rechargeForm.amount)
    if (isNaN(amount) || amount < 1) {
      ElMessage.error('充值金额必须是大于等于1的数字')
      return
    }
    if (rechargeFormRef.value) {
      rechargeFormRef.value.clearValidate()
    }
    isRecharging.value = true
    const res = await rechargeWallet(userId.value, amount)
    if (res && res.code === 200) {
      ElMessage.success(`充值成功！¥${amount}`)
      await fetchOrderData('wallet')
      handleDialogClose()
    } else {
      ElMessage.error(res?.msg || '充值失败')
    }
  } catch (err) {
    console.error('充值失败：', err)
    ElMessage.error('充值失败：' + (err?.response?.data?.msg || err.message))
  } finally {
    isRecharging.value = false
  }
}

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
  min-height: 100vh; 
  box-sizing: border-box;
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
  padding: 15px 30px;
  border-radius: 8px;
  font-size: 20px;
  background-color: #409eff;
  border-color: #409eff;
  transition: all 0.2s;
  min-width: 120px;
  height: 60px;
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

/* 待发货/待收货订单样式 */
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

/* 确认收货按钮样式 */
.confirm-receive-btn {
  color: #fff !important;
  background-color: #409eff;
  border-color: #409eff;
  padding: 6px 12px;
  border-radius: 4px;
}
.confirm-receive-btn:hover {
  background-color: #3393f3;
  border-color: #3393f3;
}

/* 保险订单样式 */
.insurance-order-item {
  padding: 0;
}

.insurance-status {
  color: inherit !important;
}

.insurance-order-detail {
  padding: 16px;
}

.insurance-info-row {
  display: flex;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-label {
  width: 80px;
  color: #666;
  flex-shrink: 0;
}

.info-value {
  color: #333;
  flex: 1;
}

/* 保险订单详情弹窗样式 */
.insurance-detail-content {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
  font-size: 14px;
}

.detail-label {
  width: 140px;
  color: #666;
  flex-shrink: 0;
}

.detail-value {
  color: #333;
  flex: 1;
}

/* 驳回理由样式 */
.reject-section {
  padding: 10px 0 20px;
}

.tip-section {
  padding: 20px 0;
  text-align: center;
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

  .insurance-info-row {
    flex-direction: column;
  }

  .info-label {
    width: 100%;
    margin-bottom: 4px;
  }

  .detail-row {
    flex-direction: column;
  }

  .detail-label {
    width: 100%;
    margin-bottom: 4px;
  }
}

/* 订单状态样式（提升优先级） */
:deep(.order-status.insurance-status) {
  padding: 2px 8px !important;
  border-radius: 4px !important;
  font-size: 12px !important;
  font-weight: 500 !important;
  display: inline-block !important;
}

/* 已支付（待审核）- 橙色 */
:deep(.order-status.insurance-status.status-pending) {
  background-color: #fff7e6 !important;
  color: #ff9a3c !important;
  border: 1px solid #ffd591 !important;
}

/* 已生效（审核通过）- 绿色 */
:deep(.order-status.insurance-status.status-success) {
  background-color: #f0f9ff !important;
  color: #409eff !important;
  border: 1px solid #b3d8ff !important;
}

/* 被驳回 - 红色 */
:deep(.order-status.insurance-status.status-danger) {
  background-color: #fff2f2 !important;
  color: #f56c6c !important;
  border: 1px solid #feb8b8 !important;
}

/* 未知状态 - 灰色 */
:deep(.order-status.insurance-status.status-default) {
  background-color: #f5f5f5 !important;
  color: #909399 !important;
  border: 1px solid #e4e7ed !important;
}

/* 移除原有冲突样式 */
.insurance-status.cancelled {
  color: inherit !important;
}
</style>