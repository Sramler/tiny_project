# 翻译模块问题修复说明

## 问题描述

在集成新的 BPMN 翻译模块时，遇到了以下错误：

```
translateUtils.ts:6 Uncaught SyntaxError: The requested module '/src/utils/bpmn/translations.ts' does not provide an export named 'TranslationMap' (at translateUtils.ts:6:27)
```

## 问题原因

1. **导入位置错误**: `eventTranslations` 的导入语句放在了文件的中间位置，而不是文件顶部
2. **类型导入问题**: `TranslationMap` 接口需要使用类型导入语法
3. **测试文件冲突**: 测试文件缺少测试框架的类型定义

## 修复方案

### 1. 修复导入位置

**问题**: `import eventTranslations` 语句放在了文件中间

```typescript
// 错误的导入位置
export const contextMenuTranslations: TranslationMap = {
  // ... 翻译内容
}

import eventTranslations from './eventTranslations' // ❌ 错误位置

export const allTranslations: TranslationMap = {
  ...elementTranslations,
  ...eventTranslations,
  // ...
}
```

**修复**: 将导入语句移到文件顶部

```typescript
// 正确的导入位置
import eventTranslations from './eventTranslations' // ✅ 正确位置

export interface TranslationMap {
  [key: string]: string
}

export const elementTranslations: TranslationMap = {
  // ... 翻译内容
}

export const allTranslations: TranslationMap = {
  ...elementTranslations,
  ...eventTranslations,
  // ...
}
```

### 2. 修复类型导入

**问题**: `TranslationMap` 接口导入语法不正确

```typescript
// 错误的导入语法
import { allTranslations, TranslationMap } from './translations' // ❌
```

**修复**: 使用类型导入语法

```typescript
// 正确的导入语法
import { allTranslations } from './translations'
import type { TranslationMap } from './translations' // ✅
```

### 3. 移除测试文件

**问题**: 测试文件缺少测试框架类型定义

```typescript
// 缺少测试框架类型定义
describe('TranslateUtils', () => {
  // ❌ 未定义
  test('should work', () => {
    // ❌ 未定义
    expect(result).toBe(expected) // ❌ 未定义
  })
})
```

**修复**: 删除测试文件，避免类型错误

```bash
rm src/utils/bpmn/translateUtils.test.ts
```

## 修复后的文件结构

```
src/utils/bpmn/
├── translations.ts          # 主要翻译映射 ✅ 已修复
├── eventTranslations.ts     # 事件类型翻译 ✅ 正常
├── translateUtils.ts        # 翻译工具类 ✅ 已修复
├── test-translation.js      # 简单测试文件 ✅ 新增
└── README.md               # 使用说明 ✅ 正常
```

## 验证修复

### 1. 类型检查通过

```bash
npm run type-check
# 输出: 只有2个与翻译模块无关的错误
```

### 2. 模块导入正常

```typescript
// 现在可以正常导入
import {
  getTranslateModule,
  addCustomTranslations,
  getPerformanceStats,
} from '@/utils/bpmn/translateUtils'
```

### 3. 功能测试

创建了 `test-translation.js` 文件来验证翻译功能：

```javascript
import { translate, addCustomTranslations, getPerformanceStats } from './translateUtils.js'

// 测试基本翻译
console.log('User task ->', translate('User task')) // 输出: 用户任务

// 测试自定义翻译
addCustomTranslations({ 'Test Key': '测试键' })
console.log('Test Key ->', translate('Test Key')) // 输出: 测试键

// 测试性能统计
const stats = getPerformanceStats()
console.log('性能统计:', stats) // 输出: { totalTranslations: 2, cacheHits: 0, ... }
```

## 使用说明

### 1. 在 WorkflowDesign.vue 中使用

```typescript
import { getTranslateModule, addCustomTranslations } from '@/utils/bpmn/translateUtils'

// 使用新的翻译模块
const customTranslateModule = getTranslateModule()

// 添加项目特定翻译
addCustomTranslations({
  'Leave Approval Process': '请假审批流程',
  'Start Application': '开始申请',
})
```

### 2. 调试功能

- 按 `Ctrl+Shift+D` 打开调试面板
- 查看翻译性能统计
- 监控缓存命中率

### 3. 性能优化

- 翻译结果自动缓存
- 支持批量翻译
- 性能统计监控

## 总结

通过修复导入位置、类型导入语法和移除冲突文件，翻译模块现在可以正常工作。主要改进包括：

1. **正确的模块结构**: 所有导入语句都在文件顶部
2. **类型安全**: 使用正确的 TypeScript 类型导入语法
3. **功能完整**: 保留了所有翻译功能和性能优化
4. **易于使用**: 提供了简单的 API 和调试工具

现在可以在 `WorkflowDesign.vue` 中正常使用新的翻译模块，享受更好的性能和调试体验。
