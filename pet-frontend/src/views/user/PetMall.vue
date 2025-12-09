<template>
  <div class="page-container">
    <div class="pet-insurance-mall-page">
      <!-- 顶部搜索与Banner区域 -->
      <div class="top-section">
        <div class="search-bar">
          <input type="text" placeholder="大宠爱驱虫药" class="search-input" />
          <button class="search-btn">搜索</button>
        </div>
        <div class="banner">
          <img src="@/assets/images/商城图标/顶部图.webp" alt="宠物医保Banner" class="banner-img" />
          <div class="banner-text">
            <h1>开通宠物医保<br>立享优惠价</h1>
            <ul class="banner-features">
              <li><span class="feature-label">看病报销</span> 定点医院 70%</li>
              <li><span class="feature-label">消费补贴</span> 每月200额度</li>
              <li><span class="feature-label">增值服务</span> 驱虫 + 疫苗</li>
            </ul>
            <div class="yearly-sub">全年消费补贴 2400</div>
            <button class="free-experience-btn">免费体验</button>
          </div>
        </div>
      </div>

      <!-- 爆品推荐轮播区域 -->
      <div class="hot-recommends rounded">
        <h2 class="section-title">爆品推荐</h2>
        <div class="carousel-container" @mouseenter="stopCarousel" @mouseleave="startCarousel">
          <div
            class="carousel-track"
            :style="{ transform: `translateX(${currentPosition}px)` }"
          >
            <div class="product-list">
              <!-- 主商品列表 -->
              <div
                class="product-item"
                v-for="(product, index) in randomProducts"
                :key="index"
              >
                <img
                  :src="getImgUrl(product.imgPath)"
                  alt="产品图片"
                  class="product-img"
                />
                <p class="product-title">{{ product.title }}</p>
                <p class="insurance-save">医保立省{{ calculateSave(product) }}元</p>
                <div class="product-price">
              <span class="insurance-price">
                <span class="price-now">{{ product.nowPrice }}</span>
                <button class="buy-btn">抢</button>
              </span>
                </div>
              </div>
              <!-- 复制一份用于无缝滚动 -->
              <div
                class="product-item"
                v-for="(product, index) in randomProducts"
                :key="'copy-' + index"
              >
                <img
                  :src="getImgUrl(product.imgPath)"
                  alt="产品图片"
                  class="product-img"
                />
                <p class="product-title">{{ product.title }}</p>
                <p class="insurance-save">医保立省{{ calculateSave(product) }}元</p>
                <div class="product-price">
              <span class="insurance-price">
                <span class="price-now">{{ product.nowPrice }}</span>
                <button class="buy-btn">抢</button>
              </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分类导航区域 -->
      <div class="category-nav">
        <div class="main-category-tabs">
          <div
            class="main-tab"
            :class="{ active: currentMainCategory === 'cat' }"
            @click="handleMainTabClick('cat')"
          >
          猫咪站
        </div>
        <div
          class="main-tab"
          :class="{ active: currentMainCategory === 'dog' }"
          @click="handleMainTabClick('dog')"
        >
        狗狗站
      </div>
    </div>

    <!-- 猫咪子分类 -->
        <div class="sub-category-tabs" v-if="currentMainCategory === 'cat'">
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === 'all' }"
            data-sub="all"
            @click="handleSubTabClick('all')"
          >
            全部
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '猫咪主粮' }"
            data-sub="猫咪主粮"
            @click="handleSubTabClick('猫咪主粮')"
          >
            主粮
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '猫咪零食' }"
            data-sub="猫咪零食"
            @click="handleSubTabClick('猫咪零食')"
          >
            零食
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '猫咪护理' }"
            data-sub="猫咪护理"
            @click="handleSubTabClick('猫咪护理')"
          >
            护理
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '猫咪保健' }"
            data-sub="猫咪保健"
            @click="handleSubTabClick('猫咪保健')"
          >
            保健
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '猫咪清洁' }"
            data-sub="猫咪清洁"
            @click="handleSubTabClick('猫咪清洁')"
          >
            清洁
          </div>
        </div>

        <!-- 狗狗子分类 -->
        <div class="sub-category-tabs" v-else-if="currentMainCategory === 'dog'">
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === 'all' }"
            data-sub="all"
            @click="handleSubTabClick('all')"
          >
            全部
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '狗狗主粮' }"
            data-sub="狗狗主粮"
            @click="handleSubTabClick('狗狗主粮')"
          >
            主粮
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '狗狗零食' }"
            data-sub="狗狗零食"
            @click="handleSubTabClick('狗狗零食')"
          >
            零食
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '狗狗护理' }"
            data-sub="狗狗护理"
            @click="handleSubTabClick('狗狗护理')"
          >
            护理
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '狗狗保健' }"
            data-sub="狗狗保健"
            @click="handleSubTabClick('狗狗保健')"
          >
            保健
          </div>
          <div
            class="sub-tab"
            :class="{ active: currentSubCategory === '狗狗生活' }"
            data-sub="狗狗生活"
            @click="handleSubTabClick('狗狗生活')"
          >
            用品
          </div>
        </div>
        <div class="sort-tabs">
          <div
            class="tab"
            :class="{ active: currentSort === 'recommend' }"
            @click="handleSortTabClick('recommend')"
          >
          推荐
        </div>
        <div
          class="tab"
          :class="{ active: currentSort === 'priceAsc' }"
          @click="handleSortTabClick('priceAsc')"
        >
        价格↑
      </div>
          <div
            class="tab"
            :class="{ active: currentSort === 'priceDesc' }"
            @click="handleSortTabClick('priceDesc')"
          >
            价格↓
          </div>
    </div>
      </div>
      <!-- 商品内容展示区 -->
      <div class="product-content">
        <div class="product-grid">
          <div
            class="product-item"
            v-for="(product, index) in filteredProducts"
            :key="product.id"
            @mouseenter="activeProduct = index"
            @mouseleave="activeProduct = -1"
          >
            <img
              :src="getImgUrl(product.imgPath)"
              alt="product.title"
              class="product-img"
              :class="{ 'scale-center': activeProduct === index }"
            />
            <p class="product-title">{{ product.title }}</p>
            <p class="product-desc">{{ product.description || '无详细描述' }}</p>
            <div class="product-price">
              <span class="insurance-price">
                <span class="insurance-tag">医保价</span>
                <span class="price-now">{{ product.nowPrice }}</span>
              </span>
              <span class="original-price">
                <span class="price-old">{{ product.oldPrice }}</span>
              </span>
            </div>
          </div>
          <div v-if="filteredProducts.length === 0" class="no-product">
            暂无该分类商品~
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'

// 所有商品数据（核心数据源）
const allProducts = ref([])
// 随机选取的8个爆品商品
const randomProducts = ref([])
// 筛选后的商品（分类/排序后展示）
const filteredProducts = ref([])
// 激活状态（商品hover效果）
const activeProduct = ref(-1)
// 当前选中的分类和排序
const currentMainCategory = ref('dog') // 默认狗狗站
const currentSubCategory = ref('all') // 默认全部子分类
const currentSort = ref('recommend') // 默认推荐排序
// 轮播相关状态
const currentPosition = ref(0) // 轮播滚动位置（像素）
const itemWidth = ref(0) // 单个商品宽度（含间距）
const carouselTimer = ref(null) // 轮播定时器
const isTransitioning = ref(false) // 轮播过渡状态

// 处理图片路径（适配后端资源映射）
const getImgUrl = (imgPath) => {
  if (!imgPath) return '/assets/images/default-product.png'
  const relativePath = imgPath.split('productimg\\')[1]?.replace(/\\/g, '/')
  return relativePath ? `http://localhost:8080/product-images/${relativePath}` : '/assets/images/default-product.png'
}

// 计算医保节省金额
const calculateSave = (product) => {
  const nowPrice = Number(product.nowPrice.replace(/[^\d.]/g, ''))
  const oldPrice = Number(product.oldPrice.replace(/[^\d.]/g, ''))
  return (oldPrice - nowPrice).toFixed(2)
}

// 从所有商品中随机选取8个作为爆品
const initRandomProducts = () => {
  if (allProducts.value.length === 0) return
  const shuffled = [...allProducts.value].sort(() => 0.5 - Math.random())
  randomProducts.value = shuffled.slice(0, 8)
}

// 加载商品数据（从后端接口获取）
const loadProducts = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/products/list')
    allProducts.value = res.data
    initRandomProducts()
    filterProducts()
  } catch (err) {
    console.error('加载商品失败：', err)
  }
}

// 筛选商品（主分类+子分类+排序）
const filterProducts = () => {
  let result = [...allProducts.value]

  // 1. 主分类筛选（狗狗/猫咪）
  if (currentMainCategory.value === 'dog') {
    result = result.filter(product => product.mainCategory?.startsWith('狗狗'))
  } else if (currentMainCategory.value === 'cat') {
    result = result.filter(product => product.mainCategory?.startsWith('猫咪'))
  }

  // 2. 子分类筛选（全部/主粮/零食等）
  if (currentSubCategory.value !== 'all') {
    result = result.filter(product => product.mainCategory === currentSubCategory.value)
  }

  // 3. 排序处理
  result = sortProducts(result, currentSort.value)

  filteredProducts.value = result
}

// 商品排序逻辑
const sortProducts = (products, sortType) => {
  const sorted = [...products]
  switch (sortType) {
    case 'priceAsc': // 价格升序
      return sorted.sort((a, b) => {
        const priceA = Number(a.nowPrice?.replace(/[^\d.]/g, '') || 0)
        const priceB = Number(b.nowPrice?.replace(/[^\d.]/g, '') || 0)
        return priceA - priceB
      })
    case 'priceDesc': // 价格降序
      return sorted.sort((a, b) => {
        const priceA = Number(a.nowPrice?.replace(/[^\d.]/g, '') || 0)
        const priceB = Number(b.nowPrice?.replace(/[^\d.]/g, '') || 0)
        return priceB - priceA
      })
    default: // 推荐（默认顺序）
      return sorted
  }
}

// 子分类点击事件（通过Vue响应式控制高亮）
const handleSubTabClick = (subCategory) => {
  currentSubCategory.value = subCategory
  filterProducts()
}

// 主分类点击事件
const handleMainTabClick = (mainCategory) => {
  currentMainCategory.value = mainCategory
  currentSubCategory.value = 'all' // 切换主分类时自动选中"全部"子分类
  currentSort.value = 'recommend' // 重置排序为推荐
  filterProducts()
}

// 排序点击事件
const handleSortTabClick = (sortType) => {
  currentSort.value = sortType
  filterProducts()
}

// 自动轮播逻辑（3秒切换一次，一次滚动4个商品）
const startCarousel = () => {
  if (carouselTimer.value) clearInterval(carouselTimer.value)

  carouselTimer.value = setInterval(() => {
    if (randomProducts.value.length < 4) return

    isTransitioning.value = true
    currentPosition.value -= itemWidth.value * 4

    const totalWidth = itemWidth.value * randomProducts.value.length
    if (Math.abs(currentPosition.value) >= totalWidth) {
      setTimeout(() => {
        isTransitioning.value = false
        currentPosition.value = 0
      }, 500)
    }
  }, 3000)
}

// 停止轮播
const stopCarousel = () => {
  if (carouselTimer.value) {
    clearInterval(carouselTimer.value)
    carouselTimer.value = null
  }
}

// 初始化商品宽度（一行4个，动态计算）
const initItemWidth = () => {
  nextTick(() => {
    const container = document.querySelector('.carousel-container')
    if (container) {
      itemWidth.value = container.offsetWidth / 4 - 20
    }
  })
}

// 页面加载初始化
onMounted(() => {
  loadProducts()
  initItemWidth()
  window.addEventListener('resize', initItemWidth)

  // 轮播初始化（等待商品数据加载完成）
  const checkProductsLoaded = setInterval(() => {
    if (randomProducts.value.length > 0) {
      startCarousel()
      clearInterval(checkProductsLoaded)
    }
  }, 100)
})

// 页面卸载时清理
onUnmounted(() => {
  stopCarousel()
  window.removeEventListener('resize', initItemWidth)
})
</script>

<style scoped>
/* 外层居中容器 */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 15px;
}

/* 基础样式 */
.pet-insurance-mall-page {
  font-family: "PingFang SC", sans-serif;
  color: #333;
  background-color: #fff;
  min-height: 100vh;
  overflow-x: hidden;
}

/* 顶部搜索与Banner */
.top-section {
  width: 100%;
}
.search-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #d0e6fb;
  border-radius: 25px;
  margin: 10px 0;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  background-color: transparent;
  font-size: 14px;
}
.search-btn {
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 25px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
}
.banner {
  position: relative;
  width: 100%;
  height: 300px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  margin: 10px 0;
}
.banner-img {
  width: 55%;
  height: 100%;
  object-fit: cover;
}
.banner-text {
  background-color: #fcfcfc;
  flex: 1;
  padding: 0 20px;
  color: #000;
}
.banner-text h1 {
  font-size: 32px;
  font-weight: bold;
  margin: 0 0 20px;
  line-height: 1.2;
}
.banner-features {
  list-style: none;
  padding: 0;
  margin: 0 0 20px;
}
.banner-features li {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}
.feature-label {
  background-color: #fff000;
  color: #333;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
  font-size: 12px;
}
.yearly-sub {
  background-color: #ffcc00;
  color: #333;
  font-size: 18px;
  font-weight: bold;
  padding: 10px;
  border-radius: 10px;
  transform: rotate(15deg);
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
  display: inline-block;
  margin-bottom: 10px;
}
.free-experience-btn {
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 12px 30px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
}

/* 爆品推荐 */
.hot-recommends {
  padding: 20px;
  background-color: #ffe9ea;
  border-radius: 12px;
}

.carousel-container:active {
  cursor: grabbing;
}
.carousel-track {
  display: flex;
  transition: transform 0.3s ease-out;
}
.product-list {
  display: flex;
  width: 200%; /* 8个商品 → 分2行，每行4个 */
}
.product-item {
  flex: 0 0 25%; /* 100% ÷ 4个 = 25% 宽度 */
  box-sizing: border-box;
  padding: 0 10px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
.product-img {
  width: 100%;
  height: 150px;
  object-fit: contain;
  margin-bottom: 10px;
}
.product-title {
  font-size: 14px;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin: 0 0 8px;
}
.insurance-save {
  font-size: 13px;
  color: #e53935;
  margin: 0 0 8px;
}
.product-price {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.insurance-price {
  display: inline-flex;
  align-items: center;
  background-color: #e53935;
  color: #fff;
  border-radius: 4px;
  padding: 3px 8px;
  font-size: 14px;
  margin-bottom: 5px;
}
.insurance-tag {
  margin-right: 4px;
  font-weight: bold;
}
.price-now {
  font-size: 16px;
  font-weight: bold;
}
.original-price {
  color: #999;
  font-size: 12px;
  text-decoration: line-through;
  margin-bottom: 5px;
}
.buy-btn {
  background-color: #e53935;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 6px 12px;
  font-size: 14px;
  cursor: pointer;
  margin-left: 8px;
}

/* 分类导航 */
.category-nav {
  width: 100%;
  background-color: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  padding: 10px 20px;
  box-sizing: border-box;
}
.main-category-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 10px;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 8px;
}
.main-tab {
  padding: 8px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  border-radius: 4px 4px 0 0;
  transition: all 0.3s ease;
}
.main-tab.active {
  color: #ff6b6b;
  border-bottom: 3px solid #ff6b6b;
}
.main-tab:hover:not(.active) {
  color: #333;
  background-color: #f0f0f0;
}
.sub-category-tabs {
  display: flex;
  gap: 2px;
  margin-bottom: 10px;
}
.sub-tab {
  padding: 6px 18px;
  font-size: 14px;
  color: #555;
  cursor: pointer;
  border-radius: 20px;
  transition: all 0.2s ease;
}
.sub-tab.active {
  background-color: #ff6b6b;
  color: white;
}
.sub-tab:hover:not(.active) {
  background-color: #f0f0f0;
  color: #333;
}
.sort-tabs {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-top: 5px;
  border-top: 1px dashed #e0e0e0;
}
.sort-tabs .tab {
  padding: 4px 12px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s ease;
}
.sort-tabs .tab:hover {
  color: #ff6b6b;
}

/* 商品内容展示 */
.product-content {
  padding: 20px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
  margin-top: 15px;
}
.product-item {
  width: 100%;
  box-sizing: border-box;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  overflow: hidden;
}
.product-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
.product-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
.product-title {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0;
  line-height: 1.4;
  height: 22px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  padding: 0 8px;
}
.product-desc {
  font-size: 14px;
  color: #666;
  margin: 6px 0;
  height: 20px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  text-overflow: ellipsis;
  padding: 0 8px;
}
.product-price {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-top: 1px solid #f5f5f5;
}
.no-product {
  grid-column: 1 / -1;
  text-align: center;
  padding: 50px 0;
  color: #666;
  font-size: 16px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .product-item {
    width: 140px;
  }
  .itemWidth {
    value: 160;
  }
  .product-img {
    height: 120px;
  }
  .product-title, .product-desc {
    font-size: 13px;
  }
  .buy-btn {
    padding: 4px 8px;
    font-size: 12px;
  }
  .main-category-tabs, .sub-category-tabs, .sort-tabs {
    flex-wrap: wrap;
  }
  .main-tab {
    padding: 6px 15px;
    font-size: 14px;
  }
  .sub-tab {
    padding: 5px 12px;
    font-size: 13px;
  }
  .product-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }
}

</style>
