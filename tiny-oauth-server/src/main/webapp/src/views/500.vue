<template>
  <div class="error-page">
    <h1>500</h1>
    <p>抱歉，服务器发生错误，请稍后重试。</p>
  </div>
</template>

<script setup lang="ts">
// 500 服务器错误页面，无需特殊逻辑
</script>

<style scoped>
.error-page {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  color: #d4380d;
}
h1 {
  font-size: 72px;
  margin-bottom: 16px;
}
p {
  font-size: 20px;
}
</style> 