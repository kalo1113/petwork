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
      width="800px"
      top="20px"
      @close="resetClaimDetail"
    >
      <div v-loading="detailLoading" class="detail-content">
        <!-- 理赔基础信息 -->
        <el-card title="理赔基础信息" style="margin-bottom: 15px">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="理赔ID">{{ claimDetail?.claim?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="理赔单号">{{ claimDetail?.claim?.claimNo || '-' }}</el-descriptions-item>
            <el-descriptions-item label="用户ID">{{ claimDetail?.claim?.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="保险订单ID">{{ claimDetail?.claim?.insuranceOrderId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物种类">{{ claimDetail?.claim?.petType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物昵称">{{ claimDetail?.claim?.petNickname || '-' }}</el-descriptions-item>
            <el-descriptions-item label="就诊费用">{{ claimDetail?.claim?.medicalCost || 0 }} 元</el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatTimeForDetail(claimDetail?.claim?.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="出险时间">
              {{ formatTimeForDetail(claimDetail?.claim?.accidentTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="理赔状态">
              <el-tag v-if="claimDetail?.claim?.claimStatus === 0" type="warning">待审核</el-tag>
              <el-tag v-if="claimDetail?.claim?.claimStatus === 2" type="success">审核通过</el-tag>
              <el-tag v-if="claimDetail?.claim?.claimStatus === 3" type="danger">审核驳回</el-tag>
              <el-tag v-if="claimDetail?.claim?.claimStatus === 4" type="info">已打款</el-tag>
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="审核时间">
              {{ formatTimeForDetail(claimDetail?.claim?.auditTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="是否手术">
              {{ claimDetail?.claim?.isSurgery === 1 ? '是' : claimDetail?.claim?.isSurgery === 0 ? '否' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="就诊医院类型">
              {{ claimDetail?.claim?.hospitalType === 1 ? '定点医院' : claimDetail?.claim?.hospitalType === 2 ? '非定点医院' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="宠物病情概述">{{ claimDetail?.claim?.illnessDesc || '-' }}</el-descriptions-item>
            <el-descriptions-item label="审核备注">{{ claimDetail?.claim?.auditRemark || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 理赔材料 -->
        <el-card title="理赔材料" style="margin-bottom: 15px">
          <div v-if="claimDetail?.materials && claimDetail.materials.length" class="materials-list">
            <div v-for="(material, index) in claimDetail.materials" :key="index" class="material-item">
              <span class="material-label">{{ material.type || `材料${index+1}` }}：</span>
              <el-image
                :src="material.url"
                style="width: 150px; height: 150px; margin: 5px"
                fit="cover"
                :preview-src-list="[material.url]"
              />
            </div>
          </div>
          <div v-else class="empty-tip">暂无理赔材料</div>
        </el-card>

        <!-- 宠物信息 -->
        <el-card title="宠物信息" style="margin-bottom: 15px">
          <el-descriptions :column="2" border v-if="claimDetail?.petInfo">
            <el-descriptions-item label="宠物ID">{{ claimDetail?.petInfo?.id || '-' }}</el-descriptions-item>
            <el-descriptions-item label="所属用户ID">{{ claimDetail?.petInfo?.userId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物名称">{{ claimDetail?.petInfo?.petName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物类型">{{ claimDetail?.petInfo?.petType || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物性别">{{ claimDetail?.petInfo?.petGender || '-' }}</el-descriptions-item>
            <el-descriptions-item label="绝育状态">{{ claimDetail?.petInfo?.isSterilized ? '是' : '否' || '-' }}</el-descriptions-item>
            <el-descriptions-item label="宠物正面照">
              <el-image
                v-if="claimDetail?.claim?.petFrontPhotoUrl"
                :src="claimDetail.claim.petFrontPhotoUrl"
                style="width: 100px; height: 100px"
                fit="cover"
                :preview-src-list="[claimDetail.claim.petFrontPhotoUrl]"
              />
              <span v-else>-</span>
            </el-descriptions-item>
            <el-descriptions-item label="宠物全身照">
              <el-image
                v-if="claimDetail?.claim?.petFullPhotoUrl"
                :src="claimDetail.claim.petFullPhotoUrl"
                style="width: 100px; height: 100px"
                fit="cover"
                :preview-src-list="[claimDetail.claim.petFullPhotoUrl]"
              />
              <span v-else>-</span>
            </el-descriptions-item>
          </el-descriptions>
          <div v-else class="empty-tip">暂无宠物信息</div>
        </el-card>

        <!-- 审核操作 -->
        <el-card 
          title="审核操作" 
          style="margin-bottom: 15px"
          v-if="claimDetail?.claim?.claimStatus === 0"
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

      <!-- 弹窗底部按钮 -->
      <template #footer>
        <div v-if="claimDetail?.claim?.claimStatus === 0">
          <el-button @click="switchAuditType('pass')" v-if="auditType === 'reject'">返回</el-button>
          <el-button type="primary" @click="handleAudit('pass')" v-if="auditType === 'pass'">审核通过</el-button>
          <el-button type="danger" @click="switchAuditType('reject')" v-if="auditType === 'pass'">审核驳回</el-button>
          <el-button type="danger" @click="handleAudit('reject')" v-if="auditType === 'reject'">确认驳回</el-button>
        </div>
        <el-button 
          type="success" 
          @click="handlePay()" 
          v-if="claimDetail?.claim?.claimStatus === 2"
        >
          确认打款
        </el-button>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
// 引入封装的理赔接口
import { 
  getMerchantClaimList, 
  getMerchantClaimDetail, 
  auditClaim, 
  confirmClaimPay 
} from '@/api/merchant/insurance'

// ========== 核心变量 ==========
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
const claimDetail = reactive({
  claim: {},
  petInfo: {},
  materials: []
})

// ========== 审核相关变量 ==========
const auditType = ref('pass')
const rejectFormRef = ref(null)
const rejectForm = reactive({
  reason: '',
  claimId: ''
})
const rejectRules = {
  reason: [{ required: true, message: '请输入驳回原因', trigger: ['blur', 'change'] }]
}

// ========== 工具函数 ==========
const formatTime = (row) => {
  return row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const formatTimeForDetail = (time) => {
  if (!time) return '-'
  const timeStr = time.toString().replace('T', ' ')
  return dayjs(timeStr).isValid() ? dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// ========== 业务逻辑函数 ==========
// 获取理赔列表数据
const getClaimData = async () => {
  try {
    loading.value = true
    // 构造查询参数
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      claimNo: searchParams.claimNo,
      userId: searchParams.userId ? Number(searchParams.userId) : undefined,
      claimStatus: searchParams.claimStatus ? Number(searchParams.claimStatus) : undefined
    }
    // 调用后端接口
    const res = await getMerchantClaimList(params)
    if (res.data?.success) {
      claimList.value = res.data.data.records || []
      pagination.total = res.data.data.total || 0
    } else {
      ElMessage.error(res.data?.message || '查询理赔列表失败')
    }
  } catch (error) {
    console.error('查询理赔列表异常：', error)
    ElMessage.error('查询理赔列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 行点击-查看详情
const handleRowClick = async (row) => {
  try {
    detailLoading.value = true
    // 调用详情接口
    const res = await getMerchantClaimDetail(row.id)
    if (res.data?.success) {
      claimDetail.claim = res.data.data.claim || {}
      claimDetail.petInfo = res.data.data.petInfo || {}
      claimDetail.materials = res.data.data.materials || []
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.data?.message || '查询理赔详情失败')
    }
  } catch (error) {
    console.error('查询理赔详情异常：', error)
    ElMessage.error('查询理赔详情失败，请重试')
  } finally {
    detailLoading.value = false
  }
}

// 切换审核类型（通过/驳回）
const switchAuditType = (type) => {
  auditType.value = type
  if (type === 'reject') {
    rejectForm.claimId = claimDetail.claim.id || ''
    if (rejectFormRef.value) {
      rejectFormRef.value.clearValidate()
    }
  }
}

// 处理审核操作
const handleAudit = async (type) => {
  const claimId = claimDetail.claim.id
  if (!claimId) {
    ElMessage.warning('未获取到理赔单ID')
    return
  }

  // 驳回需校验表单
  if (type === 'reject') {
    const valid = await rejectFormRef.value.validate()
    if (!valid) return
  }

  try {
    // 确认操作
    await ElMessageBox.confirm(
      type === 'pass' ? '确定要审核通过该理赔单吗？' : '确定要驳回该理赔单吗？',
      '操作确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: type === 'pass' ? 'success' : 'warning'
      }
    )

    // 构造审核参数
    const auditParams = {
      id: claimId,
      claimStatus: type === 'pass' ? 2 : 3, // 2=通过，3=驳回
      auditRemark: type === 'reject' ? rejectForm.reason : ''
    }

    // 调用审核接口
    const res = await auditClaim(auditParams)
    if (res.data?.success) {
      ElMessage.success(type === 'pass' ? '审核通过成功' : '审核驳回成功')
      detailDialogVisible.value = false
      // 刷新列表
      getClaimData()
    } else {
      ElMessage.error(res.data?.message || (type === 'pass' ? '审核通过失败' : '审核驳回失败'))
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核操作异常：', error)
      ElMessage.error('审核操作失败，请重试')
    }
  }
}

// 处理打款确认
const handlePay = async () => {
  const claimId = claimDetail.claim.id
  if (!claimId) {
    ElMessage.warning('未获取到理赔单ID')
    return
  }

  try {
    // 确认操作
    await ElMessageBox.confirm(
      '确定要确认打款该理赔单吗？',
      '打款确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success'
      }
    )

    // 调用打款接口
    const res = await confirmClaimPay({
      id: claimId,
      auditRemark: '商家确认打款'
    })

    if (res.data?.success) {
      ElMessage.success('打款确认成功')
      detailDialogVisible.value = false
      // 刷新列表
      getClaimData()
    } else {
      ElMessage.error(res.data?.message || '打款确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('打款确认异常：', error)
      ElMessage.error('打款确认失败，请重试')
    }
  }
}

// 重置详情弹窗
const resetClaimDetail = () => {
  claimDetail.claim = {}
  claimDetail.petInfo = {}
  claimDetail.materials = []
  auditType.value = 'pass'
  rejectForm.reason = ''
  rejectForm.claimId = ''
  if (rejectFormRef.value) {
    rejectFormRef.value.resetFields()
  }
}

// 分页大小变更
const handleSizeChange = (val) => {
  pagination.pageSize = val
  getClaimData()
}

// 分页页码变更
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  getClaimData()
}

// 重置搜索条件
const resetSearch = () => {
  searchParams.claimNo = ''
  searchParams.userId = ''
  searchParams.claimStatus = ''
  pagination.pageNum = 1
  getClaimData()
}

// 页面初始化加载数据
onMounted(() => {
  getClaimData()
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
.cursor-pointer {
  cursor: pointer;
}
.detail-content {
  max-height: 70vh;
  overflow-y: auto;
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