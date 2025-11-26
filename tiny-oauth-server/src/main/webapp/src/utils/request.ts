// HTTP请求工具
import axios from 'axios'
import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  InternalAxiosRequestConfig,
} from 'axios'
// 引入 auth.ts 中的认证方法
import { useAuth, logout } from '@/auth/auth'
import router from '@/router' // 引入路由实例
// 引入 TRACE_ID 工具
import { getOrCreateTraceId, generateRequestId } from '@/utils/traceId'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000/', // 从环境变量获取API基础URL
  timeout: 5000, // 请求超时时间（5秒，缩短以快速检测后端不可用）
  headers: {
    'Content-Type': 'application/json',
  },
})

// 防抖：避免多个请求同时触发多次跳转
let redirectTimer: ReturnType<typeof setTimeout> | null = null
const REDIRECT_DELAY = 200 // 200ms 内的多个错误只触发一次跳转

// 请求拦截器
service.interceptors.request.use(
  // 将拦截器设为异步，以便调用异步的 getAccessToken
  async (config: InternalAxiosRequestConfig) => {
    // 在发送请求之前做些什么
    console.log('发送请求:', config.url, config.method)

    // 添加 TRACE_ID 到请求头
    // 后端支持的 header 名称（按优先级）：
    // - traceparent
    // - x-b3-traceid
    // - x-trace-id
    // - trace-id
    // - x-request-id
    const traceId = getOrCreateTraceId()
    const requestId = generateRequestId()
    
    // 优先使用 x-trace-id（与后端标准一致）
    config.headers['X-Trace-Id'] = traceId
    // 同时添加 x-request-id，后端会使用它作为 fallback
    config.headers['X-Request-Id'] = requestId

    // 调用 getAccessToken 动态获取有效 token
    const { getAccessToken } = useAuth()
    const token = await getAccessToken()

    if (token) {
      // 如果获取到 token，则添加到请求头中
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => {
    // 对请求错误做些什么
    console.error('请求错误:', error)
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 对响应数据做点什么
    const requestId = response.headers['x-request-id'] || response.headers['X-Request-Id']
    const traceId = response.headers['x-trace-id'] || response.headers['X-Trace-Id']
    
    // 记录 TRACE_ID 和 Request ID（可选：用于调试）
    if (import.meta.env.DEV) {
      console.log('收到响应:', response.config.url, response.status, {
        requestId,
        traceId,
      })
    }

    const { data } = response

    // 如果响应成功，直接返回数据
    if (response.status >= 200 && response.status < 300) {
      return data
    }

    // 如果响应失败，抛出错误
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  async (error) => {
    // 对响应错误做点什么
    console.error('响应错误:', error)

    // 处理401未授权错误
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || 'unknown'
      console.log('[401] axios 拦截器检测到 401 错误，URL:', requestUrl)
      
      // 记录持久化日志（避免302跳转清空控制台）
      import('@/utils/logger').then(({ persistentLogger }) => {
        persistentLogger.warn('[401] axios 拦截器检测到 401 错误', {
          url: requestUrl,
          method: error.config?.method,
          headers: error.config?.headers,
          timestamp: new Date().toISOString(),
        }, requestUrl, 401)
      })
      
      // ⚠️ 重要：先跳转到 401 页面，不要先调用 logout()
      // 因为 logout() 会触发 window.location.href，会覆盖我们的跳转
      // 只在当前路由不是 /login、/callback 或 /exception/401 时跳转，避免死循环
      const currentPath = router.currentRoute.value.path
      console.log('[401] 当前路径:', currentPath)
      if (
        currentPath !== '/login' &&
        currentPath !== '/callback' &&
        currentPath !== '/exception/401'
      ) {
        console.log('[401] 跳转到 401 页面（认证状态将在 401 页面中清理）')
        
        // 延迟跳转，给时间查看日志（开发环境）
        if (import.meta.env.DEV) {
          await new Promise(resolve => setTimeout(resolve, 100))
        }
        
        // 使用 window.location 跳转，与 fetchWithTraceId 保持一致
        // 注意：不要在这里调用 logout()，因为它会覆盖这个跳转
        // logout() 会在 401 页面加载后由页面自己处理
        window.location.href = '/exception/401'
      } else {
        console.log('[401] 已在目标页面，跳过跳转')
        // 如果已经在 401 页面，可以安全地调用 logout
        try {
      await logout()
        } catch (error) {
          console.error('[401] 注销失败:', error)
        }
      }
      return Promise.reject(error)
    }

    // 处理网络错误（后端不可用、连接失败、超时等）
    const isNetworkError =
      !error.response && // 没有响应，说明请求未到达服务器
      (error.code === 'ECONNABORTED' || // 超时
        error.code === 'ERR_NETWORK' || // 网络错误
        error.code === 'ERR_CONNECTION_REFUSED' || // 连接被拒绝
        error.message?.includes('timeout') || // 超时
        error.message?.includes('Network Error') || // 网络错误
        error.message?.includes('Failed to fetch')) // 获取失败

    if (isNetworkError) {
      console.error('后端服务不可用，跳转到登录页')
      // 只在当前路由不是 /login 或 /callback 时跳转，避免死循环
      const currentPath = router.currentRoute.value.path
      if (currentPath !== '/login' && currentPath !== '/callback') {
        // 防抖：清除之前的定时器，只保留最后一个
        if (redirectTimer) {
          clearTimeout(redirectTimer)
        }
        redirectTimer = setTimeout(() => {
        router.replace('/login')
          redirectTimer = null
        }, REDIRECT_DELAY)
      }
    }

    // 处理其他错误
    const message = error.response?.data?.message || error.message || '网络错误'
    console.error('请求失败:', message)

    return Promise.reject(error)
  },
)

// 封装请求方法
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config)
  },

  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config)
  },

  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config)
  },

  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config)
  },

  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return service.patch(url, data, config)
  },
}

export default request
