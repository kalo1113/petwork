<template>
  <div class="pet-list">
    <h2>宠物列表</h2>
    <div class="query-area">
      <input
        type="number"
        v-model="userId"
        placeholder="输入用户ID筛选"
      >
      <button @click="fetchPetList(1)">查询</button>
      <button @click="$router.push('/pet-add')">新增宠物</button>
      <button @click="$router.push('/image-upload')">上传图片</button>
    </div>

    <table>
      <thead>
      <tr>
        <th>宠物ID</th>
        <th>名称</th>
        <th>品种</th>
        <th>年龄</th>
        <th>用户ID</th>
        <th>性别</th>
        <th>疫苗状态</th>
        <th>图片</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="pet in petList" :key="pet.petId">
        <td>{{ pet.petId }}</td>
        <td>{{ pet.petName }}</td>
        <td>{{ pet.petBreed }}</td>
        <td>{{ pet.petAge }}</td>
        <td>{{ pet.userId }}</td>
        <td>{{ pet.petGender }}</td>
        <td>{{ pet.vaccineStatus }}</td>
        <td>
          <img v-if="pet.petImg" :src="pet.petImg" alt="宠物图片" style="width: 60px;">
          <span v-else>无</span>
        </td>
        <td>
          <button @click="$router.push(`/pet-edit/${pet.petId}`)">编辑</button>
          <button @click="handleDelete(pet.petId)" class="delete-btn">删除</button>
        </td>
      </tr>
      </tbody>
    </table>

    <div class="pagination" v-if="total > 0">
      <button @click="fetchPetList(1)" :disabled="currentPage === 1">首页</button>
      <button @click="fetchPetList(currentPage - 1)" :disabled="currentPage === 1">上一页</button>
      <span>第{{ currentPage }}页 / 共{{ totalPage }}页</span>
      <button @click="fetchPetList(currentPage + 1)" :disabled="currentPage >= totalPage">下一页</button>
      <button @click="fetchPetList(totalPage)" :disabled="currentPage === totalPage">尾页</button>
      <select v-model="pageSize" @change="fetchPetList(1)">
        <option value="5">5条/页</option>
        <option value="10">10条/页</option>
      </select>
    </div>
    <div v-else class="empty">暂无数据</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getPetPage, deletePet } from '@/api/user/petApi'

const userId = ref('')
const petList = ref([])
const currentPage = ref(1)
const pageSize = ref(5)
const total = ref(0)
const totalPage = ref(0)

// 获取列表数据
const fetchPetList = async page => {
  try {
    const res = await getPetPage(page, pageSize.value)
    console.log('后端返回数据：', res)// 打印完整数据，确认结构

    // 兼容不同后端格式
    const data = res.data || res // 若响应拦截器已处理，可能无需.data
    petList.value = data.records || data.list || []
    total.value = data.total || data.count || 0
    totalPage.value = data.pages || data.totalPage || 0
    currentPage.value = page
  } catch (err) {
    console.error('获取列表失败', err)
  }
}

// 删除宠物
const handleDelete = async (id) => {
  if (confirm(`确定删除ID为${id}的宠物吗？`)) {
    try {
      await deletePet(id)
      alert('删除成功')
      await fetchPetList(currentPage.value) // 刷新当前页
    } catch (err) {
      alert('删除失败：' + (err.response?.data || '网络错误'))
    }
  }
}

// 页面加载时查询
onMounted(() => {
  fetchPetList(1)
})
</script>

<style scoped>
.query-area { margin: 20px 0; }
table { width: 100%; border-collapse: collapse; margin: 20px 0; }
th, td { border: 1px solid #ddd; padding: 10px; text-align: center; }
th { background: #f5f5f5; }
button { margin: 0 5px; padding: 5px 10px; cursor: pointer; }
.delete-btn { background: #f56c6c; color: white; border: none; }
.pagination { margin: 20px 0; }
.empty { color: #999; text-align: center; padding: 20px; }
</style>
