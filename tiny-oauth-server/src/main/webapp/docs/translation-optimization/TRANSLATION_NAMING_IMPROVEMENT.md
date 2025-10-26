# 翻译命名改进说明

## 修改概述

将翻译系统的分类命名从：

- **本地翻译** → **模块翻译** (Module Translations)
- **自定义翻译** → **临时翻译** (Temporary Translations)

## 修改原因

### 1. 更准确的语义表达

**原来的问题**：

- "本地翻译" 这个名称容易让人误解为本地文件翻译
- "自定义翻译" 不够明确，无法体现其临时性质

**改进后**：

- "模块翻译" 明确表达了这是基于不同工作流引擎模块的翻译，对官方翻译的完善和本地化
- "临时翻译" 清楚表明了这是开发测试初期的临时翻译，对官方和模块翻译的补充和覆盖，后期需要迁移到对应模块

### 2. 更好的扩展性

**模块化架构**：

```
src/utils/bpmn/i18n/
├── bpmn-js/                    # BPMN.js 核心翻译模块
├── properties-panel/           # 属性面板翻译模块
├── camunda-properties-panel/   # Camunda 属性面板翻译模块
├── zeebe-properties-panel/     # Zeebe 属性面板翻译模块
├── flowable/                   # 未来：Flowable 引擎翻译模块
└── activiti/                   # 未来：Activiti 引擎翻译模块
```

**命名优势**：

- "模块翻译" 准确反映了这种模块化结构
- 为未来扩展 Flowable、Activiti 等引擎翻译提供了清晰的命名基础

### 3. 更清晰的优先级层次

**翻译优先级**：

1. **官方翻译** (Official) - 来自官方包的英文基础翻译
2. **模块翻译** (Module) - 基于不同引擎模块的翻译，对官方翻译的完善和本地化
3. **临时翻译** (Temporary) - 开发测试初期的临时翻译，对以上内容的补充和覆盖
4. **原文** (Original) - 兜底机制

**命名优势**：

- 每个层级都有明确的职责和范围
- 官方翻译作为基础，模块翻译作为完善，临时翻译作为补充
- 临时翻译明确表达了其临时性质，提醒开发者后期需要迁移
- 便于开发者理解和使用

## 修改内容

### 1. 核心翻译工具类

- `translateUtils.ts` 中的变量名和注释
- 性能统计指标命名
- 调试信息输出

### 2. 调试面板

- `DebugI18nPanel.vue` 中的显示文本
- 调试统计信息类型定义

### 3. 文档更新

- `README.md` 中的架构说明
- 使用示例和注释

## 技术优势

### 1. 类型安全

- 保持了完整的 TypeScript 类型支持
- 所有相关接口和类型定义都已更新

### 2. 向后兼容

- 保持了原有的 API 接口不变
- 只是内部命名和显示文本的改进

### 3. 可维护性

- 更清晰的命名提高了代码可读性
- 为未来的模块扩展提供了良好的基础

## 未来扩展

### 1. 新增引擎支持

```typescript
// 未来可以轻松添加新的引擎模块
import flowableTranslations from './flowable'
import activitiTranslations from './activiti'

const allTranslations: TranslationMap = {
  ...bpmnJsTranslations,
  ...propertiesPanelTranslations,
  ...camundaPropertiesPanelTranslations,
  ...zeebePropertiesPanelTranslations,
  ...flowableTranslations, // 新增
  ...activitiTranslations, // 新增
}
```

### 2. 模块化配置

```typescript
// 未来可以支持按需加载不同模块
const moduleConfig = {
  bpmn: true,
  camunda: true,
  flowable: false, // 按需启用
  activiti: false, // 按需启用
}
```

### 3. 临时翻译迁移建议

```typescript
// 开发测试初期：使用临时翻译
addCustomTranslations({
  'New Feature': '新功能',
  'Custom Element': '自定义元素',
})

// 后期整理：迁移到对应模块
// 在 src/utils/bpmn/i18n/bpmn-js/elements.ts 中添加
export const newElements = {
  'New Feature': '新功能',
  'Custom Element': '自定义元素',
}

// 然后移除临时翻译
removeCustomTranslations(['New Feature', 'Custom Element'])
```

## 总结

这次命名改进：

1. **提高了语义准确性** - 更好地反映了翻译系统的实际架构和使用场景
2. **明确了层次关系** - 官方翻译作为基础，模块翻译作为完善，临时翻译作为补充
3. **增强了扩展性** - 为多引擎支持提供了清晰的命名基础
4. **改善了可维护性** - 更清晰的命名提高了代码可读性
5. **明确了临时性质** - "临时翻译" 明确提醒开发者这是临时存储，后期需要迁移到对应模块
6. **保持了兼容性** - 不影响现有功能，只是内部命名改进

这种改进为项目未来的多引擎支持奠定了良好的基础，使翻译系统更加专业和可扩展。同时，"临时翻译" 的命名也提醒开发者在开发过程中及时整理和迁移翻译到对应的模块中。
