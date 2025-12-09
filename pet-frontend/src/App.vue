<template>
  <div id="app">
    <!-- 路由出口：显示当前页面内容 -->
    <router-view />
    <!-- 底部导航：全局显示 -->
    <TabBar />
  </div>
</template>

<script setup>
import TabBar from './components/TabBar.vue'
</script>

<style>
/* 基础样式，避免内容被底部导航遮挡 */
body {
  margin: 0;
  padding-bottom: 50px; /* 底部导航高度 */
}

</style>
