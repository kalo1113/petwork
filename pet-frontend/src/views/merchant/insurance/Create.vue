<template>
  <div class="insurance-create">
    <el-card title="创建宠物保险">
      <el-form
        :model="insuranceForm"
        label-width="140px"
        @submit.prevent="handleCreateInsurance"
        :rules="formRules"
        ref="insuranceFormRef"
      >
        <!-- 基础信息组 -->
        <el-form-item label="保险名称" prop="insuranceName">
          <el-input
            v-model="insuranceForm.insuranceName"
            placeholder="如：宠物年度医保尊享版"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="保险编号" prop="insuranceNo">
          <el-input
            v-model="insuranceForm.insuranceNo"
            placeholder="如：INS2026001（唯一）"
            maxlength="20"
            show-word-limit
          ></el-input>
        </el-form-item>

        <el-form-item label="保障方案类型" prop="planType">
          <el-select v-model="insuranceForm.planType" placeholder="选择保障方案">
            <el-option label="基础版" :value="1"></el-option>
            <el-option label="升级版" :value="2"></el-option>
            <el-option label="尊享版" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="适用宠物类型" prop="petType">
          <el-select v-model="insuranceForm.petType" placeholder="选择适用宠物">
            <el-option label="猫咪" :value="1"></el-option>
            <el-option label="狗狗" :value="2"></el-option>
            <el-option label="通用" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="保障周期(月)" prop="guaranteeCycle">
          <el-select v-model="insuranceForm.guaranteeCycle" placeholder="选择保障周期">
            <el-option label="12个月（年付）" :value="12"></el-option>
            <el-option label="6个月" :value="6"></el-option>
            <el-option label="3个月" :value="3"></el-option>
          </el-select>
        </el-form-item>

        <!-- 费用与额度组 -->
        <el-divider content-position="left">费用与额度</el-divider>

        <el-form-item label="优惠保费(元)" prop="discountPremium">
          <el-input
            v-model="insuranceForm.discountPremium"
            placeholder="如：299.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <el-form-item label="总保额(元)" prop="totalGuarantee">
          <el-input
            v-model="insuranceForm.totalGuarantee"
            placeholder="如：20000.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <el-form-item label="免赔额(元)" prop="deductible">
          <el-input
            v-model="insuranceForm.deductible"
            placeholder="如：0.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <!-- 报销规则组 -->
        <el-divider content-position="left">报销规则</el-divider>

        <el-form-item label="门诊单次赔付上限(元)" prop="outpatientLimit">
          <el-input
            v-model="insuranceForm.outpatientLimit"
            placeholder="如：1200.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <el-form-item label="手术单次赔付上限(元)" prop="surgeryLimit">
          <el-input
            v-model="insuranceForm.surgeryLimit"
            placeholder="如：2000.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <el-form-item label="定点医院赔付比例(%)" prop="inNetworkRatio">
          <el-input
            v-model.number="insuranceForm.inNetworkRatio"
            placeholder="如：80"
            type="number"
            min="0"
            max="100"
          ></el-input>
        </el-form-item>

        <el-form-item label="非定点医院赔付比例(%)" prop="outNetworkRatio">
          <el-input
            v-model.number="insuranceForm.outNetworkRatio"
            placeholder="如：60"
            type="number"
            min="0"
            max="100"
          ></el-input>
        </el-form-item>

        <!-- 等待期组 -->
        <el-divider content-position="left">等待期设置</el-divider>

        <el-form-item label="意外等待期(天)" prop="waitingPeriodAccident">
          <el-input
            v-model.number="insuranceForm.waitingPeriodAccident"
            placeholder="如：0"
            type="number"
            min="0"
          ></el-input>
        </el-form-item>

        <el-form-item label="先天性/遗传疾病等待期(天)" prop="waitingPeriodDisease">
          <el-input
            v-model.number="insuranceForm.waitingPeriodDisease"
            placeholder="如：90"
            type="number"
            min="0"
          ></el-input>
        </el-form-item>

        <el-form-item label="一般疾病等待期(天)" prop="waitingPeriodCommon">
          <el-input
            v-model.number="insuranceForm.waitingPeriodCommon"
            placeholder="如：30"
            type="number"
            min="0"
          ></el-input>
        </el-form-item>

        <!-- 补贴与赠送组 -->
        <el-divider content-position="left">补贴与赠送</el-divider>

        <el-form-item label="月消费补贴(元)" prop="monthlySubsidy">
          <el-input
            v-model="insuranceForm.monthlySubsidy"
            placeholder="如：200.00"
            type="number"
            step="0.01"
          ></el-input>
        </el-form-item>

        <el-form-item label="赠送服务" prop="giftService">
          <el-input
            v-model="insuranceForm.giftService"
            placeholder="如：3次免费驱虫"
            type="textarea"
            rows="2"
            maxlength="200"
            show-word-limit
          ></el-input>
        </el-form-item>

        <!-- 状态与时间组 -->
        <el-divider content-position="left">状态设置</el-divider>

        <el-form-item label="上下架状态" prop="status">
          <el-select v-model="insuranceForm.status" placeholder="选择状态">
            <el-option label="上架" :value="1"></el-option>
            <el-option label="下架" :value="0"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="上架时间" prop="putOnShelfTime">
          <el-date-picker
            v-model="insuranceForm.putOnShelfTime"
            type="datetime"
            placeholder="选择上架时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          ></el-date-picker>
        </el-form-item>

        <!-- 保险媒体图片（现在支持4种类型） -->
        <el-divider content-position="left">保险媒体图片</el-divider>

        <el-form-item label="产品特色图片">
          <el-upload
            :auto-upload="false"
            :file-list="productImgList"
            list-type="picture-card"
            :limit="1"
            :on-exceed="handleExceed"
            :on-remove="(file, list) => handleFileRemove(list, 'product')"
            :before-upload="beforeUpload"
            :on-change="(file, list) => handleFileChange(list, 'product')"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="理赔案例图片">
          <el-upload
            :auto-upload="false"
            :file-list="caseImgList"
            list-type="picture-card"
            :limit="5"
            :on-exceed="handleExceed"
            :on-remove="(file, list) => handleFileRemove(list, 'case')"
            :before-upload="beforeUpload"
            :on-change="(file, list) => handleFileChange(list, 'case')"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="el-upload__tip">最多上传5张，支持jpg/png格式，单张不超过2MB</div>
        </el-form-item>

        <!-- 新增：产品介绍图 -->
        <el-form-item label="产品介绍图">
          <el-upload
            :auto-upload="false"
            :file-list="introImgList"
            list-type="picture-card"
            :limit="1"
            :on-exceed="handleExceed"
            :on-remove="(file, list) => handleFileRemove(list, 'intro')"
            :before-upload="beforeUpload"
            :on-change="(file, list) => handleFileChange(list, 'intro')"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <!-- 新增：推荐图 -->
        <el-form-item label="推荐图">
          <el-upload
            :auto-upload="false"
            :file-list="recommendImgList"
            list-type="picture-card"
            :limit="1"
            :on-exceed="handleExceed"
            :on-remove="(file, list) => handleFileRemove(list, 'recommend')"
            :before-upload="beforeUpload"
            :on-change="(file, list) => handleFileChange(list, 'recommend')"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
            <!-- 只在这里加：推荐图描述（传给后端 imgRemark） -->
  <el-input
    v-model="recommendImgRemark"
    placeholder="请输入推荐图简单描述"
    maxlength="200"
    show-word-limit
    class="mt-2"
  ></el-input>
        </el-form-item>
        
        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleCreateInsurance">创建保险</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const BASE_URL = '/insurance' // 后端接口前缀，根据实际配置调整

// 表单引用
const insuranceFormRef = ref(null)

// 表单数据
const insuranceForm = reactive({
  insuranceName: '',
  insuranceNo: '',
  planType: '',
  petType: '',
  guaranteeCycle: '',
  discountPremium: '',
  totalGuarantee: '',
  deductible: '',
  outpatientLimit: '',
  surgeryLimit: '',
  inNetworkRatio: 80,
  outNetworkRatio: 50,
  waitingPeriodAccident: 0,
  waitingPeriodDisease: 30,
  waitingPeriodCommon: 15,
  monthlySubsidy: '',
  giftService: '',
  status: 1,
  putOnShelfTime: ''
})

// 本地暂存图片列表
const productImgList = ref([])   // 产品特色图片 (contentType=1)
const caseImgList = ref([])     // 理赔案例图片 (contentType=2)
const introImgList = ref([])    // 产品介绍图 (contentType=3)
const recommendImgList = ref([]) // 推荐图 (contentType=4)
const recommendImgRemark = ref('')
// 表单校验规则
const formRules = reactive({
  insuranceName: [
    { required: true, message: '请输入保险名称', trigger: 'blur' },
    { min: 2, max: 50, message: '名称长度在2-50个字符之间', trigger: 'blur' }
  ],
  insuranceNo: [
    { required: true, message: '请输入保险编号', trigger: 'blur' },
    { pattern: /^INS\d+$/, message: '编号格式为INS+数字（如INS2026001）', trigger: 'blur' }
  ],
  planType: [{ required: true, message: '请选择保障方案类型', trigger: 'change' }],
  petType: [{ required: true, message: '请选择适用宠物类型', trigger: 'change' }],
  guaranteeCycle: [{ required: true, message: '请选择保障周期', trigger: 'change' }],
  discountPremium: [
    { required: true, message: '请输入优惠保费', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        const num = parseFloat(value)
        if (isNaN(num) || num <= 0) {
          callback(new Error('保费必须大于0元'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  status: [{ required: true, message: '请选择上下架状态', trigger: 'change' }]
})

// 图片格式/大小校验
const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('仅支持JPG/PNG格式图片！')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB！')
    return false
  }
  return true
}

// 图片上传数量限制
const handleExceed = () => {
  ElMessage.warning('已达到最大上传数量！')
}

// 处理图片变更（确保列表同步）
const handleFileChange = (list, type) => {
  switch(type) {
    case 'product':
      productImgList.value = list
      break
    case 'case':
      caseImgList.value = list
      break
    case 'intro':
      introImgList.value = list
      break
    case 'recommend':
      recommendImgList.value = list
      break
  }
}

// 移除图片（确保列表同步）
const handleFileRemove = (list, type) => {
  handleFileChange(list, type)
}

// ========== 封装接口调用函数（核心修复：参数类型+请求头） ==========
/**
 * 创建保险接口
 * @param {Object} data 保险信息
 * @returns {Promise} 接口返回结果
 */
const createInsurance = async (data) => {
  try {
    const res = await axios.post(`${BASE_URL}/add`, data, {
      headers: {
        'Content-Type': 'application/json;charset=UTF-8'
      }
    })
    return res.data
  } catch (err) {
    throw new Error(`创建保险接口调用失败：${err.response?.data?.msg || err.message}`)
  }
}

/**
 * 上传保险图片接口
 * @param {FormData} formData 图片数据
 * @returns {Promise} 接口返回结果
 */
const uploadInsuranceMedia = async (formData) => {
  try {
    const res = await axios.post(`${BASE_URL}/media/upload`, formData, {
      headers: {
        // 关键：FormData必须设置此请求头，浏览器会自动补充boundary
        'Content-Type': 'multipart/form-data'
      },
      // 超时时间
      timeout: 30000
    })
    return res.data
  } catch (err) {
    throw new Error(`图片上传接口调用失败：${err.response?.data?.msg || err.message}`)
  }
}

// 创建保险主逻辑（完整修复版）
const handleCreateInsurance = async () => {
  try {
    // 1. 表单校验
    await insuranceFormRef.value.validate()

    // 2. 构造提交数据（强制类型转换，避免格式错误）
    const submitData = {
      insuranceName: insuranceForm.insuranceName.trim(),
      insuranceNo: insuranceForm.insuranceNo.trim(),
      planType: Number(insuranceForm.planType), // 强制转数字
      petType: Number(insuranceForm.petType),   // 强制转数字
      guaranteeCycle: Number(insuranceForm.guaranteeCycle), // 强制转数字
      discountPremium: parseFloat(insuranceForm.discountPremium) || 0.00,
      totalGuarantee: parseFloat(insuranceForm.totalGuarantee) || 0.00,
      deductible: parseFloat(insuranceForm.deductible) || 0.00,
      outpatientLimit: parseFloat(insuranceForm.outpatientLimit) || 0.00,
      surgeryLimit: parseFloat(insuranceForm.surgeryLimit) || 0.00,
      inNetworkRatio: Number(insuranceForm.inNetworkRatio) || 0,
      outNetworkRatio: Number(insuranceForm.outNetworkRatio) || 0,
      waitingPeriodAccident: Number(insuranceForm.waitingPeriodAccident) || 0,
      waitingPeriodDisease: Number(insuranceForm.waitingPeriodDisease) || 0,
      waitingPeriodCommon: Number(insuranceForm.waitingPeriodCommon) || 0,
      monthlySubsidy: parseFloat(insuranceForm.monthlySubsidy) || 0.00,
      giftService: insuranceForm.giftService.trim() || '无',
      status: Number(insuranceForm.status), // 强制转数字
      // 时间格式兼容：后端LocalDateTime支持的格式
      putOnShelfTime: insuranceForm.putOnShelfTime ? insuranceForm.putOnShelfTime.replace(' ', 'T') : new Date().toISOString().replace('Z', ''),
      createTime: new Date().toISOString().replace('Z', ''),
      updateTime: new Date().toISOString().replace('Z', '')
    }

    console.log('【创建保险】提交参数：', submitData)
    // 3. 调用创建保险接口
    const res = await createInsurance(submitData)
    if (res.code !== 200) {
      ElMessage.error('创建保险失败：' + res.msg)
      return
    }

    // 核心：获取保险ID并强制转为数字（解决格式问题）
    const insuranceId = Number(res.data) // 后端返回的是ID数值，直接转数字
    if (isNaN(insuranceId) || insuranceId <= 0) {
      throw new Error(`保险ID格式错误：${res.data}（必须为正整数）`)
    }
    ElMessage.success(`保险创建成功！ID：${insuranceId}`)

    // 4. 批量上传图片
    console.log('【图片上传】开始上传，保险ID：', insuranceId)
    await batchUploadImages(insuranceId)
    ElMessage.success('保险创建+图片上传全部完成！')

    // 5. 重置表单
    resetForm()
  } catch (err) {
    console.error('【操作失败】详情：', err)
    ElMessage.error(`操作失败：${err.message}`)
  }
}

// ========== 批量上传图片（核心修复：FormData传参+类型转换） ==========
const batchUploadImages = async (insuranceId) => {
  // 定义图片类型映射
  const imgTypeMap = [
    { list: productImgList.value, contentType: 1, name: '产品特色图' },
    { list: caseImgList.value, contentType: 2, name: '理赔案例图' },
    { list: introImgList.value, contentType: 3, name: '产品介绍图' },
     { list: recommendImgList.value, contentType: 4, name: '推荐图', remark: recommendImgRemark.value }
  ]

  // 统计待上传图片数量
  const totalImgs = imgTypeMap.reduce((sum, item) => sum + item.list.length, 0)
  if (totalImgs === 0) {
    ElMessage.warning('未选择任何图片，跳过上传步骤')
    return
  }

  // 遍历上传所有图片
  for (const imgType of imgTypeMap) {
    for (const fileItem of imgType.list) {
      // 跳过已上传成功的文件
      if (fileItem.status === 'success') continue

      try {
        // 构造FormData（关键：参数名与后端完全一致）
        const formData = new FormData()
        // 核心修复：insuranceId强制转为字符串，后端兼容解析
        formData.append('insuranceId', insuranceId + '') 
        formData.append('contentType', Number(imgType.contentType)) // 强制转数字
        formData.append('file', fileItem.raw) // 原始文件对象
        formData.append('imgRemark', imgType.remark || imgType.name)

        console.log(`【上传${imgType.name}】参数：`, {
          insuranceId: insuranceId + '',
          contentType: imgType.contentType,
          fileName: fileItem.name,
          fileSize: (fileItem.size / 1024).toFixed(2) + 'KB'
        })

        // 调用上传接口
        const uploadRes = await uploadInsuranceMedia(formData)
        if (uploadRes.code !== 200) {
          throw new Error(`${imgType.name}上传失败：${uploadRes.msg || '未知错误'}`)
        }

        // 标记文件上传成功
        fileItem.status = 'success'
        fileItem.response = uploadRes.data
        console.log(`【上传成功】${imgType.name}：`, uploadRes.data)
      } catch (uploadErr) {
        throw new Error(`${imgType.name}(${fileItem.name})上传失败：${uploadErr.message}`)
      }
    }
  }
}

// 重置表单
const resetForm = () => {
  if (insuranceFormRef.value) {
    insuranceFormRef.value.resetFields()
  }
  // 清空图片列表
  productImgList.value = []
  caseImgList.value = []
  introImgList.value = []
  recommendImgList.value = []
  ElMessage.info('表单已重置')
}
</script>

<style scoped>
.insurance-create {
  max-width: 1000px;
  padding: 20px;
  margin: 0 auto;
}
.el-divider {
  margin: 20px 0;
}
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}
.el-upload__tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>