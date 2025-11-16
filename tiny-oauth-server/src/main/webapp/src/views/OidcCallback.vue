<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userManager } from '@/auth/oidc.ts'
import { useAuth } from '@/auth/auth'

const router = useRouter()
const { isAuthenticated } = useAuth()
const processing = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    // 检查是否是 OIDC 回调
    const urlParams = new URLSearchParams(window.location.search)
    const hasCode = urlParams.has('code')
    const hasState = urlParams.has('state')
    
    if (hasCode && hasState) {
      console.log('检测到 OIDC 回调参数，开始处理登录回调')
      
      try {
        const user = await userManager.signinRedirectCallback()
        console.log('✅ OIDC 登录回调成功')
        console.log('👤 用户信息:', user)
        
        // 等待用户状态更新
        await new Promise(resolve => setTimeout(resolve, 100))
        
        // 登录成功后跳转回主页或原始路径
        const returnUrl = (user?.state as any)?.returnUrl || '/'
        console.log('跳转到:', returnUrl)
        
        // 使用 replace 避免历史记录问题
        await router.replace(returnUrl)
      } catch (callbackError: any) {
        // 检查是否是 state 不匹配的错误
        if (callbackError?.message?.includes('No matching state') || 
            callbackError?.message?.includes('state')) {
          console.warn('⚠️ State 不匹配，可能是直接访问回调页面或表单登录后的重定向')
          console.warn('错误详情:', callbackError.message)
          
          // 清除可能存在的无效 state
          try {
            await userManager.removeUser()
          } catch (e) {
            console.warn('清除用户状态失败:', e)
          }
          
          // 跳转到登录页，让用户重新登录
          error.value = '登录状态已失效，请重新登录'
          setTimeout(() => {
            router.replace('/login')
          }, 3000)
        } else {
          throw callbackError
        }
      }
    } else if (urlParams.has('error')) {
      // 处理 OIDC 错误
      const errorParam = urlParams.get('error')
      const errorDescription = urlParams.get('error_description')
      
      console.error('❌ OIDC 登录失败:', errorParam, errorDescription)
      error.value = errorDescription || errorParam || '登录失败'
      
      // 延迟跳转到登录页
      setTimeout(() => {
        router.replace('/login')
      }, 3000)
    } else {
      console.warn('⚠️ 非 OIDC 回调，跳转到主页')
      router.replace('/')
    }
  } catch (e) {
    console.error('❌ OIDC 登录回调处理失败:', e)
    error.value = e instanceof Error ? e.message : '登录回调处理失败'
    
    // 延迟跳转到登录页
    setTimeout(() => {
      router.replace('/login')
    }, 3000)
  } finally {
    processing.value = false
  }
})
</script>

<template>
  <div class="callback-container">
    <div class="callback-content">
      <div v-if="processing" class="processing">
        <h2>正在处理登录回调...</h2>
        <p>请稍等片刻，正在验证您的登录信息。</p>
      </div>
      
      <div v-else-if="error" class="error">
        <h2>登录失败</h2>
        <p class="error-message">{{ error }}</p>
        <p>3秒后自动跳转到登录页面...</p>
      </div>
      
      <div v-else class="success">
        <h2>登录成功</h2>
        <p>正在跳转到主页...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.callback-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.callback-content {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 400px;
}

.callback-content h2 {
  margin-bottom: 16px;
}

.processing h2 {
  color: #1890ff;
}

.success h2 {
  color: #52c41a;
}

.error h2 {
  color: #f5222d;
}

.error-message {
  color: #f5222d;
  background: #fff2f0;
  border: 1px solid #ffccc7;
  padding: 8px 12px;
  border-radius: 4px;
  margin: 12px 0;
}

.callback-content p {
  color: #666;
  margin: 8px 0;
}
</style>
