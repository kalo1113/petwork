<template>
  <div class="insurance-order-manage">
    <el-card title="保险订单管理" class="manage-card">
      <!-- 搜索栏 -->
      <div class="search-bar" style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center">
        <el-input
          v-model="searchParams.insuranceName"
          placeholder="请输入保险名称"
          style="width: 200px"
          clearable
        />
        <el-input
          v-model="searchParams.userName"
          placeholder="请输入用户id"
          style="width: 200px"
          clearable
        />
        <el-button type="primary" @click="getOrderData()">查询</el-button>
        <el-button @click="resetSearch()">重置</el-button>
      </div>

      <!-- 订单列表：移除操作列，保留行点击查看详情 -->
      <el-table
        :data="orderList"
        border
        style="margin-bottom: 10px"
        v-loading="loading"
        empty-text="暂无保险订单数据"
        :key="pagination.pageNum + '-' + pagination.pageSize"
        @row-click="handleRowClick" 
        row-class-name="cursor-pointer" 
      >
        <el-table-column prop="id" label="订单ID" width="80" />
        <el-table-column prop="userName" label="用户id" min-width="120" />
        <el-table-column prop="insuranceName" label="保险名称" min-width="180" />
        <el-table-column prop="orderNo" label="订单编号" min-width="180" />
        <el-table-column
          prop="createTime"
          label="下单时间"
          width="180"
          :formatter="formatTime"
        />
        <!-- 订单总金额列（下单时间和订单状态中间） -->
        <el-table-column
          prop="totalAmount"
          label="订单总金额"
          width="120"
        >
          <template #default="scope">
            {{ scope.row.totalAmount || 0 }} 元
          </template>
        </el-table-column>
        <el-table-column prop="orderStatus" label="订单状态" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.orderStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-if="scope.row.orderStatus === 1" type="success">已生效</el-tag>
            <el-tag v-if="scope.row.orderStatus === 2" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <!-- 移除了操作列 -->
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="text-align: right"
      />
    </el-card>

    <!-- 订单详情弹窗（整合通过/驳回按钮） -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="800px"
      top="20px"
      @close="resetOrderDetail"
    >
      <div v-loading="detailLoading" class="detail-content">
        <!-- 订单基础信息 -->
        <el-card title="订单基础信息" style="margin-bottom: 15px">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单ID">{{ orderDetail?.order?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="订单编号">{{ orderDetail?.order?.orderNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ orderDetail?.order?.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物ID">{{ orderDetail?.order?.petId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保险ID">{{ orderDetail?.order?.insuranceId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保险名称">{{ orderDetail?.order?.insuranceName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="缴费方式">
              {{ orderDetail?.order?.paymentMethod === 'lump' ? '全额支付' : '按月分期' || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="折扣后保费">{{ orderDetail?.order?.discountPremium || 0 }} 元</el-descriptions-item>
            <el-descriptions-item label="保障周期">{{ orderDetail?.order?.guaranteeCycle || 0 }} 个月</el-descriptions-item>
            <el-descriptions-item label="月均保费">{{ orderDetail?.order?.monthlyPrice || 0 }} 元</el-descriptions-item>
            <el-descriptions-item label="订单总金额">{{ orderDetail?.order?.totalAmount || 0 }} 元</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag v-if="orderDetail?.order?.orderStatus === 0" type="warning">待审核</el-tag>
              <el-tag v-if="orderDetail?.order?.orderStatus === 1" type="success">已生效</el-tag>
              <el-tag v-if="orderDetail?.order?.orderStatus === 2" type="danger">已取消</el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="订单备注">{{ orderDetail?.order?.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">
              {{ formatTimeForDetail(orderDetail?.order?.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatTimeForDetail(orderDetail?.order?.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 宠物信息 -->
        <el-card title="宠物信息" style="margin-bottom: 15px">
          <el-descriptions :column="2" border v-if="orderDetail?.petInfo">
            <el-descriptions-item label="宠物ID">{{ orderDetail?.petInfo?.petId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属用户ID">{{ orderDetail?.petInfo?.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物名称">{{ orderDetail?.petInfo?.petName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="出生日期">{{ formatTimeForDetail(orderDetail?.petInfo?.petBirthday) }}</el-descriptions-item>
            <el-descriptions-item label="宠物类型">{{ orderDetail?.petInfo?.petType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物性别">{{ orderDetail?.petInfo?.petGender || '-' }}</el-descriptions-item>
            <el-descriptions-item label="绝育状态">{{ orderDetail?.petInfo?.isSterilized || '-' }}</el-descriptions-item>
            <el-descriptions-item label="面部照片">
              <el-image
                v-if="orderDetail?.petInfo?.petFacePhoto"
                :src="orderDetail.petInfo.petFacePhoto"
                style="width: 100px; height: 100px"
                fit="cover"
                preview-src-list="[orderDetail.petInfo.petFacePhoto]"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="全身照片">
              <el-image
                v-if="orderDetail?.petInfo?.petBodyPhoto"
                :src="orderDetail.petInfo.petBodyPhoto"
                style="width: 100px; height: 100px"
                fit="cover"
                preview-src-list="[orderDetail.petInfo.petBodyPhoto]"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">
              {{ formatTimeForDetail(orderDetail?.petInfo?.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              {{ formatTimeForDetail(orderDetail?.petInfo?.updateTime) }}
            </el-descriptions-item>
          </el-descriptions>
          <div v-else class="empty-tip">暂无宠物信息</div>
        </el-card>

        <!-- 驳回原因表单（仅待审核且选择驳回时显示） -->
        <el-card 
          title="审核操作" 
          style="margin-bottom: 15px"
          v-if="orderDetail?.order?.orderStatus === 0"
        >
          <el-form 
            v-if="auditType === 'reject'" 
            :model="rejectForm" 
            :rules="rejectRules" 
            ref="rejectFormRef" 
            label-width="80px"
            style="margin-top: 10px"
          >
            <el-form-item label="驳回原因" prop="reason">
              <el-input
                v-model="rejectForm.reason"
                type="textarea"
                :rows="4"
                placeholder="请输入驳回原因（必填）"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 弹窗底部按钮：整合通过/驳回 -->
      <template #footer>
        <!-- 仅待审核订单显示通过/驳回按钮 -->
        <div v-if="orderDetail?.order?.orderStatus === 0">
          <el-button @click="switchAuditType('pass')" v-if="auditType === 'reject'">返回</el-button>
          <el-button type="primary" @click="handleAudit('pass')" v-if="auditType === 'pass'">通过</el-button>
          <el-button type="danger" @click="switchAuditType('reject')" v-if="auditType === 'pass'">驳回</el-button>
          <el-button type="danger" @click="handleAudit('reject')" v-if="auditType === 'reject'">确认驳回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
// 引入详情接口
import { getAuditList, auditOrder, getOrderDetailById } from '@/api/merchant/insurance'
import dayjs from 'dayjs'

// ========== 核心变量 ==========
const loading = ref(false)
const orderList = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const searchParams = reactive({
  insuranceName: '',
  userName: ''
})

// ========== 详情弹窗相关变量 ==========
const detailDialogVisible = ref(false) 
const detailLoading = ref(false) 
const orderDetail = reactive({
  order: {},
  petInfo: {}
})

// ========== 审核相关变量（整合到详情弹窗） ==========
const auditType = ref('pass')         // 审核类型：pass-通过，reject-驳回
const rejectFormRef = ref(null)
const rejectForm = reactive({
  reason: '',
  orderId: ''
})
// 修复1：rejectRules 改为普通常量（非ref），符合Element Plus规范
const rejectRules = {
  reason: [{ required: true, message: '请输入驳回原因', trigger: ['blur', 'change'] }]
}

// ========== 工具函数 ==========
// 表格列时间格式化（保留原有能正常显示的写法）
const formatTime = (row) => {
  return row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 详情弹窗时间格式化（兼容ISO格式）
const formatTimeForDetail = (time) => {
  if (!time) return '-'
  // 兼容带T的ISO格式时间
  const timeStr = time.toString().replace('T', ' ')
  return dayjs(timeStr).isValid() ? dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// ========== 审核相关方法 ==========
// 切换审核类型（通过/驳回）
const switchAuditType = (type) => {
  auditType.value = type
  // 切换到驳回时重置表单校验状态
  if (type === 'reject') {
    rejectForm.orderId = orderDetail.order.id || ''
    if (rejectFormRef.value) {
      rejectFormRef.value.clearValidate()
    }
  }
}

// 处理审核操作
const handleAudit = async (type) => {
  try {
    const orderId = orderDetail.order.id
    if (!orderId) {
      ElMessage.error('订单ID不存在，无法操作')
      return
    }

    // 组装基础参数（通过/驳回通用）
    const auditParams = {
      orderId: orderId, // 后端要求的参数名是orderId，不是id
      status: type === 'pass' ? 1 : 2, // 通过=1，驳回=2（关键！）
      auditRemark: '' // 初始化备注
    }

    // 驳回专属逻辑：校验原因 + 赋值备注
    if (type === 'reject') {
      // 修复2：优化表单校验逻辑，添加catch捕获校验失败
      try {
        // 触发表单校验（Element Plus的validate返回Promise，失败会reject）
        await rejectFormRef.value.validate()
      } catch (error) {
        // 校验失败时直接返回，Element Plus会自动显示校验提示
        return
      }
      // 把驳回原因赋值给后端要求的auditRemark
      auditParams.auditRemark = rejectForm.reason.trim()
      // 额外校验：驳回原因不能为空（双重保障）
      if (!auditParams.auditRemark) {
        ElMessage.error('驳回原因不能为空，请填写')
        return
      }
    }

    const confirmText = type === 'pass' ? '通过' : '驳回'
    const confirm = await ElMessageBox.confirm(
      `确定要${confirmText}【${orderDetail.order.orderNo}】的订单审核吗？`,
      `确认${confirmText}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: type === 'pass' ? 'warning' : 'danger'
      }
    )

    if (confirm) {
      // 统一调用审核接口（通过/驳回都走这一个）
      const res = await auditOrder(auditParams)
      if (res.code === 200) {
        ElMessage.success(`订单${confirmText}成功！`)
        detailDialogVisible.value = false
        getOrderData() // 刷新列表
      } else {
        // 展示后端返回的具体错误（比如“订单状态异常”）
        ElMessage.error(`订单${confirmText}失败：${res.msg || '服务器异常'}`)
      }
    }
  } catch (err) {
    // 排除用户取消操作的情况，只提示真实错误
    if (err !== 'cancel') {
      console.error(`${type}失败详情：`, err)
      // 优先展示后端返回的错误信息
      const errMsg = err.response?.data?.msg || err.message || '服务器异常'
      ElMessage.error(`审核${type === 'pass' ? '通过' : '驳回'}失败：${errMsg}`)
    }
  }
}

// ========== 核心方法 ==========
const getOrderData = async () => {
  try {
    loading.value = true
    console.log('请求参数：', { ...pagination, ...searchParams })
    
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...searchParams
    }
    const res = await getAuditList(params)
    
    console.log('后端返回完整数据：', res)
    
    if (res.code !== 200) {
      ElMessage.error('获取订单失败：' + res.msg)
      return
    }
    
    const innerData = res.data.data || {}
    let list = innerData.records || []
    console.log('后端返回records：', list)
    
    if (list.length === 0) {
      ElMessage.info('暂无订单数据')
    }
    
    list = list.map(item => ({
      ...item,
      userName: `${item.userId}`
    }))
    
    orderList.value = list
    pagination.total = innerData.total || 0
    console.log('最终渲染列表：', orderList.value)

  } catch (err) {
    console.error('获取订单失败：', err)
    ElMessage.error('获取订单列表失败：' + (err.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

// ========== 行点击事件 - 获取订单详情 ==========
const handleRowClick = async (row) => {
  try {
    detailLoading.value = true
    auditType.value = 'pass' // 重置审核类型为通过
    rejectForm.reason = ''   // 清空驳回原因
    
    console.log('点击的订单行：', row)
    console.log('订单ID：', row.id)
    
    const res = await getOrderDetailById(row.id)
    console.log('接口返回完整res：', res)
    
    const innerData = res.data.data || {}
    console.log('innerData：', innerData)
    
    if (res.code === 200) {
      orderDetail.order = { ...innerData.order } || {}
      orderDetail.petInfo = { ...innerData.petInfo } || {}
      
      console.log('赋值后 orderDetail：', orderDetail)
      detailDialogVisible.value = true
    } else {
      ElMessage.error('查询订单详情失败：' + res.msg)
    }
  } catch (err) {
    console.error('查询详情失败：', err)
    ElMessage.error('查询订单详情失败：' + (err.message || '网络异常'))
  } finally {
    detailLoading.value = false
  }
}

// ========== 重置详情数据 ==========
const resetOrderDetail = () => {
  orderDetail.order = {}
  orderDetail.petInfo = {}
  auditType.value = 'pass'
  rejectForm.reason = ''
  rejectForm.orderId = ''
  if (rejectFormRef.value) {
    rejectFormRef.value.resetFields()
  }
}

// 分页相关方法
const handleSizeChange = (val) => {
  pagination.pageSize = val
  getOrderData()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getOrderData()
}

const resetSearch = () => {
  searchParams.insuranceName = ''
  searchParams.userName = ''
  pagination.pageNum = 1
  getOrderData()
}

// 页面加载
onMounted(() => {
  getOrderData()
})
</script>

<style scoped>
.manage-card {
  max-width: 1400px;
  margin: 0 auto;
  padding: 15px;
}
.search-bar {
  padding: 0 5px;
}
/* 行点击样式 */
.cursor-pointer {
  cursor: pointer;
}
/* 详情弹窗样式 */
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}
.empty-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}
</style>