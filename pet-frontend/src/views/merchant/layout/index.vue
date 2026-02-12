<template>
  <div class="merchant-layout">
    <MerchantSidebar ref="sidebarRef" />
    <!-- 主内容区：根据侧边栏折叠状态动态调整左边距 -->
    <div class="main-content" :style="{ marginLeft: sidebarWidth + 'px' }">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import MerchantSidebar from '@/components/MtabBar.vue'

const sidebarRef = ref(null)
const sidebarWidth = ref(200)

// 监听侧边栏折叠状态，动态调整主内容边距
watch(
  () => sidebarRef.value?.isCollapsed,
  (collapsed) => {
    sidebarWidth.value = collapsed ? 64 : 200
  },
  { immediate: true }
)
</script>

<style scoped>
.merchant-layout {
  display: flex;
  height: 100vh;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background: #f9fafb;
  transition: margin-left 0.2s ease;
}
</style>