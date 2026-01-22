<template>
  <div class="pet-guarantee">
    <!-- 外层居中容器 -->
    <div class="page-container">
      <div class="pet-insurance-page">
        <!-- 单独返回按钮（固定在内容区左上角） -->
        <button class="back-btn" @click="handleBack">&lt;</button>

        <!-- 套餐选择区域（移除tab-group，保留容器） -->
        <div class="package-section">
          <!-- 套餐图片展示（根据保险ID+contentType=3加载） -->
          <div class="package-img">
            <img 
              v-if="insuranceDetail.mediaList && getImgByContentType(insuranceDetail.mediaList, 3)" 
              :src="getImageUrl(getImgByContentType(insuranceDetail.mediaList, 3))" 
              :alt="insuranceDetail.insuranceName || '保险套餐图'" 
            />
            <div v-else class="img-placeholder">暂无套餐图片</div>
          </div>
        </div>

        <!-- 核心：被保宠物档案 和 表单 互斥显示 -->
        <!-- 有宠物时显示档案（隐藏表单） -->
        <div class="pet-archive" v-if="userPetList.length > 0">
          <h3>被保宠物档案</h3>
          <div class="pet-list">
            <!-- 已绑定的宠物 -->
            <div
              class="pet-item"
              v-for="(pet) in userPetList"
              :key="pet.petId"
              @click="selectPet(pet)"
              :class="{ active: selectedPetId === pet.petId }"
            >
              <img :src="pet.petFacePhoto || '@/assets/images/默认宠物头像.svg'" alt="宠物头像" class="pet-avatar" />
              <span class="pet-name">{{ pet.petName }}</span>
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
                :class="{ active: petForm.petType === '猫' }"
                @click="petForm.petType = '猫'"
              >
                猫
              </button>
              <button
                class="radio-btn"
                :class="{ active: petForm.petType === '狗' }"
                @click="petForm.petType = '狗'"
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
            <button class="radio-btn" :class="{ active: paymentMethod === 'monthly' }" @click="paymentMethod = 'monthly'">按月缴费(分12期)</button>
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

        <!-- 产品特色（根据保险ID+contentType=1加载） -->
        <div class="product-feature">
          <h3>产品特色</h3>
          <img 
            v-if="insuranceDetail.mediaList && getImgByContentType(insuranceDetail.mediaList, 1)" 
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

        <!-- 理赔案例轮播（根据保险ID+contentType=2加载） -->
        <div class="claim-case">
          <h3>理赔案例</h3>
          <el-carousel
            v-model="currentCase"
            indicator-position="outside"
            height="2100px"
            :autoplay="true"
            v-if="caseImages.length > 0"
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

        <!-- 底部投保栏（显示对应保险价格） -->
        <div class="bottom-bar">
          <button class="consult-btn">咨询</button>
          <button class="add-btn" @click="showAddPetForm">+</button>
          <p>{{ priceText }}</p>
          <button
            class="insure-btn"
            @click="completePetInfo"
            :disabled="!canSubmit || isSubmitting"
          >
            <span v-if="!isSubmitting">我要投保</span>
            <span v-else>提交中...</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 登录弹窗（如需显示登录提示可补充） -->
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
// 导入全局配置（核心修改：使用统一的图片URL方法）
import { getImageUrl } from '@/config/index.js'
// 导入api方法（新增：获取保险详情、媒体列表）
import { addPet, uploadPetImg, updatePetPhoto, getPetListByUserId, getInsuranceMediaList, getInsuranceDetail } from '@/api/user/index.js'

// 导入Element Plus组件
import { ElDatePicker, ElCarousel, ElCarouselItem, ElMessage, ElDialog, ElButton } from 'element-plus'

// ========== 新增：保险相关状态 ==========
const insuranceDetail = ref({
  mediaList: [],
  discountPremium: 0, // 折扣后保费
  guaranteeCycle: 12, // 保障周期（月）
  insuranceName: ''
})
const caseImages = ref([]) // 理赔案例图片列表（contentType=2）
const paymentMethod = ref('monthly') // 缴费方式

// ========== 原有状态保留 ==========
// 用户登录状态相关
const userInfo = ref({
  isLogin: false,
  userId: ''
})
const dialogVisible = ref(false)

// 路由实例
const router = useRouter()
const route = useRoute()

// 状态管理
const isSubmitting = ref(false)
const isAgreed = ref(false)
const userPetList = ref([])
const selectedPetId = ref('')

// 文件Ref
const faceFileRef = ref(null)
const bodyFileRef = ref(null)

// 前端缓存：图片文件 + 预览URL
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

// 理赔案例轮播
const currentCase = ref(0)

// ========== 新增：根据contentType获取图片路径 ==========
const getImgByContentType = (mediaList, type) => {
  if (!mediaList || !Array.isArray(mediaList)) return ''
  const item = mediaList.find(item => item.contentType === type)
  return item ? item.imgPath : ''
}

// ========== 核心修复：价格计算改为computed（确保响应式） ==========
const priceText = computed(() => {
  
  // 1. 强制类型转换 + 空值兜底（关键修复：直接取接口返回的原始字段）
  const discountPremium = Number(insuranceDetail.value.discountPremium) || 0
  const guaranteeCycle = Number(insuranceDetail.value.guaranteeCycle) || 12
  
  // 2. 空值判断：如果保费为0/负数，显示默认文案
  if (discountPremium <= 0) return '0.00元/月起'
  
  // 3. 核心计算
const monthlyPrice = discountPremium / guaranteeCycle
  const extraFee = monthlyPrice * 0.016
  const totalMonthlyPrice = monthlyPrice + extraFee // 总价 = 原价 + 附加费
  
  // 4. 保留2位小数（只在最后转字符串时处理）
  const monthlyPriceFixed = totalMonthlyPrice.toFixed(2)
  const lumpPrice = discountPremium.toFixed(2)
  
  // 5. 返回文案
  return paymentMethod.value === 'monthly' 
    ? `${monthlyPriceFixed}元/月起`  // 最终总价
    : `总计${lumpPrice}元`
})

// ========== 原有方法保留 ==========
// 添加宠物按钮逻辑
const showAddPetForm = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  try {
    router.push({
      path: '/pet-id-card',
      query: { userId: userInfo.value.userId }
    })
    ElMessage.success('正在前往添加宠物页面')
  } catch (err) {
    console.error('路由跳转失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  }
}

// 跳转登录页（弹窗回调）
const toLogin = () => {
  dialogVisible.value = false
  router.push('/login')
}

// 表单校验
const canSubmit = computed(() => {
  const baseValid = isAgreed.value && !isSubmitting.value
  if (!baseValid) return false

  if (selectedPetId.value) {
    return true
  }

  const basicValid = !!petForm.userId && !!petForm.petName && !!petForm.petType &&
                    !!petForm.petGender && !!petForm.petBirthday && !!petForm.isSterilized
  const photoValid = !!cacheFiles.value.face && !!cacheFiles.value.body
  return basicValid && photoValid
})

// ========== 核心修复：页面挂载逻辑（确保数据完整赋值） ==========
onMounted(async () => {
  // 1. 获取用户登录状态
  const loginData = JSON.parse(localStorage.getItem('userData') || '{}')
  userInfo.value = {
    isLogin: !!loginData.userId,
    userId: loginData.userId || ''
  }

  const userId = userInfo.value.userId || route.query.userId
  if (!userId) {
    ElMessage.warning('请先登录后再投保！')
    return
  }
  petForm.userId = String(userId)

  // 2. 获取保险ID（从路由参数中）
  const insuranceId = route.query.id
  if (!insuranceId) {
    ElMessage.warning('未获取到保险产品信息！')
    return
  }

  console.log('【初始化】当前保险ID：', insuranceId)

  // 3. 串行加载数据（核心修复：确保保险详情完整赋值）
  try {
    // 第一步：加载保险详情（重点：完整拷贝所有字段）
    const detailRes = await getInsuranceDetail(insuranceId)
if (detailRes.code === 200) {
  // 第一步：先整体赋值
  insuranceDetail.value = {
    mediaList: [],
    discountPremium: 0,
    guaranteeCycle: 12,
    insuranceName: '',
    ...detailRes.data
  }
  // 第二步：强制显式赋值关键字段（核心修复：从 insurance 子对象里取！）
  insuranceDetail.value.discountPremium = Number(detailRes.data.insurance.discountPremium || 0)
  insuranceDetail.value.guaranteeCycle = Number(detailRes.data.insurance.guaranteeCycle || 12)
  
  console.log('保费字段：', insuranceDetail.value.discountPremium)
  console.log('周期字段：', insuranceDetail.value.guaranteeCycle)
} else {
      ElMessage.error('获取保险详情失败：' + detailRes.msg)
    }

    // 第二步：加载媒体列表
    const mediaRes = await getInsuranceMediaList(insuranceId)
    if (mediaRes.code === 200) {
      insuranceDetail.value.mediaList = mediaRes.data
      // 提取contentType=2的所有图片（理赔案例）
      caseImages.value = mediaRes.data
        .filter(item => item.contentType === 2)
        .map(item => item.imgPath)
      console.log('【初始化】媒体列表：', mediaRes.data)
    } else {
      ElMessage.error('获取媒体列表失败：' + mediaRes.msg)
    }

    // 第三步：加载宠物列表
    await fetchUserPetList(userId)

    // 额外验证：打印价格计算结果
    console.log('【初始化】最终价格：', priceText.value)
  } catch (err) {
    console.error('【初始化】数据加载失败：', err)
    ElMessage.error('页面初始化失败，请重试')
  }
})

// ========== 原有方法保留 ==========
// 获取用户宠物列表
const fetchUserPetList = async (userId) => {
  try {
    const res = await getPetListByUserId(userId)
    if (res.code === 200) {
      userPetList.value = res.data
      if (userPetList.value.length > 0) {
        selectPet(userPetList.value[0])
      }
    }
  } catch (err) {
    console.error('获取用户宠物列表失败：', err)
    userPetList.value = []
  }
}

// 选择宠物
const selectPet = (pet) => {
  selectedPetId.value = pet.petId
  petForm.petName = pet.petName
  petForm.petBirthday = pet.petBirthday
  petForm.petType = pet.petType
  petForm.petGender = pet.petGender
  petForm.isSterilized = pet.isSterilized
  petForm.petFacePhoto = pet.petFacePhoto
  petForm.petBodyPhoto = pet.petBodyPhoto
  previewUrls.value.face = pet.petFacePhoto
  previewUrls.value.body = pet.petBodyPhoto
}

// 文件上传相关
const handleFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  const fileType = file.type
  const allowTypes = [
    'image/jpg', 'image/jpeg', 'image/png', 'image/gif',
    'image/pjpeg', 'image/x-png'
  ]
  if (!allowTypes.includes(fileType)) {
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

// 提交投保逻辑
const completePetInfo = async () => {
  isSubmitting.value = true
  try {
    let petIdVal = selectedPetId.value

    if (!petIdVal) {
      const addRes = await addPet({
        userId: petForm.userId,
        petName: petForm.petName,
        petBirthday: petForm.petBirthday,
        petType: petForm.petType,
        petGender: petForm.petGender,
        isSterilized: petForm.isSterilized
      })
      if (addRes.code !== 200) throw new Error(`创建宠物失败：${addRes.msg}`)
      petIdVal = addRes.data.petId
      ElMessage.success(`宠物ID生成成功：${petIdVal}`)

      if (cacheFiles.value.face) {
        const faceRes = await uploadPhoto(cacheFiles.value.face, petIdVal, 'face')
        if (faceRes.code !== 200) throw new Error(`正脸照上传失败：${faceRes.msg}`)
        await updatePetPhoto({ petId: petIdVal, photoType: 'face', imgUrl: faceRes.data })
      }
      if (cacheFiles.value.body) {
        const bodyRes = await uploadPhoto(cacheFiles.value.body, petIdVal, 'body')
        if (bodyRes.code !== 200) throw new Error(`全身照上传失败：${bodyRes.msg}`)
        await updatePetPhoto({ petId: petIdVal, photoType: 'body', imgUrl: bodyRes.data })
      }

      await fetchUserPetList(petForm.userId)
    }

    ElMessage.success(selectedPetId.value ? '投保成功！' : `宠物身份证创建完成并投保成功！ID：${petIdVal}`)
    setTimeout(() => {
      router.go(-1)
    }, 1500)
  } catch (err) {
    console.error('===== 投保失败 =====', err)
    ElMessage.error(err.message || '提交失败，请重试')
  } finally {
    isSubmitting.value = false
    cacheFiles.value = { face: null, body: null }
    previewUrls.value = { face: '', body: '' }
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
.claim-case .el-carousel {
  width: 100%;
}
.page-container {
  max-width: 1200px;
  margin: 0 auto;
}
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

/* 套餐区域样式（移除tab-group后调整） */
.package-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 500px auto 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.package-img img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 16px;
}
/* 图片占位符样式 */
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
.pet-archive h3 {
  font-size: 20px;
  margin-bottom: 15px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.pet-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
.pet-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 80px;
  cursor: pointer;
}
.pet-item.active {
  color: #2196f3;
}
.pet-avatar {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  object-fit: cover;
  margin-bottom: 8px;
  border: 2px solid transparent;
}
.pet-item.active .pet-avatar {
  border-color: #2196f3;
}
.pet-name {
  font-size: 14px;
  text-align: center;
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
.form-group {
  margin-bottom: 20px;
}
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
.radio-group {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.radio-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f5f5f5;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}
.radio-btn:hover {
  background-color: #e9f2fa;
}
.radio-btn.active {
  background-color: #2196f3;
  color: #fff;
  border-color: #2196f3;
}
.photo-upload-group {
  display: flex;
  gap: 20px;
}
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
.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
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
.delete-btn:hover {
  background-color: #ff4949;
  color: #fff;
}
.tip {
  color: #999;
  font-size: 14px;
  margin-top: 8px;
}

/* 缴费方式样式（包含协议勾选） */
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
.warning {
  color: #f57c00;
  font-weight: 500;
}
/* 协议勾选样式（移至缴费区后） */
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
  z-index: 999; /* 确保在最上层显示 */
}
.consult-btn, .add-btn, .insure-btn {
  background: none;
  border: none;
  cursor: pointer;
}
.consult-btn {
  color: #666;
}
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
  margin: 0; /* 清除默认margin */
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