// 认证相关工具函数
import { userManager } from '@/auth/oidc'

/**
 * 清理所有 OIDC 相关的本地缓存
 * 用于解决登录重复刷新问题
 */
export async function clearOidcCache() {
  try {
    console.log('🧹 开始清理 OIDC 缓存...')

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
      console.log('删除缓存项:', key)
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
      console.log('删除会话缓存项:', key)
    })

    // 清理 userManager 中的用户数据
    await userManager.removeUser()

    console.log('✅ OIDC 缓存清理完成')
    return true
  } catch (error) {
    console.error('❌ 清理 OIDC 缓存失败:', error)
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
    console.error('检查用户认证状态失败:', error)
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

/**
 * 防抖函数，用于防止重复操作
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
): (...args: Parameters<T>) => void {
  let timeout: number | null = null

  return (...args: Parameters<T>) => {
    if (timeout) {
      clearTimeout(timeout)
    }
    timeout = window.setTimeout(() => func(...args), wait)
  }
}

/**
 * 节流函数，用于限制操作频率
 */
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number,
): (...args: Parameters<T>) => void {
  let inThrottle: boolean = false

  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => (inThrottle = false), limit)
    }
  }
}
