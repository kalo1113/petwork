<!-- src/components/TabBar.vue -->
<template>
  <div class="tab-bar" v-if="$route.meta.showFooter">
    <router-link to="/PetHome" class="tab-item">
      <img src="@/assets/images/导航栏首页.svg" alt="首页图标" class="tab-icon" />
      <span>首页</span>
    </router-link>
    <router-link to="/guarantee" class="tab-item">
      <img src="@/assets/images/导航栏保障.svg" alt="保障图标" class="tab-icon" />
      <span>保障</span>
    </router-link>
    <router-link to="/mall" class="tab-item">
      <img src="@/assets/images/导航栏商城.svg" alt="商城图标" class="tab-icon" />
      <span>商城</span>
    </router-link>
    <router-link to="/my" class="tab-item">
      <img src="@/assets/images/导航栏我的.svg" alt="我的图标" class="tab-icon" />
      <span>我的</span>
    </router-link>
  </div>
</template>

<style scoped>
.tab-bar {
  display: flex;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 70px;
  border-top: 1px solid #ddd;
  background: #fff;
  z-index: 999;
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #666;
  text-decoration: none;
}
.tab-icon {
  width: 24px;
  height: 24px;
  object-fit: contain;
  margin-bottom: 4px;
}
.tab-item.router-link-exact-active {
  color: #2196f3;
}
.tab-item.router-link-exact-active .tab-icon {
  filter: brightness(0) saturate(100%) invert(33%) sepia(92%) saturate(3506%) hue-rotate(194deg) brightness(98%) contrast(103%);
}
</style>
