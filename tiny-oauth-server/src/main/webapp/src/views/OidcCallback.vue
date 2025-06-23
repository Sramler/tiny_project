<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userManager } from '@/auth/oidc.ts'

const router = useRouter()

onMounted(async () => {
  if (window.location.search.includes('code=') && window.location.search.includes('state=')) {
    try {
      const user = await userManager.signinRedirectCallback()
      console.log('✅ OIDC 登录回调成功')
      console.log('👤 用户信息:', user)
      // 登录成功后跳转回主页或原始路径
      router.replace('/')
    } catch (e) {
      console.error('❌ OIDC 登录失败:', e)
    }
  } else {
    console.warn('⚠️ 非 OIDC 回调，跳转到主页')
    router.replace('/')
  }
})
</script>

<template>
  <div>正在处理登录回调...</div>
</template>
