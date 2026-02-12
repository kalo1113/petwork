<template>
  <div class="merchant-sidebar" :class="{ collapsed: isCollapsed }" v-if="!$route.meta.hideSidebar">
    <!-- 侧边栏头部（含折叠按钮） -->
    <div class="sidebar-header">
      <div class="header-left" @click="!isCollapsed && $router.push('/merchant')">
        <img 
          src="@/assets/images/导航栏我的.svg" 
          alt="商家管理后台" 
          class="sidebar-logo" 
          v-if="!isCollapsed"
        />
        <span class="sidebar-title" v-if="!isCollapsed">商家管理后台</span>
      </div>
      <el-icon class="collapse-btn" @click="isCollapsed = !isCollapsed">
        <ArrowLeft v-if="!isCollapsed" />
        <ArrowRight v-if="isCollapsed" />
      </el-icon>
    </div>
    
    <!-- 导航菜单 -->
    <div class="sidebar-menu">
      <!-- 保险管理组 -->
      <div class="menu-group">
        <div class="group-title" v-if="!isCollapsed">保险管理</div>
        <router-link to="/merchant/insurance/list" class="menu-item" exact-active-class="active">
          <img src="@/assets/images/导航栏我的.svg" alt="保险列表" class="menu-icon" />
          <span class="menu-text" v-if="!isCollapsed">保险列表</span>
        </router-link>
        
        <router-link to="/merchant/insurance/create" class="menu-item" exact-active-class="active">
          <img src="@/assets/images/导航栏我的.svg" alt="创建保险" class="menu-icon" />
          <span class="menu-text" v-if="!isCollapsed">创建保险</span>
        </router-link>
        
        <router-link to="/merchant/insurance/audit" class="menu-item" exact-active-class="active">
          <img src="@/assets/images/导航栏我的.svg" alt="保险订单管理" class="menu-icon" />
          <span class="menu-text" v-if="!isCollapsed">保险订单管理</span>
        </router-link>

        <router-link to="/merchant/insurance/cheak" class="menu-item" exact-active-class="active">
          <img src="@/assets/images/导航栏我的.svg" alt="理赔订单审核" class="menu-icon" />
          <span class="menu-text" v-if="!isCollapsed">理赔订单审核</span>
        </router-link>
      </div>

      <!-- 订单管理组 -->
      <div class="menu-group">
        <div class="group-title" v-if="!isCollapsed">商品管理</div>
        <router-link to="/merchant/order/shipping" class="menu-item" exact-active-class="active">
          <img src="@/assets/images/导航栏我的.svg" alt="商品发货" class="menu-icon" />
          <span class="menu-text" v-if="!isCollapsed">商品发货</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'

// 侧边栏折叠状态（默认展开）
const isCollapsed = ref(false)
</script>

<style scoped>
/* 侧边栏核心样式 - 支持折叠 */
.merchant-sidebar {
  width: 200px;
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  z-index: 999;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease;
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.05);
}

/* 折叠状态 */
.merchant-sidebar.collapsed {
  width: 64px;
}

/* 头部样式 */
.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 16px;
  cursor: default;
}

.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.sidebar-logo {
  width: 32px;
  height: 32px;
  margin-right: 8px;
  object-fit: contain;
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.collapse-btn {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #2196f3;
}

/* 菜单容器 */
.sidebar-menu {
  flex: 1;
  padding: 16px 0;
  overflow-y: auto; /* 菜单过多时滚动 */
}

/* 菜单分组 */
.menu-group {
  margin-bottom: 24px;
  padding: 0 8px;
}

.group-title {
  font-size: 12px;
  color: #999;
  padding: 0 12px 8px;
  font-weight: 500;
}

/* 菜单项样式 */
.menu-item {
  display: flex;
  align-items: center;
  height: 40px;
  padding: 0 12px;
  color: #666;
  text-decoration: none;
  border-radius: 6px;
  margin: 0 8px 4px;
  transition: all 0.2s ease;
}

.menu-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  flex-shrink: 0; /* 折叠时图标不压缩 */
}

.menu-text {
  margin-left: 12px;
  font-size: 14px;
}

/* 激活状态（和用户端TabBar配色统一） */
.menu-item.active {
  color: #2196f3;
  background-color: rgba(33, 150, 243, 0.08);
}

.menu-item.active .menu-icon {
  filter: brightness(0) saturate(100%) invert(33%) sepia(92%) saturate(3506%) hue-rotate(194deg) brightness(98%) contrast(103%);
}

/* 悬浮效果 */
.menu-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

/* 滚动条美化 */
.sidebar-menu::-webkit-scrollbar {
  width: 4px;
}

.sidebar-menu::-webkit-scrollbar-thumb {
  background-color: #ddd;
  border-radius: 2px;
}

.sidebar-menu::-webkit-scrollbar-track {
  background-color: transparent;
}
</style>