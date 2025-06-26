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

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000/', // 从环境变量获取API基础URL
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
service.interceptors.request.use(
  // 将拦截器设为异步，以便调用异步的 getAccessToken
  async (config: InternalAxiosRequestConfig) => {
    // 在发送请求之前做些什么
    console.log('发送请求:', config.url, config.method)

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
    console.log('收到响应:', response.config.url, response.status)

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
      // 调用统一的 logout 方法，处理认证失败后的逻辑
      await logout()
      // 只在当前路由不是 /login 或 /callback 时跳转，避免死循环
      const currentPath = router.currentRoute.value.path
      if (currentPath !== '/login' && currentPath !== '/callback') {
        router.replace('/login')
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
