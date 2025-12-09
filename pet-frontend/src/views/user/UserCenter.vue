<template>
  <div class="center-container">
    <div class="user-center">
      <!-- 顶部用户信息 -->
      <div class="user-info">
        <div class="avatar">
          <img src="@/assets/images/我的图标/默认头像.svg" alt="用户头像" @click="handleAvatarClick" />
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

      <!-- 宠物管理 -->
      <div class="pet-management white-bg">
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

      <!-- 我的订单 -->
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

      <!-- 宠物保障 -->
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
import { ref, reactive, onMounted } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus'
// 核心修改：导入原生axios，不再用封装的request
import axios from 'axios'

// 登录状态管理
const userInfo = ref({
  isLogin: false,
  username: '',
  userId: null,
  avatar: ''
})

// 弹窗控制
const dialogVisible = ref(false)
const activeTab = ref('login')
// 错误提示：核心变量
const errorMsg = ref('')

// 登录表单
const loginForm = reactive({
  email: '',
  password: ''
})
// 注册表单
const registerForm = reactive({
  username: '',
  email: '',
  password: ''
})

// 清空错误提示
const clearError = () => {
  errorMsg.value = ''
}

// 页面加载时检查登录状态
onMounted(() => {
  checkLoginStatus()
})

// 检查登录状态
const checkLoginStatus = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const parsed = JSON.parse(userData)
    userInfo.value = {
      isLogin: true,
      username: parsed.username || `宝友${parsed.userId?.toString().slice(-4)}`,
      userId: parsed.userId,
      avatar: parsed.avatar || ''
    }
  }
}

// 核心修复：使用原生Axios，手动配置请求
const handleAuthSubmit = async () => {
  try {
    clearError()
    // 配置Axios基础信息（替换为你的后端地址）
    const baseURL = 'http://localhost:8080'
    let url = ''
    let data = {}

    // 区分登录/注册
    if (activeTab.value === 'login') {
      url = `${baseURL}/user/login`
      data = { email: loginForm.email, password: loginForm.password }
    } else {
      url = `${baseURL}/user/register`
      data = { username: registerForm.username, email: registerForm.email, password: registerForm.password }
    }

    // 发起原生Axios请求（禁用拦截器）
    const response = await axios({
      method: 'post',
      url: url,
      data: data,
      headers: {
        'Content-Type': 'application/json;charset=utf-8'
      },
      // 禁用所有拦截器（关键）
      transformRequest: [(data) => JSON.stringify(data)],
      transformResponse: [(data) => JSON.parse(data)]
    })

    // 强制解析后端返回的Result对象
    const res = response.data || {}
    console.log('后端原始响应：', res) // 控制台打印，方便调试

    if (res.code === 200) {
      // 成功逻辑
      if (activeTab.value === 'login') {
        localStorage.setItem('userData', JSON.stringify(res.data))
        dialogVisible.value = false
        ElMessage.success('登录成功！')
        window.location.reload()
      } else {
        ElMessage.success(res.msg || '注册成功，请登录！')
        activeTab.value = 'login'
        // 清空注册表单
        registerForm.username = ''
        registerForm.email = ''
        registerForm.password = ''
      }
    } else {
      // 强制显示后端错误信息（核心）
      errorMsg.value = res.msg || (activeTab.value === 'login' ? '登录失败！' : '注册失败！')
      // 聚焦错误字段
      if (res.code === 400 || res.code === 409) {
        const emailInput = document.querySelector(`.${activeTab.value}-form input[placeholder="请输入邮箱"]`)
        if (emailInput) emailInput.focus()
      }
    }
  } catch (error) {
    // 捕获所有错误，强制解析后端msg
    console.error('请求错误：', error)
    // 优先读取后端返回的错误信息
    const errData = error.response?.data || {}
    errorMsg.value = errData.msg ||
      (activeTab.value === 'login' ? '登录失败，请检查邮箱/密码！' : '注册失败，请检查信息！')
    // 网络错误兜底
    if (!error.response) {
      errorMsg.value = '网络异常，请检查后端服务是否启动！'
    }

    // 聚焦邮箱输入框（错误时默认聚焦）
    const emailInput = document.querySelector(`.${activeTab.value}-form input[placeholder="请输入邮箱"]`)
    if (emailInput) emailInput.focus()
  }
}

// 事件处理函数（原有不变）
const handleAvatarClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
  }
}

const handleNameClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
  }
}

const handleEditClick = () => {
  ElMessage.info('编辑用户信息功能待实现')
}

const handleAddPetClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  ElMessage.info('跳转到添加宠物页面')
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
  max-width: 1000px;
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
  margin-top: 4px;
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
</style>
