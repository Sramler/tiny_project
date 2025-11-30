/**
 * OIDC 客户端配置
 * 所有可调项均通过环境变量注入，详见 docs/ENV_CONFIG.md。
 * 该文件在模块加载阶段完成配置校验，确保生产环境必须显式提供关键地址。
 */
import { UserManager, WebStorageStateStore } from 'oidc-client-ts'
import type { UserManagerSettings } from 'oidc-client-ts'
import { logger } from '@/utils/logger'

type Env = {
  VITE_OIDC_AUTHORITY?: string
  VITE_OIDC_CLIENT_ID?: string
  VITE_OIDC_REDIRECT_URI?: string
  VITE_OIDC_POST_LOGOUT_REDIRECT_URI?: string
  VITE_OIDC_SILENT_REDIRECT_URI?: string
  VITE_OIDC_SCOPES?: string
  VITE_OIDC_STORAGE?: 'local' | 'session'
}

const env = import.meta.env as Env
const isProd = import.meta.env.PROD

/**
 * 统一解析 env，支持默认值与生产环境强制校验。
 */
const resolveEnvValue = (
  value: string | undefined,
  fallback: string,
  options: { key: keyof Env; requiredInProd?: boolean } | null = null,
): string => {
  if (value) {
    return value
  }

  const key = options?.key
  if (key) {
    const warning = `[OIDC][config] ${key} 未配置，使用默认值 ${fallback}`
    if (isProd && options?.requiredInProd) {
      logger.error(`${warning}（生产环境必须显式配置）`)
      throw new Error(warning)
    }
    logger.warn(warning)
  }

  return fallback
}

const authority = resolveEnvValue(env.VITE_OIDC_AUTHORITY, 'http://localhost:9000', {
  key: 'VITE_OIDC_AUTHORITY',
  requiredInProd: true,
})
const clientId = resolveEnvValue(env.VITE_OIDC_CLIENT_ID, 'vue-client', {
  key: 'VITE_OIDC_CLIENT_ID',
  requiredInProd: true,
})
const redirectUri = resolveEnvValue(env.VITE_OIDC_REDIRECT_URI, 'http://localhost:5173/callback', {
  key: 'VITE_OIDC_REDIRECT_URI',
  requiredInProd: true,
})
const postLogoutRedirectUri = resolveEnvValue(
  env.VITE_OIDC_POST_LOGOUT_REDIRECT_URI,
  'http://localhost:5173/',
  {
    key: 'VITE_OIDC_POST_LOGOUT_REDIRECT_URI',
    requiredInProd: true,
  },
)
const silentRedirectUri = resolveEnvValue(
  env.VITE_OIDC_SILENT_REDIRECT_URI,
  'http://localhost:5173/silent-renew.html',
  {
    key: 'VITE_OIDC_SILENT_REDIRECT_URI',
    requiredInProd: true,
  },
)
const scopes = resolveEnvValue(env.VITE_OIDC_SCOPES, 'openid profile offline_access', {
  key: 'VITE_OIDC_SCOPES',
})

/**
 * 根据 `VITE_OIDC_STORAGE` 选择 localStorage 或 sessionStorage。
 * SSR/单测场景下 window 不存在时自动跳过，避免构建失败。
 */
const createOidcStore = () => {
  if (typeof window === 'undefined') {
    logger.warn('[OIDC][config] window 未定义，跳过 WebStorageStateStore 初始化')
    return undefined
  }
  const storagePreference = env.VITE_OIDC_STORAGE === 'session' ? 'session' : 'local'
  const store = storagePreference === 'session' ? window.sessionStorage : window.localStorage
  logger.info(`[OIDC][config] 使用 ${storagePreference}Storage 作为 OIDC state store`)
  return new WebStorageStateStore({ store })
}

const userStore = createOidcStore()

const baseSettings: UserManagerSettings = {
  authority,
  client_id: clientId,
  redirect_uri: redirectUri,
  post_logout_redirect_uri: postLogoutRedirectUri,
  response_type: 'code',
  scope: scopes,
  // 如果后端未开启 UserInfo 端点，或 profile 信息已有 ID Token/claims，关闭该项可减少一次请求
  loadUserInfo: true,
  automaticSilentRenew: true,
  silent_redirect_uri: silentRedirectUri,
}

if (userStore) {
  baseSettings.userStore = userStore
}

export type OidcSettings = Readonly<UserManagerSettings>

export const settings: OidcSettings = Object.freeze(baseSettings)

export const userManager = new UserManager(settings)
