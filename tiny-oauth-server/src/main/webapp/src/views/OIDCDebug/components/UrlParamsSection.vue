<template>
  <div class="debug-section">
    <h2>URL 参数</h2>
    <div class="url-params">
      <div v-if="!Object.keys(params).length" class="param-item">
        <span class="param-key">无</span>
        <span class="param-value">当前 URL 不包含查询参数</span>
      </div>
      <div v-else v-for="(value, key) in params" :key="key" class="param-item">
        <span class="param-key">{{ key }}:</span>
        <span class="param-value">{{ value }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  params: Record<string, string>
}>()
</script>

