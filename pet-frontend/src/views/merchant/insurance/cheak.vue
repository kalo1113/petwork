<template>
  <div class="insurance-order-manage">
    <el-card title="理赔订单审核" class="manage-card">
      <!-- 搜索栏 -->
      <div class="search-bar" style="margin-bottom: 15px; display: flex; gap: 10px; align-items: center">
        <el-input
          v-model="searchParams.claimNo"
          placeholder="请输入理赔单号"
          style="width: 200px"
          clearable
        />
        <el-input
          v-model="searchParams.userId"
          placeholder="请输入用户ID"
          style="width: 200px"
          clearable
        />
        <el-select
          v-model="searchParams.claimStatus"
          placeholder="请选择理赔状态"
          style="width: 180px"
          clearable
        >
          <el-option label="待审核" value="0" />
          <el-option label="审核通过" value="2" />
          <el-option label="审核驳回" value="3" />
          <el-option label="已打款" value="4" />
        </el-select>
        <el-button type="primary" @click="getClaimData()">查询</el-button>
        <el-button @click="resetSearch()">重置</el-button>
      </div>

      <!-- 理赔订单列表 -->
      <el-table
        :data="claimList"
        border
        style="margin-bottom: 10px"
        v-loading="loading"
        empty-text="暂无理赔订单数据"
        :key="pagination.pageNum + '-' + pagination.pageSize"
        @row-click="handleRowClick" 
        row-class-name="cursor-pointer" 
      >
        <el-table-column prop="id" label="理赔ID" width="80" />
        <el-table-column prop="claimNo" label="理赔单号" min-width="180" />
        <el-table-column prop="userId" label="用户ID" min-width="120" />
        <el-table-column 
          label="宠物昵称" 
          min-width="100"
          :formatter="(row) => row.petNickname || '-'"
        />
        <el-table-column prop="insuranceOrderId" label="保险订单ID" min-width="120" />
        <el-table-column
          prop="createTime"
          label="申请时间"
          width="180"
          :formatter="formatTime"
        />
        <el-table-column
          prop="medicalCost"
          label="就诊费用"
          width="120"
        >
          <template #default="scope">
            {{ scope.row.medicalCost || 0 }} 元
          </template>
        </el-table-column>
        <el-table-column prop="claimStatus" label="理赔状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.claimStatus === 0" type="warning">待审核</el-tag>
            <el-tag v-if="scope.row.claimStatus === 2" type="success">审核通过</el-tag>
            <el-tag v-if="scope.row.claimStatus === 3" type="danger">审核驳回</el-tag>
            <el-tag v-if="scope.row.claimStatus === 4" type="info">已打款</el-tag>
          </template>
        </el-table-column>
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

    <!-- 理赔详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="理赔订单详情"
      width="900px"
      top="20px"
      @close="resetClaimDetail"
    >
      <el-tabs v-model="activeTab" type="card">
        <!-- 1. 保险订单信息（新增） -->
<!-- 保险订单信息（新增，完全匹配你接口返回的字段） -->
<el-tab-pane label="保险订单信息" name="insurance">
  <div v-if="Object.keys(insuranceOrder).length > 0" class="info-card">
    <el-descriptions :column="3" border>
      <el-descriptions-item label="保险订单ID" :span="3">{{ insuranceOrder.id || '无' }}</el-descriptions-item>
      <el-descriptions-item label="订单编号" :span="3">{{ insuranceOrder.orderNo || '无' }}</el-descriptions-item>
      <el-descriptions-item label="用户ID" :span="3">{{ insuranceOrder.userId || '无' }}</el-descriptions-item>
      <el-descriptions-item label="宠物ID" :span="3">{{ insuranceOrder.petId || '无' }}</el-descriptions-item>
      <el-descriptions-item label="保险产品ID" :span="3">{{ insuranceOrder.insuranceId || '无' }}</el-descriptions-item>
      <el-descriptions-item label="保险产品名称" :span="3">{{ insuranceOrder.insuranceName || '无' }}</el-descriptions-item>
      <el-descriptions-item label="订单状态" :span="3">
        <el-tag v-if="insuranceOrder.orderStatus === 1" type="success">已生效</el-tag>
        <el-tag v-if="insuranceOrder.orderStatus === 2"  type="danger">已取消</el-tag>
        <el-tag v-if="insuranceOrder.orderStatus === 0" >已支付</el-tag>
        <span v-else>{{ insuranceOrder.orderStatus || '无' }}</span>
      </el-descriptions-item>
      <el-descriptions-item label="支付方式" :span="3">{{ insuranceOrder.paymentMethod === 'monthly' ? '月付' : '一次性支付' }}</el-descriptions-item>
      <el-descriptions-item label="月付金额" :span="3">{{ insuranceOrder.monthlyPrice || 0 }} 元</el-descriptions-item>
      <el-descriptions-item label="总金额" :span="3">{{ insuranceOrder.totalAmount || 0 }} 元</el-descriptions-item>
      <el-descriptions-item label="保障周期" :span="3">{{ insuranceOrder.guaranteeCycle || 0 }} 个月</el-descriptions-item>
      <el-descriptions-item label="优惠保费" :span="3">{{ insuranceOrder.discountPremium || 0 }} 元</el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="3">{{ formatTimeForDetail(insuranceOrder.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="保险生效时间" :span="3">{{ formatTimeForDetail(insuranceOrder.updateTime) }}</el-descriptions-item>
      <el-descriptions-item label="备注" :span="3">{{ insuranceOrder.remark || '无' }}</el-descriptions-item>
    </el-descriptions>
  </div>
  <div v-else class="empty-tip">暂无保险订单信息</div>
</el-tab-pane>

        <!-- 2. 理赔订单信息（完全复用你原来的） -->
        <el-tab-pane label="理赔订单信息" name="claim">
          <div class="info-card">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="理赔ID">{{ claimDetail?.claim?.id || '-' }}</el-descriptions-item>
              <el-descriptions-item label="理赔单号">{{ claimDetail?.claim?.claimNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="用户ID">{{ claimDetail?.claim?.userId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="保险订单ID">{{ claimDetail?.claim?.insuranceOrderId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物种类">{{ claimDetail?.claim?.petType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物昵称">{{ claimDetail?.claim?.petNickname || '-' }}</el-descriptions-item>
              <el-descriptions-item label="就诊费用">{{ claimDetail?.claim?.medicalCost || 0 }} 元</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatTimeForDetail(claimDetail?.claim?.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="出险时间">{{ formatTimeForDetail(claimDetail?.claim?.accidentTime) }}</el-descriptions-item>
              <el-descriptions-item label="理赔状态">
                <el-tag v-if="claimDetail?.claim?.claimStatus === 0" type="warning">待审核</el-tag>
                <el-tag v-if="claimDetail?.claim?.claimStatus === 2" type="success">审核通过</el-tag>
                <el-tag v-if="claimDetail?.claim?.claimStatus === 3" type="danger">审核驳回</el-tag>
                <el-tag v-if="claimDetail?.claim?.claimStatus === 4" type="info">已打款</el-tag>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="审核时间">{{ formatTimeForDetail(claimDetail?.claim?.auditTime) }}</el-descriptions-item>
              <el-descriptions-item label="是否手术">{{ claimDetail?.claim?.isSurgery === 1 ? '是' : claimDetail?.claim?.isSurgery === 0 ? '否' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="就诊医院类型">{{ claimDetail?.claim?.hospitalType === 1 ? '定点医院' : claimDetail?.claim?.hospitalType === 2 ? '非定点医院' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物病情概述" :span="2">{{ claimDetail?.claim?.illnessDesc || '-' }}</el-descriptions-item>
              <el-descriptions-item label="审核备注" :span="2">{{ claimDetail?.claim?.auditRemark || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div style="margin-top: 20px;">
              <h4>理赔材料：</h4>
              <div v-if="claimDetail?.materials && claimDetail.materials.length" class="materials-list">
                <div v-for="(material, index) in claimDetail.materials" :key="index" class="material-item">
                  <span class="material-label">{{ material.type || `材料${index+1}` }}：</span>
                  <el-image
                    :src="formatImgUrl(material.url)"
                    style="width: 150px; height: 150px; margin: 5px"
                    fit="cover"
                    :preview-src-list="[formatImgUrl(material.url)]"
                  />
                </div>
              </div>
              <div v-else class="empty-tip">暂无理赔材料</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 3. 宠物信息（完全按你图1的字段来写，保证正脸+全身照） -->
        <el-tab-pane label="宠物信息" name="pet">
          <div v-if="petInfo" class="info-card">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="宠物ID">{{ petInfo.petId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="所属用户ID">{{ petInfo.userId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物名称">{{ petInfo.petName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物类型">{{ petInfo.petType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="宠物性别">{{ petInfo.petGender || '-' }}</el-descriptions-item>
              <el-descriptions-item label="出生日期">{{ petInfo.petBirthday || '-' }}</el-descriptions-item>
              <el-descriptions-item label="绝育状态">{{ petInfo.isSterilized ? '是' : '否' }}</el-descriptions-item>
              <el-descriptions-item label="宠物正面照">
                <el-image
                  v-if="petInfo.petFacePhoto || petInfo.petFrontPhotoUrl"
                  :src="formatImgUrl(petInfo.petFacePhoto || petInfo.petFrontPhotoUrl)"
                  style="width: 150px; height: 150px"
                  fit="cover"
                  :preview-src-list="[formatImgUrl(petInfo.petFacePhoto || petInfo.petFrontPhotoUrl)]"
                />
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="宠物全身照">
                <el-image
                  v-if="petInfo.petBodyPhoto || petInfo.petFullPhoto"
                  :src="formatImgUrl(petInfo.petBodyPhoto || petInfo.petFullPhoto)"
                  style="width: 150px; height: 150px"
                  fit="cover"
                  :preview-src-list="[formatImgUrl(petInfo.petBodyPhoto || petInfo.petFullPhoto)]"
                />
                <span v-else>-</span>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-tip">暂无宠物信息</div>
        </el-tab-pane>
      </el-tabs>

      <!-- 弹窗底部按钮 -->
      <template #footer>
        <div v-if="claimDetail?.claim?.claimStatus === 0">
          <el-button @click="switchAuditType('pass')" v-if="auditType === 'reject'">返回</el-button>
          <el-button type="primary" @click="switchAuditType('pass'); handleAudit('pass')" v-if="auditType === 'pass'">审核通过</el-button>
          <el-button type="danger" @click="switchAuditType('reject')" v-if="auditType === 'pass'">审核驳回</el-button>
          <el-button type="danger" @click="handleAudit('reject')" v-if="auditType === 'reject'">确认驳回</el-button>
        </div>
        <el-button type="success" @click="handlePay()" v-if="claimDetail?.claim?.claimStatus === 2">确认打款</el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>

      <!-- 驳回理由表单 -->
      <el-form ref="rejectFormRef" v-if="auditType === 'reject'" :model="rejectForm" :rules="rejectRules" label-width="80px" style="margin-top: 10px">
        <el-form-item label="驳回原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="4" placeholder="请输入驳回原因（必填）" />
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// 引入你所有的接口
import { 
  getMerchantClaimList, 
  getMerchantClaimDetail, 
  auditClaim, 
  confirmClaimPay,
  getPetByUserIdAndName,
  getOrderDetail
} from '@/api/merchant/insurance'

// ========== 核心变量（完全保留你原来的） ==========
const loading = ref(false)
const claimList = ref([])
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})
const searchParams = reactive({
  claimNo: '',
  userId: '',
  claimStatus: ''
})

// ========== 详情弹窗相关变量 ==========
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const claimDetail = reactive({ claim: {}, materials: [] })
const insuranceOrder = ref({}) // 新增：保险订单
const petInfo = ref(null)       // 保留：宠物信息
const activeTab = ref('claim')  // 默认选中理赔订单

// ========== 审核相关变量 ==========
const auditType = ref('pass')
const rejectFormRef = ref(null)
const rejectForm = reactive({ reason: '', claimId: '' })
const rejectRules = {
  reason: [{ required: true, message: '请输入驳回原因', trigger: ['blur', 'change'] }]
}

// ========== 工具函数（完全保留你原来的） ==========
const formatTime = (row) => {
  return row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatTimeForDetail = (time) => {
  if (!time) return '-'
  const timeStr = time.toString().replace('T', ' ')
  return dayjs(timeStr).isValid() ? dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatImgUrl = (url) => {
  if (!url) return ''
  let formattedUrl = url.trim()
  if (!formattedUrl.startsWith('http')) {
    if (!formattedUrl.startsWith('/')) formattedUrl = `/${formattedUrl}`
    formattedUrl = `http://localhost:8080${formattedUrl}`
  }
  return formattedUrl
}

// ========== 理赔列表（完全保留你原来的） ==========
const getClaimData = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      claimNo: searchParams.claimNo,
      userId: searchParams.userId ? Number(searchParams.userId) : undefined,
      claimStatus: searchParams.claimStatus ? Number(searchParams.claimStatus) : undefined
    }
    const res = await getMerchantClaimList(params)
    if (res.data?.success) {
      claimList.value = res.data.data.records || []
      pagination.total = res.data.data.total || 0
    } else {
      ElMessage.error(res.data?.message || '查询理赔列表失败')
    }
  } catch (error) {
    console.error('【理赔列表】查询异常：', error)
    ElMessage.error('查询理赔列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// ========== 行点击（完全保留你原来的逻辑，只加保险订单查询） ==========
const handleRowClick = async (row) => {
  try {
    detailLoading.value = true
    petInfo.value = null
    claimDetail.claim = {}
    claimDetail.materials = []
    insuranceOrder.value = {} // 清空保险订单

    // 1. 查询理赔详情（你原来的逻辑）
    const res = await getMerchantClaimDetail(row.id)
    if (res.data?.success) {
      claimDetail.claim = res.data.data.claim || {}
      claimDetail.materials = res.data.data.materials || []
// 2. 查询保险订单
const insuranceOrderId = claimDetail.claim.insuranceOrderId
if (insuranceOrderId) {
  try {
    console.log('【保险订单】开始查询，订单ID：', insuranceOrderId)
    const orderRes = await getOrderDetail(insuranceOrderId)
    console.log('【保险订单】完整返回：', orderRes)
    console.log('【保险订单】data 字段：', orderRes.data)

    // 关键：你接口返回的是 res.data.data，不是 res.data！
    if (orderRes?.data?.code === 200) {
      insuranceOrder.value = orderRes.data.data
      console.log('【保险订单】最终赋值：', insuranceOrder.value)
    } else {
      console.log('【保险订单】查询失败：', orderRes?.data?.msg || '未知错误')
    }
  } catch (err) {
    console.error('【保险订单】查询异常：', err)
  }
}

      // 3. 查询宠物信息（你原来的逻辑，完全不变）
      const userId = claimDetail.claim.userId
      const petNickname = claimDetail.claim.petNickname
      if (userId && petNickname) {
        try {
          const petRes = await getPetByUserIdAndName(userId, petNickname)
          if (petRes.data?.success) {
            petInfo.value = petRes.data.data
          }
        } catch (petErr) {
          console.error('宠物信息查询异常：', petErr)
        }
      }

      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.data?.message || '查询理赔详情失败')
    }
  } catch (error) {
    console.error('【理赔详情】查询异常：', error)
    ElMessage.error('查询理赔详情失败，请重试')
  } finally {
    detailLoading.value = false
  }
}

// ========== 审核、打款、重置等你原来的逻辑（完全不变） ==========
const switchAuditType = (type) => {
  auditType.value = type
  if (type === 'reject') {
    rejectForm.claimId = claimDetail.claim.id || ''
    rejectFormRef.value?.clearValidate()
  }
}

const handleAudit = async (type) => {
  const claimId = claimDetail.claim.id
  if (!claimId) return ElMessage.warning('未获取到理赔单ID')
  if (type === 'reject' && !(await rejectFormRef.value?.validate())) return

  try {
    await ElMessageBox.confirm(
      type === 'pass' ? '确定要审核通过该理赔单吗？' : '确定要驳回该理赔单吗？',
      '操作确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: type === 'pass' ? 'success' : 'warning' }
    )
    const res = await auditClaim({
      id: claimId,
      claimStatus: type === 'pass' ? 2 : 3,
      auditRemark: type === 'reject' ? rejectForm.reason : ''
    })
    if (res.data?.success) {
      ElMessage.success(type === 'pass' ? '审核通过成功' : '审核驳回成功')
      detailDialogVisible.value = false
      getClaimData()
    } else {
      ElMessage.error(res.data?.message || (type === 'pass' ? '审核通过失败' : '审核驳回失败'))
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('审核操作异常')
  }
}

const handlePay = async () => {
  const claimId = claimDetail.claim.id
  if (!claimId) return ElMessage.warning('未获取到理赔单ID')
  try {
    await ElMessageBox.confirm('确定要确认打款该理赔单吗？', '打款确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' })
    const res = await confirmClaimPay({ id: claimId, auditRemark: '商家确认打款' })
    if (res.data?.success) {
      ElMessage.success('打款确认成功')
      detailDialogVisible.value = false
      getClaimData()
    } else {
      ElMessage.error(res.data?.message || '打款确认失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('打款操作异常')
  }
}

const resetClaimDetail = () => {
  claimDetail.claim = {}
  claimDetail.materials = []
  petInfo.value = null
  insuranceOrder.value = {}
  auditType.value = 'pass'
  rejectForm.reason = ''
  rejectForm.claimId = ''
  rejectFormRef.value?.resetFields()
}

const handleSizeChange = (val) => { pagination.pageSize = val; getClaimData() }
const handleCurrentChange = (val) => { pagination.pageNum = val; getClaimData() }
const resetSearch = () => {
  searchParams.claimNo = ''
  searchParams.userId = ''
  searchParams.claimStatus = ''
  pagination.pageNum = 1
  getClaimData()
}

onMounted(() => getClaimData())
</script>

<style scoped>
.insurance-order-manage {
  padding: 20px;
}
.manage-card {
  max-width: 1400px;
  margin: 0 auto;
  padding: 15px;
}
.search-bar {
  padding: 0 5px;
}
.cursor-pointer {
  cursor: pointer;
}
.info-card {
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}
.empty-tip {
  text-align: center;
  padding: 20px;
  color: #999;
}
.materials-list {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
  padding: 10px 0;
}
.material-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}
.material-label {
  min-width: 80px;
  color: #666;
}
</style>