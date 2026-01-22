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
              <span class="price-text">18.00元/月起</span>
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
              <span class="price-text">34.75元/月起</span>
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
              <span class="price-text">68.42元/月起</span>
            </div>
          </div>
        </div>
        <!-- 行动按钮区域 -->
        <div class="action-btn">
          <router-link to="/policy-detail" class="main-btn">去看看</router-link>
          <button class="free-btn">免费体验</button>
        </div>
      </div>

      <!-- 责任/意外保险区域（从数据库遍历 id ≥ 5 的保险） -->
      <div class="insurance-list rounded">
        <div class="top-more">
          <h3>更多推荐</h3>
        </div>
        <!-- 遍历数据库中 id ≥ 5 的保险产品 -->
        <div 
          class="insurance-item rounded"
          v-for="item in insuranceList" 
          :key="item.id"
        >
          <!-- img_remark 括号外文字 -->
          <p class="insurance-subtitle">
            {{ getRemarkOutside(getContentType4Remark(item.mediaList)) }}
          </p>
          <!-- img_remark 括号内文字 -->
          <p class="insurance-desc">
            {{ getRemarkInside(getContentType4Remark(item.mediaList)) || '暂无描述' }}
          </p>
          <div class="insurance-card">
            <!-- ========== 动态获取mediaList里的图片路径 ========== -->
            <img 
              :src="getInsuranceImgUrl(getDynamicImgPath(item.mediaList))" 
              :alt="item.insuranceName" 
              class="insurance-img rounded" 
              @error="(e) => e.target.src = 'https://via.placeholder.com/120x120?text=暂无图片'"
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
        <!-- 兜底：无数据时显示 -->
        <div v-if="insuranceList.length === 0" class="no-data">
          暂无更多推荐的保险产品
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getInsurancePage, getInsuranceImgUrl, getInsuranceMediaList } from '@/api/user/index.js'

const router = useRouter()
const activePackage = ref(1)
const insuranceList = ref([])

const goToInsuranceDetail = (insuranceId) => {
  // 跳转到保险详情页
  router.push({
    path: '/policy-detail-more', // 替换成你的详情页路由路径
    query: { id: insuranceId } // 携带保险ID
  })

}

// 点击套餐项
const handlePackageClick = (index) => {
  activePackage.value = index
}

// 数字换算：元 → 万（如 240000 → 24万）
const formatToWan = (num) => {
  if (!num || isNaN(Number(num))) return '0'
  const value = Number(num) / 10000
  return value.toFixed(1).replace(/\.0$/, '')
}

// 月付价格换算：总保费 / 保障周期（月）
const formatMonthlyPrice = (totalPremium, guaranteeCycle) => {
  if (!totalPremium || !guaranteeCycle || isNaN(Number(totalPremium)) || isNaN(Number(guaranteeCycle)) || Number(guaranteeCycle) <= 0) {
    return '0.00'
  }
  const monthly = Number(totalPremium) / Number(guaranteeCycle)
  return monthly.toFixed(2)
}

// ============== 新增：动态获取图片路径（仅新增这一个函数） ==============
const getDynamicImgPath = (mediaList) => {
  if (!mediaList || !Array.isArray(mediaList) || mediaList.length === 0) return '';
  // 优先取contentType=4的图片（推荐图），没有则取第一个有路径的图片
  const type4Item = mediaList.find(item => item.contentType === 4 && item.imgPath);
  if (type4Item) return type4Item.imgPath;
  // 兜底：取第一个有路径的图片
  const firstValidItem = mediaList.find(item => item.imgPath);
  return firstValidItem ? firstValidItem.imgPath : '';
};

// ============== 核心：提取 content_type=4 的 img_remark ==============
// 从 mediaList 中找到 content_type=4 的记录，并返回其 img_remark
const getContentType4Remark = (mediaList) => {
  if (!mediaList || !Array.isArray(mediaList)) return ''
  // 筛选 content_type=4 的媒体记录
  const type4Item = mediaList.find(item => item.contentType === 4)
  return type4Item ? type4Item.imgRemark : ''
}

// 从 img_remark 提取括号外文字（如“004产品特色”）
const getRemarkOutside = (remark) => {
  if (!remark || !remark.includes('（')) return remark || ''
  return remark.split('（')[0].trim()
}

// 从 img_remark 提取括号内文字（如“猫咪的专属医保”）
const getRemarkInside = (remark) => {
  if (!remark || !remark.includes('（') || !remark.includes('）')) return ''
  const start = remark.indexOf('（') + 1
  const end = remark.indexOf('）')
  return remark.substring(start, end).trim()
}

// 页面加载时，获取 id ≥ 5 的上架保险（含 mediaList）
onMounted(async () => {
  try {
    // 调用详情接口（含 mediaList），遍历 id ≥ 5
    const res = await getInsurancePage(1, 10, '', undefined, 1)
    if (res.code === 200) {
      const allInsurances = res.data.records
      // 过滤 id ≥ 5
      const filtered = allInsurances.filter(item => item.id >= 5)
      // 为每个保险补充 mediaList（调用媒体列表接口）
      const listWithMedia = await Promise.all(
        filtered.map(async (item) => {
          try {
            const mediaRes = await getInsuranceMediaList(item.id)
            if (mediaRes.code === 200) {
              return { ...item, mediaList: mediaRes.data }
            }
          } catch (err) {
            console.error(`获取保险 ${item.id} 媒体列表失败：`, err)
          }
          return { ...item, mediaList: [] }
        })
      )
      insuranceList.value = listWithMedia
      console.log('保险列表（含 mediaList，id≥5）：', insuranceList.value)
    } else {
      console.error('获取保险列表失败：', res.msg)
    }
  } catch (err) {
    console.error('请求保险列表异常：', err)
  }
})
</script>

<style scoped>
/* 原有样式全部保留，只添加兜底样式 */
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
.package-item.active .start-claim,
.package-item.active .reimbursement,
.package-item.active .subsidy,
.package-item.active .service {
  color: #000;
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
.free-btn {
  background-color: #ff3333;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  position: absolute;
  top: -10px;
  right: 20px;
}

/* 保险列表区域（原有样式不变） */
.top-more{
  padding: 0 10px;
}
.insurance-list {
  background-color: white;
  margin: 15px 0;
}
.insurance-item {
  margin-bottom: 20px;
  background-color: #f4f9fd;
}
.insurance-subtitle {
  font-size: 18px;
  font-weight: bold;
  color: #4186e8;
  margin: 8px 0;
  padding-left: 10px;
}
.insurance-desc {
  padding-left: 10px;
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
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
  /* 关键：让元素宽度贴合文字（默认是block占满整行） */
  display: inline-block;
  /* 可选：加少量内边距，让文字和边框有间距，更美观 */
  padding: 2px 8px;
  /* 圆角调整：用px或%都可以，25%改成50px（椭圆圆角）/ 8px（小圆角）更自然 */
  border-radius: 20px; /* 推荐值，比25%更圆润，适配不同文字长度 */
  /* 保留你的原有样式 */
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

/* 兜底：无数据样式 */
.no-data {
  text-align: center;
  padding: 20px;
  color: #999;
  font-size: 14px;
}
</style>