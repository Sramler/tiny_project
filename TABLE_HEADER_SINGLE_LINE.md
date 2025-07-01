# 表头单行显示控制方案

## 问题描述

在角色管理页面的表格中，表头可能会因为文字过长或容器宽度不足而换行显示，影响表格的美观性和一致性。

## 解决方案

### 1. 列宽度设置

**文件：** `tiny-oauth-server/src/main/webapp/src/views/role/role.vue`

为所有列设置合适的固定宽度：

```javascript
const INITIAL_COLUMNS = [
  { title: "ID", dataIndex: "id", width: 80 },
  { title: "角色名", dataIndex: "name", width: 120 },
  { title: "角色标识", dataIndex: "code", width: 150 },
  { title: "描述", dataIndex: "description", width: 200 },
  { title: "是否内置", dataIndex: "builtin", width: 100 },
  { title: "是否启用", dataIndex: "enabled", width: 100 },
  { title: "创建时间", dataIndex: "createdAt", width: 160 },
  { title: "更新时间", dataIndex: "updatedAt", width: 160 },
  {
    title: "操作",
    dataIndex: "action",
    width: 160,
    fixed: "right",
    align: "center",
  },
];
```

### 2. 表头样式控制

#### 基础表头样式

```css
/* 表头单行显示控制 */
:deep(.ant-table-thead > tr > th) {
  white-space: nowrap !important; /* 防止换行 */
  overflow: hidden !important; /* 隐藏溢出内容 */
  text-overflow: ellipsis !important; /* 显示省略号 */
  word-break: keep-all !important; /* 防止单词断开 */
  line-height: 1.2 !important; /* 控制行高 */
  padding: 12px 8px !important; /* 调整内边距 */
  height: 48px !important; /* 固定表头高度 */
  vertical-align: middle !important; /* 垂直居中 */
  min-width: 80px !important; /* 最小宽度 */
  max-width: none !important; /* 不限制最大宽度 */
}
```

#### 表头文字样式

```css
/* 表头文字样式 */
:deep(.ant-table-thead > tr > th .ant-table-column-title) {
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  display: block !important;
  width: 100% !important;
  flex: 1 !important;
  min-width: 0 !important;
}
```

#### 表头布局控制

```css
/* 表头单元格内容布局 */
:deep(.ant-table-thead > tr > th > div) {
  white-space: nowrap !important;
  overflow: hidden !important;
  text-overflow: ellipsis !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  height: 100% !important;
}

/* 表头排序图标位置调整 */
:deep(.ant-table-thead > tr > th .ant-table-column-sorter) {
  margin-left: 4px !important;
  flex-shrink: 0 !important;
}

/* 表头复选框位置调整 */
:deep(.ant-table-thead > tr > th .ant-checkbox-wrapper) {
  margin: 0 !important;
  flex-shrink: 0 !important;
}

/* 确保表头行高度固定 */
:deep(.ant-table-thead > tr) {
  height: 48px !important;
}
```

### 3. 表格滚动配置

确保表格有足够的水平滚动空间：

```vue
<a-table
  :scroll="{ x: 'max-content', y: tableBodyHeight }"
  <!-- 其他属性 -->
>
```

## 技术说明

### 关键 CSS 属性

1. **white-space: nowrap** - 防止文本换行
2. **overflow: hidden** - 隐藏溢出内容
3. **text-overflow: ellipsis** - 显示省略号
4. **word-break: keep-all** - 防止单词断开
5. **line-height: 1.2** - 控制行高
6. **height: 48px** - 固定表头高度

### 布局策略

1. **Flexbox 布局** - 使用 flex 布局控制表头内容排列
2. **固定高度** - 确保表头行高度一致
3. **最小宽度** - 防止列过窄导致文字挤压
4. **省略号处理** - 长文本显示省略号而不是换行

### 兼容性考虑

- ✅ 支持所有现代浏览器
- ✅ 响应式设计友好
- ✅ 不影响表格功能（排序、筛选等）
- ✅ 保持无障碍访问性

## 效果展示

### 修改前

- 表头可能换行显示
- 行高不一致
- 文字可能被截断

### 修改后

- 表头始终单行显示
- 统一的行高（48px）
- 长文本显示省略号
- 美观的布局

## 验证方法

1. 打开角色管理页面
2. 检查表头是否单行显示
3. 调整浏览器窗口大小，验证响应式效果
4. 检查长文本是否正确显示省略号
5. 验证表格功能（排序、筛选）是否正常

## 注意事项

1. **列宽度设置**：根据实际内容长度合理设置列宽
2. **文字长度**：避免使用过长的列标题
3. **响应式设计**：在小屏幕上可能需要调整列宽
4. **功能保持**：确保样式修改不影响表格的交互功能

## 扩展应用

此方案可以应用到其他表格页面：

1. 用户管理表格
2. 资源管理表格
3. 菜单管理表格
4. 其他数据表格

只需要将相应的 CSS 样式复制到对应的 Vue 组件中即可。
