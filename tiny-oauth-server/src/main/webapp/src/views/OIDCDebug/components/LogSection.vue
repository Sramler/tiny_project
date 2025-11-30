<template>
  <div class="debug-section">
    <h2>操作日志</h2>
    <div class="log-container">
      <div v-for="(log, index) in logs" :key="index" class="log-item">
        <span class="log-time">{{ log.time }}</span>
        <span :class="['log-level', log.level]">{{ log.level }}</span>
        <span class="log-message">{{ log.message }}</span>
      </div>
      <div v-if="!logs.length" class="log-empty">暂无日志</div>
    </div>
    <a-button type="link" @click="$emit('clear')">清空日志</a-button>
  </div>
</template>

<script setup lang="ts">
interface DebugLog {
  time: string
  level: string
  message: string
}

defineProps<{
  logs: DebugLog[]
}>()

defineEmits<{
  (e: 'clear'): void
}>()
</script>

