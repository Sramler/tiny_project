# 实时翻译调用追踪系统

## 概述

本系统实现了跨页面的实时翻译调用追踪功能，允许翻译调用追踪器页面实时接收工作流设计器页面的翻译调用数据。

## 功能特性

### 🔄 实时通信

- 基于 localStorage 的跨页面通信机制
- 自动检测页面连接状态
- 实时更新翻译调用统计

### 📊 数据追踪

- 记录翻译调用的详细信息（键、来源、组件、时间戳、结果）
- 支持按来源、组件、翻译键进行筛选
- 提供详细的调用历史和分析报告

### 🎯 智能检测

- 自动检测调用来源（palette、contextPad、propertiesPanel等）
- 自动识别调用组件（Palette、ContextPad、PropertiesPanel等）
- 支持调用栈信息记录

## 使用方法

### 1. 启动实时追踪

#### 方法一：同时打开两个页面

1. 打开工作流设计器页面：`http://localhost:5173/workflowDesign`
2. 打开翻译调用追踪器页面：`http://localhost:5175/translationCallTracker`
3. 在工作流设计器中进行操作，翻译调用追踪器会自动接收数据

#### 方法二：使用测试页面

1. 打开翻译测试页面：`http://localhost:5175/translationTest`
2. 点击"模拟工作流设计器调用"按钮
3. 观察翻译调用追踪器的实时更新

### 2. 查看实时数据

在翻译调用追踪器页面中，您可以：

- **系统状态面板**：查看翻译系统加载状态和实时连接状态
- **统计概览**：查看总调用次数、唯一翻译键、来源数量等
- **详细统计**：按来源和组件查看调用分布
- **调用历史**：查看详细的翻译调用记录

### 3. 筛选和分析

- **按来源筛选**：选择特定的调用来源（如 palette、contextPad）
- **按组件筛选**：选择特定的组件（如 Palette、ContextPad）
- **按翻译键搜索**：搜索特定的翻译键
- **导出报告**：导出详细的调用分析报告

## 技术实现

### 跨页面通信机制

```typescript
// 发送实时数据
function broadcastTranslationCall(call: TranslationCall): void {
  const eventData = {
    type: 'translation_call',
    pageId: currentPageId,
    key: call.key,
    source: call.source,
    component: call.component,
    timestamp: call.timestamp,
    result: call.result,
  }

  localStorage.setItem(REAL_TIME_EVENT_KEY, JSON.stringify(eventData))
}

// 接收实时数据
window.addEventListener('storage', (event) => {
  if (event.key === REAL_TIME_EVENT_KEY && event.newValue) {
    const data = JSON.parse(event.newValue)
    if (data.pageId !== currentPageId && data.type === 'translation_call') {
      // 处理接收到的翻译调用数据
      handleTranslationCall(data)
    }
  }
})
```

### 数据存储

翻译调用数据存储在以下位置：

1. **内存缓存**：`translationCallHistory` 数组
2. **本地存储**：`localStorage` 中的 `bpmn_translation_call_history`
3. **页面通信**：`localStorage` 中的 `bpmn_translation_realtime_event`

### 页面标识

每个页面都有唯一的标识符：

```typescript
const currentPageId = Math.random().toString(36).substr(2, 9)
localStorage.setItem('bpmn_translation_page_id', currentPageId)
```

## 配置选项

### 翻译调用追踪器配置

```typescript
// 自动刷新间隔（毫秒）
const autoRefreshInterval = 5000

// 最大历史记录数量
const maxCallHistory = 1000

// 页面大小
const pageSize = 20
```

### 工作流设计器配置

```typescript
// 调试面板显示
initDebug({
  enabled: true,
  showPanel: true,
  showButton: true,
  autoRefresh: false,
  refreshInterval: 5000,
})

// 初始化跨页面通信
initRealTimeCommunication()
```

## 故障排除

### 常见问题

1. **实时连接未建立**

   - 确保工作流设计器页面已打开
   - 检查浏览器是否支持 localStorage
   - 确认两个页面在同一域名下

2. **数据不同步**

   - 刷新翻译调用追踪器页面
   - 检查浏览器控制台是否有错误信息
   - 确认翻译系统已正确加载

3. **性能问题**
   - 减少自动刷新频率
   - 清理历史记录
   - 限制最大记录数量

### 调试方法

1. **查看控制台日志**

   ```javascript
   // 启用调试日志
   setDebugLogs(true)
   ```

2. **检查连接状态**

   ```javascript
   // 查看页面ID
   console.log(localStorage.getItem('bpmn_translation_page_id'))

   // 查看历史记录
   console.log(getTranslationCallHistory())
   ```

3. **手动触发同步**

   ```javascript
   // 刷新统计数据
   refreshStats()

   // 同步工作流设计器数据
   syncWorkflowDesignerData()
   ```

## API 参考

### 核心函数

```typescript
// 初始化跨页面通信
initRealTimeCommunication(): void

// 清理跨页面通信
cleanupRealTimeCommunication(): void

// 获取翻译调用历史
getTranslationCallHistory(): TranslationCall[]

// 获取翻译调用统计
getTranslationCallStats(): TranslationCallStats

// 清除调用历史
clearCallHistory(): void

// 导出调用报告
exportCallReport(): void
```

### 数据类型

```typescript
interface TranslationCall {
  key: string
  source: string
  component: string
  timestamp: number
  stack?: string
  result: string
}

interface TranslationCallStats {
  totalCalls: number
  callsBySource: Record<string, number>
  callsByComponent: Record<string, number>
  recentCalls: TranslationCall[]
  uniqueKeys: string[]
}
```

## 最佳实践

1. **性能优化**

   - 定期清理历史记录
   - 使用适当的刷新间隔
   - 避免在短时间内产生大量调用

2. **数据管理**

   - 定期导出重要数据
   - 监控存储空间使用情况
   - 及时处理异常数据

3. **用户体验**
   - 提供清晰的状态指示
   - 支持实时数据更新
   - 提供丰富的筛选和分析功能

## 更新日志

### v1.0.0 (2024-03-21)

- 实现跨页面实时通信机制
- 添加翻译调用追踪功能
- 支持实时数据同步和状态监控
- 提供完整的用户界面和交互功能
