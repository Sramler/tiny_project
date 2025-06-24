import { ref, onUnmounted } from 'vue'

/**
 * 创建一个节流函数，在指定延迟时间内最多执行一次。
 * @param fn 要节流的函数
 * @param delay 延迟时间（毫秒），默认为 500ms
 * @returns 节流后的函数
 */
export function useThrottleFn<T extends (...args: any[]) => any>(fn: T, delay = 500) {
  // 用于存储定时器的引用
  const timer = ref<ReturnType<typeof setTimeout> | null>(null)
  // 标记是否正在节流中
  const isThrottled = ref(false)

  // 节流处理函数
  const throttledFn = (...args: Parameters<T>) => {
    // 如果正在节流中，则直接返回，不执行函数
    if (isThrottled.value) {
      return
    }

    // 执行传入的函数
    fn(...args)
    // 进入节流状态
    isThrottled.value = true

    // 设置定时器，在延迟时间后恢复可执行状态
    timer.value = setTimeout(() => {
      isThrottled.value = false
      timer.value = null
    }, delay)
  }

  // 在组件卸载时，清除可能存在的定时器，防止内存泄漏
  onUnmounted(() => {
    if (timer.value) {
      clearTimeout(timer.value)
    }
  })

  // 返回节流后的函数
  return throttledFn
}
