# 可拖拽翻译调用追踪器面板实现总结

## 实现概述

成功为翻译调用追踪器面板添加了拖拽移动和调整大小功能，用户可以自由调整面板的位置和尺寸。

## 核心功能

### 1. 拖拽移动

- 支持通过面板头部拖拽移动
- 支持通过拖拽手柄（⋮⋮）移动
- 自动限制面板不超出视窗边界
- 拖拽时有视觉反馈

### 2. 调整大小

- 支持水平调整宽度（右边缘）
- 支持垂直调整高度（下边缘）
- 支持对角线同时调整（右下角）
- 有最小尺寸（400x300）和最大尺寸限制

### 3. 视觉反馈

- 拖拽时显示抓取光标
- 调整大小时显示调整光标
- 悬停时增强阴影效果

## 技术实现

### 1. 响应式数据

```typescript
const isDragging = ref(false)
const panelPosition = ref({ x: 20, y: 20 })
const panelSize = ref({ width: 800, height: 600 })
const isResizing = ref(false)
const resizeDirection = ref('')
```

### 2. 拖拽方法

```typescript
const startDrag = (event: MouseEvent) => {
  isDragging.value = true
  dragStart.value = {
    x: event.clientX - panelPosition.value.x,
    y: event.clientY - panelPosition.value.y,
  }
  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}
```

### 3. 调整大小方法

```typescript
const startResize = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  resizeDirection.value = target.className.includes('se')
    ? 'se'
    : target.className.includes('e')
      ? 'e'
      : 's'
  isResizing.value = true
  document.addEventListener('mousemove', onResize)
  document.addEventListener('mouseup', stopResize)
}
```

## 模板结构

```vue
<div v-if="isPanelVisible"
     class="tracker-panel tracker-panel-draggable"
     :style="panelStyle"
     @mousedown="startDrag"
     ref="panelRef">
  <div class="tracker-panel-header" @mousedown.stop>
    <div class="drag-handle" @mousedown="startDrag">
      <span class="drag-icon">⋮⋮</span>
    </div>
    <h3>翻译调用追踪器</h3>
    <button @click="togglePanel" class="close-btn">×</button>
  </div>

  <!-- 调整大小手柄 -->
  <div class="resize-handle resize-handle-se" @mousedown="startResize"></div>
  <div class="resize-handle resize-handle-e" @mousedown="startResize"></div>
  <div class="resize-handle resize-handle-s" @mousedown="startResize"></div>

  <div class="tracker-content">
    <!-- 面板内容 -->
  </div>
</div>
```

## 样式设计

### 1. 面板样式

```css
.tracker-panel-draggable {
  display: flex;
  flex-direction: column;
  min-width: 600px;
  min-height: 400px;
  max-width: 90vw;
  max-height: 90vh;
  user-select: none;
  transition: box-shadow 0.2s ease;
}
```

### 2. 拖拽手柄

```css
.drag-handle {
  width: 24px;
  height: 24px;
  cursor: grab;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.drag-handle:hover {
  background-color: rgba(0, 0, 0, 0.06);
}
```

### 3. 调整大小手柄

```css
.resize-handle {
  position: absolute;
  background: transparent;
  z-index: 1000;
}

.resize-handle-se {
  right: 0;
  bottom: 0;
  width: 12px;
  height: 12px;
  cursor: se-resize;
}
```

## 边界处理

### 1. 位置边界

```typescript
const maxX = window.innerWidth - panelSize.value.width
const maxY = window.innerHeight - panelSize.value.height
panelPosition.value.x = Math.max(0, Math.min(newX, maxX))
panelPosition.value.y = Math.max(0, Math.min(newY, maxY))
```

### 2. 尺寸边界

```typescript
const minWidth = 400
const minHeight = 300
const newWidth = Math.max(minWidth, Math.min(maxWidth, resizeStart.value.width + deltaX))
const newHeight = Math.max(minHeight, Math.min(maxHeight, resizeStart.value.height + deltaY))
```

## 事件清理

```typescript
onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
  document.removeEventListener('mousemove', onResize)
  document.removeEventListener('mouseup', stopResize)
})
```

## 功能特性

- ✅ 拖拽移动面板
- ✅ 调整面板大小
- ✅ 边界限制保护
- ✅ 视觉反馈
- ✅ 流畅操作体验
- ✅ 功能完整性保持

## 总结

可拖拽翻译调用追踪器面板提供了灵活、直观的用户体验，让用户可以根据需求调整面板布局，大大提升了工具的可用性。
