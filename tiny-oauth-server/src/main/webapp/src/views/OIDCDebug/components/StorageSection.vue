<template>
  <div class="debug-section">
    <h2>本地存储</h2>
    <div class="storage-info">
      <div class="storage-item">
        <span class="label">localStorage 项数:</span>
        <span class="value">{{ localStorageCount }}</span>
      </div>
      <div class="storage-item">
        <span class="label">sessionStorage 项数:</span>
        <span class="value">{{ sessionStorageCount }}</span>
      </div>
    </div>
    <div class="storage-keys">
      <h3>OIDC 相关缓存项:</h3>
      <div v-if="oidcKeys.length === 0" class="storage-key">无</div>
      <div v-else v-for="key in oidcKeys" :key="key" class="storage-key">
        {{ key }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  localStorageCount: number
  sessionStorageCount: number
  oidcKeys: string[]
}>()
</script>

