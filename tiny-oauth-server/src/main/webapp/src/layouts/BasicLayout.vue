<template>
  <!-- 主体布局容器 -->
  <div class="layout">
    <!-- 左侧侧边栏 -->
    <Sider />
    <!-- 右侧主区域 -->
    <div class="main">
      <!-- 顶部栏，插入标签页导航 -->
      <HeaderBar>
        <template #tags>
          <TagTabs />
        </template>
      </HeaderBar>
      <!-- 内容区，展示当前路由页面 -->
      <div class="content">
        <router-view :key="$route.fullPath + ($route.query._refresh || '')" v-slot="{ Component }">
          <!-- 使用 keep-alive 缓存页面 -->
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
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

const moreBtnRef = ref(null)
const moreMenuPos = ref({ left: 0, top: 0 })

function toggleMoreMenu() {
  moreMenuVisible.value = !moreMenuVisible.value
  if (moreMenuVisible.value && moreBtnRef.value) {
    const rect = moreBtnRef.value.getBoundingClientRect()
    moreMenuPos.value = {
      left: rect.left - 120, // 菜单宽度
      top: rect.bottom
    }
    document.addEventListener('click', closeMoreMenu)
  }
}
</script>

<style scoped>
.layout {
  /* 设置布局容器高度为100vh，保证全屏显示 */
  min-height: 100vh;
  /* 可选：设置全局背景色 */
  background: #f5f7fa; /* 参考 Ant Design Pro 默认背景 */
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
  box-sizing: border-box; /* 让padding包含在高度内 */
}
.more-menu-btn {
  position: relative;
  /* ...其他样式... */
}
.more-context-menu {
  position: fixed;
  min-width: 120px;
  /* 其余样式保持不变 */
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-radius: 4px;
  background: #fff;
  border: 1px solid #eee;
  padding: 4px 0;
  color: #333;
  font-size: 14px;
  user-select: none;
  z-index: 10;
}
.layout {
  /* 设置布局容器高度为100vh，保证全屏显示 */
  min-height: 100vh;
  /* 可选：设置全局背景色 */
  background: #f5f7fa; /* 参考 Ant Design Pro 默认背景 */
}
.header-bar {
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}
.tag-tabs {
  margin-bottom: 0 !important;
  padding-bottom: 0 !important;
}
.sider-header, .header-bar {
  height: 56px; /* 统一高度 */
  color: #fff;         /* 统一字体色 */
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04); /* 如主HeaderBar有阴影 */
}
.sider {
  width: var(--sider-width-expanded);
  transition: width 0.2s;
}
.sider.collapsed {
  width: var(--sider-width-collapsed);
}
</style> 