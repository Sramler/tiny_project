<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <Sider class="sider" :class="{ collapsed: collapsed }" />
    <!-- 主内容区 -->
    <div class="main">
      <!-- 顶部导航栏 -->
      <HeaderBar class="header-bar" />
      <!-- 标签页导航 -->
      <TagTabs class="tag-tabs" />
      <!-- 主内容区域 -->
      <div class="content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 引入自定义组件（已移动到 layouts 目录）
import Sider from './Sider.vue'
import HeaderBar from './HeaderBar.vue'
import TagTabs from './TagTabs.vue'
import { ref } from 'vue'

const collapsed = ref(false)
</script>

<style scoped>
.layout {
  /* 设置布局容器高度为100vh，保证全屏显示 */
  min-height: 100vh;
  /* 可选：设置全局背景色 */
  background: #f5f7fa;
  /* 参考 Ant Design Pro 默认背景 */
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.main {
  flex: 1;
  min-width: 0;
  /* 可选：transition 让内容区宽度变化更平滑 */
  transition: width 0.2s;
  display: flex;
  flex-direction: column;
}

.content {
  flex: 1;
  padding: var(--content-padding);
  overflow: auto;
  background: #f0f2f5;
  box-sizing: border-box;
  /* 让padding包含在高度内 */
}

.layout {
  /* 设置布局容器高度为100vh，保证全屏显示 */
  min-height: 100vh;
  /* 可选：设置全局背景色 */
  background: #f5f7fa;
  /* 参考 Ant Design Pro 默认背景 */
}

.header-bar {
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}

.tag-tabs {
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}

.sider-header {
  height: 56px;
  /* 统一高度 */
  color: #fff;
  /* 统一字体色 */
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  /* 如主HeaderBar有阴影 */
}

/* HeaderBar组件样式，不覆盖组件内部样式 */
.header-bar {
  /* 只设置基础布局样式，不覆盖组件内部样式 */
  flex-shrink: 0;
}

.sider {
  width: var(--sider-width-expanded);
  transition: width 0.2s;
}

.sider.collapsed {
  width: var(--sider-width-collapsed);
}
</style>