import { ref, computed } from 'vue'
import type { Ref, ComputedRef } from 'vue'
import { userManager } from './oidc'
import type { User } from 'oidc-client-ts'
import { jwtVerify, createRemoteJWKSet } from 'jose'

const metadata = await fetch('http://localhost:9000/.well-known/openid-configuration').then((res) =>
  res.json(),
)
const JWKS = createRemoteJWKSet(new URL(metadata.jwks_uri))

export async function verifyAccessToken(token: string) {
  try {
    console.log(JWKS)
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

// 防重复重定向标志
let loginInProgress = false
let lastLoginAttempt = 0
const LOGIN_COOLDOWN = 2000 // 2秒冷却时间

// 顶层定义，避免 useAuth() 调用循环引用
export const login = async () => {
  const now = Date.now()

  // 防止重复重定向 - 检查冷却时间
  if (loginInProgress || now - lastLoginAttempt < LOGIN_COOLDOWN) {
    console.log('登录重定向已在进行中或冷却期内，跳过重复操作')
    return
  }

  // 检查是否已经在 OIDC 流程中
  const currentUser = await userManager.getUser()
  if (currentUser && !currentUser.expired) {
    console.log('用户已认证，无需重定向')
    user.value = currentUser
    return
  }

  // 检查 URL 参数，避免重复重定向
  const urlParams = new URLSearchParams(window.location.search)
  if (urlParams.has('code') || urlParams.has('error')) {
    console.log('检测到 OIDC 回调参数，不进行重定向')
    return
  }

  // 检查是否已经在授权服务器页面
  if (window.location.href.includes('localhost:9000')) {
    console.log('已在授权服务器页面，不进行重定向')
    return
  }

  try {
    console.log('开始 OIDC 登录重定向')
    loginInProgress = true
    lastLoginAttempt = now

    await userManager.signinRedirect({
      state: {
        returnUrl: window.location.pathname + window.location.search,
      },
    })
  } catch (error) {
    console.error('OIDC 登录重定向失败:', error)
    loginInProgress = false
    throw error
  }
}

export const logout = async () => {
  await userManager.removeUser() // 本地清除 user
  user.value = null
  loginInProgress = false // 重置登录状态
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
      loginInProgress = false // 重置登录状态
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
    console.log('🔍 开始初始化认证状态...')

    // 检查是否在 OIDC 回调中
    const urlParams = new URLSearchParams(window.location.search)
    if (urlParams.has('code') || urlParams.has('error')) {
      console.log('检测到 OIDC 回调，跳过用户状态恢复')
      return
    }

    const u = await userManager.getUser()
    if (u && !u.expired) {
      user.value = u
      console.log('✅ 用户状态恢复成功')
    } else if (u && u.expired) {
      console.log('用户 token 已过期，尝试静默续期')
      await safeSilentRenew()
    } else {
      console.log('未找到用户状态，用户需要登录')
      user.value = null
    }
  } catch (error) {
    console.error('初始化认证状态失败:', error)
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
  loginInProgress = false // 重置登录状态
  //console.log(u.access_token);
  verifyAccessToken(u.access_token)
})

userManager.events.addUserUnloaded(() => {
  console.debug('[OIDC] User unloaded')
  user.value = null
  loginInProgress = false // 重置登录状态
})

userManager.events.addSilentRenewError((err) => {
  console.error('[OIDC] Silent renew error:', err)
})

userManager.events.addUserSignedOut(() => {
  console.debug('[OIDC] User signed out')
  user.value = null
  loginInProgress = false // 重置登录状态
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
