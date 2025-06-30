<template>
  <!-- 动态渲染官方 icons-vue 图标，未找到时兜底为 MenuOutlined -->
  <component :is="iconComponent" :class="iconClass" v-if="iconComponent" />
</template>

<script setup lang="ts">
// 引入 vue 相关方法
import { computed } from 'vue'
// 引入 ant-design-vue 所有图标
import * as allIcons from '@ant-design/icons-vue'

// 定义 props，icon 为图标名，className 为自定义类名
const props = defineProps<{
  icon: string, // 图标名称
  className?: string // 可选自定义类名
}>()

// 计算实际要渲染的图标组件，找不到时兜底为 MenuOutlined
const iconComponent = computed(() => {
  try {
    if (!props.icon || typeof props.icon !== 'string') {
      return allIcons.MenuOutlined
    }
    
    const iconName = props.icon.trim()
    if (!iconName) {
      return allIcons.MenuOutlined
    }
    
    const component = (allIcons as any)[iconName]
    if (component && (typeof component === 'object' || typeof component === 'function')) {
      return component
    }
    
    return allIcons.MenuOutlined
  } catch (error) {
    console.warn('Icon component error:', error)
    return allIcons.MenuOutlined
  }
})

// 组合 class，支持外部传入 className
const iconClass = computed(() => {
  try {
    const baseClass = 'icon-vue'
    return props.className ? `${baseClass} ${props.className}` : baseClass
  } catch (error) {
    console.warn('Icon class error:', error)
    return 'icon-vue'
  }
})
</script>

<style scoped>
/* 通用图标样式，可根据需要覆盖 */
.icon-vue {
  font-size: 18px; /* 默认大小 */
  color: inherit;  /* 继承父元素的颜色 */
  vertical-align: middle;
}
</style> 