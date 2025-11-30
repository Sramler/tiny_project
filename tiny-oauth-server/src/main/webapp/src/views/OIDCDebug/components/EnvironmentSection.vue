<template>
  <div class="debug-section">
    <h2>用户 Claims & 环境信息</h2>
    <div class="token-json">
      <div>
        <h3>用户 Claims</h3>
        <pre class="json-viewer">{{ prettyJson(userClaims) }}</pre>
      </div>
      <div>
        <h3>环境信息</h3>
        <div class="env-grid">
          <div class="env-item">
            <span class="label">Origin</span>
            <code class="token-value">{{ environmentInfo.origin }}</code>
          </div>
          <div class="env-item">
            <span class="label">URL</span>
            <code class="token-value">{{ environmentInfo.href }}</code>
          </div>
          <div class="env-item">
            <span class="label">在线状态</span>
            <code class="token-value">{{ environmentInfo.online ? '在线' : '离线' }}</code>
          </div>
          <div class="env-item">
            <span class="label">Cookie 长度</span>
            <code class="token-value">{{ environmentInfo.cookieSize }}</code>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface EnvironmentInfo {
  origin: string
  href: string
  userAgent: string
  online: boolean
  cookieSize: number
}

defineProps<{
  userClaims: Record<string, unknown>
  environmentInfo: EnvironmentInfo
}>()

const prettyJson = (value: unknown) => {
  if (!value) return '无'
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}
</script>

