# DebugI18nPanel 筛选面板功能测试

## 功能概述

已成功用 `a-form` 重写筛选面板，实现了与表格的完整联动。

## 新增功能

### 1. 现代化表单设计
- 使用 Ant Design Vue 的 `a-form` 组件
- 支持内联布局 (`layout="inline"`)
- 响应式设计，适配不同屏幕尺寸

### 2. 增强的筛选条件
- **来源筛选**: 下拉选择框，支持清除
- **组件筛选**: 下拉选择框，支持清除
- **翻译来源筛选**: 支持官方翻译、模块翻译、临时翻译、未翻译
- **搜索键筛选**: 文本输入框，支持回车搜索
- **时间范围筛选**: 日期时间范围选择器
- **栈深度筛选**: 数字输入框，支持最小值和最大值

### 3. 快速筛选按钮
- 未翻译
- 官方翻译
- 模块翻译
- 临时翻译
- 支持切换状态显示

### 4. 筛选结果统计
- 实时显示筛选结果数量
- 用标签显示当前激活的筛选条件
- 支持多个筛选条件同时显示

### 5. 表格联动功能
- 筛选条件变化时自动更新表格数据
- 支持分页、排序、快速跳转
- 表格加载状态显示
- 支持导出筛选结果

### 6. 数据导出功能
- 导出筛选结果为 CSV 文件
- 包含所有表格列数据
- 文件名包含时间戳

## 使用方法

### 基本筛选
1. 在来源下拉框中选择特定来源
2. 在组件下拉框中选择特定组件
3. 在翻译来源下拉框中选择翻译类型
4. 在搜索框中输入翻译键关键词
5. 点击"查询"按钮执行筛选

### 时间范围筛选
1. 点击时间范围选择器
2. 选择开始时间和结束时间
3. 筛选结果会自动更新

### 栈深度筛选
1. 在最小深度输入框中输入数值
2. 在最大深度输入框中输入数值
3. 筛选结果会自动更新

### 快速筛选
1. 点击对应的快速筛选按钮（未翻译、官方翻译等）
2. 按钮会高亮显示当前筛选状态
3. 再次点击可取消筛选

### 重置筛选
1. 点击"重置"按钮
2. 所有筛选条件将被清空
3. 表格显示所有数据

### 导出数据
1. 点击"导出筛选结果"按钮
2. 系统会自动下载 CSV 文件
3. 文件名格式：`translation_debug_YYYY-MM-DDTHH-mm-ss.csv`

## 技术实现

### 响应式数据
```typescript
const filterForm = reactive({
  source: '',
  component: '',
  translationSource: '',
  searchKey: '',
  timeRange: [] as Dayjs[],
  minStackDepth: undefined as number | undefined,
  maxStackDepth: undefined as number | undefined,
  quickFilter: '' as string
})
```

### 计算属性
```typescript
const filteredCalls = computed(() => {
  let calls = [...callHistoryData.value]
  
  // 应用各种筛选条件
  if (filterForm.source) {
    calls = calls.filter(call => call.source === filterForm.source)
  }
  
  // ... 其他筛选逻辑
  
  return calls.sort((a, b) => b.timestamp - a.timestamp)
})
```

### 表单事件处理
```typescript
const handleFilterSubmit = () => {
  currentPage.value = 1
  message.success(`筛选完成，共找到 ${filteredCalls.value.length} 条记录`)
}

const handleFilterReset = () => {
  Object.assign(filterForm, {
    source: '',
    component: '',
    translationSource: '',
    searchKey: '',
    timeRange: [],
    minStackDepth: undefined,
    maxStackDepth: undefined,
    quickFilter: ''
  })
  currentPage.value = 1
  message.success('筛选条件已重置')
}
```

## 样式优化

### 深度选择器样式
```css
.filter-panel :deep(.ant-form) {
  margin-bottom: 8px;
}

.filter-panel :deep(.ant-form-item) {
  margin-bottom: 8px;
  margin-right: 12px;
}

.filter-panel :deep(.ant-form-item-label > label) {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}
```

### 筛选结果统计样式
```css
.filter-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
}
```

## 测试要点

1. **基本筛选功能**
   - 验证各个筛选条件是否正常工作
   - 验证筛选结果是否正确显示

2. **组合筛选**
   - 测试多个筛选条件同时使用
   - 验证筛选逻辑是否正确

3. **实时更新**
   - 验证筛选条件变化时表格是否实时更新
   - 验证分页是否正确重置

4. **数据导出**
   - 验证 CSV 导出功能是否正常
   - 验证导出数据是否完整

5. **用户体验**
   - 验证表单交互是否流畅
   - 验证错误提示是否友好

## 兼容性

- 保持与原有筛选逻辑的兼容性
- 支持旧的快速筛选按钮功能
- 保持原有的统计数据显示

## 性能优化

- 使用计算属性缓存筛选结果
- 分页加载减少渲染压力
- 防抖处理避免频繁更新 