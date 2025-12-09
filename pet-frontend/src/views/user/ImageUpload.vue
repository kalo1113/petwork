<template>
  <div class="image-upload">
    <h2>上传宠物图片</h2>

    <!-- 宠物ID输入 -->
    <div class="form-item">
      <label>宠物ID：</label>
      <input
        type="number"
        v-model.number="petId"
        placeholder="请输入已存在的宠物ID"
        :class="{ error: petIdError }"
      >
      <p v-if="petIdError" class="error-text">请输入有效的宠物ID（必须是已存在的）</p>
    </div>

    <!-- 图片选择 -->
    <div class="form-item">
      <label>选择图片：</label>
      <input
        type="file"
        accept="image/*"
        @change="handleFileChange"
        :class="{ error: fileError }"
      >
      <p v-if="fileError" class="error-text">请选择一张图片文件</p>
    </div>

    <!-- 图片预览 -->
    <div v-if="previewUrl" class="preview">
      <img :src="previewUrl" alt="图片预览" style="max-width: 300px; border: 1px solid #eee;">
    </div>

    <!-- 操作按钮 -->
    <div class="button-group">
      <button
        @click="uploadAndBind"
        :disabled="isLoading"
      >
        <span v-if="!isLoading">上传并关联</span>
        <span v-if="isLoading">处理中...</span>
      </button>
      <button @click="$router.back()">返回列表</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 状态管理
const router = useRouter()
const petId = ref(null) // 宠物ID
const file = ref(null) // 选中的文件
const previewUrl = ref('') // 图片预览地址
const isLoading = ref(false) // 加载状态
const petIdError = ref(false) // 宠物ID校验错误
const fileError = ref(false) // 文件选择错误

// 处理文件选择
const handleFileChange = (e) => {
  const selectedFile = e.target.files[0]
  if (selectedFile) {
    // 验证文件类型（仅允许图片）
    if (!selectedFile.type.startsWith('image/')) {
      alert('请选择图片文件（jpg、png等）')
      file.value = null
      previewUrl.value = ''
      return
    }
    file.value = selectedFile
    previewUrl.value = URL.createObjectURL(selectedFile) // 生成预览地址
    fileError.value = false
  }
}

// 验证宠物ID是否存在（提前校验，避免关联失败）
const checkPetIdExists = async (id) => {
  try {
    const res = await axios.get(`/pet/${id}`)
    return res.data !== null // 存在返回true，不存在返回false
  } catch (err) {
    console.error('验证宠物ID失败：', err)
    return false // 网络错误时默认ID不存在
  }
}

// 上传图片并关联到宠物
const uploadAndBind = async () => {
  // 1. 前端参数校验
  petIdError.value = !petId.value
  fileError.value = !file.value
  if (petIdError.value || fileError.value) {
    return // 校验失败终止流程
  }

  // 2. 验证宠物ID是否存在
  isLoading.value = true
  const idExists = await checkPetIdExists(petId.value)
  if (!idExists) {
    petIdError.value = true
    isLoading.value = false
    return
  }

  try {
    // 3. 上传图片到后端
    const formData = new FormData()
    formData.append('file', file.value) // 字段名与后端@RequestParam("file")一致
    const uploadRes = await axios.post('/pet/upload', formData)

    // 4. 解析上传结果（兼容后端返回格式）
    let imgUrl = uploadRes.data
    // 若后端返回带提示的字符串（如"上传成功：xxx.jpg"），提取文件名
    if (typeof imgUrl === 'string' && imgUrl.includes('：')) {
      imgUrl = imgUrl.split('：')[1].trim()
    }
    if (!imgUrl) {
      throw new Error('上传失败：无法获取图片路径')
    }

    // 5. 关联图片到宠物
    const bindRes = await axios.put('/pet/update-img', null, {
      params: {
        petId: petId.value,
        imgUrl: imgUrl
      }
    })

    // 6. 处理关联结果
    if (bindRes.data.includes('成功')) {
      alert('图片上传并关联成功！')
      await router.push('/pet-list') // 跳回列表页
    } else {
      throw new Error('关联失败：' + bindRes.data)
    }
  } catch (err) {
    // 错误提示（区分网络错误和业务错误）
    const errorMsg = err.response
      ? err.response.data || '服务器处理失败'
      : '网络错误，请检查前后端服务是否正常'
    alert('操作失败：' + errorMsg)
  } finally {
    isLoading.value = false // 无论成功失败，结束加载状态
  }
}
</script>

<style scoped>
.image-upload {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #eee;
  border-radius: 8px;
}

.form-item {
  margin: 20px 0;
}

label {
  display: inline-block;
  width: 120px;
  text-align: right;
  margin-right: 15px;
  font-weight: 500;
}

input {
  padding: 8px 12px;
  width: 300px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

input.error {
  border-color: #ff4d4f;
}

.error-text {
  color: #ff4d4f;
  margin: 5px 0 0 135px;
  font-size: 14px;
}

.preview {
  margin: 20px 0 20px 135px;
}

.button-group {
  margin-left: 135px;
  margin-top: 30px;
}

button {
  padding: 8px 20px;
  margin-right: 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #1890ff;
  color: white;
  font-size: 14px;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button:last-child {
  background-color: #666;
}

button:hover:not(:disabled) {
  opacity: 0.9;
}
</style>
