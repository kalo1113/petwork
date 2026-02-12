<template>
  <div class="pet-guarantee">
    <!-- 外层居中容器 -->
    <div class="page-container">
      <div class="pet-insurance-page">
        <!-- 单独返回按钮（固定在内容区左上角） -->
        <button class="back-btn" @click="handleBack">&lt;</button>

        <!-- 套餐选择区域（仅保留3个保险的选项卡） -->
        <div class="package-section">
          <!-- 仅显示基础版/升级版/尊享版的选项卡 -->
          <div class="tab-group" v-if="isThreeInsurance">
            <button class="tab-btn" :class="{ active: activeTab === 'basic' }" @click="handleTabClick('basic')">基础版</button>
            <button class="tab-btn" :class="{ active: activeTab === 'advanced' }" @click="handleTabClick('advanced')">升级版</button>
            <button class="tab-btn" :class="{ active: activeTab === 'premium' }" @click="handleTabClick('premium')">尊享版</button>
          </div>

          <!-- 套餐图片展示（根据选中的套餐/保险切换） -->
          <div class="package-img">
            <!-- 3个保险的图片（选项卡切换） -->
            <img v-if="isThreeInsurance && activeTab === 'basic'" src="@/assets/images/保障图标/基础版.jpg" alt="基础版套餐" />
            <img v-else-if="isThreeInsurance && activeTab === 'advanced'" src="@/assets/images/保障图标/升级版.jpg" alt="升级版套餐" />
            <img v-else-if="isThreeInsurance && activeTab === 'premium'" src="@/assets/images/保障图标/尊享版.jpg" alt="尊享版套餐" />
            <!-- 其他保险的图片（无选项卡） -->
            <img v-else-if="insuranceDetail.mediaList && getImgByContentType(insuranceDetail.mediaList, 3)" 
                 :src="getImageUrl(getImgByContentType(insuranceDetail.mediaList, 3))" 
                 :alt="insuranceDetail.insuranceName || '保险套餐图'" 
            />
            <div v-else class="img-placeholder">暂无套餐图片</div>
          </div>
          
          <!-- 新增：保险适用宠物类型提示 -->
          <div v-if="insuranceDetail.applicablePetType" class="applicable-tip">
            本保险适用宠物类型：<span class="type-tag">{{ getPetTypeName(insuranceDetail.applicablePetType) }}</span>
          </div>
          <div v-else class="applicable-tip">
            本保险适用宠物类型：<span class="type-tag">全品类</span>
          </div>
        </div>

        <!-- 核心：被保宠物档案 和 表单 互斥显示 -->
        <!-- 有宠物时显示档案（隐藏表单） -->
        <div class="pet-archive" v-if="userPetList.length > 0">
          <h3>被保宠物档案</h3>
          <!-- 新增：宠物类型不匹配提示 -->
          <div v-if="selectedPetId && !isPetTypeMatch" class="match-error">
            ❌ 所选宠物类型（{{ getPetTypeName(selectedPetType) }}）与本保险适用类型（{{ getPetTypeName(insuranceDetail.applicablePetType) }}）不匹配，无法投保！
          </div>
          <div class="pet-list">
            <!-- 已绑定的宠物 -->
            <div
              class="pet-item"
              v-for="(pet) in userPetList"
              :key="pet.petId"
              @click="selectPet(pet)"
              :class="{ 
                active: selectedPetId === pet.petId,
                disabled: !checkPetTypeMatch(pet.petType)
              }"
            >
              <img :src="pet.petFacePhoto || '@/assets/images/默认宠物头像.svg'" alt="宠物头像" class="pet-avatar" />
              <span class="pet-name">{{ pet.petName }}</span>
              <span class="pet-type-tag">{{ pet.petType }}</span>
              <!-- 不匹配提示 -->
              <span v-if="!checkPetTypeMatch(pet.petType)" class="unmatch-tag">不匹配</span>
            </div>
            <!-- 添加宠物按钮 -->
            <div class="pet-item add-pet" @click="showAddPetForm">
              <span class="plus-icon">+</span>
              <span class="pet-name">添加宠物</span>
            </div>
          </div>
        </div>

        <!-- 无宠物时显示表单（隐藏档案） -->
        <div class="pet-form" v-else>
          <h3>填写被保宠物</h3>
          <!-- 新增：宠物类型不匹配提示 -->
          <div v-if="petForm.petType && !checkPetTypeMatch(petForm.petType)" class="match-error">
            ❌ 所选宠物类型（{{ getPetTypeName(petForm.petType) }}）与本保险适用类型（{{ getPetTypeName(insuranceDetail.applicablePetType) }}）不匹配，无法投保！
          </div>
          <div class="form-group">
            <label>宠物昵称</label>
            <input type="text" placeholder="请输入" v-model="petForm.petName" />
          </div>
          <div class="form-group">
            <label>出生日期</label>
            <el-date-picker
              v-model="petForm.petBirthday"
              type="date"
              placeholder="请选择出生日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </div>
          <div class="form-group">
            <label>宠物种类</label>
            <div class="radio-group">
              <button
                class="radio-btn"
                :class="{ 
                  active: petForm.petType === '猫',
                  disabled: !checkPetTypeMatch('猫')
                }"
                @click="petForm.petType = '猫'"
                :disabled="!checkPetTypeMatch('猫')"
              >
                猫
              </button>
              <button
                class="radio-btn"
                :class="{ 
                  active: petForm.petType === '狗',
                  disabled: !checkPetTypeMatch('狗')
                }"
                @click="petForm.petType = '狗'"
                :disabled="!checkPetTypeMatch('狗')"
              >
                狗
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>宠物性别</label>
            <div class="radio-group">
              <button
                class="radio-btn"
                :class="{ active: petForm.petGender === '公' }"
                @click="petForm.petGender = '公'"
              >
                公
              </button>
              <button
                class="radio-btn"
                :class="{ active: petForm.petGender === '母' }"
                @click="petForm.petGender = '母'"
              >
                母
              </button>
            </div>
          </div>
          <div class="form-group">
            <label>是否绝育</label>
            <div class="radio-group">
              <button
                class="radio-btn"
                :class="{ active: petForm.isSterilized === '否' }"
                @click="petForm.isSterilized = '否'"
              >
                否
              </button>
              <button
                class="radio-btn"
                :class="{ active: petForm.isSterilized === '是' }"
                @click="petForm.isSterilized = '是'"
              >
                是
              </button>
            </div>
          </div>
          <!-- 原生图片上传区域 -->
          <div class="form-group">
            <label>宠物照片</label>
            <div class="photo-upload-group">
              <!-- 正脸照上传 -->
              <div class="photo-upload">
                <div class="upload-content" @click="triggerFileInput('face')">
                  <img v-if="previewUrls.face" :src="previewUrls.face" class="preview-img" alt="正脸照" />
                  <div v-else class="upload-placeholder">
                    <span class="plus-icon">+</span>
                    <div class="upload-text">正脸照</div>
                  </div>
                </div>
                <input
                  type="file"
                  accept="image/*"
                  class="file-input"
                  ref="faceFileRef"
                  @change="handleFileChange($event, 'face')"
                >
                <button
                  v-if="previewUrls.face"
                  class="delete-btn"
                  @click.stop="handleDelete('face')"
                >
                  ×
                </button>
              </div>

              <!-- 全身照上传 -->
              <div class="photo-upload">
                <div class="upload-content" @click="triggerFileInput('body')">
                  <img v-if="previewUrls.body" :src="previewUrls.body" class="preview-img" alt="全身照" />
                  <div v-else class="upload-placeholder">
                    <span class="plus-icon">+</span>
                    <div class="upload-text">全身照</div>
                  </div>
                </div>
                <input
                  type="file"
                  accept="image/*"
                  class="file-input"
                  ref="bodyFileRef"
                  @change="handleFileChange($event, 'body')"
                >
                <button
                  v-if="previewUrls.body"
                  class="delete-btn"
                  @click.stop="handleDelete('body')"
                >
                  ×
                </button>
              </div>
            </div>
            <p class="tip">请上传宠物清晰正脸照和全身照</p>
          </div>
          <p class="tip">未绝育宠物，绝育后可修改一次</p>
        </div>

        <!-- 缴费方式 -->
        <div class="payment-method">
          <h3>缴费方式</h3>
          <div class="radio-group">
            <button class="radio-btn" :class="{ active: paymentMethod === 'monthly' }" @click="paymentMethod = 'monthly'">
              按月缴费(分{{ isThreeInsurance ? 12 : insuranceDetail.guaranteeCycle }}期)
            </button>
            <button class="radio-btn" :class="{ active: paymentMethod === 'lump' }" @click="paymentMethod = 'lump'">全额缴费</button>
          </div>
          <p>每月按支付宝默认扣款顺序自动扣款</p>
          <p class="warning">已使用赠送服务的用户，缴费4期后可退保</p>
          <!-- 协议勾选移至缴费方式区域 -->
          <div class="form-group agree-group">
            <label class="agree-label">
              <span style="width: 250px;">我已阅读并同意《宠物保障服务协议》</span>
              <input
                type="checkbox"
                v-model="isAgreed"
                class="agree-checkbox"
              >
            </label>
          </div>
        </div>

        <!-- 产品特色 -->
        <div class="product-feature">
          <h3>产品特色</h3>
          <!-- 3个保险的产品特色图 -->
          <img v-if="isThreeInsurance && activeTab === 'basic'" src="@/assets/images/保障图标/产品特色.png" alt="基础版产品特色" />
          <img v-else-if="isThreeInsurance && activeTab === 'advanced'" src="@/assets/images/保障图标/产品特色.png" alt="升级版产品特色" />
          <img v-else-if="isThreeInsurance && activeTab === 'premium'" src="@/assets/images/保障图标/产品特色.png" alt="尊享版产品特色" />
          <!-- 其他保险的产品特色图 -->
          <img v-else-if="insuranceDetail.mediaList && getImgByContentType(insuranceDetail.mediaList, 1)" 
               :src="getImageUrl(getImgByContentType(insuranceDetail.mediaList, 1))" 
               alt="产品特色" 
          />
          <div v-else class="img-placeholder">暂无产品特色图片</div>
        </div>

        <!-- 理赔说明 -->
        <div class="claim-instruction">
          <h3>理赔说明</h3>
          <img src="@/assets/images/保障图标/理赔说明.jpg" alt="理赔说明" />
        </div>

        <!-- 理赔案例轮播 -->
        <div class="claim-case">
          <h3>理赔案例</h3>
          <!-- 3个保险的理赔案例 -->
          <el-carousel
            v-if="isThreeInsurance"
            v-model="currentCase"
            indicator-position="outside"
            height="2100px"
            :autoplay="true"
          >
            <el-carousel-item
              v-for="(img, index) in threeCaseImages"
              :key="index"
            >
              <img
                :src="img"
                style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px;"
                alt="理赔案例"
              />
            </el-carousel-item>
          </el-carousel>
          <!-- 其他保险的理赔案例 -->
          <el-carousel
            v-else-if="caseImages.length > 0"
            v-model="currentCase"
            indicator-position="outside"
            height="2100px"
            :autoplay="true"
          >
            <el-carousel-item
              v-for="(img, index) in caseImages"
              :key="index"
            >
              <img
                :src="getImageUrl(img)"
                style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px;"
                alt="理赔案例"
              />
            </el-carousel-item>
          </el-carousel>
          <div v-else class="img-placeholder">暂无理赔案例图片</div>
        </div>

        <!-- 底部投保栏 -->
        <div class="bottom-bar">
          <button class="consult-btn">咨询</button>
          <button class="add-btn" @click="showAddPetForm">+</button>
          <p>{{ priceText }}</p>
          <button
            class="insure-btn"
            @click="submitInsuranceOrder"
            :disabled="!canSubmit || isSubmitting || !isPetTypeValid || !insuranceLoaded"
          >
            <span v-if="!isSubmitting">我要投保</span>
            <span v-else>提交中...</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 登录弹窗 -->
    <el-dialog v-model="dialogVisible" title="请先登录" width="30%">
      <p>您需要先登录才能添加宠物哦！</p>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="toLogin">去登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// 导入核心依赖
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 导入全局配置
import { getImageUrl } from '@/config/index.js'
// 导入api方法
import { 
  addPet, uploadPetImg, updatePetPhoto, getPetListByUserId, 
  getInsuranceMediaList, getInsuranceDetail, createInsuranceOrder 
} from '@/api/user/index.js'

// 导入Element Plus组件
import { ElDatePicker, ElCarousel, ElCarouselItem, ElMessage, ElDialog, ElButton } from 'element-plus'
// 导入3个保险的理赔案例图片
import case1 from '@/assets/images/保障图标/投保案例1.jpg'
import case2 from '@/assets/images/保障图标/投保案例2.jpg'
import case3 from '@/assets/images/保障图标/投保案例3.jpg'
const currentCase = ref(0)
// ========== 核心：区分3个保险和其他保险 ==========
const threeInsuranceMap = {
  1: 'basic',   // 保险ID=1 → 基础版
  2: 'advanced',// 保险ID=2 → 升级版
  3: 'premium'  // 保险ID=3 → 尊享版
}
const isThreeInsurance = ref(false) // 是否是3个带选项卡的保险
const activeTab = ref('premium')    // 3个保险的选中选项卡
const threeCaseImages = ref([case1, case2, case3]) // 3个保险的固定理赔案例

// ========== 保险相关状态 ==========
const insuranceDetail = ref({
  mediaList: [],
  discountPremium: 0,
  guaranteeCycle: 12,
  insuranceName: '',
  id: '',
  applicablePetType: ''
})
const caseImages = ref([])
const paymentMethod = ref('monthly')
const selectedPetType = ref('')
const insuranceLoaded = ref(false)

// ========== 用户&路由状态 ==========
const userInfo = ref({ isLogin: false, userId: '' })
const dialogVisible = ref(false)
const router = useRouter()
const route = useRoute()

// ========== 提交&表单状态 ==========
const isSubmitting = ref(false)
const submitLock = ref(false)
const isAgreed = ref(false)
const userPetList = ref([])
const selectedPetId = ref('')

// 文件上传状态
const faceFileRef = ref(null)
const bodyFileRef = ref(null)
const cacheFiles = ref({ face: null, body: null })
const previewUrls = ref({ face: '', body: '' })

// 表单数据
const petForm = reactive({
  userId: '',
  petName: '',
  petBirthday: '',
  petType: '',
  petGender: '',
  isSterilized: '',
  petFacePhoto: '',
  petBodyPhoto: ''
})

// ========== 3个保险的固定价格 ==========
const threePackagePrice = {
  basic: 18.00,
  advanced: 38.00,
  premium: 68.00
}

// ========== 方法：根据contentType获取图片 ==========
const getImgByContentType = (mediaList, type) => {
  if (!mediaList || !Array.isArray(mediaList)) return ''
  const item = mediaList.find(item => item.contentType === type)
  return item ? item.imgPath : ''
}

// ========== 价格计算（修复版：兼容3个保险和其他保险） ==========
const priceText = computed(() => {
  // 3个保险的固定价格计算
  if (isThreeInsurance.value) {
    const singleMonthPrice = threePackagePrice[activeTab.value] // 单月价格
    const totalPrice = singleMonthPrice * 12 // 全年总价（全额）
    return paymentMethod.value === 'monthly' 
      ? `${singleMonthPrice.toFixed(2)}元/月起` 
      : `总计${totalPrice.toFixed(2)}元`
  }
  
  // 其他保险的动态价格计算
  const discountPremium = Number(insuranceDetail.value.discountPremium) || 0 // 总优惠价（全额）
  const guaranteeCycle = Number(insuranceDetail.value.guaranteeCycle) || 12 // 保障周期（月）
  
  if (discountPremium <= 0) return '0.00元/月起'
  
  // 基础单月价格 = 总优惠价 / 保障周期
  const baseMonthlyPrice = discountPremium / guaranteeCycle
  // 按月缴费需加1.6%手续费（其他保险）
  const monthlyPriceWithFee = baseMonthlyPrice * 1.016
  
  return paymentMethod.value === 'monthly' 
    ? `${monthlyPriceWithFee.toFixed(2)}元/月起` 
    : `总计${discountPremium.toFixed(2)}元`
})

// ========== 宠物类型匹配校验（修复重复键名问题） ==========
const petTypeCodeMap = { '1': '猫', '2': '狗', '3': '通用' } // 统一字符串键，避免ESLint报错
const checkPetTypeMatch = (petType) => {
  if (!insuranceDetail.value.applicablePetType) return true
  if (insuranceDetail.value.applicablePetType === '通用') return true
  // 统一转为字符串后匹配，兼容数字/字符串类型
  const petTypeStr = typeof petType === 'number' ? String(petType) : petType
  const targetType = petTypeCodeMap[petTypeStr] || petTypeStr
  return insuranceDetail.value.applicablePetType === targetType
}
const isPetTypeMatch = computed(() => {
  if (selectedPetId.value) return checkPetTypeMatch(selectedPetType.value)
  return petForm.petType ? checkPetTypeMatch(petForm.petType) : true
})
const isPetTypeValid = computed(() => {
  if (selectedPetId.value === '' && !petForm.petType) return true
  return isPetTypeMatch.value
})
const getPetTypeName = (type) => {
  // 仅保留字符串键，避免重复
  const map = { 
    '猫': '猫', 
    '狗': '狗', 
    '通用': '猫/狗通用', 
    '1': '猫', 
    '2': '狗', 
    '3': '猫/狗通用', 
    '': '未设置' 
  }
  return map[String(type)] || '未设置' // 统一转字符串，避免类型不匹配
}

// ========== 3个保险的选项卡切换 ==========
const handleTabClick = (tab) => {
  activeTab.value = tab
  // 同步切换保险ID（对应threeInsuranceMap）
  const insuranceId = Object.keys(threeInsuranceMap).find(key => threeInsuranceMap[key] === tab)
  if (insuranceId) {
    insuranceDetail.value.id = insuranceId
    // 重新加载对应保险的媒体数据（如果需要）
    getInsuranceMediaList(insuranceId).then(res => {
      if (res.code === 200) insuranceDetail.value.mediaList = res.data
    })
  }
}

// ========== 页面初始化（区分3个保险和其他保险 + 调试日志） ==========
onMounted(async () => {
  // 1. 获取用户状态
  const loginData = JSON.parse(localStorage.getItem('userData') || '{}')
  userInfo.value = { isLogin: !!loginData.userId, userId: loginData.userId || '' }
  const userId = userInfo.value.userId || route.query.userId
  console.log('【调试】当前用户ID：', userId) // 调试日志：确认用户ID
  if (!userId) {
    ElMessage.warning('请先登录后再投保！')
    return
  }
  petForm.userId = String(userId)

  // 2. 获取保险ID
  const insuranceId = route.query.id
  console.log('【调试】当前保险ID：', insuranceId) // 调试日志：确认保险ID
  if (!insuranceId) {
    ElMessage.warning('未获取到保险产品信息！')
    return
  }
  insuranceDetail.value.id = insuranceId

  // 3. 判断是否是3个带选项卡的保险
  isThreeInsurance.value = Object.keys(threeInsuranceMap).includes(insuranceId)
  if (isThreeInsurance.value) {
    // 3个保险：初始化选项卡
    activeTab.value = threeInsuranceMap[insuranceId]
    insuranceLoaded.value = true
    await fetchUserPetList(userId)
    return
  }

  // 4. 其他保险：加载详情
  try {
    const detailRes = await getInsuranceDetail(insuranceId)
    console.log('【调试】保险详情返回：', detailRes) // 调试日志：确认保险详情
    if (detailRes.code === 200) {
      insuranceDetail.value = {
        mediaList: [],
        discountPremium: 0,
        guaranteeCycle: 12,
        insuranceName: '',
        id: insuranceId,
        applicablePetType: '',
        ...detailRes.data
      }
      insuranceDetail.value.discountPremium = Number(detailRes.data.insurance?.discountPremium || 0)
      insuranceDetail.value.guaranteeCycle = Number(detailRes.data.insurance?.guaranteeCycle || 12)
      insuranceDetail.value.insuranceName = detailRes.data.insurance?.insuranceName || ''
      insuranceDetail.value.applicablePetType = petTypeCodeMap[detailRes.data.insurance?.petType] || ''
    }

    // 加载媒体列表
    const mediaRes = await getInsuranceMediaList(insuranceId)
    if (mediaRes.code === 200) {
      insuranceDetail.value.mediaList = mediaRes.data
      caseImages.value = mediaRes.data.filter(item => item.contentType === 2).map(item => item.imgPath)
    }

    // 加载宠物列表
    await fetchUserPetList(userId)
    insuranceLoaded.value = true
  } catch (err) {
    console.error('页面初始化失败：', err)
    ElMessage.error('页面初始化失败，请重试')
  }
})

// ========== 原有方法（兼容3个保险） ==========
const fetchUserPetList = async (userId) => {
  try {
    const res = await getPetListByUserId(userId)
    console.log('【调试】宠物列表返回：', res) // 调试日志：确认宠物列表
    if (res.code === 200) {
      userPetList.value = res.data
      if (userPetList.value.length > 0) selectPet(userPetList.value[0])
    }
  } catch (err) {
    console.error('获取宠物列表失败：', err)
    userPetList.value = []
  }
}
const selectPet = (pet) => {
  selectedPetId.value = pet.petId
  selectedPetType.value = pet.petType
  Object.assign(petForm, {
    petName: pet.petName,
    petBirthday: pet.petBirthday,
    petType: pet.petType,
    petGender: pet.petGender,
    isSterilized: pet.isSterilized,
    petFacePhoto: pet.petFacePhoto,
    petBodyPhoto: pet.petBodyPhoto
  })
  previewUrls.value = { face: pet.petFacePhoto, body: pet.petBodyPhoto }
}
const showAddPetForm = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  try {
    const disabledPetTypes = insuranceDetail.value.applicablePetType === '猫' ? ['狗'] : 
                            insuranceDetail.value.applicablePetType === '狗' ? ['猫'] : []
    router.push({
      path: '/pet-id-card',
      query: { userId: userInfo.value.userId, requiredPetType: insuranceDetail.value.applicablePetType, disabledPetTypes: JSON.stringify(disabledPetTypes) }
    })
    ElMessage.success('正在前往添加宠物页面')
  } catch (err) {
    ElMessage.error('页面跳转失败，请重试')
  }
}
const toLogin = () => {
  dialogVisible.value = false
  router.push('/my')
}
const canSubmit = computed(() => {
  const baseValid = isAgreed.value && !isSubmitting.value && isPetTypeValid.value
  if (!baseValid) return false
  if (selectedPetId.value) return true
  const basicValid = !!petForm.userId && !!petForm.petName && !!petForm.petType &&
                    !!petForm.petGender && !!petForm.petBirthday && !!petForm.isSterilized
  const photoValid = !!cacheFiles.value.face && !!cacheFiles.value.body
  return basicValid && photoValid
})
const handleFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  const allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif', 'image/pjpeg', 'image/x-png']
  if (!allowTypes.includes(file.type)) {
    ElMessage.error('只能上传jpg、jpeg、png、gif格式图片！')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB！')
    return
  }
  cacheFiles.value[type] = file
  previewUrls.value[type] = URL.createObjectURL(file)
  e.target.value = ''
}
const triggerFileInput = (type) => {
  type === 'face' ? faceFileRef.value.click() : bodyFileRef.value.click()
}
const handleDelete = (type) => {
  previewUrls.value[type] = ''
  cacheFiles.value[type] = null
}
const uploadPhoto = async (file, petIdVal, photoType) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('userId', petForm.userId)
  formData.append('petId', petIdVal)
  formData.append('photoType', photoType)
  return new Promise((resolve) => {
    setTimeout(async () => {
      const res = await uploadPetImg(formData)
      resolve(res)
    }, 0)
  })
}

// ========== 提交投保（核心修复：返回值判断 + 错误处理 + 调试日志） ==========
// ========== 提交投保（新增创建权益逻辑） ==========
const submitInsuranceOrder = async () => {
  if (isSubmitting.value || submitLock.value) {
    ElMessage.warning('正在提交中，请稍候...')
    return
  }
  if (!isPetTypeValid.value) {
    ElMessage.error('宠物类型与本保险适用类型不匹配，无法投保！')
    return
  }
  if (!insuranceDetail.value.id) {
    ElMessage.error('未获取到保险产品信息，无法投保！')
    return
  }
  if (!petForm.userId) {
    ElMessage.error('用户信息异常，请重新登录！')
    return
  }

  isSubmitting.value = true
  submitLock.value = true

  try {
    let petIdVal = selectedPetId.value

    // 新增宠物（原有逻辑不变）
    if (!petIdVal) {
      if (!petForm.petName || !petForm.petType || !petForm.petBirthday) {
        throw new Error('请填写完整的宠物信息！')
      }
      const addRes = await addPet({
        userId: petForm.userId,
        petName: petForm.petName,
        petBirthday: petForm.petBirthday,
        petType: petForm.petType,
        petGender: petForm.petGender,
        isSterilized: petForm.isSterilized
      })
      console.log('【调试】新增宠物返回：', addRes)
      if (!addRes || (addRes.code !== undefined && addRes.code !== 200)) {
        throw new Error(`创建宠物失败：${addRes?.msg || '未知错误'}`)
      }
      petIdVal = addRes.data?.petId || addRes.petId
      if (!petIdVal) throw new Error('创建宠物成功，但未返回宠物ID')
      ElMessage.success(`宠物ID生成成功：${petIdVal}`)

      // 上传照片（原有逻辑不变）
      if (cacheFiles.value.face) {
        const faceRes = await uploadPhoto(cacheFiles.value.face, petIdVal, 'face')
        if (faceRes?.code !== 200) throw new Error(`正脸照上传失败：${faceRes?.msg || '未知错误'}`)
        await updatePetPhoto({ petId: petIdVal, photoType: 'face', imgUrl: faceRes.data })
      }
      if (cacheFiles.value.body) {
        const bodyRes = await uploadPhoto(cacheFiles.value.body, petIdVal, 'body')
        if (bodyRes?.code !== 200) throw new Error(`全身照上传失败：${bodyRes?.msg || '未知错误'}`)
        await updatePetPhoto({ petId: petIdVal, photoType: 'body', imgUrl: bodyRes.data })
      }

      await fetchUserPetList(petForm.userId)
    }

    // 构建订单数据（原有逻辑不变）
    const orderData = {
      userId: Number(petForm.userId) || 0,
      petId: Number(petIdVal) || 0,
      insuranceId: Number(insuranceDetail.value.id) || 0,
      insuranceName: isThreeInsurance.value 
        ? { basic: '基础版宠物保障', advanced: '升级版宠物保障', premium: '尊享版宠物保障' }[activeTab.value]
        : insuranceDetail.value.insuranceName || '未知保险',
      paymentMethod: paymentMethod.value || 'lump',
      discountPremium: isThreeInsurance.value 
        ? threePackagePrice[activeTab.value] * 12 
        : Number(insuranceDetail.value.discountPremium) || 0,
      guaranteeCycle: isThreeInsurance.value ? 12 : Number(insuranceDetail.value.guaranteeCycle) || 12,
      monthlyPrice: isThreeInsurance.value 
        ? threePackagePrice[activeTab.value] 
        : (Number(insuranceDetail.value.discountPremium) || 0) / (Number(insuranceDetail.value.guaranteeCycle) || 12) * (paymentMethod.value === 'monthly' ? 1.016 : 1),
      totalAmount: isThreeInsurance.value 
        ? (paymentMethod.value === 'monthly' ? threePackagePrice[activeTab.value] : threePackagePrice[activeTab.value] * 12)
        : (paymentMethod.value === 'monthly' 
          ? ((Number(insuranceDetail.value.discountPremium) || 0) / (Number(insuranceDetail.value.guaranteeCycle) || 12)) * 1.016 
          : Number(insuranceDetail.value.discountPremium) || 0),
      orderStatus: 0,
      remark: `投保-${isThreeInsurance.value ? 
        { basic: '基础版', advanced: '升级版', premium: '尊享版' }[activeTab.value] : 
        insuranceDetail.value.insuranceName}-${petForm.petName || '未知宠物'}`,
      requestId: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    }

    // 调用创建订单接口（原有逻辑不变）
    console.log('【调试】提交订单数据：', orderData)
    const orderRes = await createInsuranceOrder(orderData)
    console.log('【调试】订单接口返回：', orderRes)

    // 核心修改：删除重复创建权益的逻辑（后端已自动创建）
    if (orderRes && orderRes.code === 200) {
      // 提示成功并跳转
      ElMessage.success('投保成功！权益已同步创建');
      setTimeout(() => {
        router.push({ path: '/user/myorder', query: { userId: petForm.userId } });
      }, 1500);
    } else {
      throw new Error(`创建订单失败：${orderRes?.msg || '后端未返回错误信息'}`);
    }
  } catch (err) {
    console.error('创建保险订单/权益失败：', err);
    ElMessage.error(err.message || '创建保险订单失败，请重试');
  } finally {
    isSubmitting.value = false;
    submitLock.value = false;
  }
}

// 返回上一页
const handleBack = () => {
  if (router.hasRoute('/pet/list')) {
    router.push(route.query.redirect || '/pet/list')
  } else {
    router.go(-1)
  }
}
</script>

<style scoped>
/* 基础样式 */
.claim-case .el-carousel { width: 100%; }
.page-container { max-width: 1200px; margin: 0 auto; }
.pet-insurance-page {
  background-image: url("@/assets/images/保障图标/保险详情页顶部.png");
  background-size: contain;
  background-position: center top;
  background-repeat: no-repeat;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding: 0 20px;
  box-sizing: border-box;
  position: relative;
}
.back-btn {
  position: sticky;
  top: 16px;
  left: 16px;
  z-index: 999;
  background: #fff;
  border: 1px solid #ddd;
  font-size: 20px;
  cursor: pointer;
  color: #333;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 套餐区域样式 */
.package-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 500px auto 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.tab-group {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.tab-btn {
  padding: 8px 20px;
  border: 1px solid #ddd;
  border-radius: 24px;
  background-color: #f5f5f5;
  color: #666;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
}
.tab-btn:hover { background-color: #e9f2fa; }
.tab-btn.active {
  background-color: #2196f3;
  color: #fff;
  border-color: #2196f3;
  transform: scale(1.05);
}
.package-img img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 16px;
}
.applicable-tip {
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}
.type-tag { color: #2196f3; font-weight: bold; }
.img-placeholder {
  width: 100%;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 8px;
  color: #999;
  font-size: 16px;
}

/* 被保宠物档案样式 */
.pet-archive {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.match-error {
  color: #ff4949;
  font-size: 14px;
  margin-bottom: 15px;
  padding: 8px;
  background-color: #fff2f2;
  border-radius: 4px;
}
.pet-archive h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.pet-list { display: flex; gap: 20px; flex-wrap: wrap; }
.pet-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 80px;
  cursor: pointer;
  position: relative;
}
.pet-item.disabled { opacity: 0.6; cursor: not-allowed; }
.pet-item.active { color: #2196f3; }
.pet-avatar {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 8px;
  border: 2px solid transparent;
}
.pet-item.active .pet-avatar { border-color: #2196f3; }
.pet-name { font-size: 14px; text-align: center; }
.pet-type-tag { font-size: 12px; color: #999; margin-top: 4px; }
.unmatch-tag {
  position: absolute;
  top: 0;
  right: 0;
  font-size: 10px;
  color: #fff;
  background-color: #ff4949;
  padding: 2px 4px;
  border-radius: 2px;
  font-weight: bold;
}
.add-pet .plus-icon {
  font-size: 24px;
  color: #409eff;
  margin-bottom: 8px;
}

/* 宠物表单样式 */
.pet-form {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.pet-form h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.form-group { margin-bottom: 20px; }
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #555;
}
.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 14px;
  transition: all 0.3s ease;
}
.form-group input:focus {
  border-color: #2196f3;
  outline: none;
  box-shadow: 0 0 0 2px rgba(33, 150, 243, 0.1);
}
.radio-group { display: flex; gap: 12px; flex-wrap: wrap; }
.radio-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}
.radio-btn.disabled { opacity: 0.6; cursor: not-allowed; background-color: #f9f9f9; }
.radio-btn:hover:not(.disabled) { background-color: #e9f2fa; }
.radio-btn.active {
  background-color: #2196f3;
  color: #fff;
  border-color: #2196f3;
}
.photo-upload-group { display: flex; gap: 20px; }
.photo-upload {
  width: 120px;
  height: 120px;
  position: relative;
  border: 1px dashed #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
}
.upload-content {
  width: 100%;
  height: 100%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.preview-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #606266;
}
.plus-icon {
  font-size: 24px;
  color: #409eff;
  margin-bottom: 8px;
}
.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: -1;
}
.delete-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 20px;
  height: 20px;
  line-height: 18px;
  text-align: center;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.8);
  color: #ff4949;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}
.delete-btn:hover { background-color: #ff4949; color: #fff; }
.tip { color: #999; font-size: 14px; margin-top: 8px; }

/* 缴费方式样式 */
.payment-method {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.payment-method h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.warning { color: #f57c00; font-weight: 500; }
.agree-group {
  margin-top: 20px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.agree-label {
  display: flex !important;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}
.agree-checkbox {
  margin-right: 8px;
  width: 16px !important;
  height: 16px;
}

/* 产品特色/理赔说明/案例 样式 */
.product-feature, .claim-instruction, .claim-case {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.product-feature h3, .claim-instruction h3, .claim-case h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.product-feature img, .claim-instruction img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 16px;
}

/* 底部投保栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 12px;
  background-color: #fff;
  border-top: 1px solid #eee;
  box-shadow: 0 -2px 8px rgba(0,0,0,0.05);
  z-index: 999;
}
.consult-btn, .add-btn, .insure-btn {
  background: none;
  border: none;
  cursor: pointer;
}
.consult-btn { color: #666; }
.add-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background-color: #2196f3;
  color: #fff;
  font-size: 20px;
}
.bottom-bar p {
  font-size: 16px;
  font-weight: bold;
  color: #f57c00;
  margin: 0;
}
.insure-btn {
  background-color: #2196f3;
  color: #fff;
  padding: 10px 24px;
  border-radius: 20px;
}
.insure-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>