<template>
  <div class="pet-edit">
    <h2>编辑宠物</h2>
    <div class="form-item">
      <label>宠物名称：</label>
      <input
        type="text"
        v-model="petForm.petName"
        @blur="validate('petName')"
      >
      <span class="error">{{ errors.petName }}</span>
    </div>
    <div class="form-item">
      <label>宠物品种：</label>
      <input
        type="text"
        v-model="petForm.petBreed"
        @blur="validate('petBreed')"
      >
      <span class="error">{{ errors.petBreed }}</span>
    </div>
    <div class="form-item">
      <label>宠物年龄：</label>
      <input
        type="number"
        v-model.number="petForm.petAge"
        @blur="validate('petAge')"
      >
      <span class="error">{{ errors.petAge }}</span>
    </div>
    <div class="form-item">
      <label>用户ID：</label>
      <input
        type="number"
        v-model.number="petForm.userId"
        @blur="validate('userId')"
      >
      <span class="error">{{ errors.userId }}</span>
    </div>
    <div class="form-item">
      <label>性别：</label>
      <select v-model="petForm.petGender" @change="validate('petGender')">
        <option value="">请选择</option>
        <option value="公">公</option>
        <option value="母">母</option>
      </select>
      <span class="error">{{ errors.petGender }}</span>
    </div>
    <div class="form-item">
      <label>疫苗状态：</label>
      <select v-model="petForm.vaccineStatus" @change="validate('vaccineStatus')">
        <option value="">请选择</option>
        <option value="已接种">已接种</option>
        <option value="未接种">未接种</option>
      </select>
      <span class="error">{{ errors.vaccineStatus }}</span>
    </div>
    <button @click="submit">保存修改</button>
    <button @click="$router.back()" style="margin-left: 10px;">返回</button>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPetById, updatePet } from '@/api/user/petApi'

const route = useRoute()
const router = useRouter()
const petId = route.params.id
console.log('当前宠物ID：', petId) // 打印验证
const petForm = reactive({
  petId: null,
  petName: '',
  petBreed: '',
  petAge: null,
  userId: null,
  petGender: '',
  vaccineStatus: ''
})

const errors = reactive({
  petName: '',
  petBreed: '',
  petAge: '',
  userId: '',
  petGender: '',
  vaccineStatus: ''
})

// 加载宠物详情
const loadPetDetail = async () => {
  try {
    const res = await getPetById(petId)
    Object.assign(petForm, res.data || res)
  } catch (err) {
    console.error('加载详情失败：', err)
    alert('加载数据失败：' + (err.response?.data || '网络错误'))
    router.back()
  }
}

// 验证逻辑（同新增页）
const validate = (field) => {
  switch (field) {
    case 'petName':
      errors.petName = petForm.petName.trim() ? '' : '名称不能为空'
      break
    case 'petBreed':
      errors.petBreed = petForm.petBreed.trim() ? '' : '品种不能为空'
      break
    case 'petAge':
      errors.petAge = petForm.petAge > 0 ? '' : '年龄必须为正整数'
      break
    case 'userId':
      errors.userId = petForm.userId > 0 ? '' : '用户ID必须为正整数'
      break
    case 'petGender':
      errors.petGender = petForm.petGender ? '' : '请选择性别'
      break
    case 'vaccineStatus':
      errors.vaccineStatus = petForm.vaccineStatus ? '' : '请选择疫苗状态'
      break
  }
}

const validateForm = () => {
  Object.keys(errors).forEach(key => validate(key))
  return Object.values(errors).every(v => v === '')
}

// 提交修改
const submit = async () => {
  if (!validateForm()) return
  try {
    await updatePet(petForm)
    alert('修改成功')
    await router.push('/pet-list')
  } catch (err) {
    alert('修改失败：' + (err.response?.data || '网络错误'))
  }
}

// 页面加载时获取数据
onMounted(loadPetDetail)
</script>

<style scoped>
/* 同新增页样式 */
.form-item { margin: 15px 0; }
label { display: inline-block; width: 100px; text-align: right; margin-right: 10px; }
input, select { padding: 6px; width: 200px; }
.error { color: #f56c6c; font-size: 12px; margin-left: 10px; }
button { padding: 6px 15px; margin-left: 110px; cursor: pointer; }
</style>
