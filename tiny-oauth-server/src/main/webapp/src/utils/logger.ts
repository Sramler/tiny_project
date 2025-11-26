/**
 * Logger 工具类
 * 通过环境变量控制日志输出
 * 
 * 环境变量：
 * - VITE_APP_ENV: 应用环境 (dev | test | prod) (默认: dev)
 * - VITE_ENABLE_CONSOLE_LOG: 是否启用 console.log (默认: true)
 * - VITE_ENABLE_CONSOLE_DEBUG: 是否启用 console.debug (默认: 非生产环境)
 * - VITE_ENABLE_CONSOLE_WARN: 是否启用 console.warn (默认: true)
 * - VITE_ENABLE_CONSOLE_ERROR: 是否启用 console.error (默认: true)
 * - VITE_LOG_LEVEL: 日志级别 (debug | info | warn | error | none)
 * - VITE_ENABLE_PERSISTENT_LOG: 是否启用持久化日志（保存到 localStorage）(默认: 非生产环境)
 */

// 从环境变量获取配置
const isProd = import.meta.env.PROD
const appEnv = import.meta.env.VITE_APP_ENV || 'dev' // 默认 dev 环境

// 日志级别配置
const LOG_LEVELS = {
  debug: 0,
  info: 1,
  warn: 2,
  error: 3,
  none: 999,
} as const

type LogLevel = keyof typeof LOG_LEVELS

// 获取配置的环境变量
const getEnvBool = (key: string, defaultValue: boolean): boolean => {
  const value = import.meta.env[key]
  if (value === undefined) return defaultValue
  return value === 'true' || value === true
}

const getEnvString = (key: string, defaultValue: string): string => {
  const value = import.meta.env[key]
  return value || defaultValue
}

// 日志开关配置
const ENABLE_LOG = getEnvBool('VITE_ENABLE_CONSOLE_LOG', true)
const ENABLE_DEBUG = getEnvBool('VITE_ENABLE_CONSOLE_DEBUG', !isProd) // 非生产环境默认启用
const ENABLE_WARN = getEnvBool('VITE_ENABLE_CONSOLE_WARN', true)
const ENABLE_ERROR = getEnvBool('VITE_ENABLE_CONSOLE_ERROR', true)

// 日志级别配置
const LOG_LEVEL = getEnvString('VITE_LOG_LEVEL', !isProd ? 'debug' : 'info').toLowerCase() as LogLevel
const currentLogLevel = LOG_LEVELS[LOG_LEVEL] ?? LOG_LEVELS.info

// 检查是否应该输出指定级别的日志
const shouldLog = (level: LogLevel): boolean => {
  const levelValue = LOG_LEVELS[level]
  if (levelValue === undefined) return false
  
  // 如果日志级别设置为 none，不输出任何日志
  if (currentLogLevel === LOG_LEVELS.none) return false
  
  // 如果当前配置的日志级别高于目标级别，不输出
  if (currentLogLevel > levelValue) return false
  
  // 检查开关
  switch (level) {
    case 'debug':
      return ENABLE_DEBUG
    case 'info':
      return ENABLE_LOG
    case 'warn':
      return ENABLE_WARN
    case 'error':
      return ENABLE_ERROR
    default:
      return false
  }
}

/**
 * Logger 类
 * 提供统一的日志输出接口
 */
export const logger = {
  /**
   * 输出调试信息（仅在开发环境默认启用）
   */
  debug(...args: any[]): void {
    if (shouldLog('debug')) {
      console.debug(...args)
    }
  },

  /**
   * 输出普通信息
   */
  log(...args: any[]): void {
    if (shouldLog('info')) {
      console.log(...args)
    }
  },

  /**
   * 输出信息（与 log 相同）
   */
  info(...args: any[]): void {
    if (shouldLog('info')) {
      console.info(...args)
    }
  },

  /**
   * 输出警告信息
   */
  warn(...args: any[]): void {
    if (shouldLog('warn')) {
      console.warn(...args)
    }
  },

  /**
   * 输出错误信息
   */
  error(...args: any[]): void {
    if (shouldLog('error')) {
      console.error(...args)
    }
  },

  /**
   * 分组输出（仅在启用时使用）
   */
  group(...args: any[]): void {
    if (shouldLog('debug')) {
      console.group(...args)
    }
  },

  /**
   * 结束分组
   */
  groupEnd(): void {
    if (shouldLog('debug')) {
      console.groupEnd()
    }
  },

  /**
   * 表格输出（仅在启用时使用）
   */
  table(...args: any[]): void {
    if (shouldLog('debug')) {
      console.table(...args)
    }
  },
}

/**
 * 持久化日志工具
 * 将日志保存到 localStorage，避免页面跳转时丢失
 */
const PERSISTENT_LOG_KEY = 'app_debug_logs'
const MAX_LOG_ENTRIES = 100 // 最多保存 100 条日志

// 持久化日志开关配置（默认在非生产环境启用）
const ENABLE_PERSISTENT_LOG = getEnvBool('VITE_ENABLE_PERSISTENT_LOG', !isProd)

export interface LogEntry {
  timestamp: string
  level: string
  message: string
  data?: any
  url?: string
  status?: number
}

function saveToPersistentLog(level: string, message: string, data?: any, url?: string, status?: number): void {
  // 如果持久化日志未启用，直接返回
  if (!ENABLE_PERSISTENT_LOG) {
    return
  }

  try {
    const logs = getPersistentLogs()
    const entry: LogEntry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      data: data ? JSON.stringify(data, null, 2) : undefined,
      url,
      status,
    }
    logs.unshift(entry)
    // 限制日志数量
    if (logs.length > MAX_LOG_ENTRIES) {
      logs.splice(MAX_LOG_ENTRIES)
    }
    localStorage.setItem(PERSISTENT_LOG_KEY, JSON.stringify(logs))
  } catch (error) {
    // localStorage 可能已满或不可用，忽略错误
    console.warn('保存持久化日志失败:', error)
  }
}

export function getPersistentLogs(): LogEntry[] {
  try {
    const logsJson = localStorage.getItem(PERSISTENT_LOG_KEY)
    return logsJson ? JSON.parse(logsJson) : []
  } catch (error) {
    return []
  }
}

export function clearPersistentLogs(): void {
  try {
    localStorage.removeItem(PERSISTENT_LOG_KEY)
  } catch (error) {
    console.warn('清空持久化日志失败:', error)
  }
}

export function exportPersistentLogs(): string {
  const logs = getPersistentLogs()
  return JSON.stringify(logs, null, 2)
}

/**
 * 增强的 logger，支持持久化日志
 */
export const persistentLogger = {
  debug(message: string, data?: any, url?: string, status?: number): void {
    if (shouldLog('debug')) {
      console.debug(message, data)
      saveToPersistentLog('debug', message, data, url, status)
    }
  },

  log(message: string, data?: any, url?: string, status?: number): void {
    if (shouldLog('info')) {
      console.log(message, data)
      saveToPersistentLog('info', message, data, url, status)
    }
  },

  info(message: string, data?: any, url?: string, status?: number): void {
    if (shouldLog('info')) {
      console.info(message, data)
      saveToPersistentLog('info', message, data, url, status)
    }
  },

  warn(message: string, data?: any, url?: string, status?: number): void {
    if (shouldLog('warn')) {
      console.warn(message, data)
      saveToPersistentLog('warn', message, data, url, status)
    }
  },

  error(message: string, data?: any, url?: string, status?: number): void {
    if (shouldLog('error')) {
      console.error(message, data)
      saveToPersistentLog('error', message, data, url, status)
    }
  },
}

// 导出默认 logger 实例
export default logger

