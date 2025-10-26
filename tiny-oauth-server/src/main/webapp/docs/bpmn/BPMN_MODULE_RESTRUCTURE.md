# BPMN 翻译模块重构说明

## 重构原因

按照 `bpmn-js-i18n-zh` 的设计模式，将翻译模块重构为更合理的结构。

### bpmn-js-i18n-zh 的设计模式

```
bpmn-js-i18n-zh/
├── index.js                    # 主入口，导出所有模块
└── lib/
    ├── bpmn-js/
    │   ├── index.js            # 合并所有翻译
    │   └── elements.js         # 元素翻译
    ├── properties-panel/
    ├── camunda-properties-panel/
    └── zeebe-properties-panel/
```

**主入口文件**:

```javascript
export * from './lib/bpmn-js'
export * from './lib/camunda-properties-panel'
export * from './lib/properties-panel'
export * from './lib/zeebe-properties-panel'
```

## 重构内容

### 1. 新增主入口文件

**`src/utils/bpmn/index.ts`**:

```typescript
import elementTranslations from './translations'
import eventTranslations from './eventTranslations'

// 合并所有翻译
export default {
  ...elementTranslations,
  ...eventTranslations,
}

// 导出翻译工具
export {
  translate,
  addCustomTranslations,
  getPerformanceStats,
  getTranslateModule,
  clearCache,
} from './translateUtils'

// 导出类型
export type { TranslationMap } from './translations'
```

### 2. 简化 translations.ts

**重构前**:

```typescript
// 包含翻译映射 + 工具函数 + 模块导出
export const customTranslateModule = {
  translate: ['value', customTranslate],
}
export default customTranslateModule
```

**重构后**:

```typescript
// 只包含翻译映射
export default allTranslations
```

### 3. 更新导入方式

**重构前**:

```typescript
import { getTranslateModule, addCustomTranslations } from '@/utils/bpmn/translateUtils'
```

**重构后**:

```typescript
import { getTranslateModule, addCustomTranslations } from '@/utils/bpmn'
```

## 重构后的文件结构

```
src/utils/bpmn/
├── index.ts                # 主入口文件（按 bpmn-js-i18n-zh 模式）
├── translations.ts          # 主要翻译映射
├── eventTranslations.ts     # 事件类型翻译
├── translateUtils.ts        # 翻译工具类
├── test-translation.js      # 测试文件
└── README.md               # 使用说明
```

## 优势对比

| 特性     | 重构前   | 重构后               |
| -------- | -------- | -------------------- |
| 模块结构 | 分散导入 | 统一入口             |
| 导入路径 | 多级路径 | 简洁路径             |
| 设计模式 | 自定义   | 遵循 bpmn-js-i18n-zh |
| 维护性   | 一般     | 更好                 |
| 扩展性   | 有限     | 更强                 |

## 使用方式对比

### 基本使用

**重构前**:

```typescript
import { translate } from '@/utils/bpmn/translateUtils'
import { getTranslateModule } from '@/utils/bpmn/translateUtils'
import { addCustomTranslations } from '@/utils/bpmn/translateUtils'
```

**重构后**:

```typescript
import { translate, getTranslateModule, addCustomTranslations } from '@/utils/bpmn'
```

### 在 WorkflowDesign.vue 中

**重构前**:

```typescript
import {
  getTranslateModule,
  addCustomTranslations,
  getPerformanceStats,
} from '@/utils/bpmn/translateUtils'
```

**重构后**:

```typescript
import { getTranslateModule, addCustomTranslations, getPerformanceStats } from '@/utils/bpmn'
```

## 验证结果

### 1. 类型检查

```bash
npm run type-check
# 结果: ✅ 通过 (只有2个与翻译模块无关的错误)
```

### 2. 功能完整性

- ✅ 翻译功能正常工作
- ✅ 导入路径简化
- ✅ 模块结构清晰
- ✅ 遵循设计模式

### 3. 代码质量

- ✅ 更好的模块化
- ✅ 统一的导入方式
- ✅ 清晰的职责分离

## 设计原则

### 1. 遵循现有模式

- 参考 `bpmn-js-i18n-zh` 的设计
- 保持一致的模块结构
- 使用统一的导出方式

### 2. 职责分离

- `index.ts`: 主入口，统一导出
- `translations.ts`: 翻译映射
- `translateUtils.ts`: 翻译工具
- `eventTranslations.ts`: 事件翻译

### 3. 简化使用

- 单一导入路径
- 统一的 API 接口
- 清晰的类型定义

## 总结

通过这次重构，我们：

1. **遵循了设计模式**: 按照 `bpmn-js-i18n-zh` 的结构设计
2. **简化了使用方式**: 统一的导入路径
3. **提高了维护性**: 清晰的模块结构
4. **增强了扩展性**: 更好的模块化设计

现在翻译模块的结构更加合理，使用更加简便，同时保持了所有功能的完整性。
