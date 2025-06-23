import {ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import { userManager } from './oidc'
import type { User } from 'oidc-client-ts'
import { jwtVerify, createRemoteJWKSet } from 'jose'
const metadata = await fetch('http://localhost:9000/.well-known/openid-configuration').then(res => res.json());
const JWKS = createRemoteJWKSet(new URL(metadata.jwks_uri));
export async function verifyAccessToken(token: string) {
  try {
    console.log(JWKS);
    const { payload, protectedHeader } = await jwtVerify(token, JWKS, {
      algorithms: ['RS256'],
    })
    console.log('🧾 JWT Header:', protectedHeader)
    console.log('✅ JWT Payload:', payload)
    return payload
  } catch (err) {
    console.error('❌ JWT 验证失败:', err)
    return null
  }
}
export interface AuthContext {
  user: Ref<User | null>
  isAuthenticated: ComputedRef<boolean>
  login: () => Promise<void>
  logout: () => Promise<void>
  getAccessToken: () => Promise<string | null>
  fetchWithAuth: (url: string, options?: RequestInit) => Promise<Response>
}

const user = ref<User | null>(null)
const isAuthenticated = computed(() => !!user.value && !user.value.expired)

// 顶层定义，避免 useAuth() 调用循环引用
export const login = async () => {
  await userManager.signinRedirect({
    state: {
      returnUrl: window.location.pathname + window.location.search,
    },
  })
}
export const logout = async () => {
  await userManager.removeUser() // 本地清除 user
  user.value = null
  await userManager.signoutRedirect()
}

let renewInProgress = false
const FORCE_LOGOUT_ON_RENEW_FAIL = true
async function safeSilentRenew() {
  if (renewInProgress) return null
  renewInProgress = true
  try {
    const renewed = await userManager.signinSilent()
    user.value = renewed
    console.log('🔁 Silent Renew 成功') // ✅ 这里打印
    return renewed
  } catch (e) {
    console.error('[OIDC] Silent renew failed:', e)
    if (FORCE_LOGOUT_ON_RENEW_FAIL) {
      await userManager.removeUser()
      user.value = null
      window.location.href = '/login'
    }
    return null
  } finally {
    renewInProgress = false
  }
}

// 初始化恢复用户状态
export async function initAuth() {
  try {
    const u = await userManager.getUser()
    if (u && !u.expired) {
      user.value = u
    } else {
      await safeSilentRenew()
    }
  } catch {
    user.value = null
  }
}

// 提供 Vue 组件中使用的 Auth API
export function useAuth(): AuthContext {

  const getAccessToken = async () => {
    if (!user.value || user.value.expired) {
      const renewed = await safeSilentRenew()
      if (!renewed) return null
    }
    return user.value?.access_token || null
  }
  const fetchWithAuth = async (url: string, options: RequestInit = {}) => {
    const token = await getAccessToken()
    if (!token) throw new Error('Not authenticated')

    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 8000)

    try {
      return await fetch(url, {
        ...options,
        signal: controller.signal,
        headers: {
          ...options.headers,
          Authorization: `Bearer ${token}`,
        },
      })
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        console.error('[Auth] Fetch timeout')
      } else if (!navigator.onLine) {
        console.error('[Auth] Network offline')
      } else if (err instanceof Error) {
        console.error('[Auth] Fetch error', err)
      } else {
        console.error('[Auth] Unknown fetch error', err)
      }
      throw err
    } finally {
      clearTimeout(timeout)
    }
  }

  return {
    user,
    isAuthenticated,
    login,
    logout,
    getAccessToken,
    fetchWithAuth,
  }
}

// 初始化调用（模块加载即执行）
export const initPromise = initAuth()

// OIDC 事件监听
userManager.events.addUserLoaded((u) => {
  console.debug('[OIDC] User loaded:', u)
  user.value = u
  //console.log(u.access_token);
  verifyAccessToken(u.access_token);
})

userManager.events.addUserUnloaded(() => {
  console.debug('[OIDC] User unloaded')
  user.value = null
})

userManager.events.addSilentRenewError((err) => {
  console.error('[OIDC] Silent renew error:', err)
})

userManager.events.addUserSignedOut(() => {
  console.debug('[OIDC] User signed out')
  user.value = null
  // 可选：跳转登录页
  window.location.href = '/login'
})
userManager.events.addAccessTokenExpiring(async () => {
  const secondsLeft = user.value?.expires_in ?? 0
  console.debug(`[OIDC] Token expiring in ${secondsLeft}s`)
  if (secondsLeft <= 60) {
    await safeSilentRenew()
  }
})
