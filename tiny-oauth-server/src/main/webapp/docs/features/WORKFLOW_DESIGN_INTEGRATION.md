# WorkflowDesign.vue 翻译模块集成说明

## 概述

本文档说明如何在 `WorkflowDesign.vue` 中集成新的 BPMN 翻译模块，以及如何使用相关的调试功能。

## 集成内容

### 1. 导入新的翻译模块

```typescript
// 导入新的翻译工具
import {
  getTranslateModule,
  addCustomTranslations,
  getPerformanceStats,
} from '@/utils/bpmn/translateUtils'
```

### 2. 使用新的翻译模块

```typescript
// 替换原有的翻译模块配置
const customTranslateModule = getTranslateModule()
```

### 3. 添加项目特定翻译

```typescript
// 添加项目特定的自定义翻译
addCustomTranslations({
  'Leave Approval Process': '请假审批流程',
  'Start Application': '开始申请',
  'Fill Leave Application': '填写请假申请',
  // ... 更多项目特定翻译
})
```

## 新增功能

### 1. 翻译性能监控

- **实时统计**: 监控翻译次数、缓存命中率等性能指标
- **性能输出**: 每10次翻译自动输出性能统计到控制台
- **手动刷新**: 支持手动刷新性能统计数据

### 2. 调试面板

#### 功能特性

- **性能统计显示**: 实时显示翻译性能数据
- **手动控制**: 支持刷新和重置统计
- **快捷键切换**: 使用 `Ctrl+Shift+D` 切换调试面板显示

#### 显示内容

- 总翻译次数
- 缓存命中次数
- 缓存未命中次数
- 缓存命中率

#### 使用方法

1. 在 WorkflowDesign 页面按 `Ctrl+Shift+D` 打开调试面板
2. 点击"刷新统计"按钮更新数据
3. 点击"重置统计"按钮清空统计数据

### 3. 键盘快捷键

| 快捷键         | 功能                 |
| -------------- | -------------------- |
| `Ctrl+Shift+D` | 切换翻译性能调试面板 |

## 性能优化

### 1. 缓存机制

- 翻译结果自动缓存，避免重复查找
- 自定义翻译会覆盖默认翻译
- 缓存会在添加新翻译时自动清除

### 2. 性能统计

```typescript
// 获取性能统计
const stats = getPerformanceStats()
console.log('Translation performance:', stats)
// 输出: { totalTranslations: 150, cacheHits: 100, cacheMisses: 50, cacheHitRate: '66.67%' }
```

### 3. 批量翻译

```typescript
// 批量翻译多个模板
const templates = ['User task', 'Service task', 'Gateway']
const results = translateUtils.translateBatch(templates)
```

## 调试功能

### 1. 控制台输出

- 翻译服务状态检查
- 翻译测试结果
- 属性面板标题翻译
- 性能统计更新

### 2. 调试面板

- 实时性能数据显示
- 手动统计控制
- 可视化性能监控

### 3. 错误处理

- 翻译服务异常捕获
- 优雅的错误处理
- 详细的错误日志

## 使用示例

### 1. 基本使用

```typescript
// 在 BPMN Modeler 初始化时使用
modeler = new BpmnModeler({
  container: bpmnContainer.value,
  propertiesPanel: {
    parent: propertiesPanel.value,
  },
  additionalModules: [
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    CamundaPlatformPropertiesProviderModule,
    customTranslateModule, // 使用新的翻译模块
    elementLabelModule,
  ],
  moddleExtensions: {
    camunda: camundaModdleDescriptor,
  },
})
```

### 2. 添加自定义翻译

```typescript
// 添加项目特定的翻译
addCustomTranslations({
  'Project Specific Element': '项目特定元素',
  'Custom Property': '自定义属性',
  'Business Rule': '业务规则',
})
```

### 3. 性能监控

```typescript
// 在翻译更新时记录性能
const updateHeader = () => {
  setTimeout(() => {
    // ... 翻译逻辑
    const stats = getPerformanceStats()
    performanceStats.value = stats
    if (stats.totalTranslations % 10 === 0) {
      console.log('Translation performance update:', stats)
    }
  }, 100)
}
```

## 最佳实践

### 1. 翻译管理

- 将项目特定的翻译集中管理
- 使用有意义的翻译键名
- 定期检查和更新翻译内容

### 2. 性能优化

- 监控缓存命中率，优化翻译策略
- 避免频繁添加和移除翻译
- 合理使用批量翻译功能

### 3. 调试建议

- 在开发环境中启用调试面板
- 定期检查性能统计
- 及时处理翻译异常

## 注意事项

1. **兼容性**: 新翻译模块保持与原有 API 的兼容性
2. **性能**: 缓存机制会显著提高翻译性能
3. **调试**: 调试面板仅在开发环境中使用
4. **扩展**: 支持动态添加和移除翻译

## 故障排除

### 1. 翻译不生效

- 检查翻译模块是否正确导入
- 确认翻译键是否正确
- 查看控制台错误信息

### 2. 性能问题

- 检查缓存命中率
- 确认是否有重复翻译
- 查看性能统计数据

### 3. 调试面板不显示

- 确认快捷键是否正确
- 检查是否有其他事件冲突
- 查看控制台错误信息

## 总结

通过集成新的翻译模块，`WorkflowDesign.vue` 获得了：

1. **更好的性能**: 缓存机制和性能监控
2. **更强的功能**: 批量翻译和自定义翻译
3. **更好的调试**: 可视化调试面板和详细日志
4. **更好的维护**: 模块化设计和类型安全

这些改进使得 BPMN 工作流设计器的翻译功能更加完善和易用。
