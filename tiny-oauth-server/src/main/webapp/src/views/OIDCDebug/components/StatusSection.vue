<template>
  <div class="debug-section">
    <h2>认证状态</h2>
    <div class="status-grid">
      <div class="status-item">
        <span class="label">用户认证状态:</span>
        <span :class="['value', isAuthenticated ? 'success' : 'error']">
          {{ isAuthenticated ? '已认证' : '未认证' }}
        </span>
      </div>
      <div class="status-item">
        <span class="label">当前用户:</span>
        <span class="value">{{ userInfo }}</span>
      </div>
      <div class="status-item">
        <span class="label">Token 过期时间:</span>
        <span class="value">{{ tokenExpiry }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  isAuthenticated: boolean
  userInfo: string
  tokenExpiry: string
}>()
</script>

