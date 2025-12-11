<template>
  <div class="pet-id-card-container">
    <!-- 返回上一页按钮 -->
    <div class="back-btn-wrap">
      <el-button
        type="text"
        class="back-btn"
        @click="handleBack"
        icon="el-icon-arrow-left"
      >
        &lt;
      </el-button>
    </div>

    <!-- 顶部标题区 -->
    <div class="header">
      <h1>宠物身份证</h1>
      <p>宠物鼻纹识别技术</p>
    </div>

    <!-- 功能入口区 -->
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
      </div>
    </div>

    <!-- 核心表单 -->
    <div class="form-wrapper">
      <p class="form-title">请完善宠物信息</p>

      <!-- 宠物种类 -->
      <div class="form-row">
        <span class="form-label">种类</span>
        <el-radio-group v-model="petForm.petType" class="radio-group type-group">
          <el-radio label="猫" class="radio-item">
            <img :src="catIcon" alt="猫猫图标" class="radio-icon" />
            <span>猫猫</span>
          </el-radio>
          <el-radio label="狗" class="radio-item">
            <img :src="dogIcon" alt="狗狗图标" class="radio-icon" />
            <span>狗狗</span>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 宠物昵称 -->
      <div class="form-row">
        <span class="form-label">昵称</span>
        <el-input
          v-model="petForm.petName"
          placeholder="请输入爱宠昵称"
          class="form-input"
          :disabled="false"
        ></el-input>
      </div>

      <!-- 出生日期 -->
      <div class="form-row">
        <span class="form-label">生日</span>
        <el-date-picker
          v-model="petForm.petBirthday"
          type="date"
          placeholder="请选择"
          class="form-input"
          value-format="YYYY-MM-DD"
          :disabled="false"
        ></el-date-picker>
      </div>

      <!-- 宠物性别 -->
      <div class="form-row">
        <span class="form-label">性别</span>
        <el-radio-group v-model="petForm.petGender" class="radio-group gender-group">
          <el-radio label="公" class="radio-item">公</el-radio>
          <el-radio label="母" class="radio-item">母</el-radio>
        </el-radio-group>
      </div>

      <!-- 绝育状态 -->
      <div class="form-row">
        <span class="form-label">绝育情况</span>
        <el-radio-group v-model="petForm.isSterilized" class="radio-group sterilize-group">
          <el-radio label="否" class="radio-item">未绝育</el-radio>
          <el-radio label="是" class="radio-item">已绝育</el-radio>
          <el-radio label="未知" class="radio-item">未知</el-radio>
        </el-radio-group>
      </div>

      <!-- 照片上传（修复禁用逻辑：新增时先创建petId再上传） -->
      <div class="form-row photo-row">
        <span class="form-label">宠物照片</span>
        <span class="form-label">示例①</span>
        <div class="photo-upload-group">
          <!-- 全身照上传 -->
          <el-upload
            class="photo-upload"
            :action="uploadUrl"
            :show-file-list="false"
            :data="uploadBodyData"
            :on-success="handleBodyPhotoSuccess"
            :before-upload="beforeUpload"
            :disabled="!canUploadPhoto"
          >
            <img v-if="bodyPhotoUrl" :src="bodyPhotoUrl" class="upload-img" />
            <div v-else class="upload-placeholder">
              <span class="plus-icon">+</span>
            </div>
            <div class="photo-desc">全身照</div>
          </el-upload>
          <!-- 正脸照上传 -->
          <el-upload
            class="photo-upload"
            :action="uploadUrl"
            :show-file-list="false"
            :data="uploadFaceData"
            :on-success="handleFacePhotoSuccess"
            :before-upload="beforeUpload"
            :disabled="!canUploadPhoto"
          >
            <img v-if="facePhotoUrl" :src="facePhotoUrl" class="upload-img" />
            <div v-else class="upload-placeholder">
              <span class="plus-icon">+</span>
            </div>
            <div class="photo-desc">正脸照</div>
          </el-upload>
        </div>
      </div>

      <!-- 协议勾选 -->
      <div class="agreement-row">
        <el-checkbox v-model="isAgreed" class="agreement-checkbox" :disabled="false">
          阅读并同意《宠物平台服务协议》
        </el-checkbox>
      </div>

      <!-- 提交按钮 -->
      <el-button
        type="primary"
        class="submit-btn"
        @click="completePetInfo"
        :disabled="!canSubmit || isSubmitting"
      >
        领取宠物身份证
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElButton, ElRadio, ElRadioGroup, ElInput, ElDatePicker, ElCheckbox, ElUpload } from 'element-plus'
import request from '@/utils/request'

// 导入图片
import featureIcon1 from '@/assets/images/我的图标/专属身份id.svg'
import featureIcon2 from '@/assets/images/我的图标/宠物防丢保护.svg'
import featureIcon3 from '@/assets/images/我的图标/千元医疗保障.svg'
import petAvatarPlaceholder from '@/assets/images/我的图标/默认身份状态.svg'
import catIcon from '@/assets/images/我的图标/小猫猫.svg'
import dogIcon from '@/assets/images/我的图标/小狗狗.svg'

// 路由实例
const router = useRouter()
const route = useRoute()

// 表单状态
const isSubmitting = ref(false)
const petId = ref(route.query.petId || '')
const facePhotoUrl = ref('')
const bodyPhotoUrl = ref('')
const isAgreed = ref(false)

// 表单数据（确保初始化完成）
const petForm = reactive({
  petId: '',
  userId: '',
  petName: '',
  petBirthday: '',
  petType: '',
  petGender: '',
  isSterilized: '',
  petFacePhoto: '',
  petBodyPhoto: '',
  createTime: '',
  updateTime: ''
})

// 上传接口地址
const uploadUrl = ref('/pet/upload')

// 计算属性：是否可上传照片（新增时先创建petId，编辑时直接上传）
const canUploadPhoto = computed(() => {
  // 编辑模式：有petId即可上传
  if (petId.value) return true
  // 新增模式：至少填完基础信息才能上传（避免空petId）
  return !!petForm.userId && !!petForm.petName && !!petForm.petType && !!petForm.petGender && !!petForm.petBirthday && !!petForm.isSterilized
})

// 计算属性：是否可提交
const canSubmit = computed(() => {
  // 基础信息必填
  const basicInfoComplete = !!petForm.userId && !!petForm.petName && !!petForm.petType && !!petForm.petGender && !!petForm.petBirthday && !!petForm.isSterilized
  // 协议勾选
  const agreementChecked = isAgreed.value
  // 照片必填（新增/编辑都需要）
  const photoComplete = !!facePhotoUrl.value && !!bodyPhotoUrl.value
  return basicInfoComplete && agreementChecked && photoComplete
})

// 上传参数
const uploadFaceData = computed(() => ({
  userId: petForm.userId,
  petId: petId.value,
  photoType: 'face'
}))
const uploadBodyData = computed(() => ({
  userId: petForm.userId,
  petId: petId.value,
  photoType: 'body'
}))

// 页面挂载
onMounted(async () => {
  try {
    // 1. 获取用户ID
    const routeUserId = route.query.userId
    const loginUser = JSON.parse(localStorage.getItem('loginUser') || '{}')
    if (routeUserId) {
      petForm.userId = routeUserId
    } else if (loginUser.userId) {
      petForm.userId = loginUser.userId
    } else {
      ElMessage.warning('请先登录！')
      router.push({ path: '/login', query: { redirect: route.fullPath } })
      return
    }

    // 2. 编辑模式加载宠物信息
    if (petId.value) {
      const res = await request({ url: `/pet/${petId.value}`, method: 'get' })
      Object.assign(petForm, res)
      facePhotoUrl.value = res.petFacePhoto || ''
      bodyPhotoUrl.value = res.petBodyPhoto || ''
    }
  } catch (err) {
    ElMessage.error('页面初始化失败：' + err.message)
  }
})

// 图片上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')

  if (!isImage) {
    ElMessage.error('只能上传图片格式（JPG/PNG等）！')
    return false
  }
  // 新增模式：先创建petId再上传
  if (!petId.value) {
    return createPetIdBeforeUpload()
  }
  return true
}

// 新增模式：先创建petId（临时）
const createPetIdBeforeUpload = async () => {
  try {
    // 先提交基础信息获取petId
    const res = await request({
      url: '/pet/add',
      method: 'post',
      data: {
        userId: petForm.userId,
        petName: petForm.petName,
        petType: petForm.petType,
        petGender: petForm.petGender,
        petBirthday: petForm.petBirthday,
        isSterilized: petForm.isSterilized
      }
    })
    petId.value = res.data?.petId || res.petId
    ElMessage.success('宠物基础信息已保存，可上传照片')
    return true
  } catch (err) {
    ElMessage.error('创建宠物ID失败：' + err.message)
    return false
  }
}

// 正脸照上传成功
const handleFacePhotoSuccess = async (response) => {
  try {
    if (response && !response.startsWith('上传失败')) {
      facePhotoUrl.value = response
      await updatePetPhoto(petId.value, 'face', response)
      ElMessage.success('正脸照上传成功！')
    } else {
      ElMessage.error('正脸照上传失败：' + (response || '未知错误'))
    }
  } catch (err) {
    ElMessage.error('正脸照处理失败：' + err.message)
  }
}

// 全身照上传成功
const handleBodyPhotoSuccess = async (response) => {
  try {
    if (response && !response.startsWith('上传失败')) {
      bodyPhotoUrl.value = response
      await updatePetPhoto(petId.value, 'body', response)
      ElMessage.success('全身照上传成功！')
    } else {
      ElMessage.error('全身照上传失败：' + (response || '未知错误'))
    }
  } catch (err) {
    ElMessage.error('全身照处理失败：' + err.message)
  }
}

// 关联照片到宠物
const updatePetPhoto = async (petId, photoType, imgUrl) => {
  await request({
    url: '/pet/update-photo',
    method: 'put',
    params: { petId, photoType, imgUrl }
  })
}

// 提交表单
const completePetInfo = async () => {
  isSubmitting.value = true
  try {
    let res
    if (petId.value) {
      // 更新
      petForm.petId = petId.value
      res = await request({ url: '/pet/update', method: 'put', data: petForm })
    } else {
      // 新增（理论上不会走到这里，因为上传时已创建petId）
      res = await request({ url: '/pet/add', method: 'post', data: petForm })
      petId.value = res.data?.petId || res.petId
    }

    if (res.code === 200 || res === '修改成功' || res === '新增成功') {
      ElMessage.success('宠物身份证领取成功！')
      router.push({ path: `/pet-card/${petId.value}`, query: { userId: petForm.userId } })
    } else {
      ElMessage.error('操作失败：' + (res.msg || res || '未知错误'))
    }
  } catch (err) {
    ElMessage.error('提交失败：' + err.message)
  } finally {
    isSubmitting.value = false
  }
}

// 返回上一页
const handleBack = () => {
  try {
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (err) {
    router.push('/')
  }
}
</script>

<style scoped>
/* 基础容器 */
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
  position: relative;
  z-index: 1; /* 确保容器层级正常 */
}

/* 返回按钮 */
.back-btn {
  font-size: 30px;
  display: flex;
  align-items: center;
  color: #333;
  z-index: 10; /* 按钮层级 */
}

/* 顶部标题 */
.header {
  text-align: center;
  margin-bottom: 20px;
  padding: 10px;
  position: relative;
  z-index: 10;
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

/* 功能区 */
.features {
  display: flex;
  justify-content: space-around;
  margin-bottom: 30px;
  text-align: center;
  padding: 15px;
  border-radius: 8px;
  position: relative;
  z-index: 10;
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

/* 宠物卡片 */
.pet-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #e3f2fd, #bbdefb);
  border-radius: 12px;
  padding: 20px;
  color: #263238;
  margin-bottom: 25px;
  position: relative;
  z-index: 10;
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

/* 表单包裹层 */
.form-wrapper {
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 8px;
  position: relative;
  z-index: 10; /* 表单层级高于背景 */
  box-shadow: 0 2px 10px rgba(0,0,0,0.1); /* 增加阴影，避免被遮挡 */
}
.form-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 15px;
}

/* 表单行 */
.form-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  position: relative;
  z-index: 10;
}
.form-label {
  width: 60px;
  font-size: 14px;
  color: #333;
  margin-right: 10px;
  text-align: left;
  flex-shrink: 0; /* 固定标签宽度，不被压缩 */
}

/* 单选组 */
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
  position: relative;
  z-index: 10;
  /* 修复Element Radio点击区域 */
  &:hover {
    background-color: #f5f7fa;
    border-radius: 4px;
  }
}
.radio-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

/* 输入框/选择器 */
.form-input {
  height: 32px;
  font-size: 14px;
  flex: 1;
  border-radius: 4px;
  position: relative;
  z-index: 10;
  /* 修复输入框焦点样式 */
  &:focus {
    border-color: #409eff;
    outline: none;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
}

/* 照片上传 */
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
  position: relative;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  cursor: pointer;
  overflow: hidden;
  background: #fbfbfb;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  /* 修复上传按钮点击区域 */
  &:hover {
    border-color: #409eff;
    background-color: #f5f7fa;
  }
  /* 解决Element Upload覆盖问题 */
  :deep(.el-upload) {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }
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

/* 协议勾选 */
.agreement-row {
  margin-bottom: 20px;
  font-size: 12px;
  color: #666;
  position: relative;
  z-index: 10;
  /* 修复复选框点击 */
  :deep(.el-checkbox) {
    cursor: pointer;
  }
}
.agreement-checkbox {
  font-size: 12px;
  color: #666;
}

/* 提交按钮 */
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
  position: relative;
  z-index: 10;
}
.submit-btn:hover {
  background-color: #0f6de0;
}
.submit-btn:disabled {
  background-color: #8cb7f5;
  cursor: not-allowed;
}

/* 全局修复：Element组件穿透样式 */
:deep(.el-radio__input) {
  z-index: 100; /* 单选框点击区域层级 */
}
:deep(.el-input__wrapper) {
  background-color: #fff !important; /* 输入框背景 */
}
:deep(.el-date-editor) {
  width: 100%; /* 日期选择器宽度 */
}
</style>
