<template>
  <div class="center-container">
    <div class="user-center">
      <!-- 顶部用户信息 -->
      <div class="user-info">
        <div class="avatar">
          <img
            :src="userInfo.avatarUrl || defaultAvatar"
            alt="用户头像"
            @click="handleAvatarClick"
          />
        </div>
        <div class="name" @click="handleNameClick">
          {{ userInfo.isLogin ? userInfo.username : '暂未登录' }}
        </div>
        <button class="edit-btn" v-if="userInfo.isLogin" @click="handleEditClick">
          <img src="@/assets/images/我的图标/编辑.svg" alt="编辑图标" class="edit-icon" />
        </button>
        <div class="privacy-tag" v-if="userInfo.isLogin">
          宠物信息访客可见
        </div>
      </div>

      <!-- 条件渲染：无宠物信息 → 显示待添加盒；有信息 → 显示选项卡+卡片 -->
      <div v-if="!petList.length" class="pet-management white-bg">
        <h3>待添加</h3>
        <div class="add-pet">
          <div class="add-icon">
            <img src="@/assets/images/我的图标/添加.svg" alt="添加图标" class="add-icon-img" />
          </div>
          <div class="add-desc">
            <p class="pet-status">暂无宠物信息</p>
            <p class="pet-privilege">开启宠物卡 享多项特权</p>
          </div>
          <button class="btn primary-btn" @click="handleAddPetClick">
            添加宠物
          </button>
        </div>
      </div>

      <div v-else>
        <!-- 新增：宠物选项卡（点击切换） -->
        <div class="pet-tabs">
          <div
            v-for="(pet, index) in petList"
            :key="pet.petId"
            class="pet-tab"
            :class="{ active: activePetIndex === index }"
            @click="activePetIndex = index"
          >
            {{ pet.name }}
          </div>
          <div class="add-pet-tab" @click="handleAddPetClick">+添加宠物</div>
        </div>

        <!-- 宠物卡片容器（仅展示当前选中的宠物） -->
        <div class="pet-card-container">
          <!-- 仅渲染当前选中的宠物 -->
          <div class="pet-card">
            <!-- 宠物头像 -->
            <div class="pet-avatar">
              <img
                :src="currentPet.avatarUrl"
                alt="宠物头像"
                class="avatar-img"
              />
            </div>

            <!-- 宠物信息区（动态绑定当前选中宠物数据） -->
            <div class="pet-info">
              <div class="pet-name-row">
                <h4 class="pet-name">{{ currentPet.name }}</h4>
                <!-- 性别图标（已修复路径） -->
                <img
                  :src="currentPet.gender === '公' ? maleIcon : femaleIcon"
                  alt="性别"
                  class="sex"
                />
                <span class="pet-tag">宠物待保障</span>
              </div>
              <div class="pet-desc">
                {{ currentPet.breed }} {{ currentPet.age }}
                <img src="@/assets/images/我的图标/编辑.svg" alt="编辑图标" @click="handleEditPetClick(currentPet.petId)" />
              </div>
              <p class="pet-id">No.{{ currentPet.uniqueId }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 我的订单、宠物保障等原有模块保持不变 -->
      <div class="my-orders white-bg">
        <h3>我的订单</h3>
        <div class="order-tabs">
          <div class="tab-item" @click="handleOrderTabClick">
            <img src="@/assets/images/我的图标/待生效.svg" alt="待生效" class="order-icon" />
            <span>待生效</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick">
            <img src="@/assets/images/我的图标/待预约.svg" alt="待预约" class="order-icon" />
            <span>待预约</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick">
            <img src="@/assets/images/我的图标/待发货.svg" alt="待发货" class="order-icon" />
            <span>待发货</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick">
            <img src="@/assets/images/我的图标/待收货.svg" alt="待收货" class="order-icon" />
            <span>待收货</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick">
            <img src="@/assets/images/我的图标/待评价.svg" alt="待评价" class="order-icon" />
            <span>待评价</span>
          </div>
        </div>
      </div>

      <div class="pet-guarantee white-bg">
        <h3>宠物保障</h3>
        <div class="guarantee-card">
          <div class="guarantee-content">
            <p class="guarantee-text">当前暂无保障</p>
            <p class="guarantee-desc">快去给爱宠开启一份保障吧</p>
            <button class="btn secondary-btn" @click="handleGuaranteeClick">
              去看看
            </button>
          </div>
          <div class="guarantee-icon">
            <img src="@/assets/images/我的图标/保障.svg" alt="保障图标" />
          </div>
        </div>
      </div>
    </div>
    <!-- 登录/注册弹窗 -->
    <div>
      <!-- 登录/注册弹窗 (未登录时显示) -->
      <el-dialog
        v-model="dialogVisible"
        width="30%"
        :close-on-click-modal="false"
      >
        <!-- 新增：错误提示区域（仅在有错误时显示，不影响原有样式） -->
        <div v-if="errorMsg" class="error-tip">
          <i class="el-icon-error"></i> {{ errorMsg }}
        </div>

        <div class="login-tabs">
          <div
            class="login-tab"
            :class="{ active: activeTab === 'login' }"
            @click="() => { activeTab = 'login'; clearError() }"
          >
            登录
          </div>
          <div
            class="login-tab"
            :class="{ active: activeTab === 'register' }"
            @click="() => { activeTab = 'register'; clearError() }"
          >
            注册
          </div>
        </div>
        <div v-if="activeTab === 'login'" class="login-form">
          <el-form :model="loginForm" label-width="80px">
            <el-form-item label="邮箱">
              <el-input
                v-model="loginForm.email"
                placeholder="请输入邮箱"
                @input="clearError"
              ></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                @input="clearError"
              ></el-input>
            </el-form-item>
          </el-form>
        </div>
        <div v-else class="register-form">
          <el-form :model="registerForm" label-width="80px">
            <el-form-item label="用户名">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                @input="clearError"
              ></el-input>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                @input="clearError"
              ></el-input>
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                @input="clearError"
              ></el-input>
            </el-form-item>
          </el-form>
        </div>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAuthSubmit">
            {{ activeTab === 'login' ? '登录' : '注册' }}
          </el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { login, register, getPetListByUserId } from '@/api/user/index.js'
import defaultAvatar from '@/assets/images/我的图标/默认头像.svg'
import defaultPetAvatar from '@/assets/images/我的图标/添加.svg'
// 导入性别图标（修复动态绑定路径问题）
import maleIcon from '@/assets/images/我的图标/男.svg'
import femaleIcon from '@/assets/images/我的图标/女.svg'

const router = useRouter()

// 登录状态管理
const userInfo = ref({
  isLogin: false,
  username: '',
  userId: null,
  avatarUrl: ''
})

// 弹窗控制
const dialogVisible = ref(false)
const activeTab = ref('login')
const errorMsg = ref('')

// 登录/注册表单
const loginForm = reactive({ email: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '' })

// ========== 核心：宠物列表+选项卡激活索引 ==========
const petList = ref([])
const activePetIndex = ref(0) // 默认选中第一个宠物

// 计算属性：当前选中的宠物
const currentPet = computed(() => {
  return petList.value[activePetIndex.value] || {}
})

// 清空错误提示
const clearError = () => {
  errorMsg.value = ''
}

// 页面加载时检查登录状态+获取宠物列表
onMounted(() => {
  checkLoginStatus()
})

// 检查登录状态
const checkLoginStatus = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const parsed = JSON.parse(userData)
    const avatarUrl = parsed.avatarUrl || defaultAvatar
    userInfo.value = {
      isLogin: true,
      username: parsed.username || `宝友${parsed.userId?.toString().slice(-4)}`,
      userId: parsed.userId,
      avatarUrl: avatarUrl
    }
    // 获取宠物列表
    fetchAndPrintPetList()
  }
}

// ========== 完善：获取并格式化宠物列表 ==========
const fetchAndPrintPetList = async () => {
  try {
    const res = await getPetListByUserId(userInfo.value.userId)
    if (res.code === 200) {
      // 格式化宠物数据（适配页面展示）
      petList.value = res.data.map(pet => ({
        name: pet.petName,
        avatarUrl: pet.petFacePhoto || defaultPetAvatar,
        breed: pet.petType,
        age: calculatePetAge(pet.petBirthday), // 动态计算年龄
        uniqueId: pet.petUniqueId || `${pet.petId}`,
        petId: pet.petId,
        gender: pet.petGender || '公', // 后端无性别时默认“公”
        sterilized: pet.isSterilized
      }))
      console.log(`【用户${userInfo.value.userId}的宠物列表】`, petList.value)
    } else {
      console.log(`【获取宠物列表失败】${res.msg || '未知错误'}`)
      petList.value = []
    }
  } catch (err) {
    console.error('【获取宠物列表异常】', err)
    petList.value = []
  }
}

// ========== 新增：计算宠物年龄 ==========
const calculatePetAge = (birthday) => {
  if (!birthday) return '未知年龄'
  const birthDate = new Date(birthday)
  const now = new Date()
  let ageYear = now.getFullYear() - birthDate.getFullYear()
  let ageMonth = now.getMonth() - birthDate.getMonth()
  if (ageMonth < 0) {
    ageYear--
    ageMonth += 12
  }
  return `${ageYear}岁${ageMonth}个月`
}

// 登录/注册提交（原有逻辑保持不变）
const isLoading = ref(false)
const handleAuthSubmit = async () => {
  try {
    isLoading.value = true
    clearError()
    let result // 直接接收后端返回的{code:200, msg:"", data:{}}

    if (activeTab.value === 'login') {
      // 直接接收响应拦截器返回的后端原始数据
      result = await login({
        email: loginForm.email,
        password: loginForm.password
      })
    } else {
      result = await register({
        username: registerForm.username,
        email: registerForm.email,
        password: registerForm.password
      })
    }

    // 核心：根据后端返回的code判断成功/失败
    if (result.code === 200) {
      if (activeTab.value === 'login') {
        // 登录成功逻辑
        const userData = result.data
        userData.avatarUrl = userData.avatarUrl || defaultAvatar
        localStorage.setItem('userData', JSON.stringify(userData))
        dialogVisible.value = false
        ElMessage.success('登录成功！')
        window.location.reload()
      } else {
        ElMessage.success(result.msg || '注册成功，请登录！')
        activeTab.value = 'login'
        registerForm.username = registerForm.email = registerForm.password = ''
      }
    } else {
      // 后端返回业务失败（如密码错误）
      errorMsg.value = result.msg || (activeTab.value === 'login' ? '登录失败！' : '注册失败！')
    }
  } catch (error) {
    // 这里仅捕获网络错误（如后端服务未启动）
    console.error('请求错误：', error)
    errorMsg.value = error.msg || '网络异常，请检查后端服务是否启动！'
  } finally {
    isLoading.value = false
  }
}

// 事件处理函数（原有逻辑保持不变）
const handleAvatarClick = () => {
  if (!userInfo.value.isLogin) dialogVisible.value = true
}
const handleNameClick = () => {
  if (!userInfo.value.isLogin) dialogVisible.value = true
}
const handleEditClick = () => {
  if (!userInfo.value.userId) {
    ElMessage.warning('请先登录后再进入个人设置！')
    return
  }
  router.push({ path: '/user/setting' }).then(() => {
    ElMessage.info('正在跳转到个人设置页面')
  }).catch(err => {
    console.error('路由跳转失败：', err)
    ElMessage.error('跳转失败，请检查路由配置')
  })
}

// 编辑宠物：跳转至pet-id-card并携带petId（标识编辑模式）
const handleEditPetClick = (petId) => {
  // 1. 校验登录状态
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }

  try {
    // 2. 跳转至同一个路由，新增petId参数（区分添加/编辑）
    router.push({
      path: '/pet-id-card',
      query: {
        userId: userInfo.value.userId, // 保留用户ID
        petId: petId, // 传递要编辑的宠物ID
        type: 'edit' // 可选：显式标识是编辑模式
      }
    })
    ElMessage.success('正在前往编辑宠物信息页面')
  } catch (err) {
    console.error('编辑宠物路由跳转失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  }
}

const handleAddPetClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  try {
    router.push({ path: '/pet-id-card', query: { userId: userInfo.value.userId } })
    ElMessage.success('正在前往添加宠物页面')
  } catch (err) {
    console.error('路由跳转失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  }
}
const handleOrderTabClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  ElMessage.info('查看订单列表')
}
const handleGuaranteeClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  ElMessage.info('查看宠物保障页面')
}
</script>

<style scoped>
/* 原有样式保持不变 */
.center-container {
  width: 100%;
  max-width: 1200px;
  min-width: 320px;
  margin: 0 auto;
  padding: 20px 15px;
  box-sizing: border-box;
  background: url('@/assets/images/我的图标/背景图.jpg') no-repeat center center;
  background-size: cover;
  min-height: 100vh;
}

.user-center {
  height: 100%;
  padding: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 16px;
  cursor: pointer;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name {
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
}

.edit-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  margin-left: 8px;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.edit-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.edit-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.privacy-tag {
  font-size: 12px;
  color: #666;
  margin-left: auto;
}

.white-bg {
  background-color: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.white-bg:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.pet-management {
  margin-bottom: 20px;
}

.add-pet {
  display: flex;
  align-items: center;
  background: #e3f2fd;
  border-radius: 8px;
  padding: 16px;
  transition: background-color 0.2s;
}

.add-pet:hover {
  background: #d1e9fc;
}

.add-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  background: #bbdefb;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  transition: background-color 0.2s;
}

.add-pet:hover .add-icon {
  background: #90caf9;
}

.add-icon-img {
  width: 30px;
  height: 30px;
  object-fit: contain;
}

.add-desc {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-right: auto;
}

.pet-status {
  font-size: 16px;
  color: #333;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.pet-privilege {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.my-orders {
  margin-bottom: 20px;
}

.order-tabs {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  gap: 8px;
}

.tab-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  padding: 12px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.tab-item:hover {
  background-color: #f5f5f5;
}

.order-icon {
  width: 30px;
  height: 30px;
  object-fit: contain;
  margin-bottom: 4px;
}

.tab-item span {
  font-size: 14px;
  color: #333;
}

.pet-guarantee {
  margin-bottom: 20px;
}

.guarantee-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #ebf6fd;
  border-radius: 8px;
  padding: 20px;
  transition: background-color 0.2s;
}

.guarantee-card:hover {
  background: #e3f2fd;
}

.guarantee-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex: 1;
}

.guarantee-text {
  font-size: 16px;
  color: #333;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.guarantee-desc {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

.guarantee-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background: #f0f9ff;
  border-radius: 50%;
  margin-left: 20px;
  transition: transform 0.3s ease;
}

.guarantee-card:hover .guarantee-icon {
  transform: scale(1.05);
}

.guarantee-icon img {
  width: 60px;
  height: 60px;
  object-fit: contain;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  outline: none;
}

.primary-btn {
  background-color: #2196f3;
  color: #fff;
}

.primary-btn:hover {
  background-color: #1976d2;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.3);
  transform: translateY(-1px);
}

.primary-btn:active {
  background-color: #1565c0;
  box-shadow: 0 1px 4px rgba(33, 150, 243, 0.3);
  transform: translateY(0);
}

.secondary-btn {
  background-color: #42a5f5;
  color: #fff;
}

.secondary-btn:hover {
  background-color: #1e88e5;
  box-shadow: 0 2px 8px rgba(66, 165, 245, 0.3);
  transform: translateY(-1px);
}

.secondary-btn:active {
  background-color: #1976d2;
  box-shadow: 0 1px 4px rgba(66, 165, 245, 0.3);
  transform: translateY(0);
}

/* 新增登录弹窗样式 */
.login-tabs {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.login-tab {
  padding: 10px 20px;
  cursor: pointer;
  border-bottom: 2px solid transparent;
}

.login-tab.active {
  border-bottom-color: #2196f3;
  color: #2196f3;
  font-weight: 500;
}

.login-form,
.register-form {
  padding: 10px 0;
}

/* 响应式样式保持不变 */
@media (max-width: 768px) {
  .center-container {
    padding: 15px 10px;
  }

  .user-center {
    padding: 12px;
  }

  .avatar {
    width: 50px;
    height: 50px;
  }

  .name {
    font-size: 16px;
  }

  .add-icon {
    width: 50px;
    height: 50px;
  }

  .order-icon {
    width: 24px;
    height: 24px;
  }

  .tab-item span {
    font-size: 12px;
  }

  .guarantee-icon {
    width: 80px;
    height: 80px;
  }

  .guarantee-icon img {
    width: 50px;
    height: 50px;
  }

  .guarantee-card {
    flex-direction: column;
    text-align: center;
  }

  .guarantee-content {
    margin-bottom: 16px;
  }

  .guarantee-icon {
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .add-pet {
    flex-wrap: wrap;
    justify-content: center;
    text-align: center;
  }

  .add-desc {
    margin-right: 0;
    margin-bottom: 12px;
  }

  .btn {
    width: 100%;
    margin-left: 0;
  }

  .order-tabs {
    flex-wrap: wrap;
  }

  .tab-item {
    width: 33.333%;
    margin-bottom: 8px;
  }
}
/* 错误提示样式：红色背景+居中+内边距 */
.error-tip {
  background-color: #fef0f0;
  color: #f56c6c;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 表单容器间距 */
.form-container {
  margin-top: 20px;
}

/* 按钮宽度100% */
.btn-full {
  width: 100%;
}

/* 输入框聚焦时高亮 */
.el-input__inner:focus {
  border-color: #2196f3;
}

/* 容器样式 */
.pet-card-container {
  width: 100%;
  margin: 16px 0;
  font-family: "微软雅黑", sans-serif;
}

/* 顶部标题栏 */
.pet-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  padding: 0 4px;
}
.pet-card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.add-pet-btn {
  border: none;
  background: transparent;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
}
.add-pet-btn:hover {
  color: #2196f3;
}

/* 卡片主体 */
.pet-card {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

/* 头像区域（含徽章） */
.pet-avatar {
  position: relative;
  width: 80px;
  height: 80px;
  margin-right: 16px;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}
.pet-badge {
  position: absolute;
  bottom: -8px;
  right: -8px;
  width: 40px;
  height: 40px;
  z-index: 10;
}
.sex {
  width: 20px;
  height: 20px;
}

/* 信息区域 */
.pet-info {
  flex: 1;
}
.pet-name-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  display: flex;
}
.pet-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.pet-tag {
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 8px;
  border-radius: 4px;
  margin-left: auto
}
.pet-desc {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}
.pet-desc img{
width:15px;
margin-left: 10px;
cursor: pointer;
}
.pet-id {
  font-size: 12px;
  color: #999;
  margin-top: 20px;
}

/* 新增：宠物选项卡样式（匹配设计风格） */
.pet-tabs {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
  margin-bottom: 12px;
}
.pet-tab {
  padding: 4px 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  color: #666;
  transition: all 0.2s ease;
}
.pet-tab.active {
  color: #2196f3;
  border-bottom: 2px solid #2196f3;
}
.add-pet-tab {
  margin-left: auto;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s ease;
}
.add-pet-tab:hover {
  color: #2196f3;
}
</style>
