<template>
  <div class="debug-section">
    <h2>Token 信息</h2>
    <div class="token-grid">
      <div class="token-item">
        <div class="token-label">Access Token</div>
        <code class="token-value">{{ formatToken(accessToken) }}</code>
        <a class="token-action" @click="toggleToken('access')">
          {{ tokenVisibility.access ? '隐藏' : '展开' }}
        </a>
      </div>
      <div class="token-item">
        <div class="token-label">ID Token</div>
        <code class="token-value">{{ formatToken(idToken, 'id') }}</code>
        <a class="token-action" @click="toggleToken('id')">
          {{ tokenVisibility.id ? '隐藏' : '展开' }}
        </a>
      </div>
      <div class="token-item">
        <div class="token-label">Refresh Token</div>
        <code class="token-value">{{ formatToken(refreshToken, 'refresh') }}</code>
        <a class="token-action" @click="toggleToken('refresh')" :disabled="!refreshToken">
          {{ tokenVisibility.refresh ? '隐藏' : '展开' }}
        </a>
      </div>
      <div class="token-item">
        <div class="token-label">Scopes</div>
        <code class="token-value">{{ scopes }}</code>
      </div>
      <div class="token-item">
        <div class="token-label">Token 类型</div>
        <code class="token-value">{{ sessionInfo.tokenType }}</code>
      </div>
      <div class="token-item">
        <div class="token-label">Session State</div>
        <code class="token-value">{{ sessionInfo.sessionState }}</code>
      </div>
    </div>
    <div class="token-json">
      <div>
        <h3>Access Token Payload</h3>
        <pre class="json-viewer">{{ prettyJson(decodedAccessToken) }}</pre>
      </div>
      <div>
        <h3>ID Token Payload</h3>
        <pre class="json-viewer">{{ prettyJson(decodedIdToken) }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'

interface SessionInfo {
  tokenType: string
  sessionState: string
}

const props = defineProps<{
  accessToken: string
  idToken: string
  refreshToken: string
  scopes: string
  sessionInfo: SessionInfo
  decodedAccessToken: unknown
  decodedIdToken: unknown
}>()

const tokenVisibility = reactive({
  access: false,
  id: false,
  refresh: false,
})

const toggleToken = (type: keyof typeof tokenVisibility) => {
  tokenVisibility[type] = !tokenVisibility[type]
}

const formatToken = (token: string, type: keyof typeof tokenVisibility = 'access') => {
  if (!token) return '无'
  if (tokenVisibility[type] || token.length <= 30) return token
  return `${token.slice(0, 12)}...${token.slice(-8)}`
}

const prettyJson = (value: unknown) => {
  if (!value) return '无'
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return String(value)
  }
}
</script>

