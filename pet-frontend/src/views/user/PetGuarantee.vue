<template>
  <div class="page-container">
    <div class="pet-guarantee">
      <!-- 顶部 banner 区域 -->
      <div class="banner">
        <img src="@/assets/images/保障图标/宠物医保.png" alt="宠物医保 banner" class="banner-img" />
        <div class="banner-text">
          <h1>宠物<br>医保</h1>
          <div class="tag">为宝贝推荐</div>
        </div>
      </div>
      <div class="pet-insurance-wrapper">
        <!-- 顶部保障亮点区域 -->
        <div class="highlight-area">
          <p class="highlight-title">报销猫狗医疗费</p>
          <div class="highlight-list">
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>疾病保障全</span>
            </div>
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>定点医院多</span>
            </div>
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>报销3日完结</span>
            </div>
          </div>
        </div>

        <!-- 套餐选择区域 -->
        <div class="package-area">
          <!-- 10000医疗费套餐 -->
          <div
            class="package-item"
            :class="{ 'active': activePackage === 0 }"
            @click="handlePackageClick(0)"
          >
            <p class="package-tag" v-if="activePackage === 0">基础入门</p>
            <p class="medical-fee">10000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">200消费补贴<br>服务买药可用</p>
            <p class="service">· 体外驱虫1支</p>
            <div class="price">
              <span class="price-text">18.00元/季起</span>
            </div>
          </div>

          <!-- 20000医疗费套餐（默认选中） -->
          <div
            class="package-item recommended"
            :class="{ 'active': activePackage === 1 }"
            @click="handlePackageClick(1)"
          >
            <p class="package-tag" v-if="activePackage === 1">众多宠友力荐</p>
            <p class="medical-fee">20000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">送每月200消费补贴<br>驱虫/洗澡等5折起</p>
            <p class="service">送健康服务<br>· 多联疫苗1针<br>· 体外驱虫1支</p>
            <div class="price">
              <span class="price-text">38.00元/季起</span>
            </div>
          </div>

          <!-- 30000医疗费套餐 -->
          <div
            class="package-item"
            :class="{ 'active': activePackage === 2 }"
            @click="handlePackageClick(2)"
          >
            <p class="package-tag" v-if="activePackage === 2">大病赔得多</p>
            <p class="medical-fee">30000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">200消费补贴<br>服务买药可用</p>
            <p class="service">· 多联疫苗1针<br>· 体外驱虫1支<br>· 狂犬疫苗1针<br>· 通用体检1次</p>
            <div class="price">
              <span class="price-text">68.00元/季起</span>
            </div>
          </div>
        </div>
        <!-- 行动按钮区域 -->
        <div class="action-btn">
          <button class="main-btn" @click="handleGoToDetail">去看看</button>
        </div>
      </div>

      <!-- 责任/意外保险区域（从数据库遍历 id ≥ 5 的保险） -->
      <div class="insurance-list rounded">
        <div class="top-more">
          <h3>更多推荐</h3>
        </div>
        <!-- 加载中状态 -->
        <div v-if="loading" class="loading">加载中...</div>
        <!-- 遍历过滤后的保险列表（仅显示图片有效项） -->
        <div 
          class="insurance-item rounded"
          v-for="item in validInsuranceList" 
          :key="item.id"
          v-else
        >
          <!-- 备注仍在card上方，位置不变 -->
          <p class="insurance-subtitle">
            {{ getRemarkOutside(getContentType4Remark(item.mediaList)) || item.insuranceName }}
          </p>
          <p class="insurance-desc">
            {{ getRemarkInside(getContentType4Remark(item.mediaList)) || '暂无描述' }}
          </p>
          <div class="insurance-card">
            <!-- 图片404已被过滤，无需兜底 -->
            <img 
              :src="getInsuranceImgUrl(getDynamicImgPath(item.mediaList))" 
              :alt="item.insuranceName" 
              class="insurance-img rounded" 
            />
            <div class="insurance-info">
              <h3>{{ item.insuranceName }}</h3>
              <p class="insurance-amount">最高{{ formatToWan(item.totalGuarantee) }}万</p>
              <p class="insurance-scope">  {{ item.giftService && item.giftService.trim() ? '赠送' + item.giftService : '无赠送服务' }}</p>
              <p class="insurance-tag" v-if="item.tag">{{ item.tag }}</p>
              <div class="insurance-price-btn">
                <p class="insurance-price">{{ formatMonthlyPrice(item.discountPremium, item.guaranteeCycle) }}元/月起</p>
                <p class="insurance-bonus" v-if="item.bonus">{{ item.bonus }}</p>
                <button class="insurance-btn" @click="goToInsuranceDetail(item.id)">去看看</button>
              </div>
            </div>
          </div>
        </div>
        <!-- 兜底：无有效数据时显示 -->
        <div v-if="!loading && validInsuranceList.length === 0" class="no-data">
          暂无更多推荐的保险产品
        </div>
        <!-- 请求失败提示 -->
        <div v-if="requestFailed" class="request-failed">
          加载失败，<span @click="fetchInsuranceList" style="color:#4186e8;cursor:pointer">点击重试</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getInsurancePage, getInsuranceImgUrl, getInsuranceMediaList } from '@/api/user/index.js'

const router = useRouter()
const activePackage = ref(1)
const insuranceList = ref([]) // 原始保险列表
const validInsuranceList = ref([]) // 过滤后（图片有效）的保险列表
const loading = ref(false) // 加载状态
const requestFailed = ref(false) // 请求失败状态
let debounceTimer = null // 防抖计时器

// 防抖函数：避免频繁请求
const debounce = (fn, delay = 500) => {
  return (...args) => {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      fn.apply(this, args)
    }, delay)
  }
}

// 套餐与保险ID的映射关系
const packageInsuranceMap = {
  0: 1, // 第一个套餐 → 保险ID=1
  1: 2, // 第二个套餐 → 保险ID=2
  2: 3  // 第三个套餐 → 保险ID=3
}

const goToInsuranceDetail = (insuranceId) => {
  router.push({
    path: '/policy-detail',
    query: { id: insuranceId }
  })
}

// 点击套餐项：仅切换选中状态
const handlePackageClick = (index) => {
  activePackage.value = index
}

// 点击“去看看”按钮跳转对应保险
const handleGoToDetail = () => {
  const insuranceId = packageInsuranceMap[activePackage.value]
  router.push({
    path: '/policy-detail', 
    query: { 
      id: insuranceId,
      packageType: activePackage.value
    }
  })
}

// 数字换算：元 → 万
const formatToWan = (num) => {
  if (!num || isNaN(Number(num))) return '0'
  const value = Number(num) / 10000
  return value.toFixed(1).replace(/\.0$/, '')
}

// 月付价格换算
const formatMonthlyPrice = (totalPremium, guaranteeCycle) => {
  if (!totalPremium || !guaranteeCycle || isNaN(Number(totalPremium)) || isNaN(Number(guaranteeCycle)) || Number(guaranteeCycle) <= 0) {
    return '0.00'
  }
  const monthly = Number(totalPremium) / Number(guaranteeCycle)
  return monthly.toFixed(2)
}

// 动态获取图片路径（优先contentType=4）
const getDynamicImgPath = (mediaList) => {
  if (!mediaList || !Array.isArray(mediaList) || mediaList.length === 0) return '';
  const type4Item = mediaList.find(item => Number(item.contentType) === 4 && item.imgPath);
  if (type4Item) return type4Item.imgPath;
  const firstValidItem = mediaList.find(item => item.imgPath);
  return firstValidItem ? firstValidItem.imgPath : '';
};

// 提取contentType=4的imgRemark
const getContentType4Remark = (mediaList) => {
  if (!mediaList || !Array.isArray(mediaList)) return ''
  const type4Item = mediaList.find(item => Number(item.contentType) === 4)
  return type4Item ? (type4Item.imgRemark || '') : ''
}

// 提取括号外文字
const getRemarkOutside = (remark) => {
  if (!remark) return ''
  const splitChar = remark.includes('（') ? '（' : (remark.includes('(') ? '(' : '')
  if (!splitChar) return remark.trim()
  return remark.split(splitChar)[0].trim()
}

// 提取括号内文字
const getRemarkInside = (remark) => {
  if (!remark) return ''
  const startChar = remark.includes('（') ? '（' : (remark.includes('(') ? '(' : '')
  const endChar = remark.includes('）') ? '）' : (remark.includes(')') ? ')' : '')
  if (!startChar || !endChar) return ''
  
  const start = remark.indexOf(startChar) + 1
  const end = remark.indexOf(endChar)
  if (start >= end) return ''
  return remark.substring(start, end).trim()
}

// ========== 核心新增：检测图片是否能正常加载（Promise版） ==========
const checkImageValid = (imgUrl) => {
  return new Promise((resolve) => {
    if (!imgUrl) {
      resolve(false) // 无图片路径直接判定无效
      return
    }
    const img = new Image()
    // 超时控制（5秒）
    const timeout = setTimeout(() => {
      resolve(false)
    }, 5000)
    // 图片加载成功
    img.onload = () => {
      clearTimeout(timeout)
      resolve(true)
    }
    // 图片加载失败（404/500等）
    img.onerror = () => {
      clearTimeout(timeout)
      resolve(false)
    }
    img.src = getInsuranceImgUrl(imgUrl) // 拼接完整图片URL
  })
}

// ========== 核心修改：获取保险列表并过滤图片失效项 ==========
const fetchInsuranceList = debounce(async () => {
  loading.value = true
  requestFailed.value = false
  insuranceList.value = []
  validInsuranceList.value = []
  
  try {
    // 1. 获取原始保险列表
    const res = await getInsurancePage(1, 20, '', undefined, 1)
    if (res.code === 200) {
      const allInsurances = res.data.records || []
      const filtered = allInsurances.filter(item => 
        item.id >= 5 && item.status === 1
      )
      console.log('筛选后的保险列表（id≥5+上架）：', filtered)
      
      // 2. 批量获取媒体列表
      const listWithMedia = await Promise.allSettled(
        filtered.map(async (item) => {
          try {
            const mediaPromise = getInsuranceMediaList(item.id)
            const timeoutPromise = new Promise((_, reject) => 
              setTimeout(() => reject(new Error('请求超时')), 10000)
            )
            const mediaRes = await Promise.race([mediaPromise, timeoutPromise])
            return { ...item, mediaList: mediaRes.code === 200 ? mediaRes.data : [] }
          } catch (err) {
            console.error(`获取保险 ${item.id} 媒体列表失败：`, err)
            return { ...item, mediaList: [] }
          }
        })
      )
      
      const insuranceWithMedia = listWithMedia
        .filter(res => res.status === 'fulfilled')
        .map(res => res.value)
      
      insuranceList.value = insuranceWithMedia
      console.log('最终保险列表（含媒体）：', insuranceList.value)
      
      // 3. 检测每个保险的推荐图是否有效，过滤失效项
      const validList = []
      for (const insurance of insuranceWithMedia) {
        const imgPath = getDynamicImgPath(insurance.mediaList)
        const isImgValid = await checkImageValid(imgPath)
        if (isImgValid) {
          validList.push(insurance) // 仅保留图片有效的保险
        } else {
          console.log(`保险 ${insurance.id} 推荐图失效，已过滤`)
        }
      }
      validInsuranceList.value = validList
      
    } else {
      ElMessage.error(`获取保险列表失败：${res.msg || '未知错误'}`)
      requestFailed.value = true
    }
  } catch (err) {
    console.error('请求保险列表异常：', err)
    ElMessage.error('网络异常，获取保险列表失败')
    requestFailed.value = true
  } finally {
    loading.value = false
    clearTimeout(debounceTimer)
  }
}, 300)

// 页面加载时触发请求
onMounted(() => {
  fetchInsuranceList()
})
</script>

<style scoped>
/* 原有样式全部保留，无额外修改 */
.loading {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 14px;
}
.request-failed {
  text-align: center;
  padding: 20px;
  color: #ff6b6b;
  font-size: 14px;
}
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}
.pet-guarantee {
  background-color: #f2f2f0;
  width: 100%;
}
.rounded {
  border-radius: 12px;
  overflow: hidden;
}
.banner {
  position: relative;
  width: 100%;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-text {
  position: absolute;
  top: 20px;
  left: 20px;
  color: #fff;
}
.banner-text h1 {
  font-size: 60px;
  line-height: 1.2;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}
.tag {
  position: absolute;
  top: 150px;
  right: -50px;
  background-color: #ffcc00;
  color: #333;
  font-size: 24px;
  font-weight: bold;
  padding: 5px 15px;
  border-radius: 20px;
  transform: rotate(15deg);
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}
.highlight-area {
  background-color: #7667ea;
  color: #fff;
  padding: 15px;
  margin-bottom: 15px;
}
.highlight-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0  10px;
}
.highlight-list {
  display: flex;
  justify-content: space-between;
}
.highlight-item {
  display: flex;
  align-items: center;
}
.check-icon {
  width: 20px;
  height: 20px;
  margin-right: 5px;
}
.highlight-item span {
  font-size: 14px;
}
.package-area {
  display: flex;
  justify-content: space-around;
  padding: 0 15px 15px;
  gap: 10px;
}
.package-item {
  flex: 1;
  min-width: 0;
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s ease-in-out;
  border: 2px solid transparent;
  cursor: pointer;
}
.package-item.recommended {
  background-color: #fff9e6;
}
.package-item.active {
  border-color: #4186e8;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(65, 134, 232, 0.3);
  z-index: 10;
}
.package-item.active .medical-fee {
  color: #ff9900;
}
.package-tag {
  background-color: #ffcc00;
  color: #333;
  font-size: 12px;
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 15px;
  margin-bottom: 10px;
  display: inline-block;
}
.medical-fee {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 10px 0;
  transition: color 0.3s ease;
}
.start-claim, .reimbursement {
  font-size: 16px;
  color: #666;
  margin: 5px 0;
}
.subsidy, .service {
  font-size: 14px;
  color: #999;
  margin: 8px 0;
  line-height: 1.5;
}
.price {
  margin-top: 20px;
}
.price-text {
  font-size: 20px;
  font-weight: bold;
  color: #ff9900;
}
.action-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 15px 20px;
  position: relative;
}
.main-btn {
  text-align: center;
  text-decoration: none;
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 30px;
  padding: 15px 60px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
.main-btn:hover {
  background-color: #3076d8;
}
.insurance-list {
  background-color: white;
  margin: 15px 0;
}
.top-more{
  padding: 0 10px;
}
.insurance-item {
  margin-bottom: 20px;
  background-color: #f4f9fd;
  padding: 10px;
}
.insurance-subtitle {
  font-size: 18px;
  font-weight: bold;
  color: #4186e8;
  margin: 8px 0;
  padding-left: 10px;
  z-index: 99;
  position: relative;
}
.insurance-desc {
  padding-left: 10px;
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
  z-index: 99;
  position: relative;
}
.insurance-card {
  padding-left: 10px;
  display: flex;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
.insurance-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  margin-top: 10px;
}
.insurance-info {
  flex: 1;
  padding: 15px;
}
.insurance-info h3 {
  font-size: 18px;
  margin: 0 0 8px;
}
.insurance-amount, .insurance-scope {
  font-size: 14px;
  color: #999;
  margin: 5px 0;
}
.insurance-scope{
  display: inline-block;
  padding: 2px 8px;
  border-radius: 20px;
  background-color: #fff9e6;
  color: #ffa44b;
  font-size: 14px;
  margin: 5px 0;
  width: auto;
}
.insurance-tag {
  background-color: #fff9e6;
  color: #8e3f0f;
  font-weight: bold;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 15px;
  display: inline-block;
  margin-bottom: 10px;
}
.insurance-price-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.insurance-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff6b6b;
  margin: 0;
}
.insurance-bonus {
  font-size: 12px;
  color: #ff9900;
  background-color: #fff9e6;
  padding: 3px 8px;
  border-radius: 15px;
  margin: 0;
}
.insurance-btn {
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
}
.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}
</style>