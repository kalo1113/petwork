<template>
  <div class="pet-guarantee">
    <!-- 外层居中容器 -->
    <div class="page-container">
      <div class="pet-insurance-page">
        <!-- 单独返回按钮（固定在内容区左上角） -->
        <button class="back-btn" @click="goBack">&lt;</button>

        <!-- 套餐选择区域 -->
        <div class="package-section">
          <!-- 套餐切换按钮 -->
          <div class="tab-group">
            <button class="tab-btn" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">基础版</button>
            <button class="tab-btn" :class="{ active: activeTab === 'advanced' }" @click="activeTab = 'advanced'">升级版</button>
            <button class="tab-btn" :class="{ active: activeTab === 'premium' }" @click="activeTab = 'premium'">尊享版</button>
          </div>

          <!-- 套餐图片展示（根据选中的套餐切换） -->
          <div class="package-img">
            <img v-if="activeTab === 'basic'" src="@/assets/images/保障图标/基础版.jpg" alt="基础版套餐" />
            <img v-else-if="activeTab === 'advanced'" src="@/assets/images/保障图标/升级版.jpg" alt="升级版套餐" />
            <img v-else src="@/assets/images/保障图标/尊享版.jpg" alt="尊享版套餐" />
          </div>
        </div>

        <!-- 宠物信息表单 -->
        <div class="pet-form">
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
          <div class="form-group">
            <label>宠物照片</label>
            <div class="photo-upload-group">
              <!-- 正脸照上传组件：绑定file-list并修复回显逻辑 -->
              <el-upload
                class="photo-upload"
                :action="uploadUrl"
                list-type="picture-card"
                :limit="1"
                :data="getUploadData('face')"
                :on-success="(res, file) => handlePhotoSuccess(res, file, 'face')"
                :on-remove="(file) => handlePhotoRemove(file, 'face')"
                :file-list="faceFileList"
              >
                <template #default>
                  <div class="upload-icon">
                    <el-icon class="el-icon--upload"><Plus /></el-icon>
                    <div class="upload-text">正脸照</div>
                  </div>
                </template>
              </el-upload>
              <!-- 全身照上传组件：绑定file-list并修复回显逻辑 -->
              <el-upload
                class="photo-upload"
                :action="uploadUrl"
                list-type="picture-card"
                :limit="1"
                :data="getUploadData('body')"
                :on-success="(res, file) => handlePhotoSuccess(res, file, 'body')"
                :on-remove="(file) => handlePhotoRemove(file, 'body')"
                :file-list="bodyFileList"
              >
                <template #default>
                  <div class="upload-icon">
                    <el-icon class="el-icon--upload"><Plus /></el-icon>
                    <div class="upload-text">全身照</div>
                  </div>
                </template>
              </el-upload>
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
        </div>

        <!-- 产品特色 -->
        <div class="product-feature">
          <h3>产品特色</h3>
          <img src="@/assets/images/保障图标/产品特色.png" alt="产品特色" />
        </div>

        <!-- 理赔说明 -->
        <div class="claim-instruction">
          <h3>理赔说明</h3>
          <img src="@/assets/images/保障图标/理赔说明.jpg" alt="理赔说明" />
        </div>

        <!-- 理赔案例轮播（底部小点切换） -->
        <div class="claim-case">
          <h3>理赔案例</h3>
          <v-carousel
            v-model="currentCase"
            show-indicators
            indicator-color="primary"
            height="2100"
            rounded="lg"
            elevation="2"
          >
            <v-carousel-item
              v-for="(img, index) in caseImages"
              :key="index"
            >
              <v-img
                :src="img"
                cover
                rounded="lg"
                width="100%"
              />
            </v-carousel-item>
          </v-carousel>
        </div>

        <!-- 底部投保栏 -->
        <div class="bottom-bar">
          <button class="consult-btn">咨询</button>
          <button class="add-btn">+</button>
          <p>{{ getPriceText() }}</p>
          <button class="insure-btn" @click="submitInsure">我要投保</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { VCarousel, VCarouselItem, VImg } from 'vuetify/components'
import { ref, reactive, getCurrentInstance } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
// 导入理赔案例图片
import case1 from '@/assets/images/保障图标/投保案例1.jpg'
import case2 from '@/assets/images/保障图标/投保案例2.jpg'
import case3 from '@/assets/images/保障图标/投保案例3.jpg'

const router = useRouter()
const { proxy } = getCurrentInstance() // 获取全局实例（用于获取用户ID）

// -------- 基础配置 --------
const uploadUrl = '/pet/upload' // 后端照片上传接口地址
const petApiUrl = '/pet' // 宠物接口前缀
// 模拟获取当前登录用户ID（实际项目从全局状态/登录信息中获取）
const currentUserId = ref(1001) // 示例用户ID，需替换为真实逻辑

// -------- 套餐切换 --------
const activeTab = ref('premium') // 默认选中尊享版
// 套餐价格配置（元/月）
const packagePrice = {
  basic: 18.00,
  advanced: 38.00,
  premium: 68.00
}

// -------- 宠物表单数据 --------
const petForm = reactive({
  petName: '', // 宠物昵称
  petBirthday: '', // 出生日期（YYYY-MM-DD）
  petType: '猫', // 宠物种类（猫/狗）
  petGender: '公', // 宠物性别（公/母）
  isSterilized: '否', // 是否绝育（是/否）
  petFacePhoto: '', // 正脸照URL
  petBodyPhoto: '', // 全身照URL
  userId: currentUserId.value // 关联用户ID
})
// 照片文件列表（用于Element Plus上传组件回显，修复后可正确显示已上传图片）
const faceFileList = ref([])
const bodyFileList = ref([])

// -------- 缴费方式 --------
const paymentMethod = ref('monthly') // 默认按月缴费

// -------- 理赔案例轮播 --------
const caseImages = ref([case1, case2, case3])
const currentCase = ref(0)

// -------- 方法定义 --------
// 返回上一页
const goBack = () => {
  router.back()
}

// 获取上传参数（包含用户ID、宠物ID、照片类型）
const getUploadData = (photoType) => {
  // 宠物ID：新增宠物时可能还未生成，这里先模拟，实际需调整逻辑
  // 方案1：先新增宠物获取petId，再上传照片；方案2：后端支持先上传再关联
  const petId = ref(2001) // 示例宠物ID，实际需替换为真实逻辑
  return {
    userId: currentUserId.value,
    petId: petId.value,
    photoType: photoType // face/body
  }
}

// 照片上传成功回调（修复参数传递，确保file-list正确更新）
const handlePhotoSuccess = (res, file, photoType) => {
  if (res.startsWith('/pet-images/')) { // 后端返回有效路径
    if (photoType === 'face') {
      petForm.petFacePhoto = res
      faceFileList.value = [{
        name: file.name,
        url: res // 绑定图片URL，使el-upload回显
      }]
    } else {
      petForm.petBodyPhoto = res
      bodyFileList.value = [{
        name: file.name,
        url: res // 绑定图片URL，使el-upload回显
      }]
    }
    proxy.$message.success(` ${photoType === 'face' ? '正脸照' : '全身照'} 上传成功`)
  } else {
    proxy.$message.error(`上传失败：${res}`)
  }
}

// 照片移除回调（修复参数传递，确保file-list清空）
const handlePhotoRemove = (file, photoType) => {
  if (photoType === 'face') {
    petForm.petFacePhoto = ''
    faceFileList.value = []
  } else {
    petForm.petBodyPhoto = ''
    bodyFileList.value = []
  }
}

// 获取套餐价格文本
const getPriceText = () => {
  const price = packagePrice[activeTab.value]
  return paymentMethod.value === 'monthly' ? `${price.toFixed(2)}元/月起` : `总计${(price * 12).toFixed(2)}元`
}

// 表单校验
const validateForm = () => {
  if (!petForm.petName) {
    proxy.$message.warning('请输入宠物昵称')
    return false
  }
  if (!petForm.petBirthday) {
    proxy.$message.warning('请选择宠物出生日期')
    return false
  }
  if (!petForm.petFacePhoto) {
    proxy.$message.warning('请上传宠物正脸照')
    return false
  }
  if (!petForm.petBodyPhoto) {
    proxy.$message.warning('请上传宠物全身照')
    return false
  }
  return true
}

// 提交投保（先新增宠物，再跳转支付）
const submitInsure = async () => {
  if (!validateForm()) return

  try {
    const res = await axios.post(`${petApiUrl}/add`, petForm)
    if (res.data.code === 200) { // 后端返回code=200表示成功
      proxy.$message.success(res.data.msg)
      // 后续跳转支付等逻辑...
    } else {
      proxy.$message.error(res.data.msg || '新增宠物失败，请重试')
    }
  } catch (error) {
    console.error('投保失败：', error)
    proxy.$message.error('网络异常，请稍后重试')
  }
}
</script>

<style scoped>
/* 外层居中容器 */
.page-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 页面整体样式：浅灰色背景 + 内容居中 */
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

/* 返回按钮：固定在内容区左上角，滚动不消失 */
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

/* 通用区域样式：圆角白色背景 + 统一间距 */
.package-section, .pet-form, .payment-method, .product-feature, .claim-instruction, .claim-case {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  margin: 20px auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

/* 套餐选择区域 */
.package-section {
  margin-top: 500px;
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
.tab-btn:hover {
  background-color: #e9f2fa;
}
.tab-btn.active {
  background-color: #2196f3;
  color: #fff;
  border-color: #2196f3;
  transform: scale(1.05);
}

.detail-item span {
  width: 100px;
  color: #2196f3;
  font-weight: 600;
  text-align: right;
}

/* 宠物信息表单优化 */
.photo-upload-group {
  display: flex;
  gap: 20px;
}
.photo-upload {
  width: 120px;
  height: 120px;
}
.upload-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}
.upload-text {
  margin-top: 8px;
  font-size: 14px;
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
.form-group input, .form-group select {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  font-size: 14px;
  transition: all 0.3s ease;
}
.form-group input:focus, .form-group select:focus {
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
.tip {
  color: #999;
  font-size: 14px;
  margin-top: 8px;
}

/* 缴费方式 */
.payment-method h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.payment-method p {
  margin: 10px 0;
  color: #666;
  line-height: 1.6;
}
.warning {
  color: #f57c00;
  font-weight: 500;
}

/* 产品特色、理赔说明 */
.product-feature h3, .claim-instruction h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
}
.package-img img, .product-feature img, .claim-instruction img {
  width: 100%;
  border-radius: 8px;
  margin-bottom: 16px;
}

/* 理赔案例轮播 */
.claim-case h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #2196f3;
  padding-bottom: 8px;
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
}
.insure-btn {
  background-color: #2196f3;
  color: #fff;
  padding: 10px 24px;
  border-radius: 20px;
}
</style>
