<template>
  <div class="page-container">
    <div class="pet-guarantee">
      <!-- 顶部 banner 区域 -->
      <div class="banner">
        <img src="@/assets/images/保障图标/宠物医保.png" alt="宠物医保 banner" class="banner-img" />
        <div class="banner-text">
          <h1>宠物<br>医保</h1>
          <div class="tag">为宝贝推荐</div>
        </div>
      </div>
      <div class="pet-insurance-wrapper">
        <!-- 顶部保障亮点区域 -->
        <div class="highlight-area">
          <p class="highlight-title">报销猫狗医疗费</p>
          <div class="highlight-list">
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>疾病保障全</span>
            </div>
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>定点医院多</span>
            </div>
            <div class="highlight-item">
              <img src="@/assets/images/保障图标/打钩.svg" alt="打钩图标" class="check-icon" />
              <span>报销3日完结</span>
            </div>
          </div>
        </div>

        <!-- 套餐选择区域 -->
        <div class="package-area">
          <!-- 10000医疗费套餐 -->
          <div
            class="package-item"
            :class="{ 'active': activePackage === 0 }"
            @click="handlePackageClick(0)"
          >
            <p class="package-tag" v-if="activePackage === 0">基础入门</p>
            <p class="medical-fee">10000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">200消费补贴<br>服务买药可用</p>
            <p class="service">· 体外驱虫1支</p>
            <div class="price">
              <span class="price-text">18.00元/月起</span>
            </div>
          </div>

          <!-- 20000医疗费套餐（默认选中） -->
          <div
            class="package-item recommended"
            :class="{ 'active': activePackage === 1 }"
            @click="handlePackageClick(1)"
          >
            <p class="package-tag" v-if="activePackage === 1">众多宠友力荐</p>
            <p class="medical-fee">20000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">送每月200消费补贴<br>驱虫/洗澡等5折起</p>
            <p class="service">送健康服务<br>· 多联疫苗1针<br>· 体外驱虫1支</p>
            <div class="price">
              <span class="price-text">34.75元/月起</span>
            </div>
          </div>

          <!-- 30000医疗费套餐 -->
          <div
            class="package-item"
            :class="{ 'active': activePackage === 2 }"
            @click="handlePackageClick(2)"
          >
            <p class="package-tag" v-if="activePackage === 2">大病赔得多</p>
            <p class="medical-fee">30000医疗费</p>
            <p class="start-claim">0元起赔</p>
            <p class="reimbursement">报销比例70%</p>
            <p class="subsidy">200消费补贴<br>服务买药可用</p>
            <p class="service">· 多联疫苗1针<br>· 体外驱虫1支<br>· 狂犬疫苗1针<br>· 通用体检1次</p>
            <div class="price">
              <span class="price-text">68.42元/月起</span>
            </div>
          </div>
        </div>
        <!-- 行动按钮区域 -->
        <div class="action-btn">
          <router-link to="/policy-detail" class="main-btn">去看看</router-link>
          <button class="free-btn">免费体验</button>
        </div>
      </div>

      <!-- 责任/意外保险区域 -->
      <div class="insurance-list rounded">
        <div class="top-more">
          <h3>更多推荐</h3>
        </div>
        <!-- 宠物责任险 -->
        <div class="insurance-item rounded">
          <p class="insurance-subtitle">赔人的医药费</p>
          <p class="insurance-desc">猫狗抓人、咬人，帮你赔医药费。主人/他人均可赔，感染猫藓、弓形虫等也能赔</p>
          <div class="insurance-card">
            <img src="https://picsum.photos/seed/pet-liability/200/200" alt="宠物责任险" class="insurance-img rounded" />
            <div class="insurance-info">
              <h3>宠物责任险</h3>
              <p class="insurance-amount">最高24万</p>
              <p class="insurance-scope">主人/他人受伤</p>
              <p class="insurance-tag">蚂蚁保金选·全网不怕比</p>
              <div class="insurance-price-btn">
                <p class="insurance-price">4.98元/月起</p>
                <p class="insurance-bonus">送狂犬疫苗</p>
                <button class="insurance-btn">去看看</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 宠物意外医疗险 -->
        <div class="insurance-item">
          <p class="insurance-subtitle">受伤/误食可保</p>
          <p class="insurance-desc">摔伤、误食、打架等意外，治疗费用贵，这款保障投入小，报销高。特别适合调皮捣蛋、上蹿下跳的宝贝</p>
          <div class="insurance-card">
            <img src="https://picsum.photos/seed/pet-accident/200/200" alt="宠物意外医疗险" class="insurance-img " />
            <div class="insurance-info">
              <h3>宠物意外医疗险</h3>
              <p class="insurance-amount">最高3万</p>
              <p class="insurance-scope">意外伤害</p>
              <div class="insurance-price-btn">
                <p class="insurance-price">4.92元/月起</p>
                <p class="insurance-bonus">不限宠物年龄</p>
                <button class="insurance-btn">去看看</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

// 用于跟踪当前激活的套餐索引，默认选中推荐套餐（索引1）
const activePackage = ref(1)

// 点击套餐项时触发的事件处理函数
const handlePackageClick = (index) => {
  activePackage.value = index
}
</script>

<style scoped>
/* 外层居中容器 */
.page-container {
  max-width: 1200px; /* 可根据需求调整最大宽度 */
  margin: 0 auto;
  padding: 0 15px;
}

.pet-guarantee {
  background-color: #f2f2f0;
  width: 100%;
}
/* 圆角样式通用类 */
.rounded {
  border-radius: 12px;
  overflow: hidden;
}
/* Banner 样式 */
.banner {
  position: relative;
  width: 100%;
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.banner-text {
  position: absolute;
  top: 20px;
  left: 20px;
  color: #fff;
}
.banner-text h1 {
  font-size: 60px;
  line-height: 1.2;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}
.tag {
  position: absolute;
  top: 150px;
  right: -50px;
  background-color: #ffcc00;
  color: #333;
  font-size: 24px;
  font-weight: bold;
  padding: 5px 15px;
  border-radius: 20px;
  transform: rotate(15deg);
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

/* 保障亮点区域样式 */
.highlight-area {
  background-color: #7667ea;
  color: #fff;
  padding: 15px;
  margin-bottom: 15px;
}
.highlight-title {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px;
}

.highlight-list {
  display: flex;
  justify-content: space-between; /* 子元素水平均分 */
}

.highlight-item {
  display: flex;
  align-items: center;
}

.check-icon {
  width: 20px;
  height: 20px;
  margin-right: 5px;
}

.highlight-item span {
  font-size: 14px;
}

/* 套餐选择区域样式 */
.package-area {
  display: flex;
  justify-content: space-around;
  padding: 0 15px 15px;
  gap: 10px; /* 增加间距，防止放大时重叠 */
}
.package-item {
  flex: 1; /* 使用flex:1使三个项目宽度相等 */
  min-width: 0; /* 防止内容过长时破坏flex布局 */
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s ease-in-out; /* 平滑过渡效果 */
  border: 2px solid transparent; /* 预定义透明边框，防止布局跳动 */
}

/* 推荐套餐的基础样式（默认选中） */
.package-item.recommended {
  background-color: #fff9e6;
}

/* 点击后的激活样式 */
.package-item.active {
  border-color: #4186e8; /* 显示蓝色边框 */
  transform: scale(1.05); /* 放大1.05倍 */
  box-shadow: 0 4px 12px rgba(65, 134, 232, 0.3); /* 增强阴影 */
  z-index: 10; /* 确保在最上层 */
}

.package-item.active .medical-fee {
  color: #ff9900; /* 点击后医疗费文字变橙色 */
}

.package-item.active .start-claim,
.package-item.active .reimbursement,
.package-item.active .subsidy,
.package-item.active .service {
  color: #000; /* 点击后商品介绍字体变黑 */
}

.package-tag {
  background-color: #ffcc00;
  color: #333;
  font-size: 12px;
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 15px;
  margin-bottom: 10px;
  display: inline-block;
}
.medical-fee {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin: 10px 0;
  transition: color 0.3s ease; /* 颜色过渡效果 */
}
.start-claim, .reimbursement {
  font-size: 16px;
  color: #666;
  margin: 5px 0;
}
.subsidy, .service {
  font-size: 14px;
  color: #999;
  margin: 8px 0;
  line-height: 1.5;
}
.price {
  margin-top: 20px;
}
.price-text {
  font-size: 20px;
  font-weight: bold;
  color: #ff9900;
}
/* 行动按钮样式 */
.action-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 15px 20px;
  position: relative;
}
.main-btn {
  text-align: center;
  text-decoration: none; /* 清除路由链接下划线 */
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 30px;
  padding: 15px 60px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
.free-btn {
  background-color: #ff3333;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  position: absolute;
  top: -10px;
  right: 20px;
}

/* 保险列表区域样式 */
.top-more{
  padding: 0 10px;
}
.insurance-list {
  background-color: white;
  margin: 15px 0;
}
.insurance-item {
  margin-bottom: 20px;
  background-color: #f4f9fd;
}
.insurance-subtitle {
  font-size: 18px;
  font-weight: bold;
  color: #4186e8;
  margin-bottom: 8px;
  padding-left: 10px;
}
.insurance-desc {
  padding-left: 10px;
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
}
.insurance-card {
  padding-left: 10px;
  display: flex;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}
.insurance-img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  margin-top: 10px;
}
.insurance-info {
  flex: 1;
  padding: 15px;
}
.insurance-info h3 {
  font-size: 18px;
  margin: 0 0 8px;
}
.insurance-amount, .insurance-scope {
  font-size: 14px;
  color: #999;
  margin: 5px 0;
}
.insurance-tag {
  background-color: #fff9e6;
  color: #8e3f0f;
  font-weight: bold;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 15px;
  display: inline-block;
  margin-bottom: 10px;
}
.insurance-price-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.insurance-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff6b6b;
  margin: 0;
}
.insurance-bonus {
  font-size: 12px;
  color: #ff9900;
  background-color: #fff9e6;
  padding: 3px 8px;
  border-radius: 15px;
  margin: 0;
}
.insurance-btn {
  background-color: #4186e8;
  color: #fff;
  border: none;
  border-radius: 20px;
  padding: 8px 15px;
  font-size: 14px;
  cursor: pointer;
}

</style>
