<template>
  <!-- 登录页面内容区域 -->
  <div class="login-container">
    <div class="login-content">
      <h2>正在跳转到登录页面...</h2>
      <p>如果页面没有自动跳转，请稍等片刻或刷新页面。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/auth/auth'

const router = useRouter()
const { isAuthenticated, login } = useAuth()

onMounted(async () => {
  // 如果用户已经认证，直接跳转到首页
  if (isAuthenticated.value) {
    console.log('用户已认证，跳转到首页')
    router.replace('/')
    return
  }
  
  // 延迟一下，让用户看到提示信息
  setTimeout(async () => {
    try {
      console.log('开始 OIDC 登录重定向')
      await login()
    } catch (error) {
      console.error('OIDC 登录重定向失败:', error)
    }
  }, 1000)
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.login-content h2 {
  color: #1890ff;
  margin-bottom: 16px;
}

.login-content p {
  color: #666;
  margin: 0;
}
</style>
