/**
 * BPMN 翻译模块主入口
 * 整合所有翻译模块，提供统一的翻译服务
 */

import bpmnJsTranslations from './bpmn-js'
import propertiesPanelTranslations from './properties-panel'
import camundaPropertiesPanelTranslations from './camunda-properties-panel'
import zeebePropertiesPanelTranslations from './zeebe-properties-panel'

export interface TranslationMap {
  [key: string]: string
}

// 合并所有翻译
const allTranslations: TranslationMap = {
  ...bpmnJsTranslations,
  ...propertiesPanelTranslations,
  ...camundaPropertiesPanelTranslations,
  ...zeebePropertiesPanelTranslations,
}

// 导出各个模块的翻译
export { bpmnJsTranslations }
export { propertiesPanelTranslations }
export { camundaPropertiesPanelTranslations }
export { zeebePropertiesPanelTranslations }

// 导出合并后的翻译
export default allTranslations
