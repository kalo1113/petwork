<template>
  <div class="pet-id-card-container">
    <!-- 返回上一页按钮 -->
    <div class="back-btn-wrap">
      <button class="back-btn" @click="handleBack">
        ←
      </button>
    </div>

    <!-- 顶部标题区 -->
    <div class="header">
      <h1>{{ isEditMode ? '编辑宠物身份证' : '宠物身份证' }}</h1>
      <p>宠物鼻纹识别技术</p>
    </div>

    <!-- 功能区 -->
    <div class="features">
      <div class="feature-item">
        <img :src="featureIcon1" alt="专属身份ID图标" class="feature-icon" />
        <span>专属身份ID</span>
      </div>
      <div class="feature-item">
        <img :src="featureIcon2" alt="宠物防丢保护图标" class="feature-icon" />
        <span>宠物防丢保护</span>
      </div>
      <div class="feature-item">
        <img :src="featureIcon3" alt="千元医疗保障图标" class="feature-icon" />
        <span>千元医疗保障</span>
      </div>
    </div>

    <!-- 宠物信息卡片 -->
    <div class="pet-card">
      <div class="avatar-placeholder">
        <img
          :src="petForm.petType === '猫' ? catIcon : (petForm.petType === '狗' ? dogIcon : petAvatarPlaceholder)"
          alt="宠物种类头像"
          class="avatar-img"
        />
      </div>
      <div class="pet-info">
        <h2>{{ petForm.petName || '昵称' }}</h2>
        <div class="info-item card-info">
          <span class="label">生日：</span>
          <span class="value">{{ petForm.petBirthday || '请选择' }}</span>
        </div>
        <div class="info-item card-info">
          <span class="label">性别：</span>
          <span class="value">{{ petForm.petGender || '未选择' }}</span>
        </div>
        <!-- 显示生成的宠物ID -->
        <div class="info-item card-info" v-if="petId">
          <span class="label">宠物ID：</span>
          <span class="value">{{ petId }}</span>
        </div>
      </div>
    </div>

    <!-- 核心表单 -->
    <div class="form-wrapper">
      <p class="form-title">{{ isEditMode ? '修改宠物信息' : '请完善宠物信息' }}</p>

      <!-- 宠物种类 -->
      <div class="form-row">
        <span class="form-label">种类</span>
        <div class="radio-group type-group">
          <label class="radio-item">
            <img :src="catIcon" alt="猫猫图标" class="radio-icon" />
            <span>猫猫</span>
            <input type="radio" v-model="petForm.petType" value="猫" class="radio-input">
          </label>
          <label class="radio-item">
            <img :src="dogIcon" alt="狗狗图标" class="radio-icon" />
            <span>狗狗</span>
            <input type="radio" v-model="petForm.petType" value="狗" class="radio-input">
          </label>
        </div>
      </div>

      <!-- 宠物昵称 -->
      <div class="form-row">
        <span class="form-label">昵称</span>
        <input
          type="text"
          v-model="petForm.petName"
          placeholder="请输入爱宠昵称"
          class="form-input native-input"
        >
      </div>

      <!-- 出生日期 -->
      <div class="form-row">
        <span class="form-label">生日</span>
        <input
          type="date"
          v-model="petForm.petBirthday"
          placeholder="请选择"
          class="form-input native-input"
        >
      </div>

      <!-- 宠物性别 -->
      <div class="form-row">
        <span class="form-label">性别</span>
        <div class="radio-group gender-group">
          <label class="radio-item">
            <span>公</span>
            <input type="radio" v-model="petForm.petGender" value="公" class="radio-input">
          </label>
          <label class="radio-item">
            <span>母</span>
            <input type="radio" v-model="petForm.petGender" value="母" class="radio-input">
          </label>
        </div>
      </div>

      <!-- 绝育状态 -->
      <div class="form-row">
        <span class="form-label">绝育情况</span>
        <div class="radio-group sterilize-group">
          <label class="radio-item">
            <span>未绝育</span>
            <input type="radio" v-model="petForm.isSterilized" value="否" class="radio-input">
          </label>
          <label class="radio-item">
            <span>已绝育</span>
            <input type="radio" v-model="petForm.isSterilized" value="是" class="radio-input">
          </label>
          <label class="radio-item">
            <span>未知</span>
            <input type="radio" v-model="petForm.isSterilized" value="未知" class="radio-input">
          </label>
        </div>
      </div>

      <!-- 照片上传 -->
      <div class="form-row photo-row">
        <span class="form-label">宠物照片</span>
        <div class="photo-upload-group">
          <!-- 全身照 -->
          <div class="photo-upload">
            <img v-if="previewUrls.body || petForm.petBodyPhoto" :src="previewUrls.body || petForm.petBodyPhoto" class="upload-img" />
            <div v-else class="upload-placeholder">
              <span class="plus-icon">+</span>
            </div>
            <div class="photo-desc">全身照</div>
            <input
              type="file"
              ref="bodyFileRef"
              accept="image/*"
              class="file-input"
              @change="handleFileChange($event, 'body')"
            >
          </div>
          <!-- 正脸照 -->
          <div class="photo-upload">
            <img v-if="previewUrls.face || petForm.petFacePhoto" :src="previewUrls.face || petForm.petFacePhoto" class="upload-img" />
            <div v-else class="upload-placeholder">
              <span class="plus-icon">+</span>
            </div>
            <div class="photo-desc">正脸照</div>
            <input
              type="file"
              ref="faceFileRef"
              accept="image/*"
              class="file-input"
              @change="handleFileChange($event, 'face')"
            >
          </div>
        </div>
      </div>

      <!-- 协议勾选（编辑模式隐藏） -->
      <div class="agreement-row" v-if="!isEditMode">
        <label class="checkbox-item">
          <input
            type="checkbox"
            v-model="isAgreed"
            class="checkbox-input"
          >
          <span>阅读并同意《宠物平台服务协议》</span>
        </label>
      </div>

      <!-- 提交按钮（动态文字） -->
      <button
        class="submit-btn"
        @click="completePetInfo"
        :disabled="!canSubmit || isSubmitting"
      >
        {{ isSubmitting ? '提交中...' : (isEditMode ? '修改宠物身份证' : '领取宠物身份证') }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 导入api方法
import { addPet, uploadPetImg, updatePetPhoto, getPetInfoById, updatePetInfo } from '@/api/user/index.js'
// 导入图片
import featureIcon1 from '@/assets/images/我的图标/专属身份id.svg'
import featureIcon2 from '@/assets/images/我的图标/宠物防丢保护.svg'
import featureIcon3 from '@/assets/images/我的图标/千元医疗保障.svg'
import petAvatarPlaceholder from '@/assets/images/我的图标/默认身份状态.svg'
import catIcon from '@/assets/images/我的图标/小猫猫.svg'
import dogIcon from '@/assets/images/我的图标/小狗狗.svg'

// 消息提示
const showMessage = (type, msg) => {
  try {
    import('element-plus').then(({ ElMessage }) => {
      ElMessage({
        type: type,
        message: msg,
        duration: 3000
      })
    })
  } catch (e) {
    alert(`${type === 'success' ? '成功' : '错误'}：${msg}`)
  }
}

// 路由实例
const router = useRouter()
const route = useRoute()

// 状态管理
const isSubmitting = ref(false)
const isAgreed = ref(false)
const petId = ref('')
const isEditMode = ref(false) // 标识是否为编辑模式

// 文件Ref
const bodyFileRef = ref(null)
const faceFileRef = ref(null)

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

// 表单校验（区分新增/编辑模式）
const canSubmit = computed(() => {
  // 基础信息校验
  const basicValid = !!petForm.userId && !!petForm.petName && !!petForm.petType &&
                    !!petForm.petGender && !!petForm.petBirthday && !!petForm.isSterilized
  // 新增模式：需要照片 + 协议勾选
  if (!isEditMode.value) {
    const photoValid = !!cacheFiles.value.face && !!cacheFiles.value.body
    return basicValid && photoValid && isAgreed.value && !isSubmitting.value
  }
  // 编辑模式：照片可选（有缓存文件 或 已有照片URL）
  const photoValid = (!!cacheFiles.value.face || !!petForm.petFacePhoto) &&
                     (!!cacheFiles.value.body || !!petForm.petBodyPhoto)
  return basicValid && photoValid && !isSubmitting.value
})

// 页面挂载：初始化用户ID + 判断编辑模式 + 拉取宠物信息
onMounted(async () => {
  // 1. 初始化用户ID
  let userId = localStorage.getItem('userId') || route.query.userId || '10001'
  userId = /^\d+$/.test(userId) ? Number(userId) : 10001
  petForm.userId = userId

  // 2. 判断是否为编辑模式（有petId参数 且 type=edit）
  const queryPetId = route.query.petId
  const queryType = route.query.type
  if (queryPetId && queryType === 'edit') {
    isEditMode.value = true
    petId.value = queryPetId
    // 3. 编辑模式：拉取宠物信息回显
    try {
      const res = await getPetInfoById(Number(queryPetId))
      if (res.code === 200) {
        const petData = res.data
        // 回显基础信息
        petForm.petName = petData.petName || ''
        petForm.petBirthday = petData.petBirthday || ''
        petForm.petType = petData.petType || ''
        petForm.petGender = petData.petGender || ''
        petForm.isSterilized = petData.isSterilized || ''
        petForm.petFacePhoto = petData.petFacePhoto || ''
        petForm.petBodyPhoto = petData.petBodyPhoto || ''
        // 照片预览（使用已有URL）
        previewUrls.value.face = petData.petFacePhoto || ''
        previewUrls.value.body = petData.petBodyPhoto || ''
        showMessage('success', '宠物信息加载成功')
      }
    } catch (err) {
      console.error('拉取宠物信息失败：', err)
      showMessage('error', '加载宠物信息失败，请返回重试')
    }
  }
})

// 文件选择回调
const handleFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  console.log(`选择${type}文件：`, file)
  // 图片类型校验
  const fileType = file.type
  const allowTypes = [
    'image/jpg', 'image/jpeg', 'image/png', 'image/gif',
    'image/pjpeg', 'image/x-png'
  ]
  if (!allowTypes.includes(fileType)) {
    showMessage('error', '只能上传jpg、jpeg、png、gif格式图片！')
    return
  }

  // 图片大小校验
  if (file.size > 5 * 1024 * 1024) {
    showMessage('error', '图片大小不能超过5MB！')
    return
  }

  // 缓存文件 + 生成预览URL
  cacheFiles.value[type] = file
  previewUrls.value[type] = URL.createObjectURL(file)
  e.target.value = ''
}

// 独立上传函数
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

// 核心：提交表单（区分新增/编辑逻辑）
const completePetInfo = async () => {
  isSubmitting.value = true
  try {
    let petIdVal = petId.value
    console.log(`===== ${isEditMode.value ? '编辑' : '新增'}宠物开始 =====`)
    // ========== 新增模式逻辑 ==========
    if (!isEditMode.value) {
      // 1. 创建宠物
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
      showMessage('success', `宠物ID生成成功：${petIdVal}`)

      // 2. 上传新照片（新增模式必须传）
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
      showMessage('success', `宠物身份证创建完成！ID：${petIdVal}`)
    } else {
    // ========== 编辑模式逻辑 ==========

      // 1. 更新基础信息
      const updateRes = await updatePetInfo({
        petId: Number(petIdVal),
        userId: petForm.userId,
        petName: petForm.petName,
        petBirthday: petForm.petBirthday,
        petType: petForm.petType,
        petGender: petForm.petGender,
        isSterilized: petForm.isSterilized
      })
      if (updateRes.code !== 200) throw new Error(`更新宠物信息失败：${updateRes.msg}`)

      // 2. 上传新照片（编辑模式可选，有新文件才传）
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
      showMessage('success', '宠物身份证修改完成！')
    }

    // ========== 通用：跳转返回 ==========
    setTimeout(() => {
      router.go(-1)
    }, 1500)
  } catch (err) {
    console.error(`===== ${isEditMode.value ? '编辑' : '新增'}宠物失败 =====`, err)
    showMessage('error', err.message || '提交失败，请重试')
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
/* 原有样式不变，无需修改 */
.pet-id-card-container {
  width: 100%;
  max-width: 1200px;
  padding: 20px;
  font-family: "微软雅黑", sans-serif;
  background: url('~@/assets/images/我的图标/背景图.jpg') no-repeat center center;
  background-size: cover;
  min-height: 100vh;
  box-sizing: border-box;
  margin: 0 auto;
}

.back-btn {
  font-size: 30px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
  padding: 0;
}

.header {
  text-align: center;
  margin-bottom: 20px;
  padding: 10px;
}
.header h1 {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px;
}
.header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.features {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
}
.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #666;
  width: 120px;
}
.feature-icon {
  width: 60px;
  height: 60px;
  margin-bottom: 8px;
  object-fit: contain;
  background: #eff1fb;
  border-radius: 50%;
  padding: 10px;
}

.pet-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  border-radius: 12px;
  padding: 20px;
  color: #263238;
  margin-bottom: 25px;
}
.avatar-placeholder {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}
.avatar-img {
  width: 60%;
  height: 60%;
  object-fit: contain;
}
.pet-info h2 {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px;
  color: #1976d2;
}
.card-info {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}
.card-info .label {
  font-weight: 600;
  color: #263238;
  margin-right: 5px;
}
.card-info .value {
  color: #455a64;
}

.form-wrapper {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.form-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 15px;
}

.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}
.form-label {
  width: 60px;
  font-size: 14px;
  color: #333;
  margin-right: 10px;
  text-align: left;
  flex-shrink: 0;
}

.radio-group {
  display: flex;
  gap: 20px;
  align-items: center;
  flex: 1;
}
.radio-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  font-size: 14px;
  color: #333;
}
.radio-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}
.radio-input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.native-input {
  width: 100%;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}
.native-input:focus {
  border-color: #409eff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.photo-row {
  align-items: flex-start;
}
.photo-row .form-label {
  margin-top: 5px;
}
.photo-upload-group {
  display: flex;
  gap: 20px;
  flex: 1;
}
.photo-upload {
  width: 80px;
  height: 80px;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  background: #fbfbfb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.photo-upload:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}
.file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.plus-icon {
  font-size: 20px;
  color: #ff9900;
  font-weight: bold;
}
.upload-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.photo-desc {
  font-size: 12px;
  color: #666;
  margin-top: 5px;
}

.agreement-row {
  margin-bottom: 20px;
  font-size: 12px;
  color: #666;
}
.checkbox-item {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
}
.checkbox-input {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 500;
  background-color: #1677ff;
  border: none;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
}
.submit-btn:hover {
  background-color: #0f6de0;
}
.submit-btn:disabled {
  background-color: #8cb7f5;
  cursor: not-allowed;
}
</style>
