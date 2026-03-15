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
          <div class="tab-item" @click="handleOrderTabClick('effective')">
            <img src="@/assets/images/我的图标/已生效.svg" alt="已生效" class="order-icon" />
            <span>我的保单</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick('wallet')">
            <img src="@/assets/images/我的图标/我的钱包.svg" alt="我的钱包" class="order-icon" />
            <span>我的钱包</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick('deliver')">
            <img src="@/assets/images/我的图标/待发货.svg" alt="待发货" class="order-icon" />
            <span>待发货</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick('receive')">
            <img src="@/assets/images/我的图标/待收货.svg" alt="待收货" class="order-icon" />
            <span>待收货</span>
          </div>
          <div class="tab-item" @click="handleOrderTabClick('comment')">
            <img src="@/assets/images/我的图标/理赔相关.svg" alt="理赔订单" class="order-icon" />
            <span>理赔订单</span>
          </div>
        </div>
      </div>

      <!-- 宠物保障模块（核心修改） -->
      <div class="pet-guarantee white-bg">
        <h3>宠物保障</h3>
        <!-- 有保险权益时展示权益列表 -->
        <div v-if="insuranceBenefits.length" class="benefit-list">
          <div 
            v-for="benefit in insuranceBenefits" 
            :key="benefit.benefitId" 
            class="benefit-card"
          >
            <!-- 权益标题+状态 -->
            <div class="benefit-header">
              <h4 class="benefit-name">{{ benefit.insuranceName }}</h4>
              <span class="benefit-status" :class="benefit.status === '有效' ? 'active' : 'expired'">
                {{ benefit.status }}
              </span>
            </div>
            <!-- 权益详情 -->
            <div class="benefit-details">
              <div class="benefit-item">
                <span class="label">保障期限：</span>
                <span class="value">{{ benefit.startTime }} - {{ benefit.endTime }}</span>
              </div>
              <div class="benefit-item">
                <span class="label">剩余补贴：</span>
                <span class="value">¥{{ benefit.remainingSubsidy.toFixed(2) }}</span>
              </div>
              <div class="benefit-item">
                <span class="label">赠送服务：</span>
                <span class="value">{{ benefit.coverage }}</span>
              </div>
            </div>
            <!-- 权益操作按钮 -->
            <div class="benefit-actions">
              <button class="btn mini-btn" @click="handleBenefitDetail(benefit)">查看保单</button>
              <div class="insurance-actions">
                <button class="btn mini-btn" @click="handleClaimDetail(benefit)">申请理赔</button>
              </div>
            </div>
          </div>
        </div>
        <!-- 无保险权益时显示原样式 -->
        <div v-else class="guarantee-card">
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
import { ref, reactive, onMounted, computed, getCurrentInstance, watch } from 'vue'
import { ElDialog, ElForm, ElFormItem, ElInput, ElButton, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
// 正确导入所有需要的接口
import { 
  login, register, getPetListByUserId,
  getInsuranceBenefitListByPetId ,getInsuranceDetail, getInsuranceOrderDetail
} from '@/api/user/index.js'
import defaultAvatar from '@/assets/images/我的图标/默认头像.svg'
import defaultPetAvatar from '@/assets/images/我的图标/添加.svg'
import maleIcon from '@/assets/images/我的图标/男.svg'
import femaleIcon from '@/assets/images/我的图标/女.svg'

const router = useRouter()
const { proxy } = getCurrentInstance()
const BASE_URL = proxy.$BASE_URL

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

// 宠物列表+选项卡
const petList = ref([])
const activePetIndex = ref(0)
const currentPet = computed(() => {
  return petList.value[activePetIndex.value] || {}
})

// 保险权益相关
const insuranceBenefits = ref([])

// 日期格式化函数
const formatDate = (dateStr) => {
  if (!dateStr || dateStr === '未知') return '未知' // 新增：判断dateStr是否为空
  try {
    const date = new Date(dateStr)
    // 新增：判断date是否有效（避免无效日期字符串）
    if (isNaN(date.getTime())) return '未知'
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
  } catch (err) {
    return '未知'
  }
}

// 保险权益操作函数
const handleBenefitDetail = (benefit) => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  router.push({
    path: '/user/myorder?activeTab=effective',
    query: { 
      benefitId: benefit.benefitId, 
      petId: currentPet.value.petId,
      petName: currentPet.value.name
    }
  }).catch(err => {
    console.error('跳转权益详情页失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  })
}

// 申请理赔（跳转到理赔页，并把订单ID带过去）
const handleClaimDetail = (benefit) => {
  // 从权益对象中取出保险订单ID
  const orderId = benefit?.originalData?.insuranceOrderId || benefit?.insuranceOrderId;
  
  if (!orderId) {
    ElMessage.warning('订单信息异常，无法申请理赔')
    return
  }

  router.push({
    path: '/user/claim',
    query: {
      orderId: orderId // 现在传的是真正的保险订单ID
    }
  })
}

// 1. 修复 getPetPurchasedInsurance 函数
const getPetPurchasedInsurance = async (petId) => {
  if (!petId || !userInfo.value.isLogin) {
    console.log('【已购保险查询】宠物ID为空或未登录，无法查询')
    return []
  }
  try {
    console.log(`【第一步】开始查询宠物${petId}的权益列表`)
    const res = await getInsuranceBenefitListByPetId(petId)
    console.log(`【第二步】权益接口返回结果：`, res)
    
    if (!res || res.code !== 200 || !Array.isArray(res.data)) {
      console.log(`【第二步】权益接口返回非200或非数组，结果：`, res)
      return []
    }

    const purchasedInsurance = []
    for (const benefit of res.data) {
      console.log(`【第三步】单条权益数据：`, benefit)
      // 1. 解析权益表核心字段
      const insuranceId = benefit?.insuranceId || benefit?.insurance_id;
      const insuranceOrderId = benefit?.insuranceOrderId || benefit?.insurance_order_id; // 订单ID
      console.log(`【第四步】解析出保险ID：${insuranceId}，订单ID：${insuranceOrderId}`)
      
      // 2. 查询保险名称（移除默认值，直接取数据库数据）
      let realInsuranceName = ""; // 初始化为空，不再设置默认值
      if (insuranceId) {
        try {
          const insuranceRes = await getInsuranceDetail(insuranceId);
          // 直接读取数据库返回的名称，无数据则为空
          realInsuranceName = insuranceRes?.data?.insurance?.insuranceName || "";
        } catch (e) {
          console.error(`【查询保险${insuranceId}名称失败】`, e);
          realInsuranceName = ""; // 查询失败也置空
        }
      }

      // 3. 新增：查询订单表的状态
      let orderStatusNum = -1; // 默认-1（未查询到）
      let orderStatusText = "未知"; // 状态中文说明
      if (insuranceOrderId) {
        try {
          console.log(`【第五步】调用订单详情接口，查询订单${insuranceOrderId}状态`)
          const orderRes = await getInsuranceOrderDetail(insuranceOrderId);
          console.log(`【第六步】订单${insuranceOrderId}详情返回：`, orderRes)
          console.log(`【关键校验】orderRes.data是否存在：`, !!orderRes.data);
          console.log(`【关键校验】orderRes.data.orderStatus的值：`, orderRes.data?.orderStatus);
          console.log(`【关键校验】orderRes.data.orderStatus类型：`, typeof orderRes.data?.orderStatus);
          
          // 终极精准解析：强制转数字 + 兜底
          const rawStatus = orderRes.data?.orderStatus;
          orderStatusNum = (rawStatus !== undefined && rawStatus !== null) ? Number(rawStatus) : -1;
          console.log(`【解析后】orderStatusNum：`, orderStatusNum);
          
          // 转换为中文状态
          switch (orderStatusNum) {
            case 0:
              orderStatusText = "已支付";
              break;
            case 1:
              orderStatusText = "已生效";
              break;
            case 2:
              orderStatusText = "已取消";
              break;
            default:
              orderStatusText = `未知状态(${orderStatusNum})`; // 显示具体数字，方便排查
          }
        } catch (e) {
          console.error(`【查询订单${insuranceOrderId}状态失败】`, e);
          orderStatusText = "查询失败";
        }
      } else {
        console.log(`【第五步】订单ID为空，跳过订单状态查询`)
      }

      // 4. 组装最终数据（包含订单状态）
      purchasedInsurance.push({
        '宠物ID': petId,
        '权益ID': benefit?.id || benefit?.benefitId || '无',
        '保险ID': insuranceId || '无',
        '保险名称(真实)': realInsuranceName || '无', // 空值显示"无"
        '订单ID': insuranceOrderId || '无',
        '订单状态(数字)': orderStatusNum,
        '订单状态(中文)': orderStatusText,
        '权益表是否有效': benefit?.isValid ? '是' : '否',
        '创建时间': benefit?.createTime || '未知',
        '过期时间': benefit?.insuranceExpireTime || '未知',
        '剩余补贴': benefit?.monthlySubsidyBalance || 0,
        '赠送服务': benefit?.freeServiceRemaining || '无'
      });
    }
    // 打印最终结果（含订单状态）
    console.log(`========== 宠物ID: ${petId} 已购保险列表 ==========`)
    console.table(purchasedInsurance)
    return purchasedInsurance
  } catch (err) {
    console.error(`【已购保险查询整体失败】宠物ID: ${petId}`, err)
    return []
  }
}

// 2. 修复 getPetInsuranceBenefits 函数（核心修改：移除虚拟默认值）
const getPetInsuranceBenefits = async (petId) => {
  if (!petId || !userInfo.value.isLogin) {
    insuranceBenefits.value = []
    return
  }
  try {
    const res = await getInsuranceBenefitListByPetId(petId)
    if (!res || res.code !== 200 || !Array.isArray(res.data)) {
      insuranceBenefits.value = []
      return
    }
    const benefitList = []
    for (const benefit of res.data) {
      // 1. 查询保险名称（移除默认值，仅读取数据库真实数据）
      const insuranceId = benefit?.insuranceId || benefit?.insurance_id;
      let realInsuranceName = ""; // 初始化为空，无默认值
      if (insuranceId) {
        try {
          const insuranceRes = await getInsuranceDetail(insuranceId);
          // 仅读取数据库返回的名称，无数据则为空
          realInsuranceName = insuranceRes?.data?.insurance?.insuranceName || "";
        } catch (e) {
          console.error(`查询保险${insuranceId}名称失败`, e);
          realInsuranceName = ""; // 查询失败也置空
        }
      }

      // 2. 查询订单状态（修复：提前定义变量，扩大作用域）
      let orderStatusText = "未知";
      let orderStatusNum = -1; // 提前定义，初始值-1（未查询到）
      const insuranceOrderId = benefit?.insuranceOrderId || benefit?.insurance_order_id;
      if (insuranceOrderId) {
        try {
          const orderRes = await getInsuranceOrderDetail(insuranceOrderId);
          // 终极精准解析
          const rawStatus = orderRes.data?.orderStatus;
          orderStatusNum = (rawStatus !== undefined && rawStatus !== null) ? Number(rawStatus) : -1;
          // 转换为中文状态
          switch (orderStatusNum) {
            case 0:
              orderStatusText = "已支付";
              break;
            case 1:
              orderStatusText = "已生效";
              break;
            case 2:
              orderStatusText = "已取消";
              break;
            default:
              orderStatusText = `未知状态(${orderStatusNum})`;
          }
        } catch (e) {
          orderStatusText = "状态查询失败";
        }
      }

      // 3. 组装页面展示数据（保险名称为空则显示"无"）
      benefitList.push({
        benefitId: benefit?.id || benefit?.benefitId || '',
        insuranceName: realInsuranceName || '无', // 数据库无数据则显示"无"
        status: orderStatusText, // 页面显示订单表的中文状态
        statusNum: orderStatusNum, // 现在能正常访问，不会报错
        startTime: formatDate(benefit?.createTime),
        endTime: formatDate(benefit?.insuranceExpireTime),
        remainingSubsidy: Number(benefit?.monthlySubsidyBalance || benefit?.remainingInsuranceAmount || 0),
        coverage: benefit?.freeServiceRemaining 
          ? `${benefit.freeServiceRemaining}` 
          : '无', // 赠送服务为空显示"无"
        originalData: benefit || {}
      })
    }
    // ✅ 核心修改：过滤掉无效的「无+未知状态(-1)」数据
    insuranceBenefits.value = benefitList.filter(item => {
      // 保留：有保险名称 或 状态不是-1的有效数据
      return item.insuranceName !== '无' || item.statusNum !== -1
    })
  } catch (err) {
    console.error('【获取宠物保险权益失败】', err)
    insuranceBenefits.value = []
    ElMessage.warning('获取宠物保障信息失败，将显示默认内容')
  }
}

// 清空错误提示
const clearError = () => {
  errorMsg.value = ''
}

// 检查登录状态
const checkLoginStatus = () => {
  const userData = localStorage.getItem('userData')
  if (userData) {
    const parsed = JSON.parse(userData)
    const avatarUrl = parsed.avatarUrl 
      ? `${BASE_URL}/user-img/${parsed.avatarUrl.split('/').pop()}` 
      : defaultAvatar
    userInfo.value = {
      isLogin: true,
      username: parsed.username || `宝友${parsed.userId?.toString().slice(-4)}`,
      userId: parsed.userId,
      avatarUrl: avatarUrl
    }
    fetchAndPrintPetList()
  }
}

// 获取宠物列表
const fetchAndPrintPetList = async () => {
  try {
    const res = await getPetListByUserId(userInfo.value.userId)
    if (res?.code === 200 && Array.isArray(res.data)) {
      petList.value = res.data.map(pet => ({
        name: pet?.petName || '未知',
        avatarUrl: pet?.petFacePhoto || defaultPetAvatar,
        breed: pet?.petType || '未知',
        age: calculatePetAge(pet?.petBirthday),
        uniqueId: pet?.petUniqueId || `${pet?.petId || ''}`,
        petId: pet?.petId,
        gender: pet?.petGender || '公',
        sterilized: pet?.isSterilized || false
      }))
      console.log(`【用户${userInfo.value.userId}的宠物列表】`, petList.value)
      // 加载第一个宠物的权益 + 打印已购保险（主动触发，替代immediate）
      if (petList.value.length) {
        await getPetInsuranceBenefits(petList.value[0].petId)
        await getPetPurchasedInsurance(petList.value[0].petId)
      }
    } else {
      console.log(`【获取宠物列表失败】${res?.msg || '未知错误'}`)
      petList.value = []
    }
  } catch (err) {
    console.error('【获取宠物列表异常】', err)
    petList.value = []
  }
}

// 计算宠物年龄
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

// 监听宠物切换
watch(
  () => currentPet.value.petId,
  async (petId) => {
    if (petId && userInfo.value.isLogin) {
      // 等待前一个异步请求完成，避免并发导致的响应式异常
      await getPetInsuranceBenefits(petId)
      await getPetPurchasedInsurance(petId)
    } else {
      insuranceBenefits.value = []
    }
  }
)

// 登录/注册提交
const isLoading = ref(false)
const handleAuthSubmit = async () => {
  try {
    isLoading.value = true
    clearError()
    let result

    if (activeTab.value === 'login') {
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

    if (result.code === 200) {
      if (activeTab.value === 'login') {
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
      errorMsg.value = result.msg || (activeTab.value === 'login' ? '登录失败！' : '注册失败！')
    }
  } catch (error) {
    console.error('请求错误：', error)
    errorMsg.value = error.msg || '网络异常，请检查后端服务是否启动！'
  } finally {
    isLoading.value = false
  }
}

// 其他事件处理函数（保持不变）
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
  router.push({ 
    path: '/user/setting' ,
    query: {
      userId: userInfo.value.userId,
      type: 'edit'
    }
  }).then(() => {
    ElMessage.info('正在跳转到个人设置页面')
  }).catch(err => {
    console.error('路由跳转失败：', err)
    ElMessage.error('跳转失败，请检查路由配置')
  })
}
const handleEditPetClick = (petId) => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  try {
    router.push({
      path: '/pet-id-card',
      query: {
        userId: userInfo.value.userId,
        petId: petId,
        type: 'edit'
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
const handleOrderTabClick = (tabKey) => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  router.push({
    path: '/user/myorder',
    query: { activeTab: tabKey }
  }).catch(err => {
    console.error('跳转订单页面失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  })
}
const handleGuaranteeClick = () => {
  if (!userInfo.value.isLogin) {
    dialogVisible.value = true
    return
  }
  router.push({
    path: '/guarantee',
  }).catch(err => {
    console.error('跳转订单页面失败：', err)
    ElMessage.error('页面跳转失败，请重试')
  })
}

// 页面加载
onMounted(() => {
  checkLoginStatus()
})
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
/* 保险权益列表样式 */
.benefit-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.benefit-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border-left: 4px solid #2196f3;
}

.benefit-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.benefit-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.benefit-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: auto;
}

.benefit-status.active {
  background: #e8f5e9;
  color: #4caf50;
}

.benefit-status.expired {
  background: #ffebee;
  color: #f44336;
}

.benefit-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.benefit-item {
  display: flex;
  font-size: 14px;
}

.benefit-item .label {
  color: #666;
  min-width: 80px;
}

.benefit-item .value {
  color: #333;
  flex: 1;
}

.benefit-actions {
  display: flex;
  gap: 8px;
}

.mini-btn {
  padding: 4px 12px;
  font-size: 12px;
}

.mini-btn.primary {
  background: #2196f3;
  color: #fff;
}

.mini-btn:hover {
  opacity: 0.9;
}
</style>