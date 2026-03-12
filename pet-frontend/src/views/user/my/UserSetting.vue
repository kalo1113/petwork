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
import { ref, reactive, watch, onMounted } from 'vue'
// 修复：统一导入Element Plus组件（避免漏注册）
import { 
  ElMessage, ElLoading, ElForm, ElDialog, ElButton, 
  ElFormItem, ElInput, ElCheckbox, ElRadio, ElRadioGroup, 
  ElCascader, ElIcon 
} from 'element-plus'
import { ArrowRight } from '@element-plus/icons-vue'
import axios from 'axios'
import defaultAvatar from '@/assets/images/我的图标/默认头像.svg'
// 导入地址接口
import { getReceiverAddressList, addReceiverAddress, updateReceiverAddress, deleteReceiverAddress } from '@/api/user/index.js'
// 导入全局配置
import { BASE_URL } from '@/config/index.js'
// 正确导入省市区数据
import { areaOptions } from '@/utils/address.js'
import { useRouter, useRoute } from 'vue-router'
// 初始化路由和路由参数
const router = useRouter()
const route = useRoute() // 新增：获取当前路由参数

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

// ========== 新增：通用userId类型转换与校验方法 ==========
/** 
 * 转换userId为数字类型，返回null表示无效
 * @param {string|number} userId - 待转换的用户ID
 * @returns {number|null} 有效数字类型userId或null
 */
const convertToValidUserId = (userId) => {
  if (!userId) return null;
  // 去除首尾空格并转为字符串
  const userIdStr = String(userId).trim();
  // 校验是否为纯数字
  if (!/^\d+$/.test(userIdStr)) {
    ElMessage.error('用户ID格式错误：必须为数字');
    return null;
  }
  // 转换为数字
  const userIdNum = Number(userIdStr);
  // 校验有效性
  if (userIdNum <= 0) {
    ElMessage.error('用户ID必须大于0');
    return null;
  }
  return userIdNum;
};

// ========== 新增：根据userId获取用户详情接口（修复类型转换） ==========
/** 根据userId获取用户详情 */
const getUserDetail = async (userId) => {
  // 1. 先转换并校验userId
  const validUserId = convertToValidUserId(userId);
  if (!validUserId) return;

  try {
    // 修复：调用后端标准的/user/{userId}接口（而非自定义的/detail）
    const res = await axios.get(`${BASE_URL}/user/${validUserId}`);
    if (res.data.code === 200) {
      const userData = res.data.data;
      // 复用原有头像路径处理逻辑（后端已拼好URL，简化处理）
      let avatarUrl = userData.avatarUrl || defaultAvatar;
      if (avatarUrl && !avatarUrl.startsWith('http')) {
        avatarUrl = `${BASE_URL}/user-img/${encodeURIComponent(avatarUrl)}`;
      }
      
      // 更新用户信息（确保userId为数字类型）
      userInfo.value = {
        isLogin: true,
        userId: validUserId,
        username: userData.username || '',
        email: userData.email || '',
        avatarUrl: avatarUrl
      };
      // 同步到昵称表单
      nicknameForm.username = userInfo.value.username;
      // 加载该用户的地址列表
      loadAddressList();
      // 可选：更新本地缓存（存储数字类型userId）
      localStorage.setItem('userData', JSON.stringify({
        userId: validUserId,
        username: userData.username,
        email: userData.email,
        avatarUrl: userData.avatarUrl || ''
      }));
    } else {
      ElMessage.error(res.data.msg || '获取用户信息失败');
    }
  } catch (err) {
    console.error('获取用户详情失败：', err);
    // 更精准的错误提示
    if (err.response?.status === 404) {
      ElMessage.error('用户不存在');
    } else {
      ElMessage.error('用户信息加载失败，请检查接口是否可用');
    }
  }
};

// ========== 初始化方法（重构：强化类型校验） ==========
/** 初始化当前登录用户信息（重构） */
const initUserInfo = () => {
  try {
    // 第一步：优先获取URL中的userId参数并转换
    const urlUserId = route.query.userId || '';
    const validUrlUserId = convertToValidUserId(urlUserId);
    if (validUrlUserId) {
      getUserDetail(validUrlUserId);
      return;
    }

    // 第二步：URL中无userId，读取本地缓存
    const userData = localStorage.getItem('userData');
    if (userData) {
      const parsed = JSON.parse(userData);
      // 转换缓存中的userId为数字
      const validCacheUserId = convertToValidUserId(parsed.userId);
      if (!validCacheUserId) {
        localStorage.removeItem('userData'); // 缓存无效，清空
        return;
      }

      // 核心修复：智能处理头像路径，去重+兼容
      let avatarUrl = parsed.avatarUrl || '';
      if (avatarUrl) {
        if (avatarUrl.startsWith('http')) {
          // 完整URL：先去重重复前缀，再替换旧前缀
          avatarUrl = avatarUrl.replace(/\/user-img\/user-img\//, '/user-img/');
          avatarUrl = avatarUrl.replace('/avatar/', '/user-img/');
        } else if (avatarUrl.startsWith('/user-img/')) {
          // 带前缀的路径：直接拼接域名
          avatarUrl = `${BASE_URL}${avatarUrl}`;
        } else {
          // 纯文件名：拼接前缀
          avatarUrl = `${BASE_URL}/user-img/${encodeURIComponent(avatarUrl)}`;
        }
      } else {
        avatarUrl = defaultAvatar;
      }

      // 更新用户信息（确保userId为数字）
      userInfo.value = {
        isLogin: true,
        userId: validCacheUserId,
        username: parsed.username || '',
        email: parsed.email || '',
        avatarUrl: avatarUrl // 使用修复后的路径
      };
      nicknameForm.username = userInfo.value.username;
      // 初始化地址
      loadAddressList();
    }
  } catch (err) {
    console.error('初始化用户信息失败：', err);
    ElMessage.error('用户信息加载异常');
  }
};

/** 加载地址列表（修复：兼容URL参数中的userId，强制数字类型） */
const loadAddressList = async () => {
  // 优先使用用户信息中的数字userId，兜底用URL参数并转换
  const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
  if (!userId) return;
  
  try {
    const res = await getReceiverAddressList(userId);
    addressList.value = res.data || [];
    defaultAddress.value = res.data?.find(item => item.is_default === 1) || null;
    defaultAddressId.value = defaultAddress.value?.id || '';
  } catch (err) {
    console.warn('地址加载失败：', err);
    addressList.value = [];
    ElMessage.warning('暂无收货地址');
  }
};

// ========== 地址相关方法（全量修复userId类型） ==========
/** 打开地址弹窗 */
const openAddressDialog = () => {
  loadAddressList();
  addressDialogVisible.value = true;
};

/** 切换管理模式 */
const toggleManageMode = () => {
  isManageMode.value = !isManageMode.value;
  selectedAddressIds.value = [];
  selectAll.value = false;
};

/** 全选/取消全选 */
const toggleSelectAll = () => {
  selectedAddressIds.value = selectAll.value
    ? addressList.value.map(item => item.id)
    : [];
};

/** 设置默认地址（修复userId类型） */
const setDefaultAddress = async (id) => {
  const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
  if (!userId) return;
  
  try {
    await updateReceiverAddress({
      id,
      userId: userId,
      isDefault: 1
    });
    loadAddressList();
    ElMessage.success('已设置为默认地址');
  } catch (err) {
    console.error('设置默认地址失败：', err);
    ElMessage.error('设置默认地址失败');
  }
};

/** 打开新增地址弹窗 */
const openAddAddressDialog = () => {
  resetAddressForm();
  isEditAddress.value = false;
  addAddressDialogVisible.value = true;
};

/** 打开编辑地址弹窗 */
const openEditAddressDialog = (item) => {
  resetAddressForm();
  isEditAddress.value = true;
  // 填充表单数据
  addressForm.id = item.id;
  addressForm.receiverName = item.receiverName;
  addressForm.receiverPhone = item.receiverPhone;
  addressForm.receiverProvince = item.receiverProvince;
  addressForm.receiverCity = item.receiverCity;
  addressForm.receiverDistrict = item.receiverDistrict;
  addressForm.receiverDetailAddress = item.receiverDetailAddress;
  addressForm.tag = item.tag || '家';
  addressForm.isDefault = item.isDefault || 0;
  // 回显Cascader（修复：直接绑定名称数组）
  addressForm.area = [item.receiverProvince, item.receiverCity, item.receiverDistrict];
  addAddressDialogVisible.value = true;
};

/** 重置地址表单 */
const resetAddressForm = () => {
  addressForm.id = '';
  addressForm.receiverName = '';
  addressForm.receiverPhone = '';
  addressForm.receiverProvince = '';
  addressForm.receiverCity = '';
  addressForm.receiverDistrict = '';
  addressForm.receiverDetailAddress = '';
  addressForm.tag = '家';
  addressForm.isDefault = 0;
  addressForm.area = [];
  if (addressFormRef.value) {
    addressFormRef.value.resetFields();
  }
};

/** Cascader选择事件（简化逻辑） */
const handleAreaChange = (val) => {
  if (val && val.length === 3) {
    addressForm.receiverProvince = val[0];
    addressForm.receiverCity = val[1];
    addressForm.receiverDistrict = val[2];
  }
};

/** 保存地址（修复：强制userId为数字类型） */
const saveAddress = async () => {
  try {
    const valid = await addressFormRef.value.validate();
    if (!valid) return;

    // 转换并校验userId
    const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
    if (!userId) return;

    const addressData = {
      userId: userId,
      receiverName: addressForm.receiverName,
      receiverPhone: addressForm.receiverPhone,
      receiverProvince: addressForm.receiverProvince,
      receiverCity: addressForm.receiverCity,
      receiverDistrict: addressForm.receiverDistrict,
      receiverDetailAddress: addressForm.receiverDetailAddress,
      tag: addressForm.tag,
      isDefault: addressForm.isDefault ? 1 : 0
    };

    if (isEditAddress.value) {
      addressData.id = addressForm.id;
      await updateReceiverAddress(addressData);
      ElMessage.success('地址编辑成功');
    } else {
      await addReceiverAddress(addressData);
      ElMessage.success('地址添加成功');
    }

    addAddressDialogVisible.value = false;
    loadAddressList();
  } catch (err) {
    console.error('保存地址失败：', err);
    ElMessage.error(isEditAddress.value ? '地址编辑失败' : '地址添加失败');
  }
};

/** 删除单个地址（修复：强制userId为数字类型） */
const deleteAddress = async (id) => {
  const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
  if (!userId) return;
  
  try {
    await deleteReceiverAddress(id, userId);
    loadAddressList();
    ElMessage.success('地址删除成功');
  } catch (err) {
    console.error('删除地址失败：', err);
    ElMessage.error('地址删除失败');
  }
};

/** 删除选中地址（修复：强制userId为数字类型） */
const deleteSelectedAddresses = async () => {
  if (selectedAddressIds.value.length === 0) {
    return ElMessage.warning('请选择要删除的地址');
  }

  // 转换并校验userId
  const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
  if (!userId) return;

  try {
    for (const id of selectedAddressIds.value) {
      await deleteReceiverAddress(id, userId);
    }
    loadAddressList();
    selectedAddressIds.value = [];
    selectAll.value = false;
    ElMessage.success('选中地址已删除');
  } catch (err) {
    console.error('删除选中地址失败：', err);
    ElMessage.error('删除地址失败');
  }
};

/** 复制地址 */
const copyAddress = (item) => {
  try {
    const addressText = `${item.receiverName} ${item.receiverPhone}\n${item.receiverProvince}${item.receiverCity}${item.receiverDistrict}${item.receiverDetailAddress}`;
    navigator.clipboard.writeText(addressText).then(() => {
      ElMessage.success('地址已复制到剪贴板');
    });
  } catch (err) {
    console.error('复制地址失败：', err);
    ElMessage.error('复制地址失败');
  }
};

// ========== 其他事件处理（全量修复userId类型） ==========
/** 返回上一页 */
const handleBack = () => {
  try {
    router.push('/my').catch(err => {
      console.error('跳转个人中心失败：', err);
      ElMessage.error('返回失败，请重试');
    });
  } catch (err) {
    console.error('返回失败：', err);
  }
};

/** 头像上传处理（完整修复：增加userId类型转换） */
const handleAvatarUpload = async (e) => {
  const file = e.target.files[0];
  if (!file) return;

  // 1. 文件校验
  if (file.size > 10 * 1024 * 1024) {
    return ElMessage.error('头像文件大小不能超过10MB');
  }
  const acceptTypes = ['image/jpeg', 'image/png', 'image/gif'];
  if (!acceptTypes.includes(file.type)) {
    return ElMessage.error('仅支持jpg/png/gif格式的头像');
  }

  // 2. 转换并校验userId
  const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
  if (!userId) return;

  // 3. 显示加载状态
  const loading = ElLoading.service({
    lock: true,
    text: '头像上传中...',
    background: 'rgba(0, 0, 0, 0.7)'
  });

  try {
    // 4. 构建表单数据（传递数字类型userId）
    const formData = new FormData();
    formData.append('file', file);
    formData.append('userId', userId);

    // 5. 调用后端接口（增加超时、跨域配置）
    const res = await axios.post(`${BASE_URL}/user/uploadAvatar`, formData, {
      headers: { 
        'Content-Type': 'multipart/form-data'
        // 跨域配置移到后端CORS，前端无需设置
      },
      transformRequest: [(data) => data],
      timeout: 30000, // 超时时间30秒
      withCredentials: true // 携带cookie（如果需要）
    });

    // 6. 处理响应（兼容不同的返回格式）
    loading.close();
    if (res.data && res.data.code === 200) {
      const fileName = res.data.data || res.data.fileName || res.data.result;
      if (!fileName) {
        return ElMessage.error('上传成功但未获取到头像文件名');
      }
      
      // 核心修复：智能拼接路径，避免重复前缀
      let finalAvatarUrl = '';
      if (fileName.startsWith('/user-img/')) {
        // 后端返回带前缀，直接拼接域名
        finalAvatarUrl = `${BASE_URL}${fileName}`;
      } else {
        // 纯文件名，手动拼接前缀
        finalAvatarUrl = `${BASE_URL}/user-img/${fileName}`;
      }
      userInfo.value.avatarUrl = finalAvatarUrl;
      
      // 核心修复：缓存只存纯文件名，移除前缀
      let cacheAvatarUrl = fileName;
      if (cacheAvatarUrl.startsWith('/user-img/')) {
        cacheAvatarUrl = cacheAvatarUrl.replace('/user-img/', '');
      }
      // 更新本地缓存（确保userId为数字）
      const currentUserData = JSON.parse(localStorage.getItem('userData') || '{}');
      localStorage.setItem('userData', JSON.stringify({
        ...currentUserData,
        avatarUrl: cacheAvatarUrl, // 仅存纯文件名
        userId: userId // 强制存储数字类型
      }));
      ElMessage.success('头像上传成功');
    } else {
      ElMessage.error(res.data?.msg || '头像上传失败（后端返回非200状态）');
    }
  } catch (err) {
    loading.close();
    // 详细打印错误信息，方便排查
    console.error('头像上传失败详情：', {
      url: `${BASE_URL}/user/uploadAvatar`,
      error: err.message,
      status: err.response?.status,
      data: err.response?.data
    });
    // 分场景提示错误
    if (err.message.includes('timeout')) {
      ElMessage.error('头像上传超时，请检查网络或重试');
    } else if (err.message.includes('404')) {
      ElMessage.error('上传接口不存在，请检查后端接口地址是否正确');
    } else if (err.message.includes('500')) {
      ElMessage.error('服务器内部错误，请联系管理员');
    } else {
      ElMessage.error('头像上传失败，请检查网络或联系管理员');
    }
  }
};

/** 打开昵称编辑弹窗 */
const openNicknameEdit = () => {
  nicknameForm.username = userInfo.value.username || '默认昵称';
  setTimeout(() => {
    nicknameDialogVisible.value = true;
  }, 0);
};

/** 保存昵称修改（修复：强制userId为数字类型） */
const saveNickname = async () => {
  try {
    const valid = await nicknameFormRef.value.validate();
    if (!valid) return;

    // 转换并校验userId
    const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
    if (!userId) return;

    const res = await axios.post(`${BASE_URL}/user/updateNickname`, {
      userId: userId, // 传递数字类型
      username: nicknameForm.username
    });

    if (res.data.code === 200) {
      userInfo.value.username = nicknameForm.username;
      const currentUserData = JSON.parse(localStorage.getItem('userData') || '{}');
      localStorage.setItem('userData', JSON.stringify({
        ...currentUserData,
        username: nicknameForm.username,
        userId: userId // 确保缓存中是数字
      }));
      nicknameDialogVisible.value = false;
      ElMessage.success('昵称修改成功');
    } else {
      ElMessage.error(res.data.msg || '昵称修改失败');
    }
  } catch (err) {
    console.error('昵称修改失败：', err);
    ElMessage.error('昵称修改失败，请检查网络');
  }
};

/** 打开密码修改弹窗 */
const openPwdEdit = () => {
  pwdForm.oldPwd = '';
  pwdForm.newPwd = '';
  pwdForm.confirmPwd = '';
  pwdDialogVisible.value = true;
};

/** 保存密码修改（修复：强制userId为数字类型） */
const savePassword = async () => {
  try {
    const valid = await pwdFormRef.value.validate();
    if (!valid) return;

    // 转换并校验userId
    const userId = convertToValidUserId(userInfo.value.userId || route.query.userId);
    if (!userId) return;

    const res = await axios.post(`${BASE_URL}/user/updatePassword`, {
      userId: userId, // 传递数字类型
      oldPassword: pwdForm.oldPwd,
      newPassword: pwdForm.newPwd
    });

    if (res.data.code === 200) {
      pwdDialogVisible.value = false;
      ElMessage.success('密码修改成功，请重新登录');
      handleLogout();
    } else {
      ElMessage.error(res.data.msg || '密码修改失败（原密码错误）');
    }
  } catch (err) {
    console.error('密码修改失败：', err);
    ElMessage.error('密码修改失败，系统异常');
  }
};

/** 打开切换账号弹窗 */
const openSwitchAccount = () => {
  switchAccountDialogVisible.value = true;
};

/** 切换到指定账号（核心修复：强制userId为数字类型） */
const switchToAccount = (account) => {
  try {
    // 转换账号的userId为数字
    const validUserId = convertToValidUserId(account.userId);
    if (!validUserId) return;

    // 核心修复：处理账号头像路径
    let avatarUrl = account.avatarUrl || '';
    if (avatarUrl) {
      if (avatarUrl.startsWith('http')) {
        avatarUrl = avatarUrl.replace('/avatar/', '/user-img/');
      } else {
        avatarUrl = `${BASE_URL}/user-img/${encodeURIComponent(avatarUrl)}`;
      }
    } else {
      avatarUrl = defaultAvatar;
    }

    // 更新缓存（只存文件名，userId为数字）
    const accountData = {
      ...account,
      userId: validUserId, // 强制数字类型
      avatarUrl: account.avatarUrl && !account.avatarUrl.startsWith('http') 
        ? account.avatarUrl 
        : (account.avatarUrl || '')
    };
    localStorage.setItem('userData', JSON.stringify(accountData));
    
    userInfo.value = {
      isLogin: true,
      userId: validUserId,
      username: account.username,
      email: account.email,
      avatarUrl: avatarUrl
    };
    switchAccountDialogVisible.value = false;
    ElMessage.success(`已切换到账号：${account.username}`);
    window.location.reload();
  } catch (err) {
    console.error('切换账号失败：', err);
    ElMessage.error('切换账号失败');
  }
};

/** 打开添加账号登录弹窗 */
const handleAddAccount = () => {
  switchAccountDialogVisible.value = false;
  loginDialogVisible.value = true;
};

/** 登录（添加账号） */
const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value.validate();
    if (!valid) return;

    const res = await axios.post(`${BASE_URL}/user/my`, {
      email: loginForm.email,
      password: loginForm.password
    });

    if (res.data.code === 200) {
      const newAccount = res.data.data;
      // 转换userId为数字
      const validUserId = convertToValidUserId(newAccount.userId);
      if (!validUserId) return;

      // 修复：缓存只存文件名，不存完整URL，userId为数字
      newAccount.avatarUrl = newAccount.avatarUrl || '';
      newAccount.userId = validUserId;

      // 保存到账号列表
      const accountList = JSON.parse(localStorage.getItem('accountList') || '[]');
      if (!accountList.some(item => convertToValidUserId(item.userId) === validUserId)) {
        accountList.push(newAccount);
        localStorage.setItem('accountList', JSON.stringify(accountList));
      }
      // 设置为当前登录账号
      localStorage.setItem('userData', JSON.stringify(newAccount));
      loginDialogVisible.value = false;
      ElMessage.success('登录成功');
      router.push('/my');
      initUserInfo();
    } else {
      ElMessage.error(res.data.msg || '登录失败，邮箱或密码错误');
    }
  } catch (err) {
    console.error('登录失败：', err);
    ElMessage.error('登录失败，网络异常');
  }
};

/** 退出登录 */
const handleLogout = () => {
  try {
    localStorage.removeItem('userData');
    userInfo.value = {
      isLogin: false,
      userId: '',
      username: '',
      email: '',
      avatarUrl: ''
    };
    ElMessage.success('已退出登录');
    router.push('/my').then(() => {
      window.location.reload();
    });
  } catch (err) {
    console.error('退出登录失败：', err);
  }
};

// ========== 新增：页面挂载和路由监听 ==========
// 页面挂载时初始化用户信息
onMounted(() => {
  initUserInfo();
});

// 监听路由参数变化（userId/type），实时更新数据
watch([() => route.query.userId, () => route.query.type], () => {
  initUserInfo();
});

// 监听选中地址变化
watch(selectedAddressIds, (val) => {
  selectAll.value = val.length === addressList.value.length;
});
</script>

<style scoped>
/* 整体容器 */
.user-setting-container {
  width: 100%;
  max-width: 1200px;
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