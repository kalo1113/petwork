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
          <el-button
            type="text"
            class="edit-btn"
            @click.stop="openNicknameEdit"
          >
            编辑
          </el-button>
        </div>
      </div>

      <!-- 3. 收货地址（新增） -->
      <div class="setting-item address-item" @click="openAddressDialog">
        <div class="item-label">收货地址</div>
        <div class="address-wrap">
          <span class="address-text">{{ defaultAddress?.receiverName || '设置地址' }}</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 4. 修改密码 -->
      <div class="setting-item pwd-item" @click="openPwdEdit">
        <div class="item-label">修改密码</div>
      </div>

      <!-- 5. 切换账号 -->
      <div class="setting-item switch-account-item" @click="openSwitchAccount">
        <div class="item-label">切换账号</div>
      </div>

      <!-- 6. 退出登录 -->
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

    <!-- 收货地址弹窗（新增） -->
    <el-dialog
      v-model="addressDialogVisible"
      title="收货地址"
      width="50%"
      :close-on-click-modal="false"
    >
      <!-- 顶部操作栏 -->
      <div class="address-header">
        <el-button
          type="text"
          class="manage-btn"
          @click="toggleManageMode"
        >
          {{ isManageMode ? '退出管理' : '管理' }}
        </el-button>
        <el-button
          type="primary"
          size="small"
          class="add-address-btn"
          @click="openAddAddressDialog"
        >
          新增地址
        </el-button>
      </div>

      <!-- 地址列表 -->
      <div class="address-list" v-if="addressList.length">
        <div
          class="address-item"
          v-for="(item) in addressList"
          :key="item.id"
        >
          <!-- 管理模式：选择框 -->
          <el-checkbox
            v-if="isManageMode"
            v-model="selectedAddressIds"
            class="address-checkbox"
            :label="item.id"
          >            <!-- 空内容：彻底隐藏label文字 -->
            <span style="display: none;"></span></el-checkbox>

          <!-- 地址内容 -->
          <div class="address-content">
            <div class="address-top">
              <span class="address-name">{{ item.receiverName }}</span>
              <span class="address-phone">{{ item.receiverPhone }}</span>
              <span class="address-tag" v-if="item.isDefault === 1">默认</span>
              <span class="address-tag">{{ item.tag || '家' }}</span>
            </div>
            <div class="address-detail">
              {{ item.receiverProvince }}{{ item.receiverCity }}{{ item.receiverDistrict }}{{ item.receiverDetailAddress }}
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="address-actions">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="openEditAddressDialog(item)"
            ></el-button>
            <!-- 管理模式：置顶/删除/复制 -->
            <div v-if="isManageMode" class="manage-actions">
              <el-button
                type="text"
                size="small"
                @click="setDefaultAddress(item.id)"
                v-if="item.isDefault !== 1"
              >
                设为默认地址
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="deleteAddress(item.id)"
                class="delete-btn"
              >
                删除
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="copyAddress(item)"
              >
                复制
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 无地址提示 -->
      <div class="no-address" v-else>
        <span>暂无地址</span>
        <el-button
          type="primary"
          size="small"
          @click="openAddAddressDialog"
        >
          添加地址
        </el-button>
      </div>

      <!-- 管理模式底部 -->
      <div class="manage-footer" v-if="isManageMode && addressList.length">
        <el-checkbox
          v-model="selectAll"
          @change="toggleSelectAll"
        >
          全选
        </el-checkbox>
        <el-button
          type="danger"
          size="small"
          @click="deleteSelectedAddresses"
        >
          删除
        </el-button>
      </div>

      <template #footer>
        <el-button @click="addressDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑地址弹窗（核心修复：Cascader配置） -->
    <el-dialog
      v-model="addAddressDialogVisible"
      :title="isEditAddress ? '编辑地址' : '新增地址'"
      width="40%"
      :close-on-click-modal="false"
    >
      <el-form :model="addressForm" label-width="80px" :rules="addressRules" ref="addressFormRef">
        <!-- 省市区选择器：修复显示+校验逻辑 -->
        <el-form-item label="所在地区" prop="area">
          <el-cascader
            v-model="addressForm.area"
            :options="areaOptions"
            placeholder="请选择省/市/区"
            @change="handleAreaChange"
            :props="{ 
              expandTrigger: 'hover', 
              label: 'value',   // 显示名称（如北京市）
              value: 'value',   // 绑定名称作为值
              children: 'children' 
            }"
            style="width: 100%;"
          ></el-cascader>
        </el-form-item>
        <el-form-item label="详细地址" prop="receiverDetailAddress">
          <el-input
            v-model="addressForm.receiverDetailAddress"
            placeholder="请输入街道/门牌号"
          ></el-input>
        </el-form-item>
        <el-form-item label="收货人" prop="receiverName">
          <el-input
            v-model="addressForm.receiverName"
            placeholder="请输入收货人姓名"
          ></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="receiverPhone">
          <el-input
            v-model="addressForm.receiverPhone"
            placeholder="请输入手机号"
            maxlength="11"
          ></el-input>
        </el-form-item>
        <el-form-item label="地址标签">
          <el-radio-group v-model="addressForm.tag">
            <el-radio label="家">家</el-radio>
            <el-radio label="公司">公司</el-radio>
            <el-radio label="学校">学校</el-radio>
            <el-radio label="父母">父母</el-radio>
            <el-radio label="朋友">朋友</el-radio>
            <el-radio label="自定义">
              <el-input
                v-model="addressForm.tag"
                placeholder="自定义标签"
                maxlength="4"
                style="width: 80px; margin-left: 5px;"
              ></el-input>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addAddressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress" style="color: white;">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
// 修复：统一导入Element Plus组件（避免漏注册）
import { 
  ElMessage, ElLoading, ElForm, ElDialog, ElButton, 
  ElFormItem, ElInput, ElCheckbox, ElRadio, ElRadioGroup, 
  ElCascader, ElIcon 
} from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import defaultAvatar from '@/assets/images/我的图标/默认头像.svg'
// 导入地址接口
import { getReceiverAddressList, addReceiverAddress, updateReceiverAddress, deleteReceiverAddress } from '@/api/user/index.js'
// 导入全局配置
import { BASE_URL } from '@/config/index.js'
// 正确导入省市区数据
import { areaOptions } from '@/utils/address.js'

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
const addressDialogVisible = ref(false) 
const addAddressDialogVisible = ref(false) 

// 地址相关状态
const addressList = ref([]) 
const defaultAddress = ref(null) 
const defaultAddressId = ref('') 
const isManageMode = ref(false) 
const selectedAddressIds = ref([]) 
const selectAll = ref(false) 
const isEditAddress = ref(false) 
const addressForm = reactive({ 
  id: '',
  receiverName: '',
  receiverPhone: '',
  receiverProvince: '',
  receiverCity: '',
  receiverDistrict: '',
  receiverDetailAddress: '',
  tag: '家',
  isDefault: 0,
  area: [] // 绑定Cascader的省市区名称数组
})

// 表单Ref（用于校验）
const nicknameFormRef = ref(null)
const pwdFormRef = ref(null)
const loginFormRef = ref(null)
const addressFormRef = ref(null)

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

// ========== 修复：简化表单校验规则（避免复杂逻辑导致报错） ==========
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
// 修复：省市区校验直接检查area数组长度
const addressRules = ref({
  area: [
    { 
      required: true,
      type: 'array',
      min: 3,
      message: '请选择完整的省市区',
      trigger: 'change'
    }
  ],
  receiverDetailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
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
  try {
    const userData = localStorage.getItem('userData')
    if (userData) {
      const parsed = JSON.parse(userData)
      userInfo.value = {
        isLogin: true,
        userId: parsed.userId || '',
        username: parsed.username || '',
        email: parsed.email || '',
        avatarUrl: parsed.avatarUrl
          ? (parsed.avatarUrl.startsWith('http') ? parsed.avatarUrl : `${BASE_URL}/avatar/${encodeURIComponent(parsed.avatarUrl)}`)
          : defaultAvatar
      }
      nicknameForm.username = userInfo.value.username
      // 初始化地址
      loadAddressList()
    }
  } catch (err) {
    console.error('初始化用户信息失败：', err)
    ElMessage.error('用户信息加载异常')
  }
}

/** 初始化其他账号列表 */
const initOtherAccounts = () => {
  try {
    const accountList = localStorage.getItem('accountList')
    if (accountList) {
      otherAccounts.value = JSON.parse(accountList).filter(item => item.userId !== userInfo.value.userId)
    }
  } catch (err) {
    console.error('初始化账号列表失败：', err)
  }
}

/** 加载地址列表（修复：增加错误捕获） */
const loadAddressList = async () => {
  if (!userInfo.value.userId) return
  try {
    const res = await getReceiverAddressList(userInfo.value.userId)
    addressList.value = res.data || [] 
    defaultAddress.value = res.data?.find(item => item.is_default === 1) || null
    defaultAddressId.value = defaultAddress.value?.id || ''
  } catch (err) {
    console.warn('地址加载失败：', err)
    addressList.value = [] 
    ElMessage.warning('暂无收货地址')
  }
}

// ========== 地址相关方法 ==========
/** 打开地址弹窗 */
const openAddressDialog = () => {
  loadAddressList()
  addressDialogVisible.value = true
}

/** 切换管理模式 */
const toggleManageMode = () => {
  isManageMode.value = !isManageMode.value
  selectedAddressIds.value = []
  selectAll.value = false
}

/** 全选/取消全选 */
const toggleSelectAll = () => {
  selectedAddressIds.value = selectAll.value
    ? addressList.value.map(item => item.id)
    : []
}

/** 设置默认地址 */
const setDefaultAddress = async (id) => {
  try {
    await updateReceiverAddress({
      id,
      userId: userInfo.value.userId,
      isDefault: 1
    })
    loadAddressList()
    ElMessage.success('已设置为默认地址')
  } catch (err) {
    console.error('设置默认地址失败：', err)
    ElMessage.error('设置默认地址失败')
  }
}

/** 打开新增地址弹窗 */
const openAddAddressDialog = () => {
  resetAddressForm()
  isEditAddress.value = false
  addAddressDialogVisible.value = true
}

/** 打开编辑地址弹窗 */
const openEditAddressDialog = (item) => {
  resetAddressForm()
  isEditAddress.value = true
  // 填充表单数据
  addressForm.id = item.id
  addressForm.receiverName = item.receiverName
  addressForm.receiverPhone = item.receiverPhone
  addressForm.receiverProvince = item.receiverProvince
  addressForm.receiverCity = item.receiverCity
  addressForm.receiverDistrict = item.receiverDistrict
  addressForm.receiverDetailAddress = item.receiverDetailAddress
  addressForm.tag = item.tag || '家'
  addressForm.isDefault = item.isDefault || 0
  // 回显Cascader（修复：直接绑定名称数组）
  addressForm.area = [item.receiverProvince, item.receiverCity, item.receiverDistrict]
  addAddressDialogVisible.value = true
}

/** 重置地址表单 */
const resetAddressForm = () => {
  addressForm.id = ''
  addressForm.receiverName = ''
  addressForm.receiverPhone = ''
  addressForm.receiverProvince = ''
  addressForm.receiverCity = ''
  addressForm.receiverDistrict = ''
  addressForm.receiverDetailAddress = ''
  addressForm.tag = '家'
  addressForm.isDefault = 0
  addressForm.area = []
  if (addressFormRef.value) {
    addressFormRef.value.resetFields()
  }
}

/** Cascader选择事件（简化逻辑） */
const handleAreaChange = (val) => {
  if (val && val.length === 3) {
    addressForm.receiverProvince = val[0]
    addressForm.receiverCity = val[1]
    addressForm.receiverDistrict = val[2]
  }
}

/** 保存地址（修复：增加校验成功后的逻辑） */
const saveAddress = async () => {
  try {
    const valid = await addressFormRef.value.validate()
    if (!valid) return

    const addressData = {
      userId: userInfo.value.userId,
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      receiverProvince: addressForm.receiverProvince,
      receiverCity: addressForm.receiverCity,
      receiverDistrict: addressForm.receiverDistrict,
      receiverDetailAddress: addressForm.receiverDetailAddress,
      tag: addressForm.tag,
      isDefault: addressForm.isDefault ? 1 : 0
    }

    if (isEditAddress.value) {
      addressData.id = addressForm.id
      await updateReceiverAddress(addressData)
      ElMessage.success('地址编辑成功')
    } else {
      await addReceiverAddress(addressData)
      ElMessage.success('地址添加成功')
    }

    addAddressDialogVisible.value = false
    loadAddressList()
  } catch (err) {
    console.error('保存地址失败：', err)
    ElMessage.error(isEditAddress.value ? '地址编辑失败' : '地址添加失败')
  }
}

/** 删除单个地址 */
const deleteAddress = async (id) => {
  try {
    await deleteReceiverAddress(id, userInfo.value.userId)
    loadAddressList()
    ElMessage.success('地址删除成功')
  } catch (err) {
    console.error('删除地址失败：', err)
    ElMessage.error('地址删除失败')
  }
}

/** 删除选中地址 */
const deleteSelectedAddresses = async () => {
  if (selectedAddressIds.value.length === 0) {
    return ElMessage.warning('请选择要删除的地址')
  }

  try {
    for (const id of selectedAddressIds.value) {
      await deleteReceiverAddress(id, userInfo.value.userId)
    }
    loadAddressList()
    selectedAddressIds.value = []
    selectAll.value = false
    ElMessage.success('选中地址已删除')
  } catch (err) {
    console.error('删除选中地址失败：', err)
    ElMessage.error('删除地址失败')
  }
}

/** 复制地址 */
const copyAddress = (item) => {
  try {
    const addressText = `${item.receiverName} ${item.receiverPhone}\n${item.receiverProvince}${item.receiverCity}${item.receiverDistrict}${item.receiverDetailAddress}`
    navigator.clipboard.writeText(addressText).then(() => {
      ElMessage.success('地址已复制到剪贴板')
    })
  } catch (err) {
    console.error('复制地址失败：', err)
    ElMessage.error('复制地址失败')
  }
}

// ========== 其他事件处理 ==========
/** 返回上一页 */
const handleBack = () => {
  try {
    router.push('/my').catch(err => {
      console.error('跳转个人中心失败：', err)
      ElMessage.error('返回失败，请重试')
    })
  } catch (err) {
    console.error('返回失败：', err)
  }
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
      transformRequest: [(data) => data]
    })

    // 5. 处理响应
    loading.close()
    if (res.data.code === 200) {
      userInfo.value.avatarUrl = res.data.data
      // 更新本地缓存
      localStorage.setItem('userData', JSON.stringify({
        ...JSON.parse(localStorage.getItem('userData')),
        avatarUrl: res.data.data
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
  nicknameForm.username = userInfo.value.username || '默认昵称'
  setTimeout(() => {
    nicknameDialogVisible.value = true
  }, 0)
}

/** 保存昵称修改 */
const saveNickname = async () => {
  try {
    const valid = await nicknameFormRef.value.validate()
    if (!valid) return

    const res = await axios.post(`${BASE_URL}/user/updateNickname`, {
      userId: userInfo.value.userId,
      username: nicknameForm.username
    })

    if (res.data.code === 200) {
      userInfo.value.username = nicknameForm.username
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
  try {
    const valid = await pwdFormRef.value.validate()
    if (!valid) return

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
  try {
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
  } catch (err) {
    console.error('切换账号失败：', err)
    ElMessage.error('切换账号失败')
  }
}

/** 打开添加账号登录弹窗 */
const handleAddAccount = () => {
  switchAccountDialogVisible.value = false
  loginDialogVisible.value = true
}

/** 登录（添加账号） */
const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    const res = await axios.post(`${BASE_URL}/user/login`, {
      email: loginForm.email,
      password: loginForm.password
    })

    if (res.data.code === 200) {
      const newAccount = res.data.data
      newAccount.avatarUrl = newAccount.avatarUrl || ''

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
  try {
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
  } catch (err) {
    console.error('退出登录失败：', err)
  }
}

// 监听选中地址变化
watch(selectedAddressIds, (val) => {
  selectAll.value = val.length === addressList.value.length
})
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

/* 收货地址项（新增） */
.address-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}
.address-text {
  font-size: 15px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
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

/* 收货地址弹窗样式（新增） */
.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.manage-btn {
  color: #2196f3;
}
.add-address-btn {
  padding: 4px 12px;
  background: #eee;
  border-color: #999;
  color: #333; 
  transition: color 0.2s ease;
  border: 1px solid #999;
  cursor: pointer;
}
.add-address-btn:hover {
  color: #666;
  background: #f5f5f5;
}
.add-address-btn:active {
  color: #888;
  background: #e0e0e0;
}
.address-list {
  max-height: 400px;
  overflow-y: auto;
}
.address-item {
  display: flex;
  align-items: flex-start;
  border-bottom: 1px solid #f5f5f5;
  position: relative;
}
.address-checkbox, .address-radio {
  margin-top: 5px;
  margin-right: 15px;
}
.address-content {
  flex: 1;
}
.address-top {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 5px;
}
.address-name {
  font-size: 15px;
  font-weight: 500;
}
.address-phone {
  font-size: 14px;
  color: #666;
}
.address-tag {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  background-color: #f5f5f5;
  color: #666;
}
.address-tag:has(+ .address-tag) {
  margin-right: 5px;
}
.address-tag:first-of-type:not(:only-of-type) {
  background-color: #e3f2fd;
  color: #2196f3;
}
.address-detail {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}
.address-actions {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.manage-actions {
  display: flex;
  gap: 8px;
  margin-top: 5px;
}
.delete-btn {
  color: #f56c6c;
}
.no-address {
  text-align: center;
  padding: 40px 0;
  color: #999;
}
.no-address button {
  margin-top: 10px;
}
.manage-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
}

/* 省市区选择器样式适配 */
:deep(.el-cascader) {
  width: 100%;
}
:deep(.el-cascader__input) {
  height: 40px;
  line-height: 40px;
}
</style>