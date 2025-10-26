# BPMN 翻译 Stack Trace 分析功能

## 概述

本项目实现了基于 Stack Trace 的 BPMN 翻译调用来源分析功能，能够自动追踪和分析翻译函数的调用来源，帮助开发者了解翻译调用的完整调用链和来源信息。

## 核心功能

### 1. Stack Trace 分析

通过分析 JavaScript 的 Stack Trace，自动识别翻译调用的来源：

- **来源识别**: 自动识别调用来自 BPMN.js、Vue 组件、外部库等
- **组件识别**: 识别具体的组件类型（Palette、ContextPad、PropertiesPanel 等）
- **文件路径**: 记录调用发生的具体文件路径和行号
- **函数名**: 记录调用函数的具体名称
- **模块名**: 提取调用的模块名称

### 2. 调用链追踪

记录完整的调用链信息：

- **调用栈深度**: 记录调用栈的深度
- **调用链序列**: 记录完整的函数调用序列
- **调用频率**: 统计不同调用者的调用频率
- **时间戳**: 记录每次调用的精确时间

### 3. 实时监控

提供实时监控功能：

- **跨页面通信**: 支持多页面间的翻译调用数据同步
- **实时统计**: 实时更新翻译调用统计信息
- **性能监控**: 监控翻译调用的性能指标

## 技术实现

### 核心类和方法

#### `analyzeStackTrace(stack: string)`

分析 Stack Trace 并提取调用信息：

```typescript
function analyzeStackTrace(stack: string): {
  source: string // 调用来源
  component: string // 组件类型
  caller: string // 调用者函数名
  callChain: string[] // 调用链
  filePath: string // 文件路径
  lineNumber: number // 行号
  functionName: string // 函数名
  moduleName: string // 模块名
  isBpmnJs: boolean // 是否来自 BPMN.js
  isVueComponent: boolean // 是否来自 Vue 组件
  isExternalLibrary: boolean // 是否来自外部库
  stackDepth: number // 调用栈深度
}
```

#### `recordTranslationCall()`

记录翻译调用信息：

```typescript
function recordTranslationCall(key: string, source: string, component: string, result: string): void
```

#### `TranslateUtils` 类

提供完整的翻译工具类，包含以下主要方法：

- `analyzeCallChain(key: string)`: 分析特定翻译键的调用链
- `analyzeFileSources(key: string)`: 分析特定翻译键的文件来源
- `getDetailedCallStats()`: 获取详细的调用统计信息
- `exportDetailedCallReport()`: 导出详细的分析报告

### 数据结构

#### `TranslationCall` 接口

```typescript
interface TranslationCall {
  key: string // 翻译键
  source: string // 调用来源
  component: string // 组件类型
  timestamp: number // 时间戳
  stack?: string // 原始 Stack Trace
  result: string // 翻译结果
  callChain: string[] // 调用链
  filePath: string // 文件路径
  lineNumber: number // 行号
  functionName: string // 函数名
  moduleName: string // 模块名
  isBpmnJs: boolean // 是否来自 BPMN.js
  isVueComponent: boolean // 是否来自 Vue 组件
  isExternalLibrary: boolean // 是否来自外部库
  stackDepth: number // 调用栈深度
  executionTime?: number // 执行时间
  cacheHit?: boolean // 是否缓存命中
  translationSource?: 'custom' | 'local' | 'official' | 'none' // 翻译来源
}
```

## 使用方法

### 1. 基础使用

```typescript
import { translate, getTranslationCallStats } from './utils/bpmn/utils/translateUtils'

// 进行翻译调用
const result = translate('some.key', { param: 'value' })

// 获取调用统计
const stats = getTranslationCallStats()
console.log('翻译调用统计:', stats)
```

### 2. 详细分析

```typescript
import {
  analyzeCallChain,
  analyzeFileSources,
  getDetailedCallStats,
} from './utils/bpmn/utils/translateUtils'

// 分析特定翻译键的调用链
const callChainAnalysis = analyzeCallChain('some.key')
console.log('调用链分析:', callChainAnalysis)

// 分析特定翻译键的文件来源
const fileSourceAnalysis = analyzeFileSources('some.key')
console.log('文件来源分析:', fileSourceAnalysis)

// 获取详细统计
const detailedStats = getDetailedCallStats()
console.log('详细统计:', detailedStats)
```

### 3. 导出报告

```typescript
import { exportDetailedCallReport } from './utils/bpmn/utils/translateUtils'

// 导出详细分析报告到控制台
exportDetailedCallReport()
```

### 4. 实时监控

```typescript
import { getTranslationCallStats, onStatsUpdate } from './utils/bpmn/utils/translateUtils'

// 监听统计更新
onStatsUpdate(() => {
  const stats = getTranslationCallStats()
  console.log('统计已更新:', stats)
})
```

## 分析功能详解

### 1. 调用链分析

`analyzeCallChain(key: string)` 提供以下分析信息：

- **总调用次数**: 该翻译键被调用的总次数
- **调用链**: 完整的函数调用序列
- **唯一调用者**: 不同的调用者函数数量
- **调用频率**: 每个调用者的调用次数统计
- **平均栈深度**: 调用栈的平均深度
- **来源分类**: BPMN.js、Vue 组件、外部库的调用次数

### 2. 文件来源分析

`analyzeFileSources(key: string)` 提供以下分析信息：

- **文件路径**: 涉及的所有文件路径
- **文件频率**: 每个文件的调用次数
- **行号**: 调用发生的行号
- **模块**: 涉及的模块名称
- **模块频率**: 每个模块的调用次数

### 3. 详细统计

`getDetailedCallStats()` 提供全局统计信息：

- **总调用次数**: 所有翻译调用的总次数
- **按来源统计**: 不同来源的调用次数
- **按组件统计**: 不同组件的调用次数
- **按模块统计**: 不同模块的调用次数
- **性能指标**: 平均栈深度、缓存命中率等
- **调用者统计**: 唯一调用者数量和列表
- **文件统计**: 涉及的文件数量和列表

## 测试页面

项目提供了完整的测试页面 `test-stack-trace-analysis.html`，包含以下测试功能：

### 基础功能测试

- 基础翻译测试
- 多次翻译测试
- 不同来源测试
- 获取当前统计
- 清除所有历史

### 详细分析测试

- 分析特定键
- 获取详细统计
- 导出详细报告
- 分析调用链
- 分析文件来源

### 实时监控

- 开始实时监控
- 停止实时监控
- 模拟实时调用

### 性能测试

- 性能测试
- 压力测试
- 获取性能统计

## 配置选项

### 调试模式

```typescript
import { setDebugLogs } from './utils/bpmn/utils/translateUtils'

// 启用调试日志
setDebugLogs(true)
```

### 历史记录限制

```typescript
// 在 translateUtils.ts 中配置
const maxCallHistory = 1000 // 最大历史记录数量
```

### 存储配置

```typescript
// 在 translateUtils.ts 中配置
const STORAGE_KEY = 'translation_call_history'
const STORAGE_VERSION = '1.0.0'
```

## 性能考虑

### 1. 内存使用

- 历史记录有数量限制，避免内存泄漏
- 定期清理过期数据
- 使用 WeakMap 等弱引用数据结构

### 2. 性能影响

- Stack Trace 分析在开发模式下启用
- 生产环境可选择性禁用
- 异步处理大量数据

### 3. 存储优化

- 使用 localStorage 进行持久化存储
- 定期清理存储数据
- 压缩存储数据格式

## 故障排除

### 常见问题

1. **Stack Trace 分析不准确**

   - 检查浏览器是否支持 Error.stack
   - 确认代码没有被压缩或混淆

2. **性能问题**

   - 检查历史记录数量是否过多
   - 考虑禁用实时监控功能

3. **存储问题**
   - 检查 localStorage 是否可用
   - 确认存储空间是否充足

### 调试技巧

1. **启用调试日志**

   ```typescript
   setDebugLogs(true)
   ```

2. **查看控制台输出**

   - 所有分析结果都会输出到控制台
   - 使用 `exportDetailedCallReport()` 查看完整报告

3. **使用测试页面**
   - 打开 `test-stack-trace-analysis.html` 进行测试
   - 观察各种分析功能的效果

## 扩展功能

### 1. 自定义分析器

可以扩展 `analyzeStackTrace` 函数来支持更多来源类型：

```typescript
function analyzeStackTrace(stack: string) {
  // 添加自定义分析逻辑
  if (fileInfo.includes('custom-module')) {
    source = 'custom-module'
    component = 'Custom Component'
  }
}
```

### 2. 自定义统计

可以添加自定义的统计维度：

```typescript
// 在 recordTranslationCall 中添加自定义字段
const call: TranslationCall = {
  // ... 现有字段
  customField: 'custom-value',
}
```

### 3. 导出格式

支持导出不同格式的报告：

```typescript
// 导出 JSON 格式
export function exportJsonReport() {
  const data = getDetailedCallStats()
  return JSON.stringify(data, null, 2)
}

// 导出 CSV 格式
export function exportCsvReport() {
  // 实现 CSV 导出逻辑
}
```

## 总结

BPMN 翻译 Stack Trace 分析功能提供了强大的翻译调用追踪和分析能力，帮助开发者：

1. **了解调用来源**: 清楚知道翻译调用来自哪个模块和组件
2. **优化性能**: 识别频繁调用的翻译键和调用路径
3. **调试问题**: 快速定位翻译相关的问题
4. **监控系统**: 实时监控翻译系统的运行状态

通过合理使用这些功能，可以显著提升 BPMN 翻译系统的可维护性和性能。
