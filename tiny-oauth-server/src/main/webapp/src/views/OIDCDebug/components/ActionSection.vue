<template>
  <div class="debug-section">
    <h2>操作</h2>
    <div class="action-buttons">
      <a-button type="primary" @click="$emit('refresh')" :loading="refreshing">
        刷新状态
      </a-button>
      <a-button type="default" @click="$emit('clear-cache')" :loading="clearing">
        清理缓存
      </a-button>
      <a-button type="default" @click="$emit('force-logout')">
        强制登出
      </a-button>
      <a-button type="default" @click="$emit('go-login')">
        跳转登录
      </a-button>
      <a-button type="default" @click="$emit('go-home')">
        返回首页
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  refreshing: boolean
  clearing: boolean
}>()

defineEmits<{
  (e: 'refresh'): void
  (e: 'clear-cache'): void
  (e: 'force-logout'): void
  (e: 'go-login'): void
  (e: 'go-home'): void
}>()
</script>

