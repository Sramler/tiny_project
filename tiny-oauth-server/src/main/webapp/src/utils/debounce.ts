import { onUnmounted } from 'vue'

type AnyFn = (...args: unknown[]) => unknown
type RegisterCleanup = (cleanup: () => void) => void

function createThrottle<T extends AnyFn>(fn: T, delay = 500, registerCleanup?: RegisterCleanup) {
  let lastCall = 0
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  const clearTimer = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  registerCleanup?.(clearTimer)

  const invoke = (...args: Parameters<T>) => {
    lastCall = Date.now()
    timeoutId = null
    fn(...args)
  }

  return (...args: Parameters<T>) => {
    const now = Date.now()
    const remaining = delay - (now - lastCall)

    if (remaining <= 0) {
      clearTimer()
      invoke(...args)
      return
    }

    if (!timeoutId) {
      timeoutId = setTimeout(() => invoke(...args), remaining)
    }
  }
}

function createDebounce<T extends AnyFn>(fn: T, delay = 300, registerCleanup?: RegisterCleanup) {
  let timeoutId: ReturnType<typeof setTimeout> | null = null

  const clearTimer = () => {
    if (timeoutId) {
      clearTimeout(timeoutId)
      timeoutId = null
    }
  }

  registerCleanup?.(clearTimer)

  return (...args: Parameters<T>) => {
    clearTimer()
    timeoutId = setTimeout(() => {
      timeoutId = null
      fn(...args)
    }, delay)
  }
}

/**
 * 通用节流函数（无 Vue 依赖）
 */
export function throttle<T extends AnyFn>(fn: T, delay = 500) {
  return createThrottle(fn, delay)
}

/**
 * 通用防抖函数（无 Vue 依赖）
 */
export function debounce<T extends AnyFn>(fn: T, delay = 300) {
  return createDebounce(fn, delay)
}

/**
 * 组件内使用的节流函数，会在组件卸载时自动清理定时器
 */
export function useThrottle<T extends AnyFn>(fn: T, delay = 500) {
  const cleanupList: Array<() => void> = []
  const throttled = createThrottle(fn, delay, (cleanup) => cleanupList.push(cleanup))

  onUnmounted(() => {
    cleanupList.forEach((cleanup) => cleanup())
  })

  return throttled
}

/**
 * 组件内使用的防抖函数，会在组件卸载时自动清理定时器
 */
export function useDebounce<T extends AnyFn>(fn: T, delay = 300) {
  const cleanupList: Array<() => void> = []
  const debounced = createDebounce(fn, delay, (cleanup) => cleanupList.push(cleanup))

  onUnmounted(() => {
    cleanupList.forEach((cleanup) => cleanup())
  })

  return debounced
}
