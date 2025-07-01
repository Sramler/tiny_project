<script setup lang="ts">
import { ConfigProvider } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import { RouterView } from 'vue-router' // App 入口文件，主布局由路由嵌套控制
</script>
<template>
  <ConfigProvider :locale="zhCN">
    <!-- 路由视图，主布局由路由配置决定 -->
    <RouterView />
  </ConfigProvider>

</template>
<style scoped>
</style>
