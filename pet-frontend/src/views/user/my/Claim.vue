<template>
  <div class="claim-page">
    <!-- 页面头部 -->
    <div class="claim-header">
      <el-button type="text" class="back-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <h3 class="claim-title">保险理赔申请</h3>
    </div>

    <!-- 理赔表单主体 -->
    <div class="claim-form-container">
      <el-form
        ref="claimFormRef"
        :model="claimForm"
        :rules="claimRules"
        label-width="120px"
        class="claim-form"
      >
        <!-- 被保宠物信息 -->
        <div class="form-section">
          <h4 class="section-title">被保宠物信息</h4>
          <el-form-item label="宠物种类" prop="petType">
            <el-select v-model="claimForm.petType" placeholder="请选择宠物种类">
              <el-option label="猫" value="猫"></el-option>
              <el-option label="狗" value="狗"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="宠物昵称" prop="petNickname">
            <el-input v-model="claimForm.petNickname" placeholder="请输入宠物昵称"></el-input>
          </el-form-item>
          <el-form-item label="宠物照片">
            <div class="upload-group photo-upload-group">
              <!-- 正脸照 -->
              <div class="photo-upload">
                <img v-if="previewUrls.petFront" :src="previewUrls.petFront" class="upload-img" />
                <div v-else class="upload-placeholder">
                  <span class="plus-icon">+</span>
                </div>
                <div class="photo-desc">正脸照 <span class="required">*</span></div>
                <input
                  type="file"
                  ref="petFrontFileRef"
                  accept="image/jpeg,image/png"
                  class="file-input"
                  @change="handleFileChange($event, 'petFront')"
                >
              </div>
              <!-- 全身照 -->
              <div class="photo-upload">
                <img v-if="previewUrls.petFull" :src="previewUrls.petFull" class="upload-img" />
                <div v-else class="upload-placeholder">
                  <span class="plus-icon">+</span>
                </div>
                <div class="photo-desc">全身照 <span class="required">*</span></div>
                <input
                  type="file"
                  ref="petFullFileRef"
                  accept="image/jpeg,image/png"
                  class="file-input"
                  @change="handleFileChange($event, 'petFull')"
                >
              </div>
            </div>
          </el-form-item>
        </div>

        <!-- 理赔收款信息 -->
        <div class="form-section">
          <h4 class="section-title">理赔收款信息</h4>
          <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="claimForm.contactPhone" placeholder="请输入收款联系人电话"></el-input>
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="claimForm.realName" placeholder="请输入收款真实姓名"></el-input>
          </el-form-item>
          <el-form-item label="用户邮箱" prop="userEmail">
            <el-input v-model="claimForm.userEmail" placeholder="请输入接收理赔通知的邮箱"></el-input>
          </el-form-item>
        </div>

        <!-- 医疗出诊信息 -->
        <div class="form-section">
          <h4 class="section-title">医疗出诊信息</h4>
          <el-form-item label="是否手术" prop="isSurgery">
            <el-radio-group v-model="claimForm.isSurgery">
              <el-radio label="1">是</el-radio>
              <el-radio label="0">否</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出险时间" prop="accidentTime">
            <el-date-picker
              v-model="claimForm.accidentTime"
              type="datetime"
              placeholder="选择出险时间"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DDTHH:mm:ss"
              style="width: 100%;"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="就诊医院" prop="hospitalType">
            <el-radio-group v-model="claimForm.hospitalType">
              <el-radio label="1">定点医院</el-radio>
              <el-radio label="2">非定点医院</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="就诊费用(元)" prop="medicalCost">
            <el-input
              v-model="claimForm.medicalCost"
              type="number"
              placeholder="请输入本次就诊总费用"
              min="0"
              step="0.01"
            ></el-input>
          </el-form-item>
          <el-form-item label="宠物病情概述" prop="illnessDesc">
            <el-input
              v-model="claimForm.illnessDesc"
              type="textarea"
              :rows="4"
              placeholder="请简要描述宠物病情、就诊原因等信息"
            ></el-input>
          </el-form-item>
        </div>

        <!-- 材料上传 -->
        <div class="form-section">
          <h4 class="section-title">理赔材料上传</h4>
          <div class="materials-upload photo-upload-group">
            <!-- 就诊病历 -->
            <div class="photo-upload">
              <img v-if="previewUrls.medicalRecord" :src="previewUrls.medicalRecord" class="upload-img" />
              <div v-else class="upload-placeholder">
                <span class="plus-icon">+</span>
              </div>
              <div class="photo-desc">就诊病历 <span class="required">*</span></div>
              <input
                type="file"
                ref="medicalRecordFileRef"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleFileChange($event, 'medicalRecord')"
              >
            </div>

            <!-- 检查报告 -->
            <div class="photo-upload">
              <img v-if="previewUrls.inspectionReport" :src="previewUrls.inspectionReport" class="upload-img" />
              <div v-else class="upload-placeholder">
                <span class="plus-icon">+</span>
              </div>
              <div class="photo-desc">检查报告</div>
              <input
                type="file"
                ref="inspectionReportFileRef"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleFileChange($event, 'inspectionReport')"
              >
            </div>

            <!-- 费用明细清单 -->
            <div class="photo-upload">
              <img v-if="previewUrls.costDetail" :src="previewUrls.costDetail" class="upload-img" />
              <div v-else class="upload-placeholder">
                <span class="plus-icon">+</span>
              </div>
              <div class="photo-desc">费用明细清单</div>
              <input
                type="file"
                ref="costDetailFileRef"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleFileChange($event, 'costDetail')"
              >
            </div>

            <!-- 医疗发票 -->
            <div class="photo-upload">
              <img v-if="previewUrls.medicalInvoice" :src="previewUrls.medicalInvoice" class="upload-img" />
              <div v-else class="upload-placeholder">
                <span class="plus-icon">+</span>
              </div>
              <div class="photo-desc">医疗发票 <span class="required">*</span></div>
              <input
                type="file"
                ref="medicalInvoiceFileRef"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleFileChange($event, 'medicalInvoice')"
              >
            </div>

            <!-- 治疗中照片 -->
            <div class="photo-upload">
              <img v-if="previewUrls.treatmentPhoto" :src="previewUrls.treatmentPhoto" class="upload-img" />
              <div v-else class="upload-placeholder">
                <span class="plus-icon">+</span>
              </div>
              <div class="photo-desc">治疗中照片</div>
              <input
                type="file"
                ref="treatmentPhotoFileRef"
                accept="image/jpeg,image/png"
                class="file-input"
                @change="handleFileChange($event, 'treatmentPhoto')"
              >
            </div>
          </div>
          <div class="upload-tip">仅支持jpg/png格式，单张不超过5MB，带*为必填材料</div>
        </div>

        <!-- 提交按钮 -->
        <div class="form-submit">
          <el-button type="primary" @click="submitClaimForm" :loading="isSubmitting">
            提交理赔申请
          </el-button>
          <el-button @click="resetForm">重置表单</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { 
  createInsuranceClaim, 
  uploadClaimMaterial,
  updateMaterialClaimId,
  // 新增：调用后端更新图片URL的接口
  updateClaimUrls 
} from '@/api/user/index.js'

const router = useRouter()
const route = useRoute()

const claimFormRef = ref(null)
const isSubmitting = ref(false)

// 表单数据
const claimForm = reactive({
  userId: '',
  insuranceOrderId: '',
  petType: '',
  petNickname: '',
  contactPhone: '',
  realName: '',
  userEmail: '',
  isSurgery: '',
  accidentTime: '',
  hospitalType: '',
  medicalCost: '',
  illnessDesc: '',
  claimStatus: 0,
  // 图片URL字段
  petFrontPhotoUrl: '',
  petFullPhotoUrl: '',
  medicalRecordUrl: '',
  inspectionReportUrl: '',
  costDetailUrl: '',
  medicalInvoiceUrl: '',
  treatmentPhotoUrl: ''
})

// 文件Ref（对应每个上传项）
const petFrontFileRef = ref(null)
const petFullFileRef = ref(null)
const medicalRecordFileRef = ref(null)
const inspectionReportFileRef = ref(null)
const costDetailFileRef = ref(null)
const medicalInvoiceFileRef = ref(null)
const treatmentPhotoFileRef = ref(null)

// 预览URL和缓存文件
const previewUrls = ref({
  petFront: '',
  petFull: '',
  medicalRecord: '',
  inspectionReport: '',
  costDetail: '',
  medicalInvoice: '',
  treatmentPhoto: ''
})
const cacheFiles = ref({
  petFront: null,
  petFull: null,
  medicalRecord: null,
  inspectionReport: null,
  costDetail: null,
  medicalInvoice: null,
  treatmentPhoto: null
})

// 存储上传成功的材料URL列表（用于后续关联真实claimId）
const uploadedMaterialUrls = ref([])

// 表单校验规则
const claimRules = reactive({
  petType: [{ required: true, message: '请选择宠物种类', trigger: 'change' }],
  petNickname: [{ required: true, message: '请输入宠物昵称', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  userEmail: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  isSurgery: [{ required: true, message: '请选择是否手术', trigger: 'change' }],
  accidentTime: [{ required: true, message: '请选择出险时间', trigger: 'change' }],
  hospitalType: [{ required: true, message: '请选择就诊医院类型', trigger: 'change' }],
  medicalCost: [
    { required: true, message: '请输入就诊费用', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        const num = Number(value)
        if (isNaN(num) || num <= 0) callback(new Error('费用必须大于0'))
        else callback()
      },
      trigger: 'blur'
    }
  ],
  illnessDesc: [
    { required: true, message: '请描述宠物病情', trigger: 'blur' },
    { min: 10, message: '病情描述至少10个字', trigger: 'blur' }
  ]
})

// 初始化
onMounted(() => {
  // 从路由参数获取订单ID
  if (route.query.orderId) {
    claimForm.insuranceOrderId = route.query.orderId
  }
  
  // 获取用户ID（兼容不同存储方式）
  try {
    const userId = localStorage.getItem('userId') || 
                  (JSON.parse(localStorage.getItem('userData') || '{}')?.userId) ||
                  (JSON.parse(localStorage.getItem('user') || '{}')?.userId)
    if (userId) {
      claimForm.userId = userId
    }
  } catch (e) {
    console.warn('用户信息解析失败：', e)
  }
})

// 新增：清空文件选择框DOM值（解决重新选择图片不触发change的问题）
const clearFileInputs = () => {
  const fileInputs = [
    petFrontFileRef.value,
    petFullFileRef.value,
    medicalRecordFileRef.value,
    inspectionReportFileRef.value,
    costDetailFileRef.value,
    medicalInvoiceFileRef.value,
    treatmentPhotoFileRef.value
  ];
  fileInputs.forEach(input => {
    if (input) input.value = '';
  });
};

// 图片选择和校验
const handleFileChange = (e, type) => {
  const file = e.target.files[0]
  if (!file) return
  
  // 类型校验（兼容更多MIME类型）
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || 
                 file.type === 'image/jpg' || file.type === 'image/gif'
  if (!isImage) {
    ElMessage.error('仅支持JPG/PNG/GIF格式图片！')
    return
  }
  
  // 大小校验（5MB）
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB！')
    return
  }
  
  // 缓存文件和预览URL
  cacheFiles.value[type] = file
  previewUrls.value[type] = URL.createObjectURL(file)
  
  // 清空input值，支持重复选择同一文件
  e.target.value = ''
}

// 校验必填材料（仅检查是否选择，不上传）
const validateRequiredMaterials = () => {
  const requiredMaterials = [
    { type: 'petFront', name: '宠物正脸照' },
    { type: 'petFull', name: '宠物全身照' },
    { type: 'medicalRecord', name: '就诊病历' },
    { type: 'medicalInvoice', name: '医疗发票' }
  ];
  
  const missing = [];
  requiredMaterials.forEach(item => {
    if (!cacheFiles.value[item.type]) {
      missing.push(item.name);
    }
  });
  
  if (missing.length > 0) {
    ElMessage.error(`请选择必填材料：${missing.join('、')}`);
    return false;
  }
  return true;
};

// 上传所有图片（修改：返回URL数据，用于更新数据库）
const uploadAllMaterials = async (realClaimId) => {
  // 清空历史上传URL列表
  uploadedMaterialUrls.value = []
  // 存储每个图片类型的URL数据（用于更新数据库）
  const urlData = {}
  
  const loading = ElLoading.service({
    lock: true,
    text: '正在上传图片...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 材料类型映射（严格匹配后端定义）
    const materialTypeMap = [
      { type: 'petFront', materialType: 'petFrontPhoto', urlField: 'petFrontPhotoUrl', name: '宠物正脸照' },
      { type: 'petFull', materialType: 'petFullPhoto', urlField: 'petFullPhotoUrl', name: '宠物全身照' },
      { type: 'medicalRecord', materialType: 'medicalRecord', urlField: 'medicalRecordUrl', name: '就诊病历' },
      { type: 'inspectionReport', materialType: 'inspectionReport', urlField: 'inspectionReportUrl', name: '检查报告' },
      { type: 'costDetail', materialType: 'costDetail', urlField: 'costDetailUrl', name: '费用明细' },
      { type: 'medicalInvoice', materialType: 'medicalInvoice', urlField: 'medicalInvoiceUrl', name: '医疗发票' },
      { type: 'treatmentPhoto', materialType: 'treatmentPhoto', urlField: 'treatmentPhotoUrl', name: '治疗照片' }
    ]

    // 遍历上传（使用真实claimId）
    for (const material of materialTypeMap) {
      const file = cacheFiles.value[material.type]
      if (!file) {
        // 非必填项默认空字符串
        urlData[material.urlField] = ''
        claimForm[material.urlField] = ''
        continue
      }

      // 调用上传接口（使用真实claimId）
      const uploadRes = await uploadClaimMaterial(
        file,        
        realClaimId,         
        material.materialType 
      )

      // 适配后端返回格式
      if (uploadRes?.code === 200 && uploadRes?.data) {
        urlData[material.urlField] = uploadRes.data // 存储URL用于更新数据库
        claimForm[material.urlField] = uploadRes.data
        uploadedMaterialUrls.value.push(uploadRes.data)
        ElMessage.success(`${material.name}上传成功`)
      } else {
        throw new Error(`${material.name}上传失败：${uploadRes?.msg || uploadRes?.message || '未知错误'}`)
      }
    }

    loading.close()
    // 返回URL数据（关键：用于更新数据库）
    return { success: true, urlData }
  } catch (err) {
    loading.close()
    ElMessage.error(err.message)
    console.error('图片上传失败：', err)
    return { success: false, urlData: {} }
  }
}

// 提交表单（核心修复：图片上传后主动更新数据库URL + 失败后清空状态）
const submitClaimForm = async () => {
  try {
    // 前置校验
    if (!claimForm.userId) {
      ElMessage.error('请先登录后再提交理赔申请')
      return router.push('/login')
    }
    if (!claimForm.insuranceOrderId) {
      ElMessage.error('请选择对应的保险订单')
      return
    }

    // 表单校验
    await claimFormRef.value.validate()

    // 新增：先校验必填材料是否选择（仅检查，不上传）
    if (!validateRequiredMaterials()) {
      return
    }

    // 构造提交数据（先不传图片URL）
    const fullFormData = {
      userId: Number(claimForm.userId), 
      insuranceOrderId: Number(claimForm.insuranceOrderId), 
      petType: claimForm.petType,
      petNickname: claimForm.petNickname,
      contactPhone: claimForm.contactPhone,
      realName: claimForm.realName,
      userEmail: claimForm.userEmail.trim(), 
      isSurgery: Number(claimForm.isSurgery),
      accidentTime: claimForm.accidentTime.replace(/'/g, ''), // 修复时间格式问题
      hospitalType: Number(claimForm.hospitalType),
      medicalCost: Number(claimForm.medicalCost),
      illnessDesc: claimForm.illnessDesc.trim(),
      claimStatus: 0, 
      // 图片URL先传空
      petFrontPhotoUrl: '',
      petFullPhotoUrl: '',
      medicalRecordUrl: '',
      inspectionReportUrl: '',
      costDetailUrl: '',
      medicalInvoiceUrl: '',
      treatmentPhotoUrl: ''
    }

    isSubmitting.value = true
    const loading = ElLoading.service({
      lock: true,
      text: '正在提交理赔申请...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 第一步：先提交表单创建理赔记录
    const createRes = await createInsuranceClaim(fullFormData)
    
    // 表单创建成功后，再执行图片上传
    if (createRes?.success && createRes?.data) {
      // 获取真实理赔ID
      const newClaimId = createRes.data.id || createRes.data.claimId
      
      // 第二步：上传所有图片（使用真实claimId）
      const uploadResult = await uploadAllMaterials(newClaimId)
      
      // 第三步：核心修复！主动调用后端接口更新数据库中的图片URL
      if (uploadResult.success && Object.keys(uploadResult.urlData).length > 0) {
        const updateUrlRes = await updateClaimUrls(newClaimId, uploadResult.urlData)
        if (updateUrlRes?.success) {
          console.log('图片URL写入数据库成功：', updateUrlRes.message)
        } else {
          ElMessage.warning(`图片URL更新失败：${updateUrlRes?.message || '未知错误'}`)
        }
      }

      // 第四步：更新材料关联的claimId（如果需要）
      if (uploadResult.success && uploadedMaterialUrls.value.length > 0 && newClaimId) {
        try {
          const updateRes = await updateMaterialClaimId(
            0, // 临时ID
            newClaimId, // 真实ID
            uploadedMaterialUrls.value.join(',') // 转成逗号分隔的字符串
          )
          
          if (updateRes?.code === 200) {
            console.log('材料关联更新成功：', updateRes.message)
          } else {
            ElMessage.warning(`材料关联更新失败：${updateRes?.message || '未知错误'}`)
          }
        } catch (e) {
          ElMessage.warning(`材料关联更新异常：${e.message}`)
          console.error('更新材料关联失败：', e)
        }
      }
      
      loading.close()
      isSubmitting.value = false
      ElMessage.success('理赔申请提交成功！图片URL已写入数据库')
      // 延迟返回上一页
      setTimeout(() => {
        router.go(-1)
      }, 1500)
    } else {
      throw new Error(createRes?.message || '理赔申请创建失败')
    }
  } catch (error) {
    // 核心修复：提交失败后清空所有图片状态，避免残留
    isSubmitting.value = false
    ElLoading.service().close()
    
    // 1. 清空图片缓存和预览
    Object.keys(cacheFiles.value).forEach(key => {
      cacheFiles.value[key] = null;
    });
    Object.keys(previewUrls.value).forEach(key => {
      previewUrls.value[key] = '';
    });
    
    // 2. 清空URL字段和上传记录
    claimForm.petFrontPhotoUrl = '';
    claimForm.petFullPhotoUrl = '';
    claimForm.medicalRecordUrl = '';
    claimForm.inspectionReportUrl = '';
    claimForm.costDetailUrl = '';
    claimForm.medicalInvoiceUrl = '';
    claimForm.treatmentPhotoUrl = '';
    uploadedMaterialUrls.value = [];
    
    // 3. 清空文件选择框DOM值
    clearFileInputs();
    
    // 4. 提示用户重新选择图片
    const errMsg = error.response?.data?.message || error.message || '提交失败';
    ElMessage.error(`提交失败：${errMsg}\n请重新选择图片后再次提交`);
    console.error('提交失败：', error);
  }
}

// 重置表单（增强：清空文件选择框）
const resetForm = () => {
  ElMessageBox.confirm('确定重置表单？所有内容将清空', '提示', { 
    type: 'warning',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(() => {
      // 重置表单校验
      if (claimFormRef.value) {
        claimFormRef.value.resetFields()
      }
      
      // 清空预览和缓存
      Object.keys(previewUrls.value).forEach(key => {
        previewUrls.value[key] = ''
      })
      Object.keys(cacheFiles.value).forEach(key => {
        cacheFiles.value[key] = null
      })
      
      // 清空URL字段
      claimForm.petFrontPhotoUrl = ''
      claimForm.petFullPhotoUrl = ''
      claimForm.medicalRecordUrl = ''
      claimForm.inspectionReportUrl = ''
      claimForm.costDetailUrl = ''
      claimForm.medicalInvoiceUrl = ''
      claimForm.treatmentPhotoUrl = ''
      
      // 清空上传URL列表
      uploadedMaterialUrls.value = []
      
      // 新增：清空文件选择框
      clearFileInputs();
      
      ElMessage.info('表单已重置')
    })
    .catch(() => {
      ElMessage.info('已取消重置')
    })
}

// 返回上一页
const handleBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.claim-page {
  max-width: 1000px;
  padding: 20px;
  margin: 0 auto;
  background-color: #f9f9f9;
  min-height: calc(100vh - 80px);
}

.claim-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.back-btn {
  font-size: 16px;
  color: #666;
  padding: 4px 8px;
  transition: all 0.2s;
}

.back-btn:hover {
  color: #409eff;
  background-color: #f5f8ff;
  border-radius: 4px;
}

.claim-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0 0 0 10px;
}

.claim-form-container {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.form-section {
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  padding-left: 8px;
  border-left: 4px solid #409eff;
}

/* 上传样式优化 */
.photo-upload-group {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.photo-upload {
  width: 120px;
  height: 120px;
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
  transition: all 0.2s;
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
  font-size: 24px;
  color: #409eff;
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
  margin-top: 8px;
  text-align: center;
}

.required {
  color: #f56c6c;
}

.upload-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.form-submit {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.form-submit .el-button {
  padding: 10px 24px;
  font-size: 14px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .claim-page {
    padding: 10px;
  }
  
  .claim-form-container {
    padding: 15px;
  }

  .photo-upload-group {
    gap: 15px;
  }

  .photo-upload {
    width: calc(50% - 7.5px);
    height: 100px;
  }
  
  .form-section {
    margin-bottom: 20px;
    padding-bottom: 15px;
  }
  
  .section-title {
    font-size: 15px;
    margin-bottom: 15px;
  }
}

@media (max-width: 480px) {
  .photo-upload {
    width: 100%;
  }
  
  .claim-title {
    font-size: 18px;
  }
  
  .form-submit {
    flex-direction: column;
  }
  
  .form-submit .el-button {
    width: 100%;
  }
}
</style>