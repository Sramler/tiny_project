declare module 'bpmn-js-properties-panel' {
  export const BpmnPropertiesPanelModule: any
  export const BpmnPropertiesProviderModule: any
  export const CamundaPlatformPropertiesProviderModule: any
  export const ZeebePropertiesProviderModule: any
  export const ZeebeTooltipProvider: any
  export const useService: any
}

declare module 'bpmn-js-i18n/translations/zn.js' {
  const translations: Record<string, string>
  export default translations
}
