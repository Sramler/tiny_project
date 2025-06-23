<template>
  <!-- 展示菜单信息的默认页面 -->
  <div>
    <h2>{{ menuInfo.title }}</h2>
    <p>路径：{{ menuInfo.path }}</p>
    <p v-if="menuInfo.icon">图标：{{ menuInfo.icon }}</p>
    <pre>{{ menuInfo }}</pre>
  </div>
</template>

<script setup lang="ts">
// 接收路由 meta 传递的菜单信息
import { useRoute } from 'vue-router'
const route = useRoute()
const menuInfo = (route.meta.menuInfo || {}) as any
</script> 