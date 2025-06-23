<template>
  <!-- 基础详情页内容区域 -->
  <div>
    <h2>基础详情页</h2>
    <p>这里是基础详情页内容。</p>
  </div>
</template>

<script setup lang="ts">
import { userManager } from '@/auth/oidc.ts'
userManager.signinRedirect() // 重定向到认证服务器登录
</script>

<style scoped>
</style>
