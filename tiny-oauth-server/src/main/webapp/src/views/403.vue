<template>
  <div class="error-page">
    <h1>403</h1>
    <p>抱歉，您没有权限访问该页面。</p>
  </div>
</template>

<script setup lang="ts">
// 403 无权限页面，无需特殊逻辑
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #ff4d4f;
}
h1 {
  font-size: 72px;
  margin-bottom: 16px;
}
p {
  font-size: 20px;
}
</style> 