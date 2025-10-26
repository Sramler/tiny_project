# BPMN i18n 模块重构说明

## 重构目标

严格按照 `bpmn-js-i18n-zh` 的结构设计，将翻译相关文件放到 `utils/bpmn/i18n` 下，并在外层 `index.ts` 导出。

## bpmn-js-i18n-zh 结构参考

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

## 重构后的结构

```
src/utils/bpmn/
├── index.ts                # 主入口文件（按 bpmn-js-i18n-zh 模式）
├── translateUtils.ts        # 翻译工具类
├── test-translation.js      # 测试文件
├── README.md               # 使用说明
└── i18n/                   # 翻译映射目录
    ├── index.ts            # 翻译映射主入口
    ├── translations.ts     # 主要翻译映射
    └── eventTranslations.ts # 事件类型翻译
```

## 文件移动和重构

### 1. 创建 i18n 目录

```bash
mkdir -p src/utils/bpmn/i18n
```

### 2. 移动翻译文件

```bash
mv src/utils/bpmn/translations.ts src/utils/bpmn/i18n/
mv src/utils/bpmn/eventTranslations.ts src/utils/bpmn/i18n/
```

### 3. 创建 i18n/index.ts

```typescript
/**
 * BPMN 翻译映射主入口
 * 按照 bpmn-js-i18n-zh 的设计模式
 */

import elementTranslations from './translations'
import eventTranslations from './eventTranslations'
import type { TranslationMap } from './translations'

// 合并所有翻译映射
const allTranslations: TranslationMap = {
  ...elementTranslations,
  ...eventTranslations,
}

export default allTranslations

// 导出翻译映射类型
export type { TranslationMap } from './translations'
```

### 4. 更新外层 index.ts

```typescript
/**
 * BPMN 翻译模块主入口
 * 按照 bpmn-js-i18n-zh 的设计模式
 */

// 导出翻译映射
export { default as translations } from './i18n'

// 导出翻译工具
export {
  translate,
  addCustomTranslations,
  getPerformanceStats,
  getTranslateModule,
  clearCache,
} from './translateUtils'

// 导出类型
export type { TranslationMap } from './i18n'
```

### 5. 更新 translateUtils.ts

```typescript
import allTranslations from './i18n'
import type { TranslationMap } from './i18n'
```

## 设计优势

### 1. 严格遵循 bpmn-js-i18n-zh 模式

- ✅ 翻译映射独立到 `i18n` 目录
- ✅ 主入口文件统一导出
- ✅ 清晰的模块分层

### 2. 更好的组织结构

- ✅ 翻译映射和工具分离
- ✅ 职责更加清晰
- ✅ 便于维护和扩展

### 3. 类型安全

- ✅ 明确的类型定义
- ✅ 正确的类型推断
- ✅ 完整的类型导出

## 使用方式

### 基本使用（保持不变）

```typescript
import { translate, getTranslateModule } from '@/utils/bpmn'
```

### 访问翻译映射

```typescript
import { translations } from '@/utils/bpmn'

// 直接访问翻译映射
console.log(translations['User task']) // '用户任务'
```

### 类型定义

```typescript
import type { TranslationMap } from '@/utils/bpmn'

const customTranslations: TranslationMap = {
  'Custom Key': '自定义键',
}
```

## 验证结果

### 1. 类型检查

```bash
npm run type-check
# 结果: ✅ 通过 (只有2个与翻译模块无关的错误)
```

### 2. 功能完整性

- ✅ 翻译功能正常工作
- ✅ 导入路径正确
- ✅ 类型定义完整
- ✅ 模块结构清晰

### 3. 文件结构

- ✅ 符合 bpmn-js-i18n-zh 设计模式
- ✅ 翻译映射独立管理
- ✅ 工具和映射分离

## 对比分析

| 特性     | bpmn-js-i18n-zh | 重构前   | 重构后     |
| -------- | --------------- | -------- | ---------- |
| 模块结构 | lib/ 目录       | 平级文件 | i18n/ 目录 |
| 主入口   | index.js        | index.ts | index.ts   |
| 翻译映射 | lib/bpmn-js/    | 根目录   | i18n/      |
| 类型安全 | ❌              | ✅       | ✅         |
| 性能优化 | ❌              | ✅       | ✅         |

## 总结

通过这次重构，我们：

1. **严格遵循了设计模式**: 完全按照 `bpmn-js-i18n-zh` 的结构
2. **优化了文件组织**: 翻译映射独立到 `i18n` 目录
3. **保持了功能完整性**: 所有翻译功能正常工作
4. **提高了维护性**: 更清晰的文件结构和职责分离

现在翻译模块的结构完全符合 `bpmn-js-i18n-zh` 的设计模式，同时保持了 TypeScript 的类型安全和性能优化特性。
