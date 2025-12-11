<template>
  <div class="user-setting-container">
    <!-- 顶部导航栏 -->
    <div class="setting-header">
      <el-button
        type="text"
        class="back-btn"
        @click="handleBack"
        icon="el-icon-arrow-left"
      >
        &lt;
      </el-button>
      <h3 class="setting-title">个人设置</h3>
    </div>

    <!-- 主要内容区 -->
    <div class="setting-content">
      <!-- 1. 头像与昵称 -->
      <div class="setting-item avatar-item">
        <div class="item-label">头像</div>
        <div class="avatar-wrap">
          <img :src="userInfo.avatarUrl || defaultAvatar" alt="用户头像" class="user-avatar" />
          <input
            type="file"
            class="avatar-upload"
            accept="image/*"
            @change="handleAvatarUpload"
            id="avatarInput"
          />
          <label for="avatarInput" class="upload-text">更换头像</label>
        </div>
      </div>

      <!-- 2. 昵称编辑 -->
      <div class="setting-item nickname-item">
        <div class="item-label">昵称</div>
        <div class="nickname-wrap">
          <span class="nickname-text">{{ userInfo.username }}</span>
          <!-- 修正后 -->
<el-button
  type="text"
  class="edit-btn"
  @click.stop="openNicknameEdit"
>
  编辑
</el-button>
        </div>
      </div>

      <!-- 3. 修改密码 -->
      <div class="setting-item pwd-item" @click="openPwdEdit">
        <div class="item-label">修改密码</div>
      </div>

      <!-- 4. 切换账号 -->
      <div class="setting-item switch-account-item" @click="openSwitchAccount">
        <div class="item-label">切换账号</div>
      </div>

      <!-- 5. 退出登录 -->
      <div class="setting-item logout-item" @click="handleLogout">
        <div class="item-label logout-text">退出登录</div>
      </div>
    </div>

    <!-- 昵称编辑弹窗 -->
<el-dialog
  key="nickname-dialog"
  v-model="nicknameDialogVisible"
  title="修改昵称"
  width="30%"
  :close-on-click-modal="false"
  :append-to-body="true"
>
      <el-form :model="nicknameForm" label-width="60px" :rules="nicknameRules" ref="nicknameFormRef">
        <el-form-item label="昵称" prop="username">
          <el-input
            v-model="nicknameForm.username"
            placeholder="请输入新昵称"
            maxlength="10"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nicknameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNickname">保存</el-button>
      </template>
    </el-dialog>

    <!-- 密码修改弹窗 -->
    <el-dialog
      v-model="pwdDialogVisible"
      title="修改密码"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="pwdForm" label-width="80px" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="原密码" prop="oldPwd">
          <el-input
            v-model="pwdForm.oldPwd"
            type="password"
            placeholder="请输入原密码"
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="newPwd">
          <el-input
            v-model="pwdForm.newPwd"
            type="password"
            placeholder="请输入6位以上新密码"
            minlength="6"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPwd">
          <el-input
            v-model="pwdForm.confirmPwd"
            type="password"
            placeholder="请再次输入新密码"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePassword">保存</el-button>
      </template>
    </el-dialog>

    <!-- 切换账号弹窗 -->
    <el-dialog
      v-model="switchAccountDialogVisible"
      title="切换账号"
      width="35%"
      :close-on-click-modal="false"
    >
      <!-- 当前账号 -->
      <div class="current-account">
        <img :src="userInfo.avatarUrl || defaultAvatar" alt="当前账号头像" class="account-avatar" />
        <div class="account-info">
          <div class="account-name">{{ userInfo.username }}</div>
          <div class="account-email">{{ userInfo.email }}</div>
        </div>
        <span class="current-tag">当前使用</span>
      </div>

      <!-- 账号列表（模拟多账号，可从本地缓存读取） -->
      <div class="account-list" v-if="otherAccounts.length">
        <div class="account-item" v-for="account in otherAccounts" :key="account.userId" @click="switchToAccount(account)">
          <img :src="account.avatarUrl || defaultAvatar" alt="账号头像" class="account-avatar" />
          <div class="account-info">
            <div class="account-name">{{ account.username }}</div>
            <div class="account-email">{{ account.email }}</div>
          </div>
        </div>
      </div>

      <!-- 添加账号 -->
      <div class="add-account" @click="handleAddAccount">

        <span>添加账号</span>
      </div>

      <template #footer>
        <el-button @click="switchAccountDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 登录弹窗（添加账号用） -->
    <el-dialog
      v-model="loginDialogVisible"
      title="登录"
      width="30%"
      :close-on-click-modal="false"
    >
      <el-form :model="loginForm" label-width="60px" :rules="loginRules" ref="loginFormRef">
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="loginForm.email"
            placeholder="请输入登录邮箱"
          ></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入登录密码"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="loginDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElLoading, ElForm, ElDialog, ElButton, ElFormItem, ElInput } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'
import defaultAvatar from '@/assets/images/我的图标/默认头像.svg'
// 导入全局配置（统一管理baseURL）
import { BASE_URL } from '@/config/index.js'

// 初始化路由实例
const router = useRouter()

// ========== 状态管理 ==========
// 用户信息
const userInfo = ref({
  isLogin: false,
  userId: '',
  username: '',
  email: '',
  avatarUrl: ''
})

// 其他账号列表
const otherAccounts = ref([])

// 弹窗控制
const nicknameDialogVisible = ref(false)
const pwdDialogVisible = ref(false)
const switchAccountDialogVisible = ref(false)
const loginDialogVisible = ref(false)

// 表单Ref（用于校验）
const nicknameFormRef = ref(null)
const pwdFormRef = ref(null)
const loginFormRef = ref(null)

// 表单数据
const nicknameForm = reactive({
  username: ''
})
const pwdForm = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: ''
})
const loginForm = reactive({
  email: '',
  password: ''
})

// 表单校验规则
const nicknameRules = ref({
  username: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 10, message: '昵称长度为2-10个字符', trigger: 'blur' }
  ]
})
const pwdRules = ref({
  oldPwd: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不少于6位', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPwd) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})
const loginRules = ref({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
})

// ========== 生命周期 ==========
onMounted(() => {
  initUserInfo()
  initOtherAccounts()
})

// ========== 初始化方法 ==========
/** 初始化当前登录用户信息 */
const initUserInfo = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const parsed = JSON.parse(userData)
    userInfo.value = {
      isLogin: true,
      userId: parsed.userId,
      username: parsed.username,
      email: parsed.email,
      // 拼接完整头像URL（兼容后端返回的文件名）
      avatarUrl: parsed.avatarUrl
        ? (parsed.avatarUrl.startsWith('http') ? parsed.avatarUrl : `${BASE_URL}/avatar/${encodeURIComponent(parsed.avatarUrl)}`)
        : defaultAvatar
    }
    nicknameForm.username = userInfo.value.username
  }
}

/** 初始化其他账号列表 */
const initOtherAccounts = () => {
  const accountList = localStorage.getItem('accountList')
  if (accountList) {
    otherAccounts.value = JSON.parse(accountList).filter(item => item.userId !== userInfo.value.userId)
  }
}

// ========== 事件处理 ==========
/** 返回上一页 */
const handleBack = () => {
  router.push('/my').catch(err => {
    console.error('跳转个人中心失败：', err)
    ElMessage.error('返回失败，请重试')
  })
}

/** 头像上传处理（适配中文文件名） */
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  // 1. 文件校验
  if (file.size > 10 * 1024 * 1024) {
    return ElMessage.error('头像文件大小不能超过10MB')
  }
  const acceptTypes = ['image/jpeg', 'image/png', 'image/gif']
  if (!acceptTypes.includes(file.type)) {
    return ElMessage.error('仅支持jpg/png/gif格式的头像')
  }

  // 2. 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '头像上传中...',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    // 3. 构建表单数据（保留原始文件名）
    const formData = new FormData()
    formData.append('file', file)
    formData.append('userId', userInfo.value.userId)

    // 4. 调用后端接口
    const res = await axios.post(`${BASE_URL}/user/uploadAvatar`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      // 允许中文文件名（避免后端接收乱码）
      transformRequest: [(data) => data]
    })

    // 5. 处理响应（核心修复：直接使用后端返回的完整URL）
    loading.close()
    if (res.data.code === 200) {
      // 后端返回的是完整可访问的URL，直接赋值
      userInfo.value.avatarUrl = res.data.data
      // 更新本地缓存：存完整URL，而非文件名
      localStorage.setItem('userData', JSON.stringify({
        ...JSON.parse(localStorage.getItem('userData')),
        avatarUrl: res.data.data // 存完整URL
      }))
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.data.msg || '头像上传失败')
    }
  } catch (err) {
    loading.close()
    console.error('头像上传失败：', err)
    ElMessage.error('头像上传失败，请检查网络或联系管理员')
  }
}

/** 打开昵称编辑弹窗 */
const openNicknameEdit = () => {
  console.log('点击编辑，当前userInfo：', userInfo.value)
  console.log('弹窗初始状态：', nicknameDialogVisible.value)
  // 1. 强制赋值（避免响应式失效）
  nicknameForm.username = userInfo.value.username || '默认昵称'
  // 2. 异步触发（解决Element Plus弹窗渲染延迟）
  setTimeout(() => {
    nicknameDialogVisible.value = true
    console.log('弹窗赋值后状态：', nicknameDialogVisible.value)
  }, 0)
}

/** 保存昵称修改 */
const saveNickname = async () => {
  // 表单校验
  const valid = await nicknameFormRef.value.validate()
  if (!valid) return

  try {
    const res = await axios.post(`${BASE_URL}/user/updateNickname`, {
      userId: userInfo.value.userId,
      username: nicknameForm.username
    })

    if (res.data.code === 200) {
      userInfo.value.username = nicknameForm.username
      // 更新本地缓存
      localStorage.setItem('userData', JSON.stringify({
        ...JSON.parse(localStorage.getItem('userData')),
        username: nicknameForm.username
      }))
      nicknameDialogVisible.value = false
      ElMessage.success('昵称修改成功')
    } else {
      ElMessage.error(res.data.msg || '昵称修改失败')
    }
  } catch (err) {
    console.error('昵称修改失败：', err)
    ElMessage.error('昵称修改失败，请检查网络')
  }
}

/** 打开密码修改弹窗 */
const openPwdEdit = () => {
  pwdForm.oldPwd = ''
  pwdForm.newPwd = ''
  pwdForm.confirmPwd = ''
  pwdDialogVisible.value = true
}

/** 保存密码修改 */
const savePassword = async () => {
  // 表单校验
  const valid = await pwdFormRef.value.validate()
  if (!valid) return

  try {
    const res = await axios.post(`${BASE_URL}/user/updatePassword`, {
      userId: userInfo.value.userId,
      oldPassword: pwdForm.oldPwd,
      newPassword: pwdForm.newPwd
    })

    if (res.data.code === 200) {
      pwdDialogVisible.value = false
      ElMessage.success('密码修改成功，请重新登录')
      handleLogout()
    } else {
      ElMessage.error(res.data.msg || '密码修改失败（原密码错误）')
    }
  } catch (err) {
    console.error('密码修改失败：', err)
    ElMessage.error('密码修改失败，系统异常')
  }
}

/** 打开切换账号弹窗 */
const openSwitchAccount = () => {
  switchAccountDialogVisible.value = true
}

/** 切换到指定账号 */
const switchToAccount = (account) => {
  localStorage.setItem('userData', JSON.stringify(account))
  userInfo.value = {
    isLogin: true,
    userId: account.userId,
    username: account.username,
    email: account.email,
    avatarUrl: account.avatarUrl
      ? `${BASE_URL}/avatar/${encodeURIComponent(account.avatarUrl)}`
      : defaultAvatar
  }
  switchAccountDialogVisible.value = false
  ElMessage.success(`已切换到账号：${account.username}`)
  window.location.reload()
}

/** 打开添加账号登录弹窗 */
const handleAddAccount = () => {
  switchAccountDialogVisible.value = false
  loginDialogVisible.value = true
}

/** 登录（添加账号） */
const handleLogin = async () => {
  // 表单校验
  const valid = await loginFormRef.value.validate()
  if (!valid) return

  try {
    const res = await axios.post(`${BASE_URL}/user/login`, {
      email: loginForm.email,
      password: loginForm.password
    })

    if (res.data.code === 200) {
      const newAccount = res.data.data
      // 处理新账号的头像URL
      newAccount.avatarUrl = newAccount.avatarUrl
        ? newAccount.avatarUrl
        : ''

      // 保存到账号列表
      const accountList = JSON.parse(localStorage.getItem('accountList') || '[]')
      if (!accountList.some(item => item.userId === newAccount.userId)) {
        accountList.push(newAccount)
        localStorage.setItem('accountList', JSON.stringify(accountList))
      }
      // 设置为当前登录账号
      localStorage.setItem('userData', JSON.stringify(newAccount))
      loginDialogVisible.value = false
      ElMessage.success('登录成功')
      router.push('/my')
      initUserInfo()
    } else {
      ElMessage.error(res.data.msg || '登录失败，邮箱或密码错误')
    }
  } catch (err) {
    console.error('登录失败：', err)
    ElMessage.error('登录失败，网络异常')
  }
}

/** 退出登录 */
const handleLogout = () => {
  localStorage.removeItem('userData')
  userInfo.value = {
    isLogin: false,
    userId: '',
    username: '',
    email: '',
    avatarUrl: ''
  }
  ElMessage.success('已退出登录')
  router.push('/my').then(() => {
    window.location.reload()
  })
}
</script>

<style scoped>
/* 整体容器 */
.user-setting-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f9f9f9;
  min-height: 100vh;
}

/* 顶部导航 */
.setting-header {
  display: flex;
  align-items: center;
  padding: 10px 0;
  margin-bottom: 30px;
  border-bottom: 1px solid #eee;
}
.back-btn {
  font-size: 20px;
  color: #666;
  padding: 4px 8px;
}
.back-btn:hover {
  color: #2196f3;
  background-color: #f5f8ff;
}
.setting-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

/* 内容区 */
.setting-content {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

/* 设置项通用样式 */
.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}
.setting-item:hover {
  background-color: #fafafa;
}
.setting-item:last-child {
  border-bottom: none;
}
.item-label {
  font-size: 16px;
  color: #333;
}
.arrow-icon {
  font-size: 14px;
  color: #999;
}

/* 头像项 */
.avatar-item {
  align-items: flex-start;
}
.avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 10px;
  border: 2px solid #eee;
  cursor: pointer;
  transition: border-color 0.2s;
}
.user-avatar:hover {
  border-color: #2196f3;
}
.avatar-upload {
  display: none;
}
.upload-text {
  font-size: 14px;
  color: #2196f3;
  cursor: pointer;
  transition: color 0.2s;
}
.upload-text:hover {
  color: #1976d2;
  text-decoration: underline;
}

/* 昵称项 */
.nickname-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.nickname-text {
  font-size: 15px;
  color: #666;
}
.edit-btn {
  color: #2196f3;
  padding: 0;
  transition: color 0.2s;
}
.edit-btn:hover {
  color: #1976d2;
}

/* 退出项 */
.logout-item .logout-text {
  color: #f56c6c;
}
.logout-item:hover {
  background-color: #fef0f0;
}

/* 切换账号弹窗样式 */
.current-account {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 15px;
}
.account-list {
  margin-bottom: 20px;
}
.account-item {
  display: flex;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s;
}
.account-item:hover {
  background-color: #f5f5f5;
}
.account-avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 15px;
}
.account-info {
  flex: 1;
}
.account-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 5px;
}
.account-email {
  font-size: 14px;
  color: #999;
}
.current-tag {
  font-size: 12px;
  color: #2196f3;
  background-color: #e3f2fd;
  padding: 3px 8px;
  border-radius: 12px;
}
.add-account {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 15px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  gap: 8px;
  transition: all 0.2s;
}
.add-account:hover {
  background-color: #f9f9f9;
  border-color: #2196f3;
  color: #2196f3;
}
</style>
