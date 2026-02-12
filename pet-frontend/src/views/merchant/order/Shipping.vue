<template>
  <div class="order-shipping">
    <el-card title="商品发货" shadow="hover">
      <!-- 订单筛选 -->
      <el-form :inline="true" :model="searchForm" style="margin-bottom: 20px">
        <el-form-item label="用户ID">
          <el-input 
            v-model.number="searchForm.userId" 
            placeholder="输入用户ID"
            type="number"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="订单ID">
          <el-input 
            v-model.number="searchForm.orderId" 
            placeholder="输入订单ID"
            type="number"
            clearable
          ></el-input>
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select 
            v-model.number="searchForm.orderStatus" 
            placeholder="选择状态"
            clearable
          >
            <el-option label="全部订单" :value="-1"></el-option>
            <el-option label="待付款" :value="0"></el-option>
            <el-option label="待发货" :value="1"></el-option>
            <el-option label="待收货" :value="2"></el-option>
            <el-option label="已完成" :value="3"></el-option>
            <el-option label="已取消" :value="4"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleGetOrderList">查询</el-button>
          <el-button @click="resetSearchForm">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 订单列表 + 分页 -->
      <div class="table-container">
        <el-table 
          :data="orderList" 
          border 
          stripe
          v-loading="loading"
          empty-text="暂无订单数据"
        >
          <el-table-column prop="orderId" label="订单ID" width="180"></el-table-column>
          <el-table-column prop="userId" label="用户ID" width="100"></el-table-column>
          <el-table-column label="商品信息" min-width="300">
            <template #default="scope">
              <!-- 空值兜底：itemList为null/undefined时显示空数组 -->
              <div v-for="item in (scope.row.itemList || [])" :key="item.productId || Math.random()" class="product-item">
                <img 
                  v-if="item.productImgPath" 
                  :src="item.productImgPath" 
                  class="product-img" 
                  alt="商品图片"
                >
                <div class="product-info">
                  <div class="product-title">{{ item.productTitle || '商品已下架' }}</div>
                  <div class="product-count">数量：{{ item.productCount || 0 }}</div>
                </div>
              </div>
              <!-- 无商品数据时的兜底提示 -->
              <div v-if="!scope.row.itemList || scope.row.itemList.length === 0" style="color: #999; padding: 10px;">
                暂无商品信息
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="订单金额" width="120">
            <template #default="scope">
              ¥{{ scope.row.totalAmount || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="receiverName" label="收货人" width="100">
            <template #default="scope">
              {{ scope.row.receiverName || '暂无' }}
            </template>
          </el-table-column>
          <el-table-column prop="receiverPhone" label="联系电话" width="130">
            <template #default="scope">
              {{ scope.row.receiverPhone || '暂无' }}
            </template>
          </el-table-column>
          <!-- 收货地址列 -->
          <el-table-column label="收货地址" min-width="250">
            <template #default="scope">
              <div class="address-text">
                {{ scope.row.receiverAddress || '暂无地址信息' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="200">
            <template #default="scope">
              {{ formatTime(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderStatus" label="状态" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.orderStatus === 0" type="info">待付款</el-tag>
              <el-tag v-else-if="scope.row.orderStatus === 1" type="warning">待发货</el-tag>
              <el-tag v-else-if="scope.row.orderStatus === 2" type="primary">待收货</el-tag>
              <el-tag v-else-if="scope.row.orderStatus === 3" type="success">已完成</el-tag>
              <el-tag v-else-if="scope.row.orderStatus === 4" type="danger">已取消</el-tag>
              <el-tag v-else type="default">未知</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button 
                v-if="scope.row.orderStatus === 1" 
                type="primary" 
                size="small" 
                @click="handleShipProduct(scope.row.orderId)"
              >
                确认发货
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <el-pagination
          v-if="total > 0"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          style="margin-top: 20px; text-align: right"
        >
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 导入接口方法（请确认路径正确性）
import { 
  getMerchantProductOrderList,
  updateProductOrderStatus
} from '@/api/merchant/insurance'

// 加载状态
const loading = ref(false)
// 搜索表单
const searchForm = ref({
  userId: null,
  orderId: null,
  orderStatus: -1 // 默认查询全部订单
})
// 分页参数
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
// 订单列表
const orderList = ref([])

// 格式化时间（增加异常处理）
const formatTime = (time) => {
  if (!time) return '暂无时间'
  try {
    const date = new Date(time)
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
  } catch (e) {
    return '时间格式错误'
  }
}

// 获取订单列表（增加多层空值兜底）
const handleGetOrderList = async () => {
  try {
    loading.value = true
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: searchForm.value.userId || undefined,
      orderId: searchForm.value.orderId || undefined,
      orderStatus: searchForm.value.orderStatus !== -1 ? searchForm.value.orderStatus : undefined
    }
    
    console.log('请求参数：', params)
    const res = await getMerchantProductOrderList(params)
    console.log('返回数据：', res)
    
    // 多层空值判断，防止任意层级为null/undefined
    if (res && res.code === 200) {
      const data = res.data || {}
      const pageData = data.data || {}
      // 强制itemList为数组，避免遍历null
      orderList.value = (pageData.records || []).map(item => ({
        ...item,
        itemList: item.itemList || []
      }))
      total.value = pageData.total || 0
    } else {
      ElMessage.error(res?.msg || '获取订单列表失败')
      orderList.value = []
      total.value = 0
    }
  } catch (err) {
    console.error('获取订单异常：', err)
    ElMessage.error('获取订单失败，请检查接口连接')
    orderList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 重置搜索表单
const resetSearchForm = () => {
  searchForm.value = {
    userId: null,
    orderId: null,
    orderStatus: -1
  }
  pageNum.value = 1
  handleGetOrderList()
}

// 确认发货（增加订单ID有效性校验）
const handleShipProduct = async (orderId) => {
  // 校验订单ID有效性
  if (!orderId) {
    ElMessage.warning('订单ID无效，请刷新后重试')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要发货该订单吗？',
      '发货确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await updateProductOrderStatus({
      orderId,
      status: 2 // 改为待收货状态
    })
    
    ElMessage.success('发货成功！')
    handleGetOrderList() // 刷新列表
  } catch (err) {
    if (err !== 'cancel') { // 排除取消操作的异常
      console.error('发货失败：', err)
      ElMessage.error('发货失败，请重试')
    }
  }
}

// 分页-每页条数改变
const handleSizeChange = (val) => {
  pageSize.value = val
  handleGetOrderList()
}

// 分页-当前页改变
const handleCurrentChange = (val) => {
  pageNum.value = val
  handleGetOrderList()
}

// 页面加载时初始化数据
onMounted(() => {
  handleGetOrderList()
})
</script>

<style scoped>
.table-container {
  max-height: calc(100vh - 200px);
  overflow: auto;
}

.product-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #eee;
}

.product-img {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
}

.product-info {
  flex: 1;
}

.product-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.product-count {
  font-size: 12px;
  color: #999;
}

/* 地址列样式优化 */
.address-text {
  font-size: 14px;
  color: #666;
  line-height: 1.4;
  word-break: break-all; /* 自动换行适配长地址 */
}
</style>