<template>
  <div class="merchant-login-container">
    <div class="login-card">
      <h2>{{ isLogin ? '商家登录' : '商家注册' }}</h2>

      <!-- 登录表单（手机号登录） -->
      <el-form
        v-if="isLogin"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            手机号登录
          </el-button>
        </el-form-item>
        <el-form-item class="toggle-text">
          没有账号？
          <el-button type="text" @click="isLogin = false">立即注册</el-button>
        </el-form-item>
      </el-form>

      <!-- 注册表单（自动生成账号） -->
      <el-form
        v-else
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="login-form"
      >
        <el-form-item label="商家名称" prop="merchantName">
          <el-input v-model="registerForm.merchantName" placeholder="请输入商家名称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" @blur="generateUsername" />
        </el-form-item>
        <!-- 自动生成的账号（只读展示） -->
        <el-form-item label="登录账号（自动生成）" prop="username">
          <el-input v-model="registerForm.username" placeholder="输入手机号自动生成" readonly />
        </el-form-item>
        <el-form-item label="设置密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请设置6位以上密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="商家地址" prop="address">
          <el-input
            v-model="registerForm.address"
            type="textarea"
            placeholder="请输入商家详细地址"
            :rows="2"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleRegister"
          >
            提交注册
          </el-button>
        </el-form-item>
        <el-form-item class="toggle-text">
          已有账号？
          <el-button type="text" @click="isLogin = true">去登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { merchantLogin, merchantRegister } from '@/api/merchant/index.js'

const router = useRouter()
const loginFormRef = ref(null)
const registerFormRef = ref(null)
const loading = ref(false)
const isLogin = ref(true) // true=登录 false=注册

// 登录表单（手机号+密码）
const loginForm = reactive({
  phone: '',
  password: ''
})
const loginRules = {
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

// 注册表单
const registerForm = reactive({
  merchantName: '',
  phone: '',
  username: '', // 自动生成
  password: '',
  address: ''
})
const registerRules = {
  merchantName: [{ required: true, message: '商家名称不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  username: [{ required: true, message: '账号自动生成，请先输入手机号', trigger: 'blur' }],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  address: [{ required: true, message: '商家地址不能为空', trigger: 'blur' }]
}

// 核心：自动生成商家账号（格式：MER + 手机号后6位 + 随机数）
const generateUsername = () => {
  const phone = registerForm.phone.trim()
  if (phone && /^1[3-9]\d{9}$/.test(phone)) {
    // 手机号后6位 + 2位随机数，避免重复
    const suffix = phone.slice(-6)
    const random = Math.floor(Math.random() * 90 + 10) // 10-99随机数
    registerForm.username = `MER${suffix}${random}`
  }
}

// 手机号登录逻辑
const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      // 调用登录接口（后端需支持手机号查询商家）
      const res = await merchantLogin({
        phone: loginForm.phone,
        password: loginForm.password
      })
      if (res.code === 200) {
        localStorage.setItem('merchantInfo', JSON.stringify(res.data))
        ElMessage.success('登录成功')
        router.push('/merchant')
      } else {
        ElMessage.error(res.msg || '登录失败')
      }
    } catch (err) {
      console.error('登录异常：', err)
      ElMessage.error('登录失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

// 注册逻辑（自动带生成的账号）
const handleRegister = async () => {
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await merchantRegister(registerForm)
      if (res.code === 200) {
        ElMessage.success('注册成功，请用手机号登录')
        isLogin.value = true // 自动切回登录页
        // 清空注册表单
        registerForm.merchantName = ''
        registerForm.phone = ''
        registerForm.username = ''
        registerForm.password = ''
        registerForm.address = ''
      } else {
        ElMessage.error(res.msg || '注册失败')
      }
    } catch (err) {
      console.error('注册异常：', err)
      ElMessage.error('注册失败，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.merchant-login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.login-card {
  width: 450px;
  padding: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}
.login-card h2 {
  text-align: center;
  margin-bottom: 26px;
  color: #333;
}
.login-form {
  width: 100%;
}
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}
.toggle-text {
  text-align: center;
  color: #666;
  margin-top: 10px;
}
.el-form-item__label {
  color: #666;
}
</style>