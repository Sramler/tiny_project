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
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userManager } from '@/auth/oidc.ts'
import { useAuth } from '@/auth/auth'

const router = useRouter()
const { isAuthenticated } = useAuth()
const isRedirecting = ref(false)

onMounted(async () => {
  // 如果用户已经认证，直接跳转到首页
  if (isAuthenticated.value) {
    console.log('用户已认证，跳转到首页')
    router.replace('/')
    return
  }
  
  // 防止重复重定向
  if (isRedirecting.value) {
    console.log('正在重定向中，跳过重复操作')
    return
  }
  
  // 检查是否已经在 OIDC 流程中
  const currentUser = await userManager.getUser()
  if (currentUser && !currentUser.expired) {
    console.log('检测到有效用户，跳转到首页')
    router.replace('/')
    return
  }
  
  // 检查 URL 参数，避免重复重定向
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('code') || urlParams.has('error')) {
    console.log('检测到 OIDC 回调参数，不进行重定向')
    return
  }
  
  try {
    console.log('开始 OIDC 登录重定向')
    isRedirecting.value = true
    
    // 重定向到认证服务器登录
    await userManager.signinRedirect({
      state: {
        returnUrl: window.location.pathname + window.location.search,
      },
    })
  } catch (error) {
    console.error('OIDC 登录重定向失败:', error)
    isRedirecting.value = false
  }
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
