<template>
  <div class="page-container">
    <div class="pet-home">
      <!-- 顶部搜索栏 -->
      <div class="search-bar">
        <input type="text" placeholder="搜索想要了解的养宠知识" />
      </div>

      <!-- 宠物身份卡推广 -->
      <div class="promo-card">
        <div class="promo-text">
          <h2>添加宠物领取专属身份证</h2>
          <p>专属身份ID · 鼻纹防丢保护 · 看病买药优惠</p>
          <button class="btn-blue" @click="goToPetCard">去添加</button>
        </div>
        <img src="@/assets/images/首页图标/首页顶部图.png" class="promo-img" />
      </div>

      <!-- 功能入口 -->
      <div class="function-icons">
        <div class="icon-item">
          <div class="icon bg-orange">
            <img src="@/assets/images/首页图标/wenzhen.svg" alt="智能问诊" class="icon-img" />
          </div>
          <span>智能问诊</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-blue">
            <img src="@/assets/images/首页图标/baike-01.svg" alt="疾病百科" class="icon-img" />
          </div>
          <span>疾病百科</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-green">
            <img src="@/assets/images/首页图标/yiyuan.svg" alt="找医院" class="icon-img" />
          </div>
          <span>找医院</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-purple">
            <img src="@/assets/images/首页图标/zhishi.svg" alt="新手知识" class="icon-img" />
          </div>
          <span>新手知识</span>
        </div>
      </div>

      <!-- 宠物医保推广 -->
      <div class="insurance-header">
        <div class="header-title">
          <h2>宠物医保</h2>
          <p>日常保健可报销</p>
        </div>
        <div class="insurance-promo-wrapper">
          <div class="insurance-promo">
            <div class="promo-left">
              <img src="@/assets/images/首页图标/宠物医保.jpg" class="promo-main-img" />
              <div class="promo-overlay">
                <h3>小猫小狗也有医保啦</h3>
                <p class="highlight-text">日常保健 也能报销</p>
                <p>看病 | 买药 | 服务可报销</p>
                <button class="btn-pink" @click="handleGoToDetail">领取医保</button>
              </div>
            </div>
            <div class="promo-right">
              <!-- 关键修复1：添加v-if确保数据存在，避免渲染null/undefined -->
              <div class="product-grid" v-if="Array.isArray(randomMallProducts) && randomMallProducts.length > 0">
                <div class="product-item" v-for="(product, index) in randomMallProducts" :key="index">
                  <!-- 关键修复2：使用可选链和默认值，避免读取null属性 -->
                  <img :src="getImgUrl(product?.imgPath || '')" :alt="product?.title || '商品图片'" class="product-img" />
                  <div class="product-mask">
                    <!-- 关键修复3：添加防呆处理，product.id存在才执行 -->
                    <button class="add-cart-btn" @click="product?.id && handleAddToCart(product.id)">加入购物车</button>
                  </div>
                  <p class="product-name">{{ product?.title || '未知商品' }}</p>
                  <span class="insurance-tag">
                    <span class="tag-type">医保</span>
                    <span class="tag-desc">立省{{ calculateSave(product || {}) }}元</span>
                  </span>
                </div>
              </div>
              <!-- 加载占位 -->
              <div v-else class="product-loading">
                <p>加载商品中...</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部推荐区域-->
      <div class="recommend-section">
        <div class="section-header">
          <h2>萌宠治愈时刻</h2>
          <p>每只毛孩子都值得被宠爱</p>
        </div>

        <div v-if="recommendList.length === 0 && !isLoading" class="empty-state">
          <p>正在加载萌宠内容...</p>
        </div>

        <div v-else class="recommend-grid">
          <div class="recommend-item" v-for="item in recommendList" :key="item._id">
            <img :src="item.url" alt="萌宠图片" class="recommend-img" />
            <div class="item-info">
              <h3>{{ item.author }}</h3>
              <p>{{ item.content }}</p>
            </div>
          </div>
        </div>

        <!-- 加载提示 -->
        <div v-if="isLoading" class="loading-state">
          <p>加载更多萌宠中...</p>
        </div>

        <!-- 没有更多数据的提示 -->
        <div v-if="!hasMore && recommendList.length > 0" class="no-more-state">
          <p>没有更多萌宠了～</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watchEffect } from 'vue' // 新增 watchEffect
import axios from 'axios'
import { useRouter } from 'vue-router'
import { getProductList, addToCart } from '@/api/user/index.js'
import { ElMessage } from 'element-plus' 

// ========== 基础数据定义 ==========
const recommendList = ref([])
const isLoading = ref(false)
const hasMore = ref(true)
const router = useRouter()

const allMallProducts = ref([])
const randomMallProducts = ref([])

// 把 userId 改为响应式变量，确保登录后能实时更新
const userId = ref(1)
// 新增：单独维护登录状态，避免依赖 userId 判断
const isLogin = ref(false)

// 关键修复2：封装登录状态读取函数，统一逻辑
const checkLoginStatus = () => {
  try {
    const userInfo = localStorage.getItem('userInfo')
    // 兼容可能存储的 userData 键名（很多项目会用这个）
    const userData = localStorage.getItem('userData') || userInfo
    
    if (userData) {
      const parsed = JSON.parse(userData)
      // 兼容多种字段名：userId/id/user_id
      userId.value = parsed.userId || parsed.id || parsed.user_id || 1
      isLogin.value = true // 明确标记已登录
      console.log('当前登录用户ID：', userId.value) // 调试用，可删除
    } else {
      userId.value = 1
      isLogin.value = false
    }
  } catch (e) {
    console.error('解析用户信息失败：', e)
    userId.value = 1
    isLogin.value = false
  }
}

// 关键修复3：立即执行一次，初始化登录状态
checkLoginStatus()

// 关键修复4：监听 localStorage 变化，实时更新登录状态
watchEffect(() => {
  // 监听 userInfo/userData 变化（比如登录后存储）
  checkLoginStatus()
})

// ========== 工具函数 ==========
const formatData = (data) => {
  return (data || []).map(item => ({
    _id: item._id || item.id || Math.random().toString(36).slice(2),
    url: item.url || '',
    author: item.author || '未知作者',
    content: item.content || '暂无描述'
  }))
}

const getImgUrl = (imgPath) => {
  if (!imgPath || typeof imgPath !== 'string' || imgPath.trim() === '') {
    return '/assets/images/default-product.png';
  }

  const path = imgPath.replace(/^@\//, '/src/')
  const formattedPath = path.replace(/\\/g, '/').replace(/^\/+/, '')
  
  if (formattedPath.startsWith('http')) {
    return formattedPath
  }
  return `http://localhost:8080/${formattedPath}`
}

const calculateSave = (product) => {
  const nowPrice = Number((product?.nowPrice || '0').replace(/[^\d.]/g, ''))
  const oldPrice = Number((product?.oldPrice || '0').replace(/[^\d.]/g, ''))
  return (oldPrice - nowPrice).toFixed(2)
}

// ========== 核心方法 ==========
const handleAddToCart = async (productId) => {
  if (!productId || isNaN(Number(productId))) {
    ElMessage.error('商品ID异常，无法加入购物车')
    return
  }

  try {
    // 关键修复5：改用独立的 isLogin 变量判断，不再依赖 userId
    if (!isLogin.value) {
      ElMessage.warning('请先登录后再加入购物车')
      nextTick(() => {
        router.push('/my') // 确保 /my 是登录页面路由
      })
      return
    }

    // 调用接口（添加超时控制）
    const result = await Promise.race([
      addToCart(userId.value, productId, 1), // 注意：userId 现在是响应式，要加 .value
      new Promise((_, reject) => setTimeout(() => reject(new Error('请求超时')), 10000))
    ])

    if (typeof result === 'string') {
      if (result.includes('成功') || result === 'success' || result === '添加成功') {
        ElMessage.success('加入购物车成功！')
        nextTick(() => {
          router.push({ path: '/mall' })
        })
      } else {
        ElMessage.error(`加入购物车失败：${result}`)
      }
    } else {
      ElMessage.error('加入购物车失败，返回数据格式异常')
    }
  } catch (err) {
    console.error('加入购物车失败：', err)
    ElMessage.error(err?.msg || err?.message || '加入购物车失败，请稍后重试')
  }
}

const handleGoToDetail = () => {
  nextTick(() => {
    router.push('/guarantee')
  })
}
const goToPetCard = () => {
  router.push({
    path: '/pet-id-card',
    query: { userId: userId.value }
  })
}
// ========== 数据加载 ==========
const fetchData = async (pageSize = 6) => {
  try {
    const res = await axios.get('https://tea.qingnian8.com/tools/petShow', {
      headers: { 'access-key': '925255' },
      params: { size: pageSize, type: 'all', 'access-key': 'lp9899' },
      timeout: 10000
    })
    if (res.data.errCode === 0) {
      const formattedData = formatData(res.data.data)
      hasMore.value = formattedData.length >= pageSize
      return formattedData
    }
    return []
  } catch (err) {
    console.error('获取萌宠数据失败：', err)
    return []
  }
}

const loadMore = async () => {
  if (isLoading.value || !hasMore.value) return
  isLoading.value = true

  try {
    const newData = await fetchData(3)
    if (newData.length > 0) {
      recommendList.value = [...recommendList.value, ...newData]
    } else {
      hasMore.value = false
    }
  } finally {
    isLoading.value = false
  }
}

const loadMallProducts = async () => {
  try {
    const productData = await getProductList()
    allMallProducts.value = Array.isArray(productData) ? productData : []
    
    if (allMallProducts.value.length > 0) {
      const shuffled = [...allMallProducts.value].sort(() => 0.5 - Math.random())
      randomMallProducts.value = shuffled.slice(0, 6)
    } else {
      randomMallProducts.value = [
        { id: 1, title: '进口狂犬疫苗', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '88', oldPrice: '104' },
        { id: 2, title: '大宠爱驱虫药', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '128', oldPrice: '158' },
        { id: 3, title: '猫咪化毛膏', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '45', oldPrice: '68' },
        { id: 4, title: '狗狗钙片', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '39', oldPrice: '59' },
        { id: 5, title: '宠物益生菌', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '58', oldPrice: '88' },
        { id: 6, title: '猫砂除臭珠', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '29', oldPrice: '45' }
      ]
    }
  } catch (err) {
    console.error('加载商城商品失败：', err)
    ElMessage.error('获取商城商品失败，显示默认商品')
    randomMallProducts.value = [
      { id: 1, title: '进口狂犬疫苗', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '88', oldPrice: '104' },
      { id: 2, title: '大宠爱驱虫药', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '128', oldPrice: '158' },
      { id: 3, title: '猫咪化毛膏', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '45', oldPrice: '68' },
      { id: 4, title: '狗狗钙片', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '39', oldPrice: '59' },
      { id: 5, title: '宠物益生菌', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '58', oldPrice: '88' },
      { id: 6, title: '猫砂除臭珠', imgPath: '@/assets/images/首页图标/狂犬疫苗.jpg', nowPrice: '29', oldPrice: '45' }
    ]
  }
}

// ========== 生命周期 ==========
onMounted(async () => {
  // 关键修复6：页面挂载时重新检查登录状态（防止登录后页面没刷新）
  checkLoginStatus()
  
  await loadMallProducts()
  
  const initialData = await fetchData(6)
  recommendList.value = initialData.length > 0 ? initialData : formatData([
    { id: 1, url: 'https://picsum.photos/seed/cat1/600/400', author: '爱宠基地联盟', content: '是个黏人的崽，颜值真的没话说' },
    { id: 2, url: 'https://picsum.photos/seed/dog1/600/400', author: '汪星人日记', content: '快乐小狗，治愈每一天' },
    { id: 3, url: 'https://picsum.photos/seed/cat2/600/400', author: '喵星人部落', content: '慵懒又可爱，谁能不爱呢' },
    { id: 4, url: 'https://picsum.photos/seed/dog2/600/400', author: '短腿柯基控', content: '短腿柯基，萌化你的心' },
    { id: 5, url: 'https://picsum.photos/seed/cat3/600/400', author: '猫咪日常', content: '安静乖巧，是理想的陪伴' },
    { id: 6, url: 'https://picsum.photos/seed/dog3/600/400', author: '金毛寻回', content: '金毛寻回，暖心又忠诚' }
  ])
  hasMore.value = initialData.length >= 6

  const debounceHandleScroll = debounce(() => {
    if (window.innerHeight + document.documentElement.scrollTop >= document.documentElement.offsetHeight - 200) {
      loadMore()
    }
  }, 200)
  window.addEventListener('scroll', debounceHandleScroll)
  
  window._petHomeScrollHandler = debounceHandleScroll
})

onUnmounted(() => {
  window.removeEventListener('scroll', window._petHomeScrollHandler)
  delete window._petHomeScrollHandler
})

// 防抖工具函数
const debounce = (fn, delay) => {
  let timer = null
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}
</script>

<style scoped>
/* 基础样式（保持不变，新增商品加载占位样式） */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

.pet-home {
  padding: 16px;
}

.search-bar {
  margin-bottom: 20px;
}

.search-bar input {
  width: 100%;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.promo-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #64b5f6, #7e57c2);
  border-radius: 12px;
  color: #fff;
  padding: 20px;
  margin-bottom: 20px;
}

.promo-text {
  flex: 1;
}

.promo-img {
  width: 150px;
  height: auto;
  object-fit: contain;
  margin-left: auto;
}

.function-icons {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 23%;
}

.icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.bg-orange { background: #f4e49c; }
.bg-blue { background: #75ccf3; }
.bg-green { background: #c5f39e; }
.bg-purple { background: #d3adf6; }

.btn-blue {
  background: #2196f3;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
}

.function-icons .icon-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}

.insurance-header {
  background: #ffebee;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
  padding: 10px;
}

.header-title {
  padding: 0 16px 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title h2 {
  font-size: 18px;
  color: #e53935;
  margin: 0;
}

.header-title p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.insurance-promo-wrapper {
  padding: 0 6px 6px;
}

.insurance-promo {
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  background: #fff;
}

.promo-left {
  position: relative;
  width: 45%;
}

.promo-main-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.promo-right {
  width: 55%;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.promo-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  padding: 16px;
  color: #fff;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  width: calc(100%);
}

.highlight-text {
  font-size: 15px;
  margin: -5px 0;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.btn-pink {
  background: #ff8a80;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* 关键：商品加载占位样式 */
.product-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  font-size: 14px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, auto);
  gap: 12px;
}

.product-item {
  background: #fff;
  border-radius: 8px;
  padding: 8px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s ease-in-out;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  position: relative;
  overflow: hidden;
}

.product-item img {
  width: 100%;
  max-width: 100px;
  height: 80px;
  object-fit: contain;
  display: block;
  margin: 0 auto 8px;
}

.product-name {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: normal;
  font-size: 14px;
  margin: 8px 0;
  line-height: 1.4;
}

.product-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 8px;
}

.product-item:hover .product-mask {
  opacity: 1;
}

.add-cart-btn {
  background: #e53935;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s;
}

.add-cart-btn:hover {
  background: #d32f2f;
}

.insurance-tag {
  align-items: center;
  background: #fff;
  border-radius: 4px;
  padding: 3px 8px;
  font-size: 12px;
  color: #e53935;
}

.tag-type {
  background: #e53935;
  color: #fff;
  border-radius: 3px;
  padding: 1px 4px;
  margin-right: 4px;
  font-size: 10px;
}

.tag-desc {
  font-weight: 500;
}

.recommend-section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header {
  margin-bottom: 16px;
}

.section-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0 0 6px;
}

.section-header p {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.empty-state, .loading-state, .no-more-state {
  text-align: center;
  padding: 20px 0;
  color: #999;
  font-size: 14px;
}

.recommend-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.recommend-item {
  background: #f9f9f9;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
}

.recommend-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.recommend-img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.item-info {
  padding: 12px;
}

.item-info h3 {
  font-size: 16px;
  margin: 0 0 4px;
  color: #333;
}

.item-info p {
  font-size: 14px;
  color: #666;
  margin: 0;
}
</style>