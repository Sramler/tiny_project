// 认证相关工具函数
import { userManager } from '@/auth/oidc'
import { persistentLogger } from '@/utils/logger'

/**
 * 清理所有 OIDC 相关的本地缓存
 * 用于解决登录重复刷新问题
 */
export async function clearOidcCache() {
  try {
    persistentLogger.info('[OIDC] 开始清理本地缓存', { url: window.location.href })

    // 清理 localStorage 中的 OIDC 相关数据
    const keysToRemove: string[] = []
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i)
      if (key && (key.includes('oidc') || key.includes('user') || key.includes('auth'))) {
        keysToRemove.push(key)
      }
    }

    keysToRemove.forEach((key) => {
      localStorage.removeItem(key)
      persistentLogger.debug('[OIDC] 删除 localStorage 项', { key })
    })

    // 清理 sessionStorage
    const sessionKeysToRemove: string[] = []
    for (let i = 0; i < sessionStorage.length; i++) {
      const key = sessionStorage.key(i)
      if (key && (key.includes('oidc') || key.includes('user') || key.includes('auth'))) {
        sessionKeysToRemove.push(key)
      }
    }

    sessionKeysToRemove.forEach((key) => {
      sessionStorage.removeItem(key)
      persistentLogger.debug('[OIDC] 删除 sessionStorage 项', { key })
    })

    // 清理 userManager 中的用户数据
    await userManager.removeUser()

    persistentLogger.info('[OIDC] 本地缓存清理完成')
    return true
  } catch (error) {
    persistentLogger.error('[OIDC] 本地缓存清理失败', error)
    return false
  }
}

/**
 * 检查当前是否在 OIDC 流程中
 */
export function isInOidcFlow(): boolean {
  const urlParams = new URLSearchParams(window.location.search)
  return urlParams.has('code') || urlParams.has('error') || urlParams.has('state')
}

/**
 * 检查用户是否已认证
 */
export async function checkUserAuthenticated(): Promise<boolean> {
  try {
    const user = await userManager.getUser()
    return !!(user && !user.expired)
  } catch (error) {
    persistentLogger.error('[OIDC] 检查用户认证状态失败', error)
    return false
  }
}

/**
 * 获取当前页面的返回 URL
 */
export function getReturnUrl(): string {
  const urlParams = new URLSearchParams(window.location.search)
  return urlParams.get('returnUrl') || window.location.pathname + window.location.search
}
