<template>
  <div class="error-page">
    <h1>404</h1>
    <p>抱歉，您访问的页面不存在。</p>
  </div>
</template>

<script setup lang="ts">
// 404 页面不存在，无需特殊逻辑
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #faad14;
}
h1 {
  font-size: 72px;
  margin-bottom: 16px;
}
p {
  font-size: 20px;
}
</style> 