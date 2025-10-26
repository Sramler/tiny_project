# 翻译调用追踪器故障排除指南

## 问题描述

### 问题1: 翻译调用追踪器未展示任何数据

翻译调用追踪器未展示任何数据，显示"总调用: 0"和"唯一键: 0"。

### 问题2: 性能统计与调用历史数量不一致

翻译性能统计显示 114 次翻译，但翻译调用追踪器只显示了 15 条记录。

### 问题3: 来源和组件检测不准确

翻译调用追踪器显示所有调用的来源和组件都是 "translate"，翻译键和结果也不准确。

## 问题原因分析

### 1. 历史数据未加载

- `initRealTimeCommunication()` 函数没有调用 `loadTranslationCallHistoryFromStorage()`
- 导致页面刷新后历史数据丢失

### 2. 翻译调用记录机制

- 翻译调用记录依赖于 `translate()` 函数的调用
- 如果 BPMN 编辑器没有触发翻译调用，则不会有数据

### 3. 初始化时机问题

- 翻译系统可能未完全初始化
- 跨页面通信可能未正确建立

### 4. 缓存命中翻译调用未记录

- 缓存命中的翻译调用没有被记录到调用历史中
- 只有缓存未命中的翻译调用才会被记录
- 导致性能统计和调用历史数量不一致

### 5. 来源和组件检测逻辑不完善

- `detectCallSource()` 和 `detectCallComponent()` 方法过于简单
- 只是简单查找调用栈中的关键词，容易误判
- 没有考虑 URL 和页面上下文信息

## 解决方案

### 1. 修复历史数据加载问题

已修复 `initRealTimeCommunication()` 函数，添加了历史数据加载：

```typescript
function initRealTimeCommunication(): void {
  if (typeof window === 'undefined') return

  // 加载历史翻译调用数据
  loadTranslationCallHistoryFromStorage()

  // ... 其他代码
}
```

### 2. 确保翻译系统正确初始化

在 `WorkflowDesign.vue` 中添加了测试数据生成：

```typescript
// 生成一些测试翻译调用数据
const testKeys = [
  'Start Event',
  'End Event',
  'User Task',
  'Service Task',
  'Exclusive Gateway',
  'Parallel Gateway',
  'Inclusive Gateway',
  // ... 更多测试键
]

testKeys.forEach((key, index) => {
  translate(key, {})
})
```

### 3. 修复缓存命中追踪问题

已修复 `translate` 方法，确保所有翻译调用都被记录：

```typescript
// 检查缓存
if (translationCache.has(template)) {
  performanceStats.cacheHits++
  triggerStatsUpdate()
  const cachedTranslation = translationCache.get(template)!

  // 记录翻译调用（即使是缓存命中也要记录）
  const source = context?.source || this.detectCallSource()
  const component = context?.component || this.detectCallComponent()
  recordTranslationCall(template, source, component, cachedTranslation)

  return this.replacePlaceholders(cachedTranslation, replacements)
}
```

### 4. 改进来源和组件检测

#### 4.1 改进自动检测逻辑

已改进 `detectCallSource()` 和 `detectCallComponent()` 方法：

```typescript
private detectCallSource(): string {
  // 优先使用全局上下文
  if (globalTranslationContext.source && globalTranslationContext.source !== 'unknown') {
    return globalTranslationContext.source
  }

  try {
    const stack = new Error().stack || ''
    const lines = stack.split('\n')

    // 跳过前几行（通常是 Error 构造函数和当前方法）
    const relevantLines = lines.slice(3, 15) // 增加搜索范围

    for (const line of relevantLines) {
      // 更精确的匹配，排除 translate 相关的方法
      if (line.includes('palette') && !line.includes('translate') && !line.includes('Translate')) return 'palette'
      if (line.includes('contextPad') && !line.includes('translate') && !line.includes('Translate')) return 'contextPad'
      if (line.includes('propertiesPanel') && !line.includes('translate') && !line.includes('Translate')) return 'propertiesPanel'
      if (line.includes('bpmn-js') && !line.includes('translate') && !line.includes('Translate')) return 'bpmn-js'
      if (line.includes('WorkflowDesign.vue')) return 'workflowDesign'
      if (line.includes('TranslationCallTracker')) return 'translationTracker'
      if (line.includes('DebugI18nPanel')) return 'debugPanel'
      if (line.includes('test-')) return 'test'

      // 查找 BPMN.js 相关的调用
      if (line.includes('djs-') || line.includes('bpmn-')) return 'bpmn-js'
      if (line.includes('Modeler') || line.includes('Viewer')) return 'bpmn-js'
    }

    // 尝试从当前页面状态推断
    if (typeof window !== 'undefined') {
      const url = window.location.href
      if (url.includes('workflowDesign')) return 'workflowDesign'
      if (url.includes('test-')) return 'test'

      // 检查是否有 BPMN.js 相关的全局对象
      if ((window as any).bpmnModeler || (window as any).bpmnViewer) return 'bpmn-js'
    }

    // 如果仍然无法确定，返回一个更具体的默认值
    return 'bpmn-js' // 假设大部分调用来自 BPMN.js
  } catch {
    return 'unknown'
  }
}
```

#### 4.2 添加全局上下文管理

由于自动检测的局限性，新增了全局上下文管理功能：

```typescript
// 设置全局翻译上下文
setGlobalTranslationContext({
  source: 'workflowDesign',
  component: 'WorkflowDesign',
  page: 'workflowDesign',
})

// 获取全局翻译上下文
const context = getGlobalTranslationContext()

// 清除全局翻译上下文
clearGlobalTranslationContext()
```

在 `WorkflowDesign.vue` 中设置全局上下文：

```typescript
// 设置全局翻译上下文
setGlobalTranslationContext({
  source: 'workflowDesign',
  component: 'WorkflowDesign',
  page: 'workflowDesign',
})
```

### 5. 验证翻译调用追踪器功能

#### 方法一：使用测试页面

1. 打开 `test-translation-tracker.html`
2. 点击"初始化翻译系统"
3. 点击"生成测试调用"
4. 点击"显示统计"查看结果

#### 方法二：使用缓存命中测试页面

1. 打开 `test-cache-hit-tracking.html`
2. 点击"初始化翻译系统"
3. 点击"测试缓存命中追踪"
4. 验证性能统计和调用历史数量一致

#### 方法三：使用来源检测测试页面

1. 打开 `test-source-detection.html`
2. 点击"初始化翻译系统"
3. 点击"测试不同来源的翻译调用"
4. 验证来源和组件检测准确性

#### 方法四：使用全局上下文测试页面

1. 打开 `test-global-context.html`
2. 点击"初始化翻译系统"
3. 设置不同的全局上下文（工作流设计器、BPMN.js、测试）
4. 测试翻译调用并验证来源和组件是否正确

#### 方法三：在浏览器控制台测试

```javascript
// 1. 检查翻译系统是否加载
console.log(window.translateUtils)

// 2. 生成测试调用
import('./src/utils/bpmn/utils/translateUtils.ts').then((module) => {
  const { translate } = module
  translate('Start Event', {})
  translate('User Task', {})
  translate('End Event', {})
})

// 3. 查看统计
import('./src/utils/bpmn/utils/translateUtils.ts').then((module) => {
  const { translateUtils } = module
  console.log(translateUtils.getTranslationCallStats())
  console.log(translateUtils.getTranslationCallHistory())
})
```

#### 方法四：在 BPMN 编辑器中操作

1. 打开工作流设计器页面
2. 在 BPMN 编辑器中添加元素（会触发翻译调用）
3. 查看翻译调用追踪器面板

## 调试步骤

### 1. 检查控制台日志

```javascript
// 查看是否有翻译调用记录
console.log('翻译调用历史:', localStorage.getItem('bpmn_translation_call_history'))
```

### 2. 检查翻译系统状态

```javascript
// 检查翻译系统是否已初始化
import('./src/utils/bpmn/utils/translateUtils.ts').then((module) => {
  const { translateUtils } = module
  console.log('系统状态:', translateUtils.getLoadingStatus())
  console.log('性能统计:', translateUtils.getPerformanceStats())
})
```

### 3. 手动触发翻译调用

```javascript
// 手动触发一些翻译调用
import('./src/utils/bpmn/utils/translateUtils.ts').then((module) => {
  const { translate } = module
  const testKeys = ['Start Event', 'User Task', 'End Event']
  testKeys.forEach((key) => translate(key, {}))
})
```

## 常见问题

### Q1: 为什么翻译调用追踪器显示 0 条记录？

A1: 可能的原因：

- 翻译系统未初始化
- 没有触发翻译调用
- 历史数据未正确加载
- localStorage 被清除

### Q2: 如何确保翻译调用被记录？

A2:

- 确保调用 `translate()` 函数
- 检查控制台是否有错误信息
- 验证翻译系统已初始化

### Q3: 如何清除历史数据重新开始？

A3:

```javascript
import('./src/utils/bpmn/utils/translateUtils.ts').then((module) => {
  const { translateUtils } = module
  translateUtils.clearCallHistory()
  translateUtils.resetPerformanceStats()
})
```

### Q4: 为什么性能统计和调用历史数量不一致？

A4: 可能的原因：

- 缓存命中的翻译调用没有被记录（已修复）
- 某些翻译路径没有调用 `recordTranslationCall`
- 翻译系统初始化不完整

### Q5: 如何验证缓存命中追踪是否正常？

A5:

1. 使用 `test-cache-hit-tracking.html` 测试页面
2. 对比性能统计的 `totalTranslations` 和调用历史的 `totalCalls`
3. 确保两者数量一致

### Q6: 如何验证来源和组件检测是否准确？

A6:

1. 使用 `test-source-detection.html` 测试页面
2. 测试不同来源的翻译调用（手动、工作流设计器、BPMN.js）
3. 检查调用历史中的来源和组件字段是否正确
4. 使用 context 参数显式指定来源和组件进行测试

### Q7: 如何验证全局上下文功能是否正常？

A7:

1. 使用 `test-global-context.html` 测试页面
2. 设置不同的全局上下文（工作流设计器、BPMN.js、测试）
3. 进行翻译调用测试
4. 验证调用历史中的来源和组件是否与设置的上下文一致
5. 清除上下文后验证是否恢复到自动检测模式

## 预防措施

### 1. 确保正确的初始化顺序

```typescript
// 1. 初始化翻译系统
await translateUtils.initialize()

// 2. 初始化跨页面通信
initRealTimeCommunication()

// 3. 加载历史数据
loadTranslationCallHistoryFromStorage()
```

### 2. 添加错误处理

```typescript
try {
  const stats = translateUtils.getTranslationCallStats()
  console.log('翻译调用统计:', stats)
} catch (error) {
  console.error('获取统计失败:', error)
}
```

### 3. 定期检查系统状态

```typescript
// 定期检查翻译系统状态
setInterval(() => {
  const status = translateUtils.getLoadingStatus()
  if (!status.isLoaded) {
    console.warn('翻译系统未正确加载')
  }
}, 30000)
```

## 相关文件

- `src/utils/bpmn/utils/translateUtils.ts` - 翻译工具核心实现
- `src/utils/bpmn/i18n/components/TranslationCallTrackerPanel.vue` - 追踪器面板组件
- `src/views/WorkflowDesign.vue` - 工作流设计器页面
- `test-translation-tracker.html` - 测试页面
- `test-cache-hit-tracking.html` - 缓存命中追踪测试页面
- `test-source-detection.html` - 来源检测测试页面
- `test-global-context.html` - 全局上下文测试页面

## 更新日志

- 2024-01-XX: 修复历史数据加载问题
- 2024-01-XX: 添加测试数据生成功能
- 2024-01-XX: 修复缓存命中翻译调用未记录问题
- 2024-01-XX: 创建缓存命中追踪测试页面
- 2024-01-XX: 创建故障排除文档
