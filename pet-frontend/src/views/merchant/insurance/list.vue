<template>
  <div class="insurance-audit">
    <el-card title="保险列表（全字段）" class="card-container">
      <!-- 搜索栏：双字段查询 + 修复输入框绑定 -->
      <div class="search-bar" style="margin-bottom: 10px; display: flex; gap: 10px; align-items: center">
        <el-input
          v-model="searchKeyword" 
          placeholder="请输入保险名称/保险编号查询"
          style="width: 300px"
          clearable
          @keyup.enter="getAuditData()"
        ></el-input>
        <el-button type="primary" @click="getAuditData()">查询</el-button>
        <el-button @click="resetSearch()">重置</el-button>
      </div>

      <!-- 核心：表格容器（修复fixed列DOM冲突） -->
      <div class="table-scroll-container">
        <el-table 
          :data="auditList" 
          border 
          stripe 
          style="width: 100%;"
          :scroll="{ x: 'max-content' }" 
          :scrollbar-always-on="true"
        >
          <!-- 左侧固定列 -->
          <el-table-column prop="id" label="保险ID" width="80" fixed="left"></el-table-column>
          <el-table-column prop="insuranceNo" label="保险编号" width="120" fixed="left"></el-table-column>
          <el-table-column prop="insuranceName" label="保险名称" min-width="200" fixed="left"></el-table-column>
          
          <!-- 中间滚动列 -->
          <el-table-column prop="planType" label="保障方案类型" width="120">
            <template #default="scope">
              <el-tag type="info">{{ getPlanTypeText(scope.row.planType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="petType" label="适用宠物类型" width="120">
            <template #default="scope">
              <el-tag type="primary">{{ getPetTypeText(scope.row.petType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="guaranteeCycle" label="保障周期(月)" width="120">
            <template #default="scope">
              {{ scope.row.guaranteeCycle || 0 }} 月
            </template>
          </el-table-column>
          <el-table-column prop="discountPremium" label="优惠保费(元)" width="120">
            <template #default="scope">
              {{ formatDecimal(scope.row.discountPremium) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="totalGuarantee" label="总保额(元)" width="120">
            <template #default="scope">
              {{ formatDecimal(scope.row.totalGuarantee) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="deductible" label="免赔额(元)" width="120">
            <template #default="scope">
              {{ formatDecimal(scope.row.deductible) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="outpatientLimit" label="门诊单次赔付上限(元)" width="150">
            <template #default="scope">
              {{ formatDecimal(scope.row.outpatientLimit) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="surgeryLimit" label="手术单次赔付上限(元)" width="150">
            <template #default="scope">
              {{ formatDecimal(scope.row.surgeryLimit) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="inNetworkRatio" label="定点医院赔付比例(%)" width="160">
            <template #default="scope">
              {{ scope.row.inNetworkRatio || 0 }} %
            </template>
          </el-table-column>
          <el-table-column prop="outNetworkRatio" label="非定点医院赔付比例(%)" width="180">
            <template #default="scope">
              {{ scope.row.outNetworkRatio || 0 }} %
            </template>
          </el-table-column>
          <el-table-column prop="waitingPeriodAccident" label="意外等待期(天)" width="150">
            <template #default="scope">
              {{ scope.row.waitingPeriodAccident || 0 }} 天
            </template>
          </el-table-column>
          <el-table-column prop="waitingPeriodDisease" label="先天性/遗传疾病等待期(天)" width="200">
            <template #default="scope">
              {{ scope.row.waitingPeriodDisease || 0 }} 天
            </template>
          </el-table-column>
          <el-table-column prop="waitingPeriodCommon" label="一般疾病等待期(天)" width="180">
            <template #default="scope">
              {{ scope.row.waitingPeriodCommon || 0 }} 天
            </template>
          </el-table-column>
          <el-table-column prop="monthlySubsidy" label="月消费补贴(元)" width="140">
            <template #default="scope">
              {{ formatDecimal(scope.row.monthlySubsidy) }} 元
            </template>
          </el-table-column>
          <el-table-column prop="giftService" label="赠送服务" min-width="200">
            <template #default="scope">
              {{ scope.row.giftService || '无' }}
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="190">
            <template #default="scope">
              {{ formatLocalDateTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="updateTime" label="更新时间" width="190">
            <template #default="scope">
              {{ formatLocalDateTime(scope.row.updateTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="putOnShelfTime" label="上架时间" width="190">
            <template #default="scope">
              {{ formatLocalDateTime(scope.row.putOnShelfTime) }}
            </template>
          </el-table-column>

          <!-- 右侧固定列 -->
          <el-table-column prop="status" label="上下架状态" width="100" fixed="right">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '上架' : '下架' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 分页组件：修复pageSize显示 -->
      <div class="pagination-container" style="margin-top: 10px; text-align: right;">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSizeShow"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-sizes="[10, 20, 50]"  
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getInsuranceList } from '@/api/merchant/insurance'

// **************************
// 核心修复1：简化所有响应式变量，避免嵌套对象导致的正则解析异常
// **************************
// 搜索关键词（直接绑定基础类型，不嵌套）
const searchKeyword = ref('')
// 分页参数（扁平化，避免嵌套对象）
const pageNum = ref(1)
const pageSize = ref(-1)        // 实际传给后端的参数
const pageSizeShow = ref(10)    // 展示用参数
const total = ref(0)            // 总条数
// 表格数据
const auditList = ref([])

// 4. 保障方案类型转换
const getPlanTypeText = (type) => {
  // 修复：先判断type是否为null/undefined
  if (type === null || type === undefined) return '未知'
  const map = { 1: '基础版', 2: '升级版', 3: '尊享版' }
  return map[type] || '未知'
}

// 5. 适用宠物类型转换
const getPetTypeText = (type) => {
  if (type === null || type === undefined) return '未知'
  const map = { 1: '猫咪', 2: '狗狗', 3: '通用' }
  return map[type] || '未知'
}

// 6. 金额格式化（增加空值强校验）
const formatDecimal = (num) => {
  if (num === null || num === undefined || isNaN(num)) return '0.00'
  return Number(num).toFixed(2)
}

// 7. 时间格式化（修复正则解析问题）
const formatLocalDateTime = (time) => {
  if (!time) return '-'
  try {
    // 修复：先转字符串，避免传入非字符串导致正则报错
    const timeStr = String(time).replace('T', ' ')
    const date = new Date(timeStr)
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
  } catch (e) {
    return '-'
  }
}

// 8. 获取保险列表（简化传参，避免多参数冲突）
const getAuditData = async () => {
  try {
    await nextTick()
    
    const params = {
      pageNum: pageNum.value || 1,
      pageSize: pageSize.value || -1,
    }

    // 核心修改：只传insuranceName，后端同时匹配名称/编号
    const keyword = searchKeyword.value?.trim() || ''
    if (keyword) {
      params.insuranceName = keyword; // 只传这一个参数即可
      // 删掉 params.insuranceNo = keyword 这一行！
    }

    const res = await getInsuranceList(params)
    const result = res?.data || {}
    const pageData = result?.data || {}

    auditList.value = Array.isArray(pageData.records) ? pageData.records : []
    total.value = Number(pageData.total) || 0

    if (pageSize.value === -1) {
      pageSizeShow.value = total.value > 0 ? total.value : 10
    }
  } catch (err) {
    auditList.value = []
    total.value = 0
    ElMessage.error('获取保险列表失败：' + (err.message || '系统异常'))
  }
}

// 9. 分页大小改变（修复：避免val为null）
const handleSizeChange = (val) => {
  if (val === null || val === undefined) return
  pageSizeShow.value = val
  // 选择"全部"逻辑：如果val大于等于总条数，传-1
  pageSize.value = (val >= total.value && total.value > 0) ? -1 : val
  getAuditData()
}

// 10. 当前页改变
const handleCurrentChange = (val) => {
  if (val === null || val === undefined) return
  pageNum.value = val
  getAuditData()
}

// 11. 重置搜索（简化逻辑，避免嵌套对象）
const resetSearch = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  pageSize.value = -1
  pageSizeShow.value = 10
  nextTick(() => {
    getAuditData()
  })
}

// 页面加载
onMounted(async () => {
  await nextTick()
  getAuditData()
})
</script>

<style scoped>
.insurance-audit {
  padding: 20px;
}
.search-bar {
  flex-wrap: wrap;
  align-items: center;
}
/* 修复输入框交互 */
:deep(.el-input) {
  pointer-events: auto;
  z-index: 10;
}
/* 表格容器修复 */
.table-scroll-container {
  overflow-x: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 10px;
  z-index: 1;
  position: relative;
}
/* 固定列样式 */
:deep(.el-table__fixed-left),
:deep(.el-table__fixed-right) {
  background: #fff;
  z-index: 2;
}
.pagination-container {
  padding: 5px 0;
}
</style>