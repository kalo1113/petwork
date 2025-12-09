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
          <button class="btn-blue">去添加</button>
        </div>
        <img src="@/assets/images/首页图标/首页顶部图.png" class="promo-img" />
      </div>

      <!-- 功能入口 -->
      <div class="function-icons">
        <div class="icon-item">
          <div class="icon bg-orange">
            <!-- 智能问诊图标 -->
            <img src="@/assets/images/首页图标/wenzhen.svg" alt="智能问诊" class="icon-img" />
          </div>
          <span>智能问诊</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-blue">
            <!-- 疾病百科图标 -->
            <img src="@/assets/images/首页图标/baike-01.svg" alt="疾病百科" class="icon-img" />
          </div>
          <span>疾病百科</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-green">
            <!-- 找医院图标 -->
            <img src="@/assets/images/首页图标/yiyuan.svg" alt="找医院" class="icon-img" />
          </div>
          <span>找医院</span>
        </div>
        <div class="icon-item">
          <div class="icon bg-purple">
            <!-- 新手知识图标 -->
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
                <button class="btn-pink">领取医保</button>
              </div>
            </div>
            <div class="promo-right">
              <div class="product-grid">
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
                <div class="product-item">
                  <img src="@/assets/images/首页图标/狂犬疫苗.jpg" alt="进口狂犬疫苗" class="product-img" />
                  <p class="product-name">进口狂犬疫苗</p>
                  <span class="insurance-tag">
                   <span class="tag-type">医保</span>
                   <span class="tag-desc">立省16元</span>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!--    底部推荐区域-->
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
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const recommendList = ref([])
const isLoading = ref(false) // 是否正在加载中
const hasMore = ref(true) // 是否还有更多数据可以加载

// 格式化数据，统一接口和备用数据的结构
const formatData = (data) => {
  return data.map(item => ({
    _id: item._id || item.id,
    url: item.url,
    author: item.author,
    content: item.content
  }))
}

// 从接口获取数据
const fetchData = async (page = 1, pageSize = 6) => {
  try {
    // 模拟分页，实际项目中如果接口支持分页，需要传page参数
    // 这里用size控制每次加载的数量
    const res = await axios.get('https://tea.qingnian8.com/tools/petShow', {
      headers: {
        'access-key': '925255' // 将密钥放入请求头
      },
      params: {
        size: pageSize,
        type: 'all',
        'access-key': 'lp9899'
      }
    })

    if (res.data.errCode === 0) {
      const formattedData = formatData(res.data.data)
      // 如果返回的数据少于请求数量，说明没有更多数据了
      if (formattedData.length < pageSize) {
        hasMore.value = false
      }
      return formattedData
    }
    return []
  } catch (err) {
    console.error('接口请求失败：', err)
    return []
  }
}

// 加载更多数据的函数
const loadMore = async () => {
  // 如果正在加载中，或者没有更多数据了，则不执行任何操作
  if (isLoading.value || !hasMore.value) return

  isLoading.value = true // 开始加载

  try {
    // 每次加载3条新数据
    const newData = await fetchData(1, 3)

    if (newData.length > 0) {
      recommendList.value = [...recommendList.value, ...newData] // 追加新数据
    } else {
      hasMore.value = false // 如果没有获取到新数据，说明已加载完毕
    }
  } finally {
    isLoading.value = false // 无论成功失败，都结束加载状态
  }
}

// 监听滚动事件
const handleScroll = () => {
  // 当距离底部小于200px时，触发加载更多
  if (window.innerHeight + document.documentElement.scrollTop >= document.documentElement.offsetHeight - 200) {
    loadMore()
  }
}

onMounted(async () => {
  // 初始加载6条数据
  const initialData = await fetchData(1, 6)
  if (initialData.length > 0) {
    recommendList.value = initialData
  } else {
    // 如果初始加载失败，则使用备用数据
    recommendList.value = formatData([
      { id: 1, url: 'https://picsum.photos/seed/cat1/600/400', author: '爱宠基地联盟', content: '是个黏人的崽，颜值真的没话说' },
      { id: 2, url: 'https://picsum.photos/seed/dog1/600/400', author: '汪星人日记', content: '快乐小狗，治愈每一天' },
      { id: 3, url: 'https://picsum.photos/seed/cat2/600/400', author: '喵星人部落', content: '慵懒又可爱，谁能不爱呢' },
      { id: 4, url: 'https://picsum.photos/seed/dog2/600/400', author: '短腿柯基控', content: '短腿柯基，萌化你的心' },
      { id: 5, url: 'https://picsum.photos/seed/cat3/600/400', author: '猫咪日常', content: '安静乖巧，是理想的陪伴' },
      { id: 6, url: 'https://picsum.photos/seed/dog3/600/400', author: '金毛寻回', content: '金毛寻回，暖心又忠诚' }
    ])
    hasMore.value = false // 备用数据只有6条，所以没有更多了
  }

  // 开始监听滚动事件
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  // 组件销毁时，移除滚动监听，防止内存泄漏
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
/* 外层居中容器 */
.page-container {
  max-width: 1200px; /* 可根据需求调整最大宽度 */
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
  width: 120px;
  height: 120px;
  object-fit: contain;
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

/* 调整顶部推广图的尺寸和对齐 */
.promo-card {
  display: flex;
  align-items: center; /* 垂直居中 */
  background: linear-gradient(135deg, #64b5f6, #7e57c2);
  border-radius: 12px;
  color: #fff;
  padding: 20px;
  margin-bottom: 20px;
}
.promo-img {
  width: 150px; /* 调整宽度适配设计 */
  height: auto; /* 高度自动，保持比例 */
  object-fit: contain;
  margin-left: auto; /* 右对齐 */
}

/* 图标样式适配 */
.function-icons .icon-img {
  width: 40px;
  height: 40px;
  object-fit: contain;
}
/* 宠物医保推广区域 */
/* 标题区域样式 */
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

/* 左侧图片区域 */
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

/* 右侧商品区域 */
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
  width: calc(100% - 32px);
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

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr); /* 3列，每列等宽 */
  grid-template-rows: repeat(2, auto);  /* 2行，高度自适应内容 */
  gap: 12px; /* 列与行之间的间距 */
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
}

.product-item img {
  width: 100%;
  max-width: 100px;
  height: 80px;
  object-fit: contain;
  display: block;
  margin: 0 auto 8px;
}
.insurance-tag {
  display: inline-flex;
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
/*底部推荐区域*/
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
