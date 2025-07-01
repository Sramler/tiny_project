# 表格滚动条隐藏解决方案

## 问题描述

在角色管理页面的 `table-scroll-container` 中显示了滚动条，这不符合预期的 UI 设计。

## 解决方案

### 1. 隐藏容器滚动条

**文件：** `tiny-oauth-server/src/main/webapp/src/views/role/role.vue`

```css
.table-scroll-container {
  /* 不要设置 flex: 1; */
  min-height: 0; /* 可选，防止撑开 */
  overflow: auto; /* 内容多时滚动 */
  /* 隐藏滚动条但保持滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE and Edge */
}

/* 隐藏 Webkit 浏览器的滚动条 */
.table-scroll-container::-webkit-scrollbar {
  display: none;
}
```

### 2. 隐藏表格内部滚动条

```css
/* 隐藏所有滚动条但保持滚动功能 */
:deep(.ant-table-body),
:deep(.ant-table-content),
:deep(.ant-table-scroll) {
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* IE and Edge */
}

:deep(.ant-table-body::-webkit-scrollbar),
:deep(.ant-table-content::-webkit-scrollbar),
:deep(.ant-table-scroll::-webkit-scrollbar) {
  display: none !important;
}

/* 确保表格容器也不显示滚动条 */
.table-scroll-container :deep(.ant-table) {
  scrollbar-width: none !important;
  -ms-overflow-style: none !important;
}

.table-scroll-container :deep(.ant-table::-webkit-scrollbar) {
  display: none !important;
}
```

### 3. 优化表格滚动配置

将表格的滚动配置从固定宽度改为自适应：

```vue
<!-- 修改前 -->
:scroll="{ x: 900, y: tableBodyHeight }"

<!-- 修改后 -->
:scroll="{ x: 'max-content', y: tableBodyHeight }"
```

## 技术说明

### 滚动条隐藏方法

1. **Firefox**: 使用 `scrollbar-width: none`
2. **IE/Edge**: 使用 `-ms-overflow-style: none`
3. **Webkit 浏览器** (Chrome/Safari): 使用 `::-webkit-scrollbar { display: none }`

### 保持滚动功能

虽然隐藏了滚动条，但滚动功能仍然保持：

- 鼠标滚轮可以滚动
- 触摸设备可以滑动
- 键盘方向键可以导航

### 兼容性

- ✅ Chrome/Edge (Webkit)
- ✅ Firefox
- ✅ Safari
- ✅ IE 11+

## 验证方法

1. 打开角色管理页面
2. 确保表格内容超出容器高度
3. 验证滚动条不可见
4. 验证鼠标滚轮和触摸滑动功能正常
5. 验证键盘导航功能正常

## 注意事项

1. **用户体验**: 虽然隐藏了滚动条，但用户仍可以通过滚轮、触摸或键盘进行滚动
2. **可访问性**: 键盘导航功能保持完整，符合无障碍设计原则
3. **性能**: 滚动条隐藏不会影响性能，只是视觉上的改变
4. **维护性**: 使用 `!important` 确保样式优先级，避免被其他样式覆盖

## 相关文件

- `tiny-oauth-server/src/main/webapp/src/views/role/role.vue` - 主要修改文件
- 其他表格页面可以参考此方案进行类似修改
