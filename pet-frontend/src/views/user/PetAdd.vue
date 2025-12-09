<template>
  <div class="pet-add">
    <h3>1. 新增宠物</h3>
    <div class="form-item">
      <label>宠物名称：</label>
      <input
        type="text"
        v-model="petForm.petName"
        placeholder="请输入宠物名称"
        @blur="validateField('petName')"
      >
      <span class="error">{{ errors.petName }}</span>
    </div>
    <div class="form-item">
      <label>宠物品种：</label>
      <input
        type="text"
        v-model="petForm.petBreed"
        placeholder="请输入宠物品种"
        @blur="validateField('petBreed')"
      >
      <span class="error">{{ errors.petBreed }}</span>
    </div>
    <div class="form-item">
      <label>宠物年龄：</label>
      <input
        type="number"
        v-model.number="petForm.petAge"
        placeholder="请输入宠物年龄（正整数）"
        @blur="validateField('petAge')"
      >
      <span class="error">{{ errors.petAge }}</span>
    </div>
    <div class="form-item">
      <label>所属用户ID：</label>
      <input
        type="number"
        v-model.number="petForm.userId"
        placeholder="请输入所属用户ID"
        @blur="validateField('userId')"
      >
      <span class="error">{{ errors.userId }}</span>
    </div>
    <div class="form-item">
      <label>宠物性别：</label>
      <select
        v-model="petForm.petGender"
        @change="validateField('petGender')"
      >
        <option value="">请选择性别</option>
        <option value="公">公</option>
        <option value="母">母</option>
      </select>
      <span class="error">{{ errors.petGender }}</span>
    </div>
    <div class="form-item">
      <label>疫苗状态：</label>
      <select
        v-model="petForm.vaccineStatus"
        @change="validateField('vaccineStatus')"
      >
        <option value="">请选择疫苗状态</option>
        <option value="已接种">已接种</option>
        <option value="未接种">未接种</option>
      </select>
      <span class="error">{{ errors.vaccineStatus }}</span>
    </div>
    <button @click="submitAddPet">提交新增</button>
    <button @click="resetForm" style="margin-left: 10px;">重置</button>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
// import { useRouter } from 'vue-router'
import { addPet } from '@/api/user/petApi' // 导入新增接口
// 路由实例（用于跳转）
// const router = useRouter()
// 表单数据
const petForm = reactive({
  petName: '',
  petBreed: '',
  petAge: null, // 初始为null（数字类型）
  userId: null, // 初始为null（数字类型）
  petGender: '',
  vaccineStatus: ''
})
// 错误信息
const errors = reactive({
  petName: '',
  petBreed: '',
  petAge: '',
  userId: '',
  petGender: '',
  vaccineStatus: ''
})

// 单个字段验证
const validateField = (field) => {
  switch (field) {
    case 'petName':
      errors.petName = petForm.petName.trim() ? '' : '宠物名称不能为空'
      break
    case 'petBreed':
      errors.petBreed = petForm.petBreed.trim() ? '' : '宠物品种不能为空'
      break
    case 'petAge':
      errors.petAge = petForm.petAge > 0 ? '' : '请输入有效的年龄（正整数）'
      break
    case 'userId':
      errors.userId = petForm.userId > 0 ? '' : '请输入有效的用户ID（正整数）'
      break
    case 'petGender':
      errors.petGender = petForm.petGender ? '' : '请选择宠物性别'
      break
    case 'vaccineStatus':
      errors.vaccineStatus = petForm.vaccineStatus ? '' : '请选择疫苗状态'
      break
  }
}

// 全表单验证
const validateForm = () => {
  // 验证所有字段
  Object.keys(errors).forEach(field => validateField(field))
  // 检查是否有错误
  return Object.values(errors).every(error => error === '')
}

// 提交新增
const submitAddPet = async () => {
  if (!validateForm()) return// 验证不通过则退出

  try {
    // 调用新增接口
    const res = await addPet(petForm)
    alert(res.data || '新增宠物成功')// 显示后端返回的提示
    resetForm()// 重置表单
    // 跳回列表页
    // router.push('/pet-list');
  } catch (err) {
    console.error('新增失败', err)
    alert('新增失败：' + (err.response?.data || '网络错误'))
  }
}

// 重置表单
const resetForm = () => {
  petForm.petName = ''
  petForm.petBreed = ''
  petForm.petAge = null
  petForm.userId = null
  petForm.petGender = ''
  petForm.vaccineStatus = ''
  // 清空错误信息
  Object.keys(errors).forEach(field => { errors[field] = '' })
}
</script>

<style scoped>

.form-item { margin-bottom: 15px; }
label {
  display: inline-block;
  width: 100px;
  text-align: right;
  margin-right: 10px;
  color: #666;
}
input, select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 200px;
}
button {
  padding: 8px 20px;
  background: #409EFF;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 110px;
}
button:hover { background: #66b1ff; }
.error {
  color: #F56C6C;
  font-size: 12px;
  margin-left: 10px;
}
h3 {
  color: #409EFF;
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}
</style>
