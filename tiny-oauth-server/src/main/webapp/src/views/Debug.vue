<template>
  <div class="debug-container">
    <div class="debug-content">
      <h1>OIDC 调试工具</h1>
      
      <!-- 认证状态 -->
      <div class="debug-section">
        <h2>认证状态</h2>
        <div class="status-grid">
          <div class="status-item">
            <span class="label">用户认证状态:</span>
            <span :class="['value', isAuthenticated ? 'success' : 'error']">
              {{ isAuthenticated ? '已认证' : '未认证' }}
            </span>
          </div>
          <div class="status-item">
            <span class="label">当前用户:</span>
            <span class="value">{{ userInfo }}</span>
          </div>
          <div class="status-item">
            <span class="label">Token 过期时间:</span>
            <span class="value">{{ tokenExpiry }}</span>
          </div>
        </div>
      </div>
      
      <!-- URL 参数 -->
      <div class="debug-section">
        <h2>URL 参数</h2>
        <div class="url-params">
          <div v-for="(value, key) in urlParams" :key="key" class="param-item">
            <span class="param-key">{{ key }}:</span>
            <span class="param-value">{{ value }}</span>
          </div>
        </div>
      </div>
      
      <!-- 本地存储 -->
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
          <div v-for="key in oidcKeys" :key="key" class="storage-key">
            {{ key }}
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="debug-section">
        <h2>操作</h2>
        <div class="action-buttons">
          <a-button type="primary" @click="refreshStatus" :loading="refreshing">
            刷新状态
          </a-button>
          <a-button type="default" @click="clearCache" :loading="clearing">
            清理缓存
          </a-button>
          <a-button type="default" @click="forceLogout">
            强制登出
          </a-button>
          <a-button type="default" @click="goToLogin">
            跳转登录
          </a-button>
          <a-button type="default" @click="goHome">
            返回首页
          </a-button>
        </div>
      </div>
      
      <!-- 日志 -->
      <div class="debug-section">
        <h2>操作日志</h2>
        <div class="log-container">
          <div v-for="(log, index) in logs" :key="index" class="log-item">
            <span class="log-time">{{ log.time }}</span>
            <span :class="['log-level', log.level]">{{ log.level }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
        <a-button type="link" @click="clearLogs">清空日志</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/auth/auth'
import { userManager } from '@/auth/oidc'
import { clearOidcCache, isInOidcFlow } from '@/utils/auth-utils'
import { message } from 'ant-design-vue'

const router = useRouter()
const { user, isAuthenticated } = useAuth()

// 响应式数据
const refreshing = ref(false)
const clearing = ref(false)
const logs = ref<Array<{ time: string; level: string; message: string }>>([])

// 计算属性
const userInfo = computed(() => {
  if (!user.value) return '无用户信息'
  return `${user.value.profile?.name || user.value.profile?.preferred_username || '未知用户'} (${user.value.profile?.sub || '无ID'})`
})

const tokenExpiry = computed(() => {
  if (!user.value || !user.value.expires_at) return '无过期时间'
  const expiry = new Date(user.value.expires_at * 1000)
  const now = new Date()
  const diff = expiry.getTime() - now.getTime()
  const minutes = Math.floor(diff / 60000)
  return `${expiry.toLocaleString()} (${minutes > 0 ? `${minutes}分钟后过期` : '已过期'})`
})

const urlParams = computed(() => {
  const params = new URLSearchParams(window.location.search)
  const result: Record<string, string> = {}
  params.forEach((value, key) => {
    result[key] = value
  })
  return result
})

const localStorageCount = computed(() => localStorage.length)
const sessionStorageCount = computed(() => sessionStorage.length)

const oidcKeys = computed(() => {
  const keys: string[] = []
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i)
    if (key && (key.includes('oidc') || key.includes('user') || key.includes('auth'))) {
      keys.push(key)
    }
  }
  return keys
})

// 方法
function addLog(level: string, message: string) {
  logs.value.unshift({
    time: new Date().toLocaleTimeString(),
    level,
    message
  })
  // 限制日志数量
  if (logs.value.length > 50) {
    logs.value = logs.value.slice(0, 50)
  }
}

async function refreshStatus() {
  refreshing.value = true
  try {
    addLog('info', '刷新认证状态...')
    // 强制重新获取用户信息
    const currentUser = await userManager.getUser()
    addLog('info', `当前用户状态: ${currentUser ? '已登录' : '未登录'}`)
    if (currentUser) {
      addLog('info', `用户ID: ${currentUser.profile?.sub}`)
    }
    message.success('状态刷新完成')
  } catch (error) {
    addLog('error', `刷新状态失败: ${error}`)
    message.error('刷新状态失败')
  } finally {
    refreshing.value = false
  }
}

async function clearCache() {
  clearing.value = true
  try {
    addLog('info', '开始清理缓存...')
    const success = await clearOidcCache()
    if (success) {
      addLog('success', '缓存清理成功')
      message.success('缓存清理成功')
    } else {
      addLog('error', '缓存清理失败')
      message.error('缓存清理失败')
    }
  } catch (error) {
    addLog('error', `清理缓存异常: ${error}`)
    message.error('清理缓存异常')
  } finally {
    clearing.value = false
  }
}

async function forceLogout() {
  try {
    addLog('info', '执行强制登出...')
    await userManager.removeUser()
    addLog('success', '强制登出成功')
    message.success('强制登出成功')
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    addLog('error', `强制登出失败: ${error}`)
    message.error('强制登出失败')
  }
}

function goToLogin() {
  addLog('info', '跳转到登录页')
  router.push('/login')
}

function goHome() {
  addLog('info', '返回首页')
  router.push('/')
}

function clearLogs() {
  logs.value = []
  addLog('info', '日志已清空')
}

// 生命周期
onMounted(() => {
  addLog('info', '调试页面加载完成')
  addLog('info', `当前路径: ${window.location.pathname}`)
  addLog('info', `是否在 OIDC 流程中: ${isInOidcFlow()}`)
  addLog('info', `认证状态: ${isAuthenticated.value}`)
})
</script>

<style scoped>
.debug-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.debug-content h1 {
  color: #1890ff;
  margin-bottom: 24px;
  text-align: center;
}

.debug-section {
  margin-bottom: 32px;
  padding: 20px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background: #fafafa;
}

.debug-section h2 {
  color: #262626;
  margin-bottom: 16px;
  font-size: 18px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.label {
  font-weight: 500;
  color: #595959;
}

.value {
  color: #262626;
}

.value.success {
  color: #52c41a;
}

.value.error {
  color: #f5222d;
}

.url-params {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 8px;
}

.param-item {
  display: flex;
  justify-content: space-between;
  padding: 6px 10px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.param-key {
  font-weight: 500;
  color: #1890ff;
}

.param-value {
  color: #262626;
  word-break: break-all;
}

.storage-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.storage-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.storage-keys h3 {
  margin-bottom: 8px;
  color: #595959;
}

.storage-key {
  padding: 4px 8px;
  margin-bottom: 4px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
  font-family: monospace;
  font-size: 12px;
  color: #262626;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  background: white;
  padding: 12px;
  margin-bottom: 12px;
}

.log-item {
  display: flex;
  gap: 8px;
  margin-bottom: 4px;
  font-family: monospace;
  font-size: 12px;
}

.log-time {
  color: #8c8c8c;
  min-width: 80px;
}

.log-level {
  min-width: 60px;
  font-weight: 500;
}

.log-level.info {
  color: #1890ff;
}

.log-level.success {
  color: #52c41a;
}

.log-level.error {
  color: #f5222d;
}

.log-message {
  color: #262626;
  flex: 1;
}
</style> 