<template>
  <!-- 基础布局容器 -->
  <div class="layout">
    <!-- 侧边栏 -->
    <Sider class="sider" />
    <!-- 主内容区 -->
    <div class="main">
      <!-- 顶部导航栏 -->
      <HeaderBar class="header-bar" />
      <!-- 标签页导航 -->
      <TagTabs class="tag-tabs" />
      <!-- 主内容区域 -->
      <div class="content">
        <!-- 使用 key 强制重新渲染组件，实现刷新功能 -->
        <router-view :key="routerViewKey" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sider from './Sider.vue'
import HeaderBar from './HeaderBar.vue'
import TagTabs from './TagTabs.vue'

/**
 * 组件配置
 */
defineOptions({
  name: 'BasicLayout'
})

/**
 * 路由实例
 */
const route = useRoute()

/**
 * 计算 router-view 的 key
 * 当 _refresh 参数变化时，key 会变化，从而强制重新渲染组件
 */
const routerViewKey = computed(() => {
  // 使用路径和 _refresh 参数组合作为 key
  // 当 _refresh 参数变化时，组件会重新渲染
  return `${route.path}-${route.query._refresh || 'default'}`
})
</script>

<style scoped>
/**
 * 基础布局样式
 * 
 * 布局结构：
 * - layout: 最外层容器，使用 flex 横向布局
 *   - sider: 侧边栏，固定宽度
 *   - main: 主内容区，自适应宽度
 *     - header-bar: 顶部导航栏
 *     - tag-tabs: 标签页导航
 *     - content: 主内容区域（router-view）
 */

/* 布局容器 */
.layout {
  display: flex;
  min-height: 100vh;
  height: 100vh;
  background: #f0f2f5; /* 整体背景色 */
}

/* 侧边栏 */
.sider {
  flex-shrink: 0; /* 不允许收缩 */
  width: var(--sider-width-expanded); /* 使用 CSS 变量定义宽度 */
  transition: width 0.2s; /* 宽度变化动画 */
}

/* 主内容区 */
.main {
  flex: 1; /* 占据剩余空间 */
  min-width: 0; /* 允许 flex 子元素收缩到内容宽度以下 */
  display: flex;
  flex-direction: column; /* 纵向布局 */
  transition: width 0.2s; /* 宽度变化动画（侧边栏折叠/展开时） */
}

/* 顶部导航栏 */
.header-bar {
  flex-shrink: 0; /* 不允许收缩 */
  margin-bottom: 0;
  padding-bottom: 0;
}

/* 标签页导航 */
.tag-tabs {
  flex-shrink: 0; /* 不允许收缩 */
  margin-bottom: 0;
  padding-bottom: 0;
}

/* 主内容区域 */
.content {
  flex: 1; /* 占据剩余空间 */
  padding: var(--content-padding); /* 使用 CSS 变量定义内边距 */
  overflow: auto; /* 内容溢出时显示滚动条 */
  background: #f0f2f5; /* 内容区域背景色 */
  box-sizing: border-box; /* 确保 padding 包含在高度内 */
}
</style>
