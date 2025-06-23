<template>
  <!-- 标签页导航容器 -->
  <div class="tag-tabs">
    <!-- 标签滚动区域 -->
    <div class="tabs-scroll-area" ref="tabScrollRef">
      <div
        v-for="(tab, idx) in tabs"
        :key="tab.path"
        :class="['tab', { active: tab.path === activePath }]"
        @click="switchTab(tab.path)"
        @contextmenu.prevent="openContextMenu($event, idx)"
        :ref="el => tabRefs[idx] = el"
      >
        <!-- 标签标题部分，自动撑开 -->
        <span class="tab-title">{{ tab.title }}</span>
        <!-- 操作区，关闭和刷新按钮固定在右侧 -->
        <span class="tab-actions">
          <ReloadOutlined
            v-if="tab.path === '/' || (tab.path !== '/' && tab.path === activePath)"
            class="refresh-icon"
            :class="{ spinning: spinningIdx === idx }"
            @click.stop="handleRefresh(idx)"
            title="刷新"
          />
          <CloseOutlined v-if="tab.path !== '/'" class="close" @click.stop="closeTab(idx)" />
        </span>
      </div>
    </div>
    <!-- 溢出标签菜单按钮 -->
    <div v-if="overflowTabs.length > 0" class="overflow-menu-btn" @click.stop="toggleOverflowMenu">
      <EllipsisOutlined class="overflow-icon" />
      <div
        v-if="overflowMenuVisible"
        class="context-menu overflow-context-menu"
        :style="{ left: overflowMenuPos.left + 'px', top: overflowMenuPos.top + 'px' }"
        @mouseleave="closeOverflowMenu"
      >
        <div
          class="menu-item"
          v-for="tab in overflowTabs"
          :key="tab.path"
          @click="switchTab(tab.path); closeOverflowMenu()"
        >
          {{ tab.title }}
        </div>
      </div>
    </div>
    <!-- 右侧三个点菜单按钮 -->
    <div class="more-menu-btn" ref="moreBtnRef" @click.stop="toggleMoreMenu">
      <MoreOutlined class="more-icon" />
      <div
        v-if="moreMenuVisible"
        class="context-menu more-context-menu"
        :style="{ left: moreMenuPos.left + 'px', top: moreMenuPos.top + 'px' }"
        @mouseleave="closeMoreMenu"
      >
        <div class="menu-item" @click="closeOtherTabs(activeTabIdx)">关闭其他</div>
        <div class="menu-item" @click="refreshTab(activeTabIdx)">刷新当前页</div>
      </div>
    </div>
    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }"
      @mouseleave="closeContextMenu"
    >
      <div class="menu-item" @click="refreshTab(contextMenu.idx)">刷新当前页</div>
      <div class="menu-item" @click="closeOtherTabs(contextMenu.idx)">关闭其他</div>
      <div
        class="menu-item"
        v-if="contextMenu.idx > 0"
        @click="closeLeftTabs(contextMenu.idx)"
      >关闭左侧</div>
      <div
        class="menu-item"
        v-if="contextMenu.idx < tabs.length - 1"
        @click="closeRightTabs(contextMenu.idx)"
      >关闭右侧</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
// 引入 Ant Design Icons
import { 
  ReloadOutlined, 
  CloseOutlined, 
  EllipsisOutlined, 
  MoreOutlined 
} from '@ant-design/icons-vue'

const router = useRouter()
const route = useRoute()
const tabScrollRef = ref<HTMLElement | null>(null)
const moreBtnRef = ref<HTMLElement | null>(null)

// 标签页列表，初始包含首页，类型明确
const tabs = ref<{ title: string, path: string }[]>([
  { title: '工作台', path: '/' },
])
// 当前激活标签页路径
const activePath = ref(route.path)

// 右键菜单状态
const contextMenu = ref({ visible: false, x: 0, y: 0, idx: -1 })
// 正在旋转的刷新图标索引
const spinningIdx = ref(-1)
// 右侧更多菜单状态
const moreMenuVisible = ref(false)
// 当前激活tab索引
const activeTabIdx = computed(() => tabs.value.findIndex(tab => tab.path === activePath.value))
// 右侧更多菜单位置
const moreMenuPos = ref({ left: 0, top: 0 })

// 标签DOM引用数组
const tabRefs = ref<any[]>([])
// 溢出标签列表
const overflowTabs = ref<any[]>([])
// 溢出菜单显示状态
const overflowMenuVisible = ref(false)
// 溢出菜单位置
const overflowMenuPos = ref({ left: 0, top: 0 })

// 监听路由变化，自动添加新标签页
watch(
  () => route.path,
  async (newPath) => {
    activePath.value = newPath
    // 优先用 menu.json 的 title
    let title = route.meta.menuInfo?.title || route.meta.title || '未知页面' // 优先取 menuInfo.title
    // 如果标签不存在则添加
    if (!tabs.value.find(tab => tab.path === newPath)) {
      tabs.value.push({ title: title as string, path: newPath }) // 明确类型，避免类型报错
    } else {
      // 已存在则更新标题（防止菜单数据变化）
      const tab = tabs.value.find(tab => tab.path === newPath) as { title: string, path: string }
      if (tab) tab.title = title // 明确类型，避免类型报错
    }
    // 跳转后自动滚动到当前标签
    await nextTick()
    scrollToActiveTab()
  },
  { immediate: true }
)
// 切换标签页
function switchTab(path: string) {
  router.push(path)
}
// 关闭标签页
function closeTab(idx: number) {
  // 关闭后如果是当前页，自动切换到前一个标签
  if (tabs.value[idx].path === activePath.value) {
    const next = tabs.value[idx - 1] || tabs.value[idx + 1] || { path: '/' }
    router.push(next.path)
  }
  tabs.value.splice(idx, 1)
}
// 滚动到当前激活标签
function scrollToActiveTab() {
  const container = tabScrollRef.value
  if (!container) return
  const active = container.querySelector('.tab.active') as HTMLElement
  if (active) {
    const left = active.offsetLeft
    const width = active.offsetWidth
    const scrollLeft = container.scrollLeft
    const containerWidth = container.offsetWidth
    if (left < scrollLeft) {
      container.scrollLeft = left
    } else if (left + width > scrollLeft + containerWidth) {
      container.scrollLeft = left + width - containerWidth
    }
  }
}
// 右键菜单相关
function openContextMenu(e: MouseEvent, idx: number) {
  contextMenu.value.visible = true
  contextMenu.value.x = e.clientX
  contextMenu.value.y = e.clientY
  contextMenu.value.idx = idx
  document.addEventListener('click', closeContextMenu)
}
function closeContextMenu() {
  contextMenu.value.visible = false
  contextMenu.value.idx = -1
  document.removeEventListener('click', closeContextMenu)
}
function closeOtherTabs(idx: number) {
  const current = tabs.value[idx]
  tabs.value = tabs.value.filter((tab, i) => i === idx || tab.path === '/')
  // 保证当前标签激活
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeContextMenu(); closeMoreMenu();
}
function closeLeftTabs(idx: number) {
  const current = tabs.value[idx]
  tabs.value = tabs.value.filter((tab, i) => i >= idx || tab.path === '/')
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeContextMenu(); closeMoreMenu();
}
function closeRightTabs(idx: number) {
  const current = tabs.value[idx]
  tabs.value = tabs.value.filter((tab, i) => i <= idx || tab.path === '/')
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeContextMenu(); closeMoreMenu();
}
function refreshTab(idx: number) {
  // 简单实现：重新 push 当前路由
  router.replace({ path: tabs.value[idx].path, query: { ...route.query, _refresh: Date.now() } })
  closeContextMenu(); closeMoreMenu();
}
// 刷新图标点击动画
function handleRefresh(idx: number) {
  spinningIdx.value = idx
  refreshTab(idx)
  setTimeout(() => {
    if (spinningIdx.value === idx) spinningIdx.value = -1
  }, 600)
}
// 右侧更多菜单
function toggleMoreMenu() {
  moreMenuVisible.value = !moreMenuVisible.value
  if (moreMenuVisible.value && moreBtnRef.value) {
    const rect = moreBtnRef.value.getBoundingClientRect()
    // 右侧与按钮对齐，菜单下方弹出
    moreMenuPos.value = {
      left: rect.right - 120, // 120为菜单宽度
      top: rect.bottom
    }
    document.addEventListener('click', closeMoreMenu)
  }
}
function closeMoreMenu() {
  moreMenuVisible.value = false
  document.removeEventListener('click', closeMoreMenu)
}

// 计算溢出标签
function calcOverflowTabs() {
  // 获取滚动区域和所有标签的DOM
  const container = tabScrollRef.value
  if (!container) return
  const containerLeft = container.scrollLeft
  const containerRight = containerLeft + container.offsetWidth
  // 计算哪些标签未完全显示在可视区域
  overflowTabs.value = tabs.value.filter((tab, idx) => {
    const el = tabRefs.value[idx] as HTMLElement
    if (!el) return false
    const left = el.offsetLeft
    const right = left + el.offsetWidth
    // 如果标签的右侧小于可视区左侧，或左侧大于可视区右侧，则为溢出
    return right > containerRight || left < containerLeft
  })
}
// 监听窗口变化和标签变化
onMounted(() => {
  window.addEventListener('resize', calcOverflowTabs)
  if (tabScrollRef.value) {
    tabScrollRef.value.addEventListener('scroll', calcOverflowTabs)
  }
  watch([tabs, activePath], async () => {
    await nextTick()
    calcOverflowTabs()
  }, { immediate: true })
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', calcOverflowTabs)
  if (tabScrollRef.value) {
    tabScrollRef.value.removeEventListener('scroll', calcOverflowTabs)
  }
})
// 溢出菜单显示/隐藏
function toggleOverflowMenu(e: MouseEvent) {
  overflowMenuVisible.value = !overflowMenuVisible.value
  // 计算菜单位置（右对齐按钮，展示在按钮下方）
  const btn = (e?.currentTarget as HTMLElement) || null
  if (btn) {
    const rect = btn.getBoundingClientRect()
    const menuWidth = 140 // 设定菜单宽度，与样式保持一致
    // 计算菜单左侧，使其右对齐按钮
    let left = rect.right - menuWidth
    // 防止菜单超出屏幕左侧
    if (left < 0) left = 0
    overflowMenuPos.value = {
      left,
      top: rect.bottom
    }
  }
  if (overflowMenuVisible.value) {
    document.addEventListener('click', closeOverflowMenu)
  }
}
function closeOverflowMenu() {
  overflowMenuVisible.value = false
  document.removeEventListener('click', closeOverflowMenu)
}
</script>

<style scoped>
/* 标签栏主容器，去掉底部分割线 */
.tag-tabs {
  display: flex;
  align-items: center;
  background: #fafbfc; /* 浅灰背景 */
  padding: 0 12px 0 0; /* 只保留右侧内边距，左侧为0，确保第一个tab紧贴左侧 */
  height: 40px; /* 稍高，显得更精致 */
  position: relative;  /* 保证子元素定位 */
  min-width: 0;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
  /* 移除高饱和色背景 */
}

/* 标签滚动区域 */
.tabs-scroll-area {
  display: flex;
  flex: 1 1 auto;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding-bottom: -1px;
  margin-bottom: -1px; /* 让激活标签底部线和下方分割线重叠 */
}
.tabs-scroll-area::-webkit-scrollbar {
  display: none;
}

/* 单个标签 */
.tab {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  margin-right: 2px; /* 标签间隙 */
  height: 40px; /* 标签高度与容器一致 */
  line-height: 40px; /* 让内容垂直居中 */
  background: #f7f8fa; /* 浅灰色背景 */
  color: #222;
  border: none;        /* 去掉所有边框 */
  border-bottom: 1px solid #e5e6eb; /* 底部分割线 */
  border-right: 1px solid #f0f0f0; /* 右侧弱分割线 */
  border-radius: 0;    /* 去掉圆角 */
  cursor: pointer;
  position: relative;
  transition: background 0.2s, color 0.2s;
  white-space: nowrap;
  min-width: 80px; /* 更小的最小宽度 */
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 0 0 auto;
  box-shadow: none;    /* 去掉阴影 */
  z-index: 1;
}
/* 第一个标签左侧无间隙，右侧间隙也为0 */
.tab:first-child {
  margin-left: 0;
  margin-right: 0; /* 让第一个标签紧贴左侧 */
}
.tab:last-child {
  border-right: none; /* 最后一个标签不显示右分割线 */
}
.tab:hover {
  background: #f0f5ff; /* 悬浮主色浅色 */
  color: #1890ff;
}
.tab.active {
  background: #f0f2f5; /* 白色背景 */
  color: #1890ff;   /* 蓝色文字 */
  border: none;     /* 去掉所有边框 */
  border-bottom: none !important; /* 彻底去掉底部线条 */
  border-radius: 0;
  box-shadow: none;
  z-index: 2;
  position: relative;
}
.tab.active span,
.tab.active .refresh-icon {
  color: #1890ff !important; /* 强制蓝色 */
}
.tab:focus {
  outline: none;
  background: #e6f7ff;
  color: #1890ff;
}
.tab:focus span {
  color: #1890ff;
}

/* 标签标题部分 */
.tab-title {
  flex: 1 1 auto;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
  font-weight: 500;
}

/* 标签操作区 */
.tab-actions {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  margin-left: 8px;
}

/* 刷新图标样式 */
.refresh-icon {
  margin-left: 6px;
  margin-right: 2px;
  font-size: 15px;
  color: #1890ff;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s;
  display: flex;
  align-items: center;
}
.tab.active .refresh-icon {
  color: #1890ff; /* 激活标签刷新按钮主色 */
}
.refresh-icon:hover {
  color: #40a9ff;
}
.refresh-icon.spinning {
  animation: spin 0.6s linear;
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 关闭按钮样式 */
.close {
  margin-left: 6px;
  font-size: 13px;
  cursor: pointer;
  color: #bfbfbf;
  transition: color 0.2s;
}
.close:hover {
  color: #ff4d4f;
}

/* 更多菜单按钮（右侧三个点） */
.more-menu-btn {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  height: 32px;
  margin-left: 8px;
  position: relative;
  cursor: pointer;
  user-select: none;
  background: transparent;
  z-index: 2;
  width: 32px;
  justify-content: center;
  margin-left: auto;
  border-radius: 4px;
  transition: background 0.2s;
}
.more-menu-btn:hover {
  background: #f0f5ff;
}
.more-icon {
  font-size: 18px;
  color: #595959;
  transition: color 0.2s;
}
.more-icon:hover {
  color: #1890ff;
}

/* 更多菜单弹出层 */
.more-context-menu {
  position: fixed;
  min-width: 120px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e6eb;
  padding: 4px 0;
  color: #333;
  font-size: 15px;
  user-select: none;
  z-index: 9999;
}

/* 通用右键菜单样式 */
.context-menu {
  position: fixed;
  z-index: 9999;
  min-width: 120px;
  background: #fff;
  border: 1px solid #e5e6eb;
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
  border-radius: 8px;
  padding: 4px 0;
  color: #333;
  font-size: 15px;
  user-select: none;
}

/* 菜单项样式 */
.menu-item {
  padding: 8px 22px;
  cursor: pointer;
  white-space: nowrap;
  border-radius: 4px;
  transition: background 0.2s, color 0.2s;
}
.menu-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}

/* 溢出菜单按钮（...） */
.overflow-menu-btn {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  height: 32px;
  margin-left: 6px;
  position: relative;
  cursor: pointer;
  user-select: none;
  background: transparent;
  z-index: 2;
  width: 32px;
  justify-content: center;
  border-radius: 4px;
  transition: background 0.2s;
}
.overflow-menu-btn:hover {
  background: #f0f5ff;
}
.overflow-icon {
  font-size: 18px;
  color: #595959;
  transition: color 0.2s;
}
.overflow-icon:hover {
  color: #1890ff;
}

/* 溢出菜单弹出层 */
.overflow-context-menu {
  position: fixed;
  min-width: 140px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e5e6eb;
  padding: 4px 0;
  color: #333;
  font-size: 15px;
  user-select: none;
  z-index: 9999;
}
</style> 