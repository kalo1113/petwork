<template>
  <div class="page-container">
    <div class="pet-insurance-mall-page">
      <!-- 顶部搜索与Banner区域 -->
      <div class="top-section">
        <div class="search-bar">
          <input
            type="text"
            placeholder="大宠爱驱虫药"
            class="search-input"
            v-model="searchKeyword"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
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
                @mouseenter="activeHotProduct = index"
                @mouseleave="activeHotProduct = -1"
              >
                <img
                  :src="getImgUrl(product.imgPath)"
                  alt="产品图片"
                  class="product-img"
                />
                <div
                  class="product-mask"
                  v-if="activeHotProduct === index"
                >
                  <span class="add-cart-btn" @click="addToCartHandler(product)">+ 加入购物车</span>
                </div>
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
            />
            <div
              class="product-mask"
              v-if="activeProduct === index"
            >
              <span class="add-cart-btn" @click="addToCartHandler(product)">+ 加入购物车</span>
            </div>
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

    <!-- 右侧：购物车（悬浮框） + 返回顶部按钮 -->
    <div class="right-fixed-btn">
      <!-- 购物车按钮 + 悬浮框 -->
      <div class="cart-wrapper">
        <button
          class="fixed-btn cart-btn"
          @click="toggleCart"
        >
          <img src="@/assets/images/商城图标/购物车.svg" alt="购物车" class="btn-icon" />
          <!-- 购物车数量角标 -->
          <span class="cart-count" v-if="cartList.length > 0">{{ cartList.reduce((t, i) => t + i.count, 0) }}</span>
        </button>
        <!-- 购物车悬浮框 -->
        <div class="cart-popup" v-show="isCartOpen">
          <div class="cart-header">
            <h3>购物车</h3>
            <button class="close-btn" @click="isCartOpen = false">×</button>
          </div>
          <!-- 购物车空状态 -->
          <div class="cart-empty" v-if="cartList.length === 0">
            <p>购物车是空的~</p>
            <button class="go-shop-btn" @click="isCartOpen = false">去逛逛</button>
          </div>
          <!-- 购物车商品列表 + 地址选择 + 底部结算栏 -->
          <div v-else>
            <!-- 商品列表 -->
            <div class="cart-list">
              <div
                class="cart-item"
                v-for="(item, index) in cartList"
                :key="item.productId"
                @touchmove="handleTouchMove($event, index)"
                @touchend="handleTouchEnd(index)"
                :style="{ transform: `translateX(-${item.swipeDistance}px)` }"
              >
                <!-- 商品选择框（仅前端展示，不调用后端） -->
                <div class="cart-item-check">
                  <input
                    type="checkbox"
                    v-model="item.checked"
                    class="check-input"
                    @change="calculateTotal()" 
                  />
                </div>
                <div class="cart-item-content">
                  <img :src="getImgUrl(item.imgPath)" alt="商品图片" class="cart-item-img" />
                  <div class="cart-item-info">
                    <p class="cart-item-title">{{ item.title }}</p>
                    <!-- 显示旧价和医保价 -->
                    <p class="cart-item-price">
                      原价: <span class="price-old">{{ item.oldPrice }}</span><br/>
                      医保价: <span class="price-now">{{ item.nowPrice }}</span>
                    </p>
                  </div>
                  <div class="cart-item-count">
                    <button class="count-btn minus" @click="changeCountHandler(item, -1)" :disabled="item.count <= 1">-</button>
                    <span class="count">{{ item.count }}</span>
                    <button class="count-btn plus" @click="changeCountHandler(item, 1)">+</button>
                  </div>
                </div>
                <!-- 左滑删除按钮 -->
                <button class="delete-btn" @click="deleteCartItemHandler(item.cartId)">删除</button>
              </div>
            </div>
            
            <!-- 只展示默认地址 + 修改地址弹窗 -->
            <div class="address-section">
              <!-- 默认地址展示 -->
              <div v-if="defaultAddress.id" class="address-item active">
                <div class="address-info">
                  <p><strong>{{ defaultAddress.receiverName }}</strong> {{ defaultAddress.receiverPhone }}</p>
                  <p class="address-detail">
                    {{ defaultAddress.receiverProvince }}{{ defaultAddress.receiverCity }}{{ defaultAddress.receiverDistrict }}{{ defaultAddress.receiverDetailAddress }}
                  </p>
                </div>
                <!-- 修改地址按钮 -->
                <button class="edit-address-btn" @click="openAddressModal">修改地址</button>
              </div>
              <!-- 无默认地址提示 -->
              <div v-else class="no-address">
                <p>暂无收货地址，请先添加</p>
                <button @click="openAddressModal" class="add-address-btn">添加收货地址</button>
              </div>

              <!-- 地址选择/新增弹窗（Element Plus Dialog） -->
              <el-dialog
                v-model="addressModalVisible"
                title="选择/新增收货地址"
                width="350px"
                destroy-on-close
              >
                <!-- 地址列表选择区域 -->
                <div class="address-select-list">
                  <div 
                    class="address-select-item"
                    v-for="addr in allAddressList" 
                    :key="addr.id"
                    :class="{ active: addr.id === defaultAddress.id }"
                    @click="selectAddress(addr)"
                  >
                    <div class="address-select-info">
                      <p><strong>{{ addr.receiverName }}</strong> {{ addr.receiverPhone }}</p>
                      <p class="address-select-detail">
                        {{ addr.receiverProvince }}{{ addr.receiverCity }}{{ addr.receiverDistrict }}{{ addr.receiverDetailAddress }}
                      </p>
                    </div>
                  </div>
                  <!-- 无可选地址提示 -->
                  <div v-if="allAddressList.length === 0" class="no-address-tip">
                    <p>暂无可选地址，请先新增</p>
                  </div>
                </div>

                <!-- 分隔线 -->
                <div class="address-modal-divider"></div>

                <!-- 新增地址按钮（跳转到新增地址页/内嵌表单） -->
                <div class="address-modal-footer">
                  <button class="add-new-address-btn" @click="goToAddAddress">新增收货地址</button>
                </div>

                <!-- 弹窗底部按钮 -->
                <template #footer>
                  <el-button @click="addressModalVisible = false">取消</el-button>
                  <el-button type="primary" @click="confirmAddress">确认选择</el-button>
                </template>
              </el-dialog>
            </div>

            <!-- 底部结算栏（左右结构） -->
            <div class="cart-footer">
              <div class="footer-left">
                <p>商品总价：<span class="total-old-price">¥{{ totalOldPrice.toFixed(2) }}</span></p>
                <p>医保减：<span class="insurance-reduce">¥{{ insuranceReduce.toFixed(2) }}</span></p>
              </div>
              <div class="footer-right">
                <p class="total">合计: <span class="total-now-price">¥{{ totalNowPrice.toFixed(2) }}</span></p>
                <button 
                  class="checkout-btn" 
                  @click="handleCheckout"
                  :disabled="selectedCount === 0"
                >
                  结算({{ selectedCount }})
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 返回顶部按钮 -->
      <button
        class="fixed-btn top-btn"
        @click="handleTopClick"
      >
        <img src="@/assets/images/商城图标/置顶.svg" alt="返回顶部" class="btn-icon" />
      </button>
    </div>
        <!-- 新增/编辑地址弹窗（核心修复：Cascader配置） -->
    <el-dialog
      v-model="addAddressDialogVisible"
      :title="isEditAddress ? '编辑地址' : '新增地址'"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form :model="addressForm" label-width="80px" :rules="addressRules" ref="addressFormRef">
        <!-- 省市区选择器：修复显示+校验逻辑 -->
        <el-form-item label="所在地区" prop="area">
          <el-cascader
            v-model="addressForm.area"
            :options="areaOptions"
            placeholder="请选择省/市/区"
            @change="handleAreaChange"
            :props="{ 
              expandTrigger: 'hover', 
              label: 'value',   // 显示名称（如北京市）
              value: 'value',   // 绑定名称作为值
              children: 'children' 
            }"
            style="width: 100%;"
          ></el-cascader>
        </el-form-item>
        <el-form-item label="详细地址" prop="receiverDetailAddress">
          <el-input
            v-model="addressForm.receiverDetailAddress"
            placeholder="请输入街道/门牌号"
          ></el-input>
        </el-form-item>
        <el-form-item label="收货人" prop="receiverName">
          <el-input
            v-model="addressForm.receiverName"
            placeholder="请输入收货人姓名"
          ></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input
            v-model="addressForm.receiverPhone"
            placeholder="请输入手机号"
            maxlength="11"
          ></el-input>
        </el-form-item>
        <el-form-item label="地址标签">
          <el-radio-group v-model="addressForm.tag">
            <el-radio label="家">家</el-radio>
            <el-radio label="公司">公司</el-radio>
            <el-radio label="学校">学校</el-radio>
            <el-radio label="父母">父母</el-radio>
            <el-radio label="朋友">朋友</el-radio>
            <el-radio label="自定义">
              <el-input
                v-model="addressForm.tag"
                placeholder="自定义标签"
                maxlength="4"
                style="width: 80px; margin-left: 5px;"
              ></el-input>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addAddressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" style="color: white;">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router' // 新增：引入路由
import { getProductList } from '@/api/user/index.js'
// 引入封装的购物车接口（移除toggleCartSelect）
import { 
  addToCart, 
  getCartList, 
  updateCartCount, 
  deleteCartItem, 
} from '@/api/user/index.js' 
// 新增：引入订单接口和地址接口
import { createOrder } from '@/api/user/index.js'
import { getDefaultReceiverAddress, getReceiverAddressList } from '@/api/user/index.js'
import { ElMessage, ElMessageBox } from 'element-plus' 

// 模拟登录用户ID（实际项目中从登录态获取）
const userId = ref(1) 
// 新增：路由实例
const router = useRouter()

// 搜索框绑定值
const searchKeyword = ref('')
// 爆品区hover激活态
const activeHotProduct = ref(-1)
// 所有商品数据
const allProducts = ref([])
// 随机爆品商品
const randomProducts = ref([])
// 筛选后的商品
const filteredProducts = ref([])
// 商品hover激活态
const activeProduct = ref(-1)
// 分类和排序
const currentMainCategory = ref('dog')
const currentSubCategory = ref('all')
const currentSort = ref('recommend')
// 轮播相关
const currentPosition = ref(0)
const itemWidth = ref(0)
const carouselTimer = ref(null)
const isTransitioning = ref(false)
// 购物车相关（改为从后端获取）
const cartList = ref([]) // 购物车列表
const isCartOpen = ref(false) // 购物车是否展开
// 结算相关数据
const totalOldPrice = ref(0) // 选中商品的旧价总和
const totalNowPrice = ref(0) // 选中商品的医保价总和
const insuranceReduce = ref(0) // 医保减免的差价总和
const selectedCount = ref(0) // 选中商品数量

// ========== 修改：地址相关数据 ==========
const defaultAddress = ref({}) // 只存储默认地址
const isAddressLoaded = ref(false) // 标记地址是否已加载
const addressModalVisible = ref(false) // 地址弹窗是否显示
const allAddressList = ref([]) // 所有可选地址列表（用于弹窗选择）

// 返回顶部
const handleTopClick = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  ElMessage.success('已返回顶部~')
}

// 切换购物车显示/隐藏（打开时拉取最新购物车数据）
const toggleCart = async () => {
  isCartOpen.value = !isCartOpen.value
  // 打开购物车时拉取后端数据 + 重置左滑状态 + 计算总价
  if (isCartOpen.value) {
    await loadCartList() // 拉取最新购物车
    cartList.value.forEach(item => item.swipeDistance = 0)
    calculateTotal()
  }
}

// 处理图片路径
const getImgUrl = (imgPath) => {
  if (!imgPath) return '/assets/images/default-product.png'
  const relativePath = imgPath.split('productimg\\')[1]?.replace(/\\/g, '/')
  return relativePath ? `http://localhost:8080/product-images/${relativePath}` : '/assets/images/default-product.png'
}

// 计算医保节省金额
const calculateSave = (product) => {
  const nowPrice = Number(product.nowPrice?.replace(/[^\d.]/g, '') || 0)
  const oldPrice = Number(product.oldPrice?.replace(/[^\d.]/g, '') || 0)
  return (oldPrice - nowPrice).toFixed(2)
}

// 初始化爆品
const initRandomProducts = () => {
  if (allProducts.value.length === 0) return
  const shuffled = [...allProducts.value].sort(() => 0.5 - Math.random())
  randomProducts.value = shuffled.slice(0, 8)
}

// 加载商品数据
const loadProducts = async () => {
  try {
    const productData = await getProductList()
    allProducts.value = Array.isArray(productData) ? productData : []
    initRandomProducts()
    filterProducts()
  } catch (err) {
    ElMessage.error('获取商品列表失败，请重试')
    console.error('加载商品失败：', err)
    allProducts.value = []
  }
}

// 加载购物车数据（从后端拉取，适配数据库下划线字段）
const loadCartList = async () => {
  try {
    // 校验userId是否为有效数字
    const userIdNum = Number(userId.value)
    if (isNaN(userIdNum)) {
      return ElMessage.error('用户ID异常，请检查登录状态')
    }
    const cartData = await getCartList(userIdNum)
    // 增加空值判断，确保cartData是数组
    if (!Array.isArray(cartData)) {
      cartList.value = []
      return ElMessage.warning('购物车数据格式异常')
    }
    // 适配后端返回的Map格式（数据库下划线字段→前端驼峰）
    cartList.value = cartData.map(item => ({
      cartId: item.cart_id || item.cartId || '', // 匹配后端返回的cart_id
      productId: item.product_id || item.productId || '', // 匹配product_id
      title: item.title || '未知商品', // 需后端关联商品表返回
      oldPrice: item.old_price || item.oldPrice || '0.00', // 匹配old_price
      nowPrice: item.now_price || item.nowPrice || '0.00', // 匹配now_price
      imgPath: item.img_path || item.imgPath || '', // 匹配img_path
      count: item.product_count || item.count || 1, // 匹配product_count
      swipeDistance: 0, // 前端左滑字段
      checked: true // 选中状态仅前端维护，默认选中
    }))
  } catch (err) {
    ElMessage.error('获取购物车失败，请重试')
    console.error('加载购物车失败：', err)
    cartList.value = []
  }
}

// ========== 修改：加载默认地址（移除列表逻辑） ==========
const loadDefaultAddress = async () => {
  // 前置校验
  if (isAddressLoaded.value) {
    console.log('✅ 默认地址已加载，无需重复请求')
    return
  }
  if (selectedCount.value === 0) {
    console.log('⚠️ 未选中商品，不加载地址')
    return
  }

  // 读取用户ID（从localStorage取，确保和测试逻辑一致）
  let userIdNum = ''
  try {
    const userData = localStorage.getItem('userData')
    if (!userData) {
      console.log('❌ 无用户信息，请先登录')
      return
    }
    const userInfo = JSON.parse(userData)
    userIdNum = Number(userInfo.userId)
    if (isNaN(userIdNum)) {
      console.log('❌ 用户ID异常：', userInfo.userId)
      return
    }
    console.log('✅ 解析到用户ID：', userIdNum)
  } catch (err) {
    console.error('❌ 读取用户ID失败：', err)
    return
  }

  // 调用默认地址接口（复用测试成功的逻辑）
  try {
    console.log('🔍 开始请求默认地址接口...')
    const addressRes = await getDefaultReceiverAddress(userIdNum)
    console.log('✅ 默认地址接口返回：', addressRes)
    
    // 解析地址数据（兼容不同返回格式）
    defaultAddress.value = addressRes.data || {}
    isAddressLoaded.value = true
    
    if (defaultAddress.value.id) {
      console.log('✅ 加载默认地址成功：', defaultAddress.value)
    } else {
      console.log('⚠️ 暂无默认收货地址')
    }
  } catch (err) {
    console.error('❌ 加载默认地址失败：', err)
    defaultAddress.value = {}
    isAddressLoaded.value = true
  }
}

// ========== 新增：地址弹窗相关逻辑 ==========
// 打开地址弹窗（同时加载所有地址列表）
const openAddressModal = async () => {
  addressModalVisible.value = true
  // 加载所有地址列表（供弹窗选择）
  await loadAllAddressList()
}

// 加载所有地址列表（供弹窗选择）
const loadAllAddressList = async () => {
  // 读取用户ID
  let userIdNum = ''
  try {
    const userData = localStorage.getItem('userData')
    if (!userData) return
    const userInfo = JSON.parse(userData)
    userIdNum = Number(userInfo.userId)
    if (isNaN(userIdNum)) return
  } catch (err) {
    console.error('读取用户ID失败：', err)
    return
  }

  // 调用地址列表接口
  try {
    const addressRes = await getReceiverAddressList(userIdNum)
    allAddressList.value = addressRes.data || []
  } catch (err) {
    console.error('加载地址列表失败：', err)
    allAddressList.value = defaultAddress.value.id ? [defaultAddress.value] : []
  }
}

// 选择弹窗中的地址
const selectAddress = (addr) => {
  defaultAddress.value = addr // 选中的地址设为默认地址
}

// 确认选择地址（关闭弹窗）
const confirmAddress = () => {
  if (!defaultAddress.value.id && allAddressList.value.length > 0) {
    // 未选择时默认选第一个
    defaultAddress.value = allAddressList.value[0]
  }
  addressModalVisible.value = false
  ElMessage.success('地址已更新')
}

// 跳转到新增地址页面
const goToAddAddress = () => {
  addressModalVisible.value = false // 先关闭弹窗
  router.push('/user/setting') // 替换成你实际的地址管理路由
  // 新增地址后返回时，重新加载默认地址
  setTimeout(async () => {
    await loadDefaultAddress()
  }, 1000)
}

// 筛选商品
const filterProducts = () => {
  let result = [...allProducts.value]
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.trim().toLowerCase().replace(/\s+/g, '')
    result = result.filter(product => {
      const titleMatch = product.title?.toLowerCase().replace(/\s+/g, '').includes(keyword)
      const descMatch = product.description?.toLowerCase().replace(/\s+/g, '').includes(keyword)
      const cateMatch = product.mainCategory?.toLowerCase().replace(/\s+/g, '').includes(keyword)
      return titleMatch || descMatch || cateMatch
    })
  }
  if (currentMainCategory.value === 'dog') {
    result = result.filter(product => product.mainCategory?.startsWith('狗狗'))
  } else if (currentMainCategory.value === 'cat') {
    result = result.filter(product => product.mainCategory?.startsWith('猫咪'))
  }
  if (currentSubCategory.value !== 'all') {
    result = result.filter(product => product.mainCategory === currentSubCategory.value)
  }
  result = sortProducts(result, currentSort.value)
  filteredProducts.value = result
  if (searchKeyword.value && filteredProducts.value.length === 0) {
    ElMessage.info(`未找到包含「${searchKeyword.value}」的商品`)
  }
}

// 商品排序
const sortProducts = (products, sortType) => {
  const sorted = [...products]
  switch (sortType) {
    case 'priceAsc':
      return sorted.sort((a, b) => {
        const aPrice = Number(a.nowPrice?.replace(/[^\d.]/g, '') || 0)
        const bPrice = Number(b.nowPrice?.replace(/[^\d.]/g, '') || 0)
        return aPrice - bPrice
      })
    case 'priceDesc':
      return sorted.sort((a, b) => {
        const aPrice = Number(a.nowPrice?.replace(/[^\d.]/g, '') || 0)
        const bPrice = Number(b.nowPrice?.replace(/[^\d.]/g, '') || 0)
        return bPrice - aPrice
      })
    default:
      return sorted
  }
}

// 分类/排序点击事件
const handleSubTabClick = (subCategory) => {
  currentSubCategory.value = subCategory
  filterProducts()
}
const handleMainTabClick = (mainCategory) => {
  currentMainCategory.value = mainCategory
  currentSubCategory.value = 'all'
  currentSort.value = 'recommend'
  filterProducts()
}
const handleSortTabClick = (sortType) => {
  currentSort.value = sortType
  filterProducts()
}

// 搜索事件
const handleSearch = () => {
  searchKeyword.value = searchKeyword.value.trim()
  filterProducts()
}

const addToCartHandler = async (product) => {
  try {
    // 【关键修复】先缓存参数，避免后续product被篡改
    const cacheProductId = product.id; 
    const cacheUserId = userId.value;

    // 打印缓存的参数（验证此时还是对的）
    console.log('缓存的参数：', {
      userId: cacheUserId,
      productId: cacheProductId
    });

    // 强制转换（基于缓存值）
    const userIdNum = Number(cacheUserId);
    const productIdNum = Number(cacheProductId);
    const countNum = 1;

    // 校验
    if (isNaN(userIdNum)) {
      return ElMessage.error('用户ID异常，请重新登录');
    }
    if (isNaN(productIdNum)) {
      return ElMessage.error('商品ID异常，无法加入购物车');
    }
    
    // 调用接口（用缓存的数值）
    await addToCart(userIdNum, productIdNum, countNum);
    
    await loadCartList();
    calculateTotal();
    ElMessage.success('已加入购物车！');
  } catch (err) {
    ElMessage.error('加购失败，请重试~');
    console.error('加购失败：', err);
  }
};

// 购物车数量修改（调用后端接口）
const changeCountHandler = async (item, num) => {
  // 先更新前端UI（优化体验）
  const newCount = item.count + num
  if (newCount < 1) return ElMessage.warning('商品数量不能小于1')
  
  item.count = newCount
  calculateTotal()
  
  try {
    // 校验cartId有效性
    const cartIdNum = Number(item.cartId)
    if (isNaN(cartIdNum)) {
      throw new Error('购物车项ID异常')
    }
    // 调用后端修改数量接口
    await updateCartCount(cartIdNum, newCount)
  } catch (err) {
    // 后端失败则回滚数量
    item.count -= num
    calculateTotal()
    ElMessage.error('修改数量失败，请重试')
    console.error('修改数量失败：', err)
  }
}

// 计算选中商品的旧价、医保价、减免差价
const calculateTotal = () => {
  let oldTotal = 0
  let nowTotal = 0
  let count = 0
  cartList.value.forEach(item => {
    if (item.checked) {
      const oldPrice = Number(item.oldPrice?.replace(/[^\d.]/g, '') || 0)
      const nowPrice = Number(item.nowPrice?.replace(/[^\d.]/g, '') || 0)
      oldTotal += oldPrice * item.count
      nowTotal += nowPrice * item.count
      count += item.count
    }
  })
  totalOldPrice.value = oldTotal
  totalNowPrice.value = nowTotal
  insuranceReduce.value = oldTotal - nowTotal // 医保减免的差价
  selectedCount.value = count
}

// 删除购物车商品（调用后端接口）
const deleteCartItemHandler = async (cartId) => {
  try {
    // 校验cartId有效性
    const cartIdNum = Number(cartId)
    if (isNaN(cartIdNum)) {
      throw new Error('购物车项ID异常')
    }
    // 调用后端删除接口
    await deleteCartItem(cartIdNum)
    // 重新拉取购物车数据
    await loadCartList()
    calculateTotal()
    ElMessage.success('已删除商品')
  } catch (err) {
    ElMessage.error('删除失败，请重试~')
    console.error('删除失败：', err)
  }
}

// 结算事件（完整版：写入数据库 + 默认地址逻辑）
const handleCheckout = async () => {
  // 1. 基础校验：选商品
  if (selectedCount.value === 0) {
    ElMessage.warning('请选择要结算的商品')
    return
  }

  // 2. 加载默认地址（确保地址已获取）
  await loadDefaultAddress()

  // 3. 校验用户登录状态
  let userIdNum = ''
  try {
    const userData = localStorage.getItem('userData')
    if (!userData) {
      ElMessage.warning('请先登录后再结算')
      router.push('/login')
      return
    }
    const userInfo = JSON.parse(userData)
    userIdNum = Number(userInfo.userId)
    if (isNaN(userIdNum)) {
      ElMessage.warning('用户ID异常，请重新登录')
      router.push('/login')
      return
    }
  } catch (err) {
    ElMessage.warning('登录状态异常，请重新登录')
    router.push('/login')
    return
  }

  // 4. 提取默认地址信息（兜底默认值）
  const receiverName = defaultAddress.value.receiverName || '测试收货人'
  const receiverPhone = defaultAddress.value.receiverPhone || '13800138000'
  const receiverAddress = defaultAddress.value.receiverProvince 
    ? `${defaultAddress.value.receiverProvince}${defaultAddress.value.receiverCity}${defaultAddress.value.receiverDistrict}${defaultAddress.value.receiverDetailAddress}`
    : '北京市朝阳区测试地址'

  // 5. 二次确认

try {
  // 先拼接好地址文本，避免模板字符串缩进干扰
  const addressText = `收货地址：<br/>${receiverName} ${receiverPhone}<br/>${receiverAddress}`;
  const redPrice = (price) => `<span style="color: #f56c6c; font-weight: 600;">${price}</span>`
  await ElMessageBox.confirm(
    `${addressText}<br/><br/>
    确认结算${selectedCount.value}件商品？<br/>
    原价¥${totalOldPrice.value.toFixed(2)}，医保减免：¥${insuranceReduce.value.toFixed(2)}
    <br/>合计¥${redPrice(totalNowPrice.value.toFixed(2))}<br/>
    
    `,
    '结算确认',
    {
      confirmButtonText: '确认结算',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true // 关键：允许解析 HTML 标签
    }
  )
} catch (error) {
  ElMessage.info('已取消结算')
  return
}

  // 6. 构造订单商品列表
  const orderItemList = cartList.value
    .filter(item => item.checked)
    .map(item => ({
      productId: Number(item.productId),
      productTitle: item.title,
      productImg: item.imgPath,
      productPrice: Number(item.nowPrice?.replace(/[^\d.]/g, '') || 0),
      productCount: item.count,
      itemAmount: Number(item.nowPrice?.replace(/[^\d.]/g, '') || 0) * item.count
    }))

  // 7. 调用创建订单接口
  try {
    const res = await createOrder(
      userIdNum,
      orderItemList,
      receiverName,
      receiverPhone,
      receiverAddress
    )
    ElMessage.success(`订单创建成功！订单ID：${res.data || '未知'}`)
    
    // 8. 结算成功后重置
    cartList.value.forEach(item => item.checked = false)
    calculateTotal()
    isCartOpen.value = false
  } catch (err) {
    console.error('结算失败详情：', err)
    ElMessage.error(`结算失败：${err.msg || err.message || '服务器异常'}`)
  }
}

// 轮播逻辑
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
const stopCarousel = () => {
  if (carouselTimer.value) clearInterval(carouselTimer.value)
  carouselTimer.value = null
}
const initItemWidth = () => {
  nextTick(() => {
    const container = document.querySelector('.carousel-container')
    if (container) {
      itemWidth.value = container.offsetWidth / 4 - 20
    }
  })
}

// 监听购物车变化，自动计算总价
watch([() => cartList.value, () => cartList.value.map(i => i.checked), () => cartList.value.map(i => i.count)], () => {
  calculateTotal()
}, { deep: true })

// 监听选中商品数量变化，自动加载默认地址
watch(selectedCount, (newVal) => {
  if (newVal > 0) {
    loadDefaultAddress()
  }
})

// 页面生命周期
onMounted(async () => {
  // 先加载商品，再加载购物车
  await loadProducts()
  await loadCartList() // 初始化加载购物车
  initItemWidth()
  window.addEventListener('resize', initItemWidth)
  const checkProductsLoaded = setInterval(() => {
    if (randomProducts.value.length > 0) {
      startCarousel()
      clearInterval(checkProductsLoaded)
    }
  }, 100)
})
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
  position: relative;
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
  padding: 5px 0;
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
  width: 200%;
}
.product-item {
  flex: 0 0 25%;
  box-sizing: border-box;
  padding: 0 10px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  position: relative;
}
.product-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
.product-item:hover .product-mask {
  opacity: 1;
}
.add-cart-btn {
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #ff6b6b;
  padding: 8px 20px;
  border-radius: 20px;
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
  position: relative;
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
.product-content .product-mask {
  height: 180px;
}
.product-title {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0;
  line-height: 1.4;
  height: 22px;
  overflow: hidden;
  display: -webkit-box;
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

/* 右侧固定按钮区域 */
.right-fixed-btn {
  position: fixed;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 15px;
  z-index: 999;
}
/* 购物车悬浮框容器 */
.cart-wrapper {
  position: relative;
}
/* 按钮通用样式 */
.fixed-btn {
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}
.btn-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
}
/* 购物车按钮样式 */
/* 购物车数量角标 */
.cart-count {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background-color: #ff6b6b;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 返回顶部按钮 */
.fixed-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 购物车悬浮框样式 */
.cart-popup {
  position: absolute;
  right: 60px; /* 位于购物车按钮左侧 */
  top: 50%;
  transform: translateY(-50%);
  width: 400px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  z-index: 998;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
/* 购物车头部 */
.cart-header {
  padding: 15px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cart-header h3 {
  margin: 0;
  font-size: 16px;
}
.close-btn {
  border: none;
  background-color: transparent;
  font-size: 20px;
  cursor: pointer;
  color: #666;
}
/* 购物车空状态 */
.cart-empty {
  padding: 30px;
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.cart-empty p {
  margin: 0 0 15px;
  color: #666;
}
.go-shop-btn {
  padding: 8px 20px;
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}
/* 购物车商品列表 */
.cart-list {
  padding: 10px 0;
  flex: 1;
  overflow-y: auto;
  max-height: calc(500px - 180px); /* 预留地址和结算栏高度 */
}
/* 购物车商品项（支持左滑） */
.cart-item {
  padding: 12px 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  position: relative;
  transition: transform 0.3s ease;
  overflow: hidden;
}
/* 商品选择框 */
.cart-item-check {
  margin-right: 10px;
}
.check-input {
  width: 18px;
  height: 18px;
  cursor: pointer;
}
/* 商品内容区域 */
.cart-item-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 15px;
  flex: 1;
}
.cart-item-img {
  width: 70px;
  height: 70px;
  object-fit: cover;
  border-radius: 4px;
}
.cart-item-info {
  flex: 2;
}
.cart-item-title {
  font-size: 15px;
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  text-overflow: ellipsis;
  word-wrap: break-word;
  word-break: break-all;
}
.cart-item-price {
  font-size: 14px;
  color: #e53935;
  margin: 0;
}
/* 数量控制区域 */
.cart-item-count {
  display: flex;
  align-items: center;
  gap: 10px;
}
.count-btn {
  width: 28px;
  height: 28px;
  border: 1px solid #eee;
  background-color: #fff;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.count-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}
.count {
  font-size: 15px;
  min-width: 24px;
  text-align: center;
}
/* 左滑删除按钮 */
.delete-btn {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 50px;
  background-color: #ff6b6b;
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 15px;
}

/* ========== 地址区域样式 ========== */
.address-section {
  padding: 10px 20px;
  border-top: 1px solid #eee;
}
.address-item {
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  margin-bottom: 6px;
  font-size: 12px;
  position: relative;
}
.address-item.active {
  border-color: #409eff;
  background-color: #f5f9ff;
}
.address-info {
  overflow: hidden;
}
.address-detail {
  color: #666;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.edit-address-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #409eff;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}
.no-address {
  padding: 10px;
  color: #999;
  text-align: center;
  font-size: 12px;
}
.add-address-btn {
  margin-top: 8px;
  padding: 4px 12px;
  font-size: 12px;
  color: #409eff;
  background: transparent;
  border: 1px solid #409eff;
  border-radius: 4px;
  cursor: pointer;
}

/* 地址弹窗样式 */
.address-select-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 15px;
}
.address-select-item {
  padding: 10px;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
}
.address-select-item.active {
  border-color: #409eff;
  background: #f5f9ff;
}
.address-select-detail {
  color: #666;
  font-size: 13px;
  margin-top: 5px;
}
.no-address-tip {
  padding: 20px;
  text-align: center;
  color: #999;
  border: 1px dashed #e5e5e5;
  border-radius: 4px;
}
.address-modal-divider {
  height: 1px;
  background: #e5e5e5;
  margin: 15px 0;
}
.add-new-address-btn {
  width: 100%;
  padding: 10px;
  border: 1px dashed #409eff;
  color: #409eff;
  background: #f5f9ff;
  border-radius: 4px;
  cursor: pointer;
}
.address-modal-footer {
  margin-bottom: 15px;
}

/* 购物车底部结算栏（左右结构） */
.cart-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background-color: #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.footer-left {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.total-old-price {
  color: #e53935;
  font-weight: 600;
}
.insurance-reduce {
  color: #4186e8;
  font-weight: 600;
}
.footer-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 5px;
}
.total {
  font-size: 16px;
  font-weight: 600;
}
.total-now-price {
  color: #e53935;
  font-size: 18px;
}
.checkout-btn {
  padding: 10px 30px;
  background-color: #ff6b6b;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
}
.checkout-btn:disabled {
  background-color: #ccc !important;
  cursor: not-allowed;
}

/* 响应式适配（小屏幕） */
@media (max-width: 768px) {
  .product-item {
    width: 140px;
  }
  .product-img {
    height: 120px;
  }
  .product-content .product-mask {
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
  .right-fixed-btn {
    right: 15px;
    gap: 10px;
  }
  .fixed-btn {
    width: 40px;
    height: 40px;
  }
  .btn-icon {
    width: 24px;
    height: 24px;
  }
  /* 购物车悬浮框小屏幕适配 */
  .cart-popup {
    width: 320px;
    right: 50px;
  }
  .cart-footer {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  .footer-right {
    align-items: flex-start;
    width: 100%;
  }
  .checkout-btn {
    width: 100%;
  }
}
</style>