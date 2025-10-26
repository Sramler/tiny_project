/**
 * BPMN 翻译模块主入口
 * 按照 bpmn-js-i18n-zh 的设计模式
 */

// 导出翻译映射
export { default as translations } from './i18n'

// 导出各个翻译模块
export {
  bpmnJsTranslations,
  propertiesPanelTranslations,
  camundaPropertiesPanelTranslations,
  zeebePropertiesPanelTranslations,
} from './i18n'

// 导出 BPMN.js 翻译相关函数
export {
  loadOfficialTranslations,
  getMergedTranslations,
  getLocalTranslations,
  getOfficialTranslations,
  isOfficialTranslationsLoaded,
} from './i18n/bpmn-js'

// 导出调试功能
export {
  initDebug,
  setDebugEnabled,
  setDebugPanelVisible,
  toggleDebugPanel,
  refreshStats,
  resetStats,
  exportUntranslated,
  setAutoRefresh,
  getDebugConfig,
  getDebugStats,
  isDebugEnabled,
  isDebugPanelVisible,
  useDebug,
} from './i18n/debug'

export type { DebugConfig, DebugStats } from './i18n/debug'

// 导出翻译工具
export {
  translate,
  addCustomTranslations,
  getPerformanceStats,
  getTranslateModule,
  clearCache,
  setOfficialFallback,
  addOfficialTranslations,
  getTranslationSource,
  getUntranslatedKeys,
  getUntranslatedStats,
  exportUntranslatedKeys,
  generateTranslationSuggestions,
  getOfficialPackagesInfo,
  getOfficialTranslationStatus,
  initializeTranslationSystem,
} from './utils/translateUtils'

// 导出类型
export type { TranslationMap } from './i18n'
