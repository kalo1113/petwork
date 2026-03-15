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
        <!-- 统一显示「打款金额」 -->
        <el-table-column
          label="打款金额"
          width="120"
        >
          <template #default="scope">
            {{ scope.row.paymentAmount || 0 }} 元
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
        <!-- 新增：等待期校验状态列 -->
        <el-table-column
          label="等待期校验"
          width="180"
        >
          <template #default="scope">
            <el-tag v-if="scope.row.waitingPeriodCheck === 'allPass'" type="success">全部通过</el-tag>
            <el-tag v-if="scope.row.waitingPeriodCheck === 'partialFail'" type="warning">部分未通过</el-tag>
            <el-tag v-if="scope.row.waitingPeriodCheck === 'allFail'" type="danger">全部未通过</el-tag>
            <el-tag v-if="scope.row.waitingPeriodCheck === 'unknown'" type="info">待校验</el-tag>
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
      width="1000px"
      top="20px"
      @close="resetClaimDetail"
    >
      <el-tabs v-model="activeTab" type="card">
        <!-- 1. 保险订单信息 -->
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
              
              <!-- 新增：三种等待期展示（修复：增加数据加载提示） -->
              <el-descriptions-item label="意外等待期" :span="1">
                <span v-if="Object.keys(petInsurance).length === 0">加载中...</span>
                <span v-else>{{ petInsurance.waitingPeriodAccident || 0 }} 天</span>
              </el-descriptions-item>
              <el-descriptions-item label="先天性/遗传疾病等待期" :span="1">
                <span v-if="Object.keys(petInsurance).length === 0">加载中...</span>
                <span v-else>{{ petInsurance.waitingPeriodDisease || 0 }} 天</span>
              </el-descriptions-item>
              <el-descriptions-item label="一般疾病等待期" :span="1">
                <span v-if="Object.keys(petInsurance).length === 0">加载中...</span>
                <span v-else>{{ petInsurance.waitingPeriodCommon || 0 }} 天</span>
              </el-descriptions-item>
              
              <!-- 新增：三种等待期截止时间 -->
              <el-descriptions-item label="意外等待期截止" :span="1">{{ waitingPeriodEndTime.accident || '-' }}</el-descriptions-item>
              <el-descriptions-item label="遗传疾病等待期截止" :span="1">{{ waitingPeriodEndTime.disease || '-' }}</el-descriptions-item>
              <el-descriptions-item label="一般疾病等待期截止" :span="1">{{ waitingPeriodEndTime.common || '-' }}</el-descriptions-item>
              
              <!-- 新增：等待期校验结果汇总 -->
              <el-descriptions-item label="等待期校验结果" :span="3">
                <div v-if="waitingPeriodCheckResult.allPass">
                  <el-tag type="success">✅ 所有等待期均已通过</el-tag>
                </div>
                <div v-if="waitingPeriodCheckResult.partialFail">
                  <el-tag type="warning">⚠️ 部分等待期未通过</el-tag>
                  <div style="margin-top: 5px; font-size: 12px; color: #f56c6c;">
                    {{ waitingPeriodCheckResult.failReasons.join('、') }}
                  </div>
                </div>
                <div v-if="waitingPeriodCheckResult.allFail">
                  <el-tag type="danger">❌ 所有等待期均未通过</el-tag>
                  <div style="margin-top: 5px; font-size: 12px; color: #f56c6c;">
                    {{ waitingPeriodCheckResult.failReasons.join('、') }}
                  </div>
                </div>
                <div v-if="waitingPeriodCheckResult.unknown">
                  <el-tag type="info">ℹ️ 待校验（信息不全）</el-tag>
                </div>
              </el-descriptions-item>
              
              <el-descriptions-item label="备注" :span="3">{{ insuranceOrder.remark || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-tip">暂无保险订单信息</div>
        </el-tab-pane>

        <!-- 2. 理赔订单信息 -->
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
              <!-- 统一显示「打款金额」 -->
              <el-descriptions-item label="打款金额">{{ claimDetail?.claim?.paymentAmount || 0 }} 元</el-descriptions-item>
              <el-descriptions-item label="申请时间">{{ formatTimeForDetail(claimDetail?.claim?.createTime) }}</el-descriptions-item>
              <el-descriptions-item label="出险时间">{{ formatTimeForDetail(claimDetail?.claim?.accidentTime) }}</el-descriptions-item>
              <!-- 新增：病情类型选择（用于匹配对应等待期） -->
              <el-descriptions-item label="理赔类型" :span="2">
                <el-select 
                  v-model="claimDiseaseType" 
                  placeholder="请选择理赔对应的病情类型"
                  style="width: 200px"
                  @change="recheckWaitingPeriod"
                >
                  <el-option label="意外事故" value="accident"></el-option>
                  <el-option label="先天性/遗传疾病" value="disease"></el-option>
                  <el-option label="一般疾病" value="common"></el-option>
                </el-select>
                <span style="margin-left: 10px; color: #666; font-size: 12px;">
                  选择后将精准校验对应等待期
                </span>
              </el-descriptions-item>
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

        <!-- 3. 宠物信息 -->
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

      <!-- 弹窗底部操作区域 - 统一为「打款金额」 -->
      <template #footer>
        <!-- 1. 待审核状态：输入打款金额+备注 -->
        <div v-if="claimDetail?.claim?.claimStatus === 0">
          <!-- 新增：等待期校验失败提示 -->
          <div v-if="!waitingPeriodCheckResult.allPass && waitingPeriodCheckResult.unknown === false" style="margin-bottom: 10px; color: #f56c6c;">
            ⚠️ {{ waitingPeriodCheckResult.failReasons.length > 0 ? waitingPeriodCheckResult.failReasons.join('、') : '该理赔申请未过等待期，无法审核通过！' }}
          </div>
          
          <div v-if="auditType === 'pass'" style="margin-bottom: 10px;">
            <el-form-item label="打款金额：" style="margin: 0 10px 0 0; display: inline-block;">
              <el-input-number
                v-model="paymentAmount"
                :min="0.01"
                :step="0.01"
                placeholder="请输入打款金额"
                style="width: 200px"
                controls-position="right"
                :disabled="!waitingPeriodCheckResult.allPass && waitingPeriodCheckResult.unknown === false"
              />
              <span style="margin-left: 5px;">元</span>
            </el-form-item>
            <el-form-item label="审核备注：" style="margin: 0; display: inline-block;">
              <el-input 
                v-model="auditRemark" 
                type="textarea" 
                placeholder="请输入审核备注（选填）" 
                style="width: 300px"
                :rows="2"
                :disabled="!waitingPeriodCheckResult.allPass && waitingPeriodCheckResult.unknown === false"
              />
            </el-form-item>
          </div>

          <div v-if="auditType === 'reject'" style="margin-bottom: 10px;">
            <el-form-item label="驳回原因：" style="margin: 0;">
              <el-input 
                v-model="rejectReason" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入驳回原因（必填）" 
                style="width: 500px"
              />
            </el-form-item>
          </div>

          <el-button @click="switchAuditType('pass')" v-if="auditType === 'reject'">返回</el-button>
          <el-button 
            type="primary" 
            @click="handleAuditPass()" 
            v-if="auditType === 'pass'"
            :disabled="!waitingPeriodCheckResult.allPass && waitingPeriodCheckResult.unknown === false"
          >
            审核通过
          </el-button>
          <el-button 
            type="danger" 
            @click="switchAuditType('reject')" 
            v-if="auditType === 'pass'"
          >
            审核驳回
          </el-button>
          <el-button 
            type="danger" 
            @click="handleAuditReject()" 
            v-if="auditType === 'reject'"
          >
            确认驳回
          </el-button>
        </div>

        <!-- 2. 审核通过状态：修改打款金额+确认打款 -->
        <div v-if="claimDetail?.claim?.claimStatus === 2">
          <div style="margin-bottom: 10px;">
            <el-form-item label="修改打款金额：" style="margin: 0 10px 0 0; display: inline-block;">
              <el-input-number
                v-model="editPaymentAmount"
                :min="0.01"
                :step="0.01"
                placeholder="请输入新的打款金额"
                style="width: 200px"
                controls-position="right"
              />
              <span style="margin-left: 5px;">元</span>
              <span style="color: #999; margin-left: 10px;">（当前金额：{{ claimDetail?.claim?.paymentAmount || 0 }} 元）</span>
            </el-form-item>
            <el-button 
              type="warning" 
              @click="handleUpdatePaymentAmount()"
              style="margin-left: 10px;"
            >
              更新金额
            </el-button>
          </div>

          <div style="margin-bottom: 10px;">
            <el-form-item label="打款备注：" style="margin: 0; display: inline-block;">
              <el-input 
                v-model="finalPayRemark" 
                type="textarea" 
                placeholder="请输入打款备注（选填）" 
                style="width: 300px"
                :rows="2"
              />
            </el-form-item>
          </div>
          
          <el-button type="success" @click="handleConfirmPay()">确认打款</el-button>
        </div>

        <!-- 3. 已打款/已驳回状态：仅显示关闭按钮 -->
        <div v-if="claimDetail?.claim?.claimStatus === 3 || claimDetail?.claim?.claimStatus === 4">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>

        <!-- 通用关闭按钮（兜底） -->
        <el-button v-if="!['3', '4'].includes(claimDetail?.claim?.claimStatus + '')" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// 引入商家端接口
import { 
  getMerchantClaimList, 
  getMerchantClaimDetail, 
  auditClaim, 
  confirmClaimPay,
  getPetByUserIdAndName,
  getOrderDetail,
  updateClaimPaymentAmount
} from '@/api/merchant/insurance.js'

// 引入用户端的保险详情接口（核心修改）
import { getInsuranceDetail } from '@/api/user/index.js'

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
const claimDetail = reactive({ claim: {}, materials: [] })
const insuranceOrder = ref({}) 
const petInfo = ref(null)       
const activeTab = ref('claim')  

// ========== 审核/打款核心变量（统一为 paymentAmount） ==========
const auditType = ref('pass')
const paymentAmount = ref(0) // 审核时输入的打款金额
const editPaymentAmount = ref(0) // 审核通过后修改的打款金额
const auditRemark = ref('')
const rejectReason = ref('')
const finalPayRemark = ref('')

// ========== 等待期相关变量 ==========
const petInsurance = ref({}) // 保险详情（包含三种等待期）
const claimDiseaseType = ref('') // 理赔类型：accident/disease/common
// 等待期截止时间
const waitingPeriodEndTime = reactive({
  accident: '', // 意外等待期截止
  disease: '',  // 先天性/遗传疾病等待期截止
  common: ''    // 一般疾病等待期截止
})
// 等待期校验结果
const waitingPeriodCheckResult = reactive({
  allPass: false,      // 全部通过
  partialFail: false,  // 部分未通过
  allFail: false,      // 全部未通过
  unknown: true,       // 未知（信息不全）
  failReasons: []      // 失败原因
})

// 监听：同步金额到修改输入框
watch([paymentAmount, () => claimDetail.claim.paymentAmount], ([newAmount, claimAmount]) => {
  editPaymentAmount.value = claimAmount || newAmount
}, { immediate: true })

// 监听：理赔类型变化时重新校验
watch(claimDiseaseType, () => {
  recheckWaitingPeriod()
})

// ========== 工具函数 ==========
const formatTime = (row) => {
  try {
    return row.createTime ? dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'
  } catch (e) {
    return '-'
  }
}

const formatTimeForDetail = (time) => {
  try {
    if (!time) return '-'
    const timeStr = time.toString().replace('T', ' ')
    return dayjs(timeStr).isValid() ? dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss') : '-'
  } catch (e) {
    return '-'
  }
}

const formatImgUrl = (url) => {
  try {
    if (!url) return ''
    let formattedUrl = url.trim()
    if (!formattedUrl.startsWith('http')) {
      if (!formattedUrl.startsWith('/')) formattedUrl = `/${formattedUrl}`
      formattedUrl = `http://localhost:8080${formattedUrl}`
    }
    return formattedUrl
  } catch (e) {
    return ''
  }
}

// ========== 核心函数：获取保险详情（包含三种等待期） ==========
const getInsuranceInfo = async (insuranceId) => {
  try {
    petInsurance.value = {} // 清空旧数据
    const res = await getInsuranceDetail(insuranceId)
    
    // 🔥 核心修复：接口返回的等待期在 res.data.insurance 里
    const targetData = res.data?.insurance || res.data; // 自动兼容两种格式
    
    if (res.success || res.code === 200) {
      petInsurance.value = targetData; // ✅ 赋值正确的数据
      console.log('拿到的保险详情：', petInsurance.value); // 查看是否有数据
      checkWaitingPeriod();
    } else {
      ElMessage.error('获取保险详情失败：' + res.message);
    }
  } catch (error) {
    ElMessage.error('获取保险详情异常：' + (error.msg || error.message));
  }
};

// ========== 核心函数：三种等待期校验 ==========
const checkWaitingPeriod = () => {
  try {
    // 重置校验结果
    waitingPeriodCheckResult.allPass = false
    waitingPeriodCheckResult.partialFail = false
    waitingPeriodCheckResult.allFail = false
    waitingPeriodCheckResult.unknown = true
    waitingPeriodCheckResult.failReasons = []

    // 1. 校验必要信息是否完整
    const insuranceEffectiveTime = insuranceOrder.value.updateTime || insuranceOrder.value.createTime
    const accidentTime = claimDetail.claim.accidentTime
    
    if (!insuranceEffectiveTime || !accidentTime || Object.keys(petInsurance.value).length === 0) {
      waitingPeriodCheckResult.unknown = true
      return
    }

    // 2. 解析时间
    const effectiveTimeObj = dayjs(insuranceEffectiveTime.toString().replace('T', ' '))
    const accidentTimeObj = dayjs(accidentTime.toString().replace('T', ' '))
    
    if (!effectiveTimeObj.isValid() || !accidentTimeObj.isValid()) {
      waitingPeriodCheckResult.unknown = true
      return
    }

    // 3. 获取三种等待期天数（核心修复2：兼容字符串/数字类型）
    const accidentDays = Number(petInsurance.value.waitingPeriodAccident) || 0
    const diseaseDays = Number(petInsurance.value.waitingPeriodDisease) || 0
    const commonDays = Number(petInsurance.value.waitingPeriodCommon) || 0

    console.log('等待期天数：', { accidentDays, diseaseDays, commonDays }) // 调试用

    // 4. 计算各等待期截止时间
    waitingPeriodEndTime.accident = effectiveTimeObj.add(accidentDays, 'day').format('YYYY-MM-DD HH:mm:ss')
    waitingPeriodEndTime.disease = effectiveTimeObj.add(diseaseDays, 'day').format('YYYY-MM-DD HH:mm:ss')
    waitingPeriodEndTime.common = effectiveTimeObj.add(commonDays, 'day').format('YYYY-MM-DD HH:mm:ss')

    // 5. 开始校验
    waitingPeriodCheckResult.unknown = false
    const checkResults = {
      accident: accidentDays === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(accidentDays, 'day')),
      disease: diseaseDays === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(diseaseDays, 'day')),
      common: commonDays === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(commonDays, 'day'))
    }

    // 6. 处理校验结果
    const failTypes = []
    if (!checkResults.accident) failTypes.push('意外等待期未通过')
    if (!checkResults.disease) failTypes.push('先天性/遗传疾病等待期未通过')
    if (!checkResults.common) failTypes.push('一般疾病等待期未通过')

    waitingPeriodCheckResult.failReasons = failTypes

    // 7. 判断整体状态
    if (failTypes.length === 0) {
      waitingPeriodCheckResult.allPass = true
    } else if (failTypes.length === Object.keys(checkResults).length) {
      waitingPeriodCheckResult.allFail = true
    } else {
      waitingPeriodCheckResult.partialFail = true
    }

  } catch (e) {
    console.error('等待期校验失败：', e)
    waitingPeriodCheckResult.unknown = true
    waitingPeriodCheckResult.failReasons = ['等待期校验异常']
  }
}

// ========== 重新校验等待期（根据选择的理赔类型） ==========
const recheckWaitingPeriod = () => {
  if (!claimDiseaseType.value) {
    // 未选择类型，显示全部校验结果
    checkWaitingPeriod()
    return
  }

  // 重置校验结果
  waitingPeriodCheckResult.allPass = false
  waitingPeriodCheckResult.partialFail = false
  waitingPeriodCheckResult.allFail = false
  waitingPeriodCheckResult.unknown = true
  waitingPeriodCheckResult.failReasons = []

  // 1. 校验必要信息
  const insuranceEffectiveTime = insuranceOrder.value.updateTime || insuranceOrder.value.createTime
  const accidentTime = claimDetail.claim.accidentTime
  
  if (!insuranceEffectiveTime || !accidentTime || Object.keys(petInsurance.value).length === 0) {
    waitingPeriodCheckResult.unknown = true
    return
  }

  // 2. 解析时间
  const effectiveTimeObj = dayjs(insuranceEffectiveTime.toString().replace('T', ' '))
  const accidentTimeObj = dayjs(accidentTime.toString().replace('T', ' '))
  
  if (!effectiveTimeObj.isValid() || !accidentTimeObj.isValid()) {
    waitingPeriodCheckResult.unknown = true
    return
  }

  waitingPeriodCheckResult.unknown = false
  
  // 3. 根据选择的类型进行精准校验
  let checkResult = false
  let failReason = ''
  let days = 0

  switch (claimDiseaseType.value) {
    case 'accident':
      days = Number(petInsurance.value.waitingPeriodAccident) || 0
      checkResult = days === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(days, 'day'))
      failReason = `意外等待期未通过（需等待${days}天）`
      break
    case 'disease':
      days = Number(petInsurance.value.waitingPeriodDisease) || 0
      checkResult = days === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(days, 'day'))
      failReason = `先天性/遗传疾病等待期未通过（需等待${days}天）`
      break
    case 'common':
      days = Number(petInsurance.value.waitingPeriodCommon) || 0
      checkResult = days === 0 || accidentTimeObj.isAfter(effectiveTimeObj.add(days, 'day'))
      failReason = `一般疾病等待期未通过（需等待${days}天）`
      break
  }

  if (checkResult) {
    waitingPeriodCheckResult.allPass = true
    waitingPeriodCheckResult.failReasons = []
  } else {
    waitingPeriodCheckResult.allFail = true
    waitingPeriodCheckResult.failReasons = [failReason]
  }
}

// ========== 理赔列表 ==========
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
    const data = res.data || {}
    if (data.success || data.code === 200) {
      // 为列表数据添加等待期校验状态
      const list = data.data?.records || []
      claimList.value = list.map(item => {
        return {
          ...item,
          waitingPeriodCheck: 'unknown' // 列表页暂不做具体校验，详情页再校验
        }
      })
      pagination.total = data.data?.total || 0
    } else {
      ElMessage.error(data.message || data.msg || '查询理赔列表失败')
    }
  } catch (error) {
    console.error('【理赔列表】查询异常：', error)
    ElMessage.error('查询理赔列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// ========== 行点击 ==========
const handleRowClick = async (row) => {
  try {
    detailLoading.value = true
    resetClaimDetail()
    
    const res = await getMerchantClaimDetail(row.id)
    const data = res.data || {}
    if (data.success || data.code === 200) {
      claimDetail.claim = data.data?.claim || {}
      claimDetail.materials = data.data?.materials || []

      // 初始化金额：优先用数据库的 paymentAmount，没有则用就诊费用
      const medicalCost = Number(claimDetail.claim.medicalCost || 0)
      paymentAmount.value = Number(claimDetail.claim.paymentAmount || medicalCost)
      editPaymentAmount.value = paymentAmount.value
      auditRemark.value = claimDetail.claim.auditRemark || ''
      finalPayRemark.value = claimDetail.claim.auditRemark || ''

      // 查询保险订单
      const insuranceOrderId = claimDetail.claim.insuranceOrderId
      if (insuranceOrderId) {
        try {
          const orderRes = await getOrderDetail(insuranceOrderId)
          const orderData = orderRes.data || {}
          if (orderData.code === 200) {
            insuranceOrder.value = orderData.data || {}
            
            // 获取保险详情（包含三种等待期）
            const insuranceId = insuranceOrder.value.insuranceId
            if (insuranceId) {
              // 核心修复3：等待接口返回后再继续
              await getInsuranceInfo(insuranceId) 
            } else {
              ElMessage.warning('未获取到保险产品ID')
            }
            
          } else {
            ElMessage.error('获取保险订单失败：' + (orderData.message || orderData.msg))
          }
        } catch (err) {
          console.error('【保险订单】查询异常：', err)
          ElMessage.error('获取保险订单异常：' + (err.message || '未知错误'))
        }
      } else {
        ElMessage.warning('未获取到保险订单ID')
      }

      // 查询宠物信息
      const userId = claimDetail.claim.userId
      const petNickname = claimDetail.claim.petNickname
      if (userId && petNickname) {
        try {
          const petRes = await getPetByUserIdAndName(userId, petNickname)
          const petData = petRes.data || {}
          if (petData.success) {
            petInfo.value = petData.data || null
          }
        } catch (petErr) {
          console.error('宠物信息查询异常：', petErr)
        }
      }

      detailDialogVisible.value = true
    } else {
      ElMessage.error(data.message || data.msg || '查询理赔详情失败')
    }
  } catch (error) {
    console.error('【理赔详情】查询异常：', error)
    ElMessage.error('查询理赔详情失败，请重试')
  } finally {
    detailLoading.value = false
  }
}

// ========== 审核类型切换 ==========
const switchAuditType = (type) => {
  auditType.value = type
}

// ========== 审核通过（保存打款金额） ==========
const handleAuditPass = async () => {
  // 审核通过前校验等待期
  if (!waitingPeriodCheckResult.allPass && waitingPeriodCheckResult.unknown === false) {
    ElMessage.error(waitingPeriodCheckResult.failReasons.join('、'))
    return
  }
  
  try {
    const claimId = claimDetail.claim.id
    if (!claimId) {
      ElMessage.warning('未获取到理赔单ID')
      return
    }
    if (paymentAmount.value <= 0) {
      ElMessage.warning('打款金额必须大于0')
      return
    }

    await ElMessageBox.confirm(
      `确定要审核通过该理赔单，打款金额为${paymentAmount.value}元吗？`,
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' }
    )

    // 1. 先更新打款金额
    await updateClaimPaymentAmount({
      id: claimId,
      paymentAmount: paymentAmount.value
    })

    // 2. 再更新状态为审核通过
    const params = {
      id: claimId,
      claimStatus: 2,
      auditorId: 1,
      auditRemark: auditRemark.value || `审核通过，打款金额：${paymentAmount.value}元`
    }
    const res = await auditClaim(params)
    const data = res.data || {}
    if (data.success || data.code === 200) {
      ElMessage.success('审核通过成功，已保存打款金额')
      detailDialogVisible.value = false
      getClaimData()
    } else {
      ElMessage.error(data.message || data.msg || '审核通过失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核通过异常：', error)
      ElMessage.error('审核通过操作失败，请重试')
    }
  }
}

// ========== 更新打款金额 ==========
const handleUpdatePaymentAmount = async () => {
  try {
    const claimId = claimDetail.claim.id
    if (!claimId) {
      ElMessage.warning('未获取到理赔单ID')
      return
    }
    if (editPaymentAmount.value <= 0) {
      ElMessage.warning('打款金额必须大于0')
      return
    }
    if (editPaymentAmount.value === Number(claimDetail.claim.paymentAmount || 0)) {
      ElMessage.info('新金额与当前金额一致，无需更新')
      return
    }

    await ElMessageBox.confirm(
      `确定要将打款金额更新为${editPaymentAmount.value}元吗？`,
      '金额更新确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    const res = await updateClaimPaymentAmount({
      id: claimId,
      paymentAmount: editPaymentAmount.value
    })
    const data = res.data || {}
    if (data.success || data.code === 200) {
      ElMessage.success('打款金额更新成功')
      claimDetail.claim.paymentAmount = editPaymentAmount.value
      getClaimData()
    } else {
      ElMessage.error(data.message || data.msg || '金额更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新打款金额异常：', error)
      ElMessage.error('金额更新操作失败，请重试')
    }
  }
}

// ========== 审核驳回 ==========
const handleAuditReject = async () => {
  try {
    const claimId = claimDetail.claim.id
    if (!claimId) {
      ElMessage.warning('未获取到理赔单ID')
      return
    }
    if (!rejectReason.value.trim()) {
      ElMessage.warning('请输入驳回原因')
      return
    }

    await ElMessageBox.confirm(
      '确定要驳回该理赔单吗？',
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )

    const params = {
      id: claimId,
      claimStatus: 3,
      auditorId: 1,
      auditRemark: rejectReason.value
    }

    const res = await auditClaim(params)
    const data = res.data || {}
    if (data.success || data.code === 200) {
      ElMessage.success('审核驳回成功')
      detailDialogVisible.value = false
      getClaimData()
    } else {
      ElMessage.error(data.message || data.msg || '审核驳回失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核驳回异常：', error)
      ElMessage.error('审核驳回操作失败，请重试')
    }
  }
}

// ========== 确认打款 ==========
const handleConfirmPay = async () => {
  try {
    const claimId = claimDetail.claim.id
    if (!claimId) {
      ElMessage.warning('未获取到理赔单ID')
      return
    }
    if (editPaymentAmount.value <= 0) {
      ElMessage.warning('打款金额必须大于0')
      return
    }

    await ElMessageBox.confirm(
      `确定要向该用户打款${editPaymentAmount.value}元吗？`,
      '打款确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'success' }
    )

    // 1. 先更新打款金额（确保最终金额）
    await updateClaimPaymentAmount({
      id: claimId,
      paymentAmount: editPaymentAmount.value
    })

    // 2. 再更新状态为已打款
    const params = {
      id: claimId,
      auditRemark: finalPayRemark.value || `打款金额：${editPaymentAmount.value}元`
    }
    const res = await confirmClaimPay(params)
    const data = res.data || {}
    if (data.success || data.code === 200) {
      ElMessage.success('打款确认成功')
      detailDialogVisible.value = false
      getClaimData()
    } else {
      ElMessage.error(data.message || data.msg || '打款确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('打款异常：', error)
      ElMessage.error('打款操作失败，请重试')
    }
  }
}

// ========== 重置详情弹窗 ==========
const resetClaimDetail = () => {
  claimDetail.claim = {}
  claimDetail.materials = []
  petInfo.value = null
  insuranceOrder.value = {}
  petInsurance.value = {}
  auditType.value = 'pass'
  paymentAmount.value = 0
  editPaymentAmount.value = 0
  auditRemark.value = ''
  rejectReason.value = ''
  finalPayRemark.value = ''
  claimDiseaseType.value = ''
  
  // 重置等待期相关变量
  waitingPeriodEndTime.accident = ''
  waitingPeriodEndTime.disease = ''
  waitingPeriodEndTime.common = ''
  
  waitingPeriodCheckResult.allPass = false
  waitingPeriodCheckResult.partialFail = false
  waitingPeriodCheckResult.allFail = false
  waitingPeriodCheckResult.unknown = true
  waitingPeriodCheckResult.failReasons = []
}

// ========== 分页和搜索 ==========
const handleSizeChange = (val) => { 
  pagination.pageSize = val; 
  getClaimData() 
}
const handleCurrentChange = (val) => { 
  pagination.pageNum = val; 
  getClaimData() 
}
const resetSearch = () => {
  searchParams.claimNo = ''
  searchParams.userId = ''
  searchParams.claimStatus = ''
  pagination.pageNum = 1
  getClaimData()
}

// 初始化加载列表
onMounted(() => {
  try {
    getClaimData()
  } catch (e) {
    console.error('初始化列表失败：', e)
    ElMessage.error('页面加载失败，请刷新重试')
  }
})
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
/* 适配数字输入框样式 */
:deep(.el-input-number) {
  --el-input-number-input-width: 200px;
}
</style>