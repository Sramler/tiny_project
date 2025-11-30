/**
 * BPMN 翻译工具类
 * 提供类型安全和性能优化的翻译功能
 * 支持官方翻译兜底机制
 */

import {
  getMergedTranslations,
  loadOfficialTranslations as loadBpmnJsOfficialTranslations,
  isOfficialTranslationsLoaded,
} from '../i18n/bpmn-js'
import allTranslations, {
  bpmnJsTranslations,
  propertiesPanelTranslations,
  camundaPropertiesPanelTranslations,
  zeebePropertiesPanelTranslations,
} from '../i18n'
import type { TranslationMap } from '../i18n'

/**
 * 翻译工具类
 */
export class TranslateUtils {
  private static instance: TranslateUtils
  private customTranslations: TranslationMap = {}
  private officialTranslations: TranslationMap = {}
  private enableOfficialFallback: boolean = true
  private enableDebugLogs: boolean = false

  private constructor() {
    // 构造函数中不加载翻译，等待显式初始化
  }

  /**
   * 获取单例实例
   */
  public static getInstance(): TranslateUtils {
    if (!TranslateUtils.instance) {
      TranslateUtils.instance = new TranslateUtils()
    }
    return TranslateUtils.instance
  }

  /**
   * 调试日志输出
   */
  private debugLog(message: string, ...args: any[]): void {
    if (this.enableDebugLogs) {
      console.debug(message, ...args)
    }
  }

  /**
   * 初始化翻译系统
   */
  public async initialize(): Promise<void> {
    try {
      await this.loadOfficialTranslations()
      console.log('✅ 翻译系统已初始化')
    } catch (error) {
      console.error('❌ 翻译系统初始化失败:', error)
    }
  }

  /**
   * 加载官方翻译
   */
  private async loadOfficialTranslations(): Promise<void> {
    this.debugLog('🔄 开始加载官方翻译...')
    try {
      // 尝试同步加载官方翻译
      this.debugLog('📥 尝试同步加载官方翻译...')
      const syncLoaded = this.loadOfficialTranslationsSync()

      if (syncLoaded) {
        this.debugLog('✅ 官方翻译同步加载成功')
      } else {
        // 如果同步加载失败，尝试异步加载
        if (Object.keys(this.officialTranslations).length === 0) {
          this.debugLog('📥 同步加载失败，尝试异步加载官方翻译...')
          await this.loadOfficialTranslationsAsync()
          this.debugLog('✅ 官方翻译异步加载成功')
        }
      }

      const count = Object.keys(this.officialTranslations).length
      this.debugLog(`📊 官方翻译加载完成，共 ${count} 个翻译`)
    } catch (error) {
      console.error('❌ 加载官方翻译过程中出现错误:', error)
    }
  }

  /**
   * 同步加载官方翻译
   */
  private loadOfficialTranslationsSync(): boolean {
    try {
      // 方案1: 从全局变量加载
      if (typeof window !== 'undefined' && (window as any).bpmnOfficialTranslations) {
        Object.assign(this.officialTranslations, (window as any).bpmnOfficialTranslations)
        this.debugLog('✅ 从全局变量同步加载官方翻译成功')
        return true
      }

      // 方案2: 检查是否已经通过其他方式加载
      if (isOfficialTranslationsLoaded()) {
        const officialTranslations = getMergedTranslations()
        Object.assign(this.officialTranslations, officialTranslations)
        this.debugLog('✅ 从已加载的官方翻译同步获取成功')
        return true
      }

      // 方案3: 尝试直接导入（同步方式）
      try {
        // 在浏览器环境中，尝试从全局变量获取预加载的翻译
        if (typeof window !== 'undefined' && (window as any).bpmnOfficialTranslationsPreloaded) {
          Object.assign(
            this.officialTranslations,
            (window as any).bpmnOfficialTranslationsPreloaded,
          )
          this.debugLog('✅ 从预加载的官方翻译同步获取成功')
          return true
        }
      } catch (importError) {
        this.debugLog('⚠️ 同步导入官方翻译失败:', importError)
      }

      this.debugLog('⚠️ 同步加载官方翻译不可用，将使用异步加载')
      return false
    } catch (error) {
      this.debugLog('❌ 同步加载官方翻译失败，将尝试异步加载:', error)
      return false
    }
  }

  /**
   * 异步加载官方翻译
   */
  private async loadOfficialTranslationsAsync(): Promise<void> {
    try {
      console.log('📥 开始异步加载 BPMN.js 官方翻译包...')
      // 加载 BPMN.js 官方翻译包
      await loadBpmnJsOfficialTranslations()

      // 如果官方翻译已加载，更新本地存储
      if (isOfficialTranslationsLoaded()) {
        const officialTranslations = getMergedTranslations()
        Object.assign(this.officialTranslations, officialTranslations)
      } else {
        console.log('⚠️ 异步加载官方翻译包完成，但翻译未正确加载')
      }
    } catch (error) {
      console.error('❌ 异步加载官方翻译失败:', error)
    }
  }

  /**
   * 翻译文本
   * @param template 翻译模板
   * @param replacements 替换参数
   * @param context 调用上下文（可选）
   * @returns 翻译后的文本
   */
  public translate(
    template: string,
    replacements?: Record<string, any>,
    context?: {
      source?: string
      component?: string
    },
  ): string {
    try {
      replacements = replacements || {}
      if (context) {
        this.debugLog('🌐 翻译上下文:', context)
      }

      // 输入验证
      if (!template || typeof template !== 'string') {
        console.warn('❌ 无效的翻译键:', template)
        return String(template || '')
      }

      // 翻译优先级：官方翻译(中文基础) > 模块翻译(完善) > 临时翻译(补充) > BPMN.js默认英文(兜底)
      let translation = template
      let isTranslated = false

      if (this.enableOfficialFallback && this.officialTranslations[template]) {
        translation = this.officialTranslations[template]
        isTranslated = true
        this.debugLog(`🌍 [官方] ${template} -> ${translation}`)
        return this.replacePlaceholders(translation, replacements)
      }

      if (!isTranslated && allTranslations[template]) {
        translation = allTranslations[template]
        isTranslated = true

        // 确定具体是哪个模块提供的翻译
        let moduleName = 'unknown'
        if (template in bpmnJsTranslations) {
          moduleName = 'bpmn-js'
        } else if (template in propertiesPanelTranslations) {
          moduleName = 'properties-panel'
        } else if (template in camundaPropertiesPanelTranslations) {
          moduleName = 'camunda-properties-panel'
        } else if (template in zeebePropertiesPanelTranslations) {
          moduleName = 'zeebe-properties-panel'
        }

        this.debugLog(`📚 [${moduleName}] ${template} -> ${translation}`)
        return this.replacePlaceholders(translation, replacements)
      }

      if (!isTranslated && this.customTranslations[template]) {
        translation = this.customTranslations[template]
        isTranslated = true
        this.debugLog(`🎨 [临时] ${template} -> ${translation}`)
        return this.replacePlaceholders(translation, replacements)
      }

      if (!isTranslated) {
        const caseInsensitiveTranslation = this.findTranslationCaseInsensitive(template)
        if (caseInsensitiveTranslation) {
          translation = caseInsensitiveTranslation
          isTranslated = true
          this.debugLog(`🔍 使用大小写不敏感匹配: ${template} -> ${translation}`)
          return this.replacePlaceholders(translation, replacements)
        }
      }

      // 记录未翻译的键
      if (!isTranslated) {
        console.warn(`❌ 翻译键未找到: ${template}`)
      }

      // 替换占位符
      return this.replacePlaceholders(translation, replacements)
    } catch (error) {
      console.error('❌ 翻译过程中发生错误:', error, '翻译键:', template)
      // 容错处理：返回原文
      return String(template || '')
    }
  }

  /**
   * 替换占位符
   * @param text 文本
   * @param replacements 替换参数
   * @returns 替换后的文本
   */
  private replacePlaceholders(text: string, replacements: Record<string, any>): string {
    return text.replace(/{([^}]+)}/g, (_, key: string) => {
      return replacements[key] || `{${key}}`
    })
  }

  /**
   * 添加临时翻译
   * @param translations 临时翻译映射
   */
  public addCustomTranslations(translations: TranslationMap): void {
    console.log('🎨 开始添加临时翻译...')
    const beforeCount = Object.keys(this.customTranslations).length
    Object.assign(this.customTranslations, translations)
    const afterCount = Object.keys(this.customTranslations).length
    const addedCount = afterCount - beforeCount

    console.log(`✅ 添加临时翻译: +${addedCount}, 总计${afterCount}个`)
  }

  /**
   * 移除临时翻译
   * @param keys 要移除的翻译键数组
   */
  public removeCustomTranslations(keys: string[]): void {
    console.log('🗑️ 开始移除临时翻译...')
    const beforeCount = Object.keys(this.customTranslations).length
    keys.forEach((key) => {
      delete this.customTranslations[key]
    })
    const afterCount = Object.keys(this.customTranslations).length
    const removedCount = beforeCount - afterCount

    console.log(`✅ 移除临时翻译: -${removedCount}, 剩余${afterCount}个`)
  }

  /**
   * 检查翻译是否存在
   * @param key 翻译键
   * @returns 是否存在翻译
   */
  public hasTranslation(key: string): boolean {
    return (
      key in this.customTranslations || key in allTranslations || key in this.officialTranslations
    )
  }

  /**
   * 获取所有可用的翻译键
   * @returns 翻译键数组
   */
  public getAvailableKeys(): string[] {
    const allKeys = [
      ...Object.keys(allTranslations),
      ...Object.keys(this.customTranslations),
      ...Object.keys(this.officialTranslations),
    ]
    return Array.from(new Set(allKeys))
  }

  /**
   * 批量翻译
   * @param templates 翻译模板数组
   * @param replacements 替换参数
   * @returns 翻译后的文本数组
   */
  public translateBatch(templates: string[], replacements?: Record<string, any>): string[] {
    return templates.map((template) => this.translate(template, replacements))
  }

  /**
   * 获取翻译模块配置
   * @returns BPMN.js 翻译模块配置
   */
  public async getTranslateModule(showDebugInfo: boolean = false) {
    // 设置调试日志开关
    this.enableDebugLogs = showDebugInfo

    // 等待官方翻译加载完成
    await this.initialize().catch((error) => {
      if (this.enableDebugLogs) {
        console.error('自动初始化翻译系统失败:', error)
      }
    })

    return {
      translate: [
        'value',
        (template: string, replacements?: Record<string, any>) => {
          const result = this.translate(template, replacements)
          return result
        },
      ],
    }
  }

  /**
   * 启用/禁用官方翻译兜底
   * @param enable 是否启用
   */
  public setOfficialFallback(enable: boolean): void {
    this.enableOfficialFallback = enable
  }

  /**
   * 获取官方翻译兜底状态
   * @returns 是否启用
   */
  public isOfficialFallbackEnabled(): boolean {
    return this.enableOfficialFallback
  }

  /**
   * 启用/禁用调试日志
   * @param enable 是否启用
   */
  public setDebugLogs(enable: boolean): void {
    this.enableDebugLogs = enable
    console.log(`🔧 调试日志已${enable ? '启用' : '禁用'}`)
  }

  /**
   * 获取调试日志状态
   * @returns 是否启用
   */
  public isDebugLogsEnabled(): boolean {
    return this.enableDebugLogs
  }

  /**
   * 手动添加官方翻译
   * @param translations 官方翻译映射
   */
  public addOfficialTranslations(translations: TranslationMap): void {
    Object.assign(this.officialTranslations, translations)
    console.debug(`手动添加了 ${Object.keys(translations).length} 个官方翻译`)
  }

  /**
   * 获取翻译来源
   * @param key 翻译键
   * @returns 翻译来源
   */
  public getTranslationSource(key: string): 'custom' | 'local' | 'official' | 'none' {
    // 按照优先级顺序检查：官方 > 本地 > 自定义
    if (this.enableOfficialFallback && key in this.officialTranslations) return 'official'
    if (key in allTranslations) return 'local'
    if (key in this.customTranslations) return 'custom'
    return 'none'
  }

  /**
   * 大小写不敏感的翻译查找
   * @param key 翻译键
   * @returns 找到的翻译或 null
   */
  public findTranslationCaseInsensitive(key: string): string | null {
    // 按优先级查找：官方翻译 > 模块翻译 > 临时翻译

    // 1. 优先查找官方翻译
    const officialKeys = Object.keys(this.officialTranslations)
    const officialMatch = officialKeys.find((k) => k === key)
    if (officialMatch) {
      return this.officialTranslations[officialMatch]
    }

    // 2. 查找模块翻译
    const localKeys = Object.keys(allTranslations)
    const localMatch = localKeys.find((k) => k === key)
    if (localMatch) {
      return allTranslations[localMatch]
    }

    // 3. 查找临时翻译
    const customKeys = Object.keys(this.customTranslations)
    const customMatch = customKeys.find((k) => k === key)
    if (customMatch) {
      return this.customTranslations[customMatch]
    }

    // 4. 所有翻译源都没找到，返回 null
    return null
  }

  /**
   * 获取合并后的翻译（同步方式）
   */
  public getMergedTranslations(): TranslationMap {
    return {
      ...this.officialTranslations, // 官方翻译作为基础
      ...this.customTranslations, // 自定义翻译覆盖官方翻译
    }
  }

  /**
   * 导入翻译数据
   * @param data 翻译数据
   */
  public importTranslations(data: { official?: TranslationMap; custom?: TranslationMap }): void {
    if (data.official) {
      this.officialTranslations = { ...data.official }
    }
    if (data.custom) {
      this.customTranslations = { ...data.custom }
    }

    console.log('📥 翻译数据导入完成')
  }
}

// 导出单例实例
export const translateUtils = TranslateUtils.getInstance()

// 导出便捷函数
export const translate = (
  template: string,
  replacements?: Record<string, any>,
  context?: {
    source?: string
    component?: string
  },
) => translateUtils.translate(template, replacements, context)

export const addCustomTranslations = (translations: TranslationMap) =>
  translateUtils.addCustomTranslations(translations)

export const getTranslateModule = async (showDebugInfo?: boolean) =>
  await translateUtils.getTranslateModule(showDebugInfo)

export const setOfficialFallback = (enable: boolean) => translateUtils.setOfficialFallback(enable)

export const addOfficialTranslations = (translations: TranslationMap) =>
  translateUtils.addOfficialTranslations(translations)

export const getTranslationSource = (key: string) => translateUtils.getTranslationSource(key)

export const findTranslationCaseInsensitive = (key: string) =>
  translateUtils.findTranslationCaseInsensitive(key)

export const getAvailableKeys = () => translateUtils.getAvailableKeys()

export const setDebugLogs = (enable: boolean) => translateUtils.setDebugLogs(enable)

export const isDebugLogsEnabled = () => translateUtils.isDebugLogsEnabled()

export const importTranslations = (data: any) => translateUtils.importTranslations(data)

// 全局上下文管理
let globalTranslationContext = {
  source: 'unknown',
  component: 'Unknown',
  page: 'unknown',
}

/**
 * 设置全局翻译上下文
 */
export function setGlobalTranslationContext(context: {
  source?: string
  component?: string
  page?: string
}): void {
  Object.assign(globalTranslationContext, context)
}

/**
 * 获取全局翻译上下文
 */
export function getGlobalTranslationContext() {
  return { ...globalTranslationContext }
}

/**
 * 清除全局翻译上下文
 */
export function clearGlobalTranslationContext(): void {
  globalTranslationContext = {
    source: 'unknown',
    component: 'Unknown',
    page: 'unknown',
  }
}

// 简化的翻译对象
export const bpmnTranslations = {
  // 同步获取翻译（如果已初始化）
  get: () => translateUtils.getMergedTranslations(),

  // 异步初始化并获取翻译
  initialize: async () => {
    await translateUtils.initialize()
    return translateUtils.getMergedTranslations()
  },

  // 添加自定义翻译
  add: (translations: TranslationMap) => translateUtils.addCustomTranslations(translations),

  // 翻译单个文本
  translate: (template: string, replacements?: Record<string, any>) =>
    translateUtils.translate(template, replacements),
}
