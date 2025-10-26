# BPMN.js 翻译工具

## 概述

这是一个用于 BPMN.js 的翻译工具，支持中文翻译和官方英文翻译的混合使用。

## 为什么 BPMN.js 翻译比 Ant Design Vue 复杂？

### 🎯 **Ant Design Vue 翻译（简单）**

```typescript
import zhCN from 'ant-design-vue/es/locale/zh_CN'
```

**为什么这么简单：**

- 使用标准的 ES6 `import` 语法
- 翻译文件在构建时就被打包到主包中
- 同步加载，翻译数据立即可用
- 遵循标准的模块导出规范

### 🔧 **BPMN.js 翻译（复杂）**

```typescript
// 需要动态导入
const englishTranslations = await import('bpmn-js-i18n/translations/en.js')
```

**为什么这么复杂：**

- 使用 `import()` 动态导入语法
- 翻译包被分离到独立的 chunk 中
- 需要异步加载，等待网络请求完成
- 包的导出方式不标准

### 📦 **包结构差异**

**Ant Design Vue:**

```
node_modules/ant-design-vue/es/locale/
├── zh_CN.js          # 标准 ES6 模块
├── en_US.js          # 标准 ES6 模块
└── index.js          # 主入口文件
```

**BPMN.js i18n:**

```
node_modules/bpmn-js-i18n/
├── package.json      # "type": "module"
└── translations/
    ├── en.js         # 纯 ES6 模块
    ├── zh.js         # 纯 ES6 模块
    └── ...
```

## 简化使用方式

现在你可以像使用 Ant Design Vue 一样简单地使用 BPMN.js 翻译：

### 1. **按需加载方式（推荐）**

在具体使用的模块页面中导入和初始化：

```vue
<template>
  <div class="workflow-design">
    <div ref="bpmnContainer" class="bpmn-container">
      <DebugI18nPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'
import { initDebug, DebugI18nPanel } from '@/utils/bpmn'

const bpmnContainer = ref<HTMLDivElement | null>(null)
let modeler: BpmnModeler | null = null

onMounted(async () => {
  // 初始化翻译系统
  console.log('Initializing translation system...')
  await bpmnTranslations.initialize()
  console.log('Translation system initialized successfully')

  // 创建翻译模块
  const translateModule = {
    translate: [
      'value',
      (template: string, replacements?: Record<string, any>) =>
        bpmnTranslations.translate(template, replacements)
    ]
  }

  // 创建 BPMN 建模器
  modeler = new BpmnModeler({
    container: bpmnContainer.value,
    additionalModules: [translateModule],
  })
})
</script>
```

### 2. **简单导入方式**

```typescript
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

// 初始化翻译（一次性）
const translations = await bpmnTranslations.initialize()

// 使用翻译
const result = bpmnTranslations.translate('Start Event')
console.log(result) // 输出: 开始事件
```

### 3. **像 Ant Design Vue 一样使用**

```typescript
// 在应用启动时初始化
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

// 初始化（类似 Ant Design Vue 的 ConfigProvider）
await bpmnTranslations.initialize()

// 添加自定义翻译
bpmnTranslations.add({
  'Custom Task': '自定义任务',
  'My Process': '我的流程',
})

// 使用翻译
const translated = bpmnTranslations.translate('Task {name}', { name: '审批任务' })
```

### 3. **在 BPMN.js 中使用**

```typescript
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

// 初始化翻译
await bpmnTranslations.initialize()

// 获取翻译模块配置
const translateModule = {
  translate: (template: string, replacements?: Record<string, any>) =>
    bpmnTranslations.translate(template, replacements),
}

// 在 BPMN.js 中使用
const modeler = new BpmnModeler({
  container: '#canvas',
  additionalModules: [
    {
      __init__: ['translate'],
      translate: ['value', translateModule],
    },
  ],
})
```

## 性能优化

### 1. **应用启动时预加载**

```typescript
// main.ts
import { preloadOfficialTranslations } from '@/utils/bpmn/utils/translateUtils'

async function bootstrap() {
  // 预加载官方翻译包
  await preloadOfficialTranslations()

  // 创建应用...
}
```

### 2. **缓存机制**

翻译结果会被缓存，重复使用相同键时性能更好：

```typescript
// 第一次翻译（较慢）
const result1 = bpmnTranslations.translate('Start Event')

// 第二次翻译（很快，从缓存获取）
const result2 = bpmnTranslations.translate('Start Event')
```

## 调试和监控

### 启用调试日志

```typescript
import { setDebugLogs } from '@/utils/bpmn/utils/translateUtils'

setDebugLogs(true)
```

### 获取性能统计

```typescript
import { getPerformanceStats } from '@/utils/bpmn/utils/translateUtils'

const stats = getPerformanceStats()
console.log('翻译性能:', stats)
```

## 常见问题

### Q: 为什么不能像 Ant Design Vue 一样直接导入？

A: 因为 BPMN.js i18n 包使用了 `"type": "module"`，需要动态导入。但通过我们的简化包装器，现在可以像 Ant Design Vue 一样简单使用。

### Q: 如何避免重复加载？

A: 使用 `bpmnTranslations.initialize()` 方法，它会确保只加载一次，后续调用直接返回缓存的结果。

### Q: 翻译加载失败怎么办？

A: 系统会自动降级到本地翻译，确保基本功能正常。检查控制台日志了解具体错误原因。

## 最佳实践

1. **按需加载**: 在具体使用的模块页面中导入和初始化，避免全局加载
2. **使用缓存**: 重复翻译会自动使用缓存
3. **添加自定义翻译**: 补充缺失的翻译
4. **监控性能**: 使用调试工具监控翻译性能
5. **错误处理**: 翻译初始化失败时优雅降级到本地翻译

这样，你就可以像使用 Ant Design Vue 翻译一样简单地使用 BPMN.js 翻译了！

## 完整使用示例

### 1. **按需加载方式（推荐）**

```vue
<!-- WorkflowDesign.vue -->
<template>
  <div class="workflow-design">
    <div ref="bpmnContainer" class="bpmn-container">
      <DebugI18nPanel />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'
import { initDebug, DebugI18nPanel } from '@/utils/bpmn'

const bpmnContainer = ref<HTMLDivElement | null>(null)
let modeler: BpmnModeler | null = null

onMounted(async () => {
  // 初始化翻译系统
  console.log('Initializing translation system...')
  await bpmnTranslations.initialize()
  console.log('Translation system initialized successfully')

  // 创建翻译模块
  const translateModule = {
    translate: [
      'value',
      (template: string, replacements?: Record<string, any>) =>
        bpmnTranslations.translate(template, replacements)
    ]
  }

  // 创建 BPMN 建模器
  modeler = new BpmnModeler({
    container: bpmnContainer.value,
    additionalModules: [translateModule],
  })
})
</script>
```

### 2. **在组件中使用**

```vue
<template>
  <div>
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

const title = ref('')
const description = ref('')

onMounted(async () => {
  // 确保翻译已初始化
  await bpmnTranslations.initialize()

  // 使用翻译
  title.value = bpmnTranslations.translate('Workflow Design')
  description.value = bpmnTranslations.translate('Create and edit BPMN diagrams')
})
</script>
```

### 3. **添加自定义翻译**

```typescript
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

// 添加自定义翻译
bpmnTranslations.add({
  'Custom Task': '自定义任务',
  'My Process': '我的流程',
  'Welcome {user}': '欢迎 {user}',
})

// 使用带参数的翻译
const welcome = bpmnTranslations.translate('Welcome {user}', { user: '张三' })
console.log(welcome) // 输出: 欢迎 张三
```

### 4. **在 BPMN.js 中集成**

```typescript
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { bpmnTranslations } from '@/utils/bpmn/utils/translateUtils'

async function createBpmnModeler() {
  // 确保翻译已初始化
  await bpmnTranslations.initialize()

  // 创建翻译模块
  const translateModule = {
    translate: [
      'value',
      (template: string, replacements?: Record<string, any>) =>
        bpmnTranslations.translate(template, replacements)
    ]
  }

  // 创建 BPMN 建模器
  const modeler = new BpmnModeler({
    container: '#canvas',
    additionalModules: [translateModule],
  })

  return modeler
}
```

### 5. **性能监控**

```typescript
import { getPerformanceStats, setDebugLogs } from '@/utils/bpmn/utils/translateUtils'

// 启用调试日志
setDebugLogs(true)

// 获取性能统计
const stats = getPerformanceStats()
console.log('翻译性能统计:', {
  总翻译次数: stats.totalTranslations,
  缓存命中率: stats.cacheHitRate,
  官方兜底次数: stats.officialFallbacks,
})
```

这样，你就可以像使用 Ant Design Vue 一样简单地使用 BPMN.js 翻译了！🎉
