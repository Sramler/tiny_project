<template>
  <!-- 标签页导航容器 -->
  <div class="tag-tabs" v-bind="$attrs">
    <!-- 标签滚动区域 -->
    <div class="tabs-scroll-area" ref="tabScrollRef">
      <div v-for="(tab, idx) in tabs" :key="tab.path" :class="['tab', { active: tab.path === activePath }]"
        @click="switchTab(tab.path)" @contextmenu.prevent="openContextMenu($event, idx)"
        :ref="el => setTabRef(el, idx)">
        <!-- 标签标题部分，自动撑开 -->
        <span class="tab-title">{{ tab.title }}</span>
        <!-- 操作区，关闭和刷新按钮固定在右侧 -->
        <span class="tab-actions">
          <!-- 刷新按钮：首页始终显示，其他页面仅在激活时显示 -->
          <ReloadOutlined v-if="tab.path === '/' || (tab.path !== '/' && tab.path === activePath)" class="refresh-icon"
            :class="{ spinning: spinningIdx === idx }" @click.stop="handleRefresh(idx)" title="刷新" />
          <!-- 关闭按钮：首页不显示 -->
          <CloseOutlined v-if="tab.path !== '/'" class="close" @click.stop="closeTab(idx)" />
        </span>
      </div>
    </div>
    <!-- 溢出标签菜单按钮（当标签过多超出可视区域时显示） -->
    <div v-if="overflowTabs.length > 0" class="overflow-menu-btn" @click.stop="toggleOverflowMenu">
      <EllipsisOutlined class="overflow-icon" />
      <!-- 溢出菜单弹出层 -->
      <div v-if="overflowMenuVisible" class="context-menu overflow-context-menu"
        :style="{ left: overflowMenuPos.left + 'px', top: overflowMenuPos.top + 'px' }" @mouseleave="closeOverflowMenu">
        <div class="menu-item" v-for="tab in overflowTabs" :key="tab.path"
          @click="switchTab(tab.path); closeOverflowMenu()">
          {{ tab.title }}
        </div>
      </div>
    </div>
    <!-- 右侧三个点菜单按钮 -->
    <div class="more-menu-btn" ref="moreBtnRef" @click.stop="toggleMoreMenu">
      <MoreOutlined class="more-icon" />
      <!-- 更多菜单弹出层 -->
      <div v-if="moreMenuVisible" class="context-menu more-context-menu"
        :style="{ left: moreMenuPos.left + 'px', top: moreMenuPos.top + 'px' }" @mouseleave="closeMoreMenu">
        <div class="menu-item" @click="closeOtherTabs(activeTabIdx)">关闭其他</div>
        <div class="menu-item" @click="refreshTab(activeTabIdx)">刷新当前页</div>
      </div>
    </div>
    <!-- 右键菜单 -->
    <div v-if="contextMenu.visible" class="context-menu"
      :style="{ left: contextMenu.x + 'px', top: contextMenu.y + 'px' }" @mouseleave="closeContextMenu">
      <div class="menu-item" @click="refreshTab(contextMenu.idx)">刷新当前页</div>
      <div class="menu-item" @click="closeOtherTabs(contextMenu.idx)">关闭其他</div>
      <div class="menu-item" v-if="contextMenu.idx > 0" @click="closeLeftTabs(contextMenu.idx)">
        关闭左侧
      </div>
      <div class="menu-item" v-if="contextMenu.idx < tabs.length - 1" @click="closeRightTabs(contextMenu.idx)">
        关闭右侧
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  ReloadOutlined,
  CloseOutlined,
  EllipsisOutlined,
  MoreOutlined
} from '@ant-design/icons-vue'

/**
 * 类型定义
 */

/**
 * 标签页数据结构
 */
interface Tab {
  title: string // 标签标题
  path: string // 路由路径
}

/**
 * 右键菜单状态
 */
interface ContextMenuState {
  visible: boolean // 是否显示
  x: number // 鼠标 X 坐标
  y: number // 鼠标 Y 坐标
  idx: number // 当前标签索引
}

/**
 * 菜单位置信息
 */
interface MenuPosition {
  left: number // 左侧位置
  top: number // 顶部位置
}

/**
 * 常量定义
 */
const HOME_PATH = '/' // 首页路径
const HOME_TITLE = '工作台' // 首页标题
const UNKNOWN_PAGE_TITLE = '未知页面' // 未知页面标题

const REFRESH_ANIMATION_DURATION = 600 // 刷新动画持续时间（毫秒）
const MORE_MENU_WIDTH = 120 // 更多菜单宽度（像素）
const OVERFLOW_MENU_WIDTH = 140 // 溢出菜单宽度（像素）
const DEBOUNCE_DELAY = 150 // 防抖延迟（毫秒）

/**
 * 组件配置
 */
defineOptions({
  name: 'TagTabs',
  inheritAttrs: false // 禁用自动属性继承，手动控制属性绑定
})

/**
 * 路由实例
 */
const router = useRouter()
const route = useRoute()

/**
 * DOM 引用
 */
const tabScrollRef = ref<HTMLElement | null>(null) // 标签滚动区域引用
const moreBtnRef = ref<HTMLElement | null>(null) // 更多菜单按钮引用

/**
 * 响应式状态
 */
// 标签页列表（默认包含首页）
const tabs = ref<Tab[]>([
  { title: HOME_TITLE, path: HOME_PATH }
])

// 当前激活标签页路径
const activePath = ref<string>(route.path)

// 右键菜单状态
const contextMenu = ref<ContextMenuState>({ visible: false, x: 0, y: 0, idx: -1 })

// 正在旋转的刷新图标索引（用于动画效果）
const spinningIdx = ref<number>(-1)

// 右侧更多菜单状态
const moreMenuVisible = ref<boolean>(false)
const moreMenuPos = ref<MenuPosition>({ left: 0, top: 0 })

// 当前激活标签索引（计算属性）
const activeTabIdx = computed(() => tabs.value.findIndex(tab => tab.path === activePath.value))

// 标签 DOM 引用数组（用于计算溢出标签）
const tabRefs = ref<(HTMLElement | null)[]>([])

// 溢出标签列表（超出可视区域的标签）
const overflowTabs = ref<Tab[]>([])

// 溢出菜单显示状态
const overflowMenuVisible = ref<boolean>(false)
const overflowMenuPos = ref<MenuPosition>({ left: 0, top: 0 })

/**
 * 工具函数
 */

/**
 * 防抖函数
 * 用于限制函数调用频率，优化性能
 * @param fn 要防抖的函数
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的函数
 */
function debounce<T extends (...args: unknown[]) => void>(fn: T, delay: number): T {
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  return ((...args: Parameters<T>) => {
    if (timeoutId) clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }) as T
}

/**
 * 设置标签引用
 * 用于获取标签 DOM 元素，计算溢出标签
 * @param el DOM 元素
 * @param idx 标签索引
 */
function setTabRef(el: unknown, idx: number) {
  tabRefs.value[idx] = el as HTMLElement | null
}

/**
 * 获取标签标题
 * 优先从路由 meta.menuInfo 获取，其次从 meta.title 获取，最后使用默认值
 * @returns 标签标题
 */
function getTabTitle(): string {
  const menuInfo = route.meta.menuInfo as { title?: string } | undefined
  const metaTitle = route.meta.title as string | undefined
  return menuInfo?.title || metaTitle || UNKNOWN_PAGE_TITLE
}

/**
 * 更新或添加标签
 * 如果标签已存在则更新标题，否则添加新标签
 * @param path 路由路径
 * @param title 标签标题
 */
function updateOrAddTab(path: string, title: string) {
  const existingTab = tabs.value.find(tab => tab.path === path)
  if (existingTab) {
    existingTab.title = title
  } else {
    tabs.value.push({ title, path })
  }
}

/**
 * 关闭所有菜单的统一函数
 * 用于确保同时只显示一个菜单
 */
function closeAllMenus() {
  closeContextMenu()
  closeMoreMenu()
  closeOverflowMenu()
}

/**
 * 业务逻辑函数
 */

/**
 * 切换标签页
 * @param path 要切换到的路由路径
 */
function switchTab(path: string) {
  router.push(path)
}

/**
 * 关闭标签页
 * 如果关闭的是当前激活的标签，自动切换到前一个标签（或后一个，或首页）
 * @param idx 要关闭的标签索引
 */
function closeTab(idx: number) {
  const tabToClose = tabs.value[idx]
  if (!tabToClose) return

  // 关闭后如果是当前页，自动切换到前一个标签
  if (tabToClose.path === activePath.value) {
    const prevTab = tabs.value[idx - 1]
    const nextTab = tabs.value[idx + 1]
    const fallbackTab: Tab = { title: HOME_TITLE, path: HOME_PATH }
    const targetTab = prevTab || nextTab || fallbackTab
    router.push(targetTab.path)
  }
  tabs.value.splice(idx, 1)
  // 清理对应的 ref
  tabRefs.value.splice(idx, 1)
}

/**
 * 滚动到当前激活标签
 * 确保激活的标签在可视区域内
 */
function scrollToActiveTab() {
  const container = tabScrollRef.value
  if (!container) return

  const active = container.querySelector('.tab.active') as HTMLElement
  if (!active) return

  const left = active.offsetLeft
  const width = active.offsetWidth
  const scrollLeft = container.scrollLeft
  const containerWidth = container.offsetWidth

  // 如果激活标签在可视区域左侧，滚动到标签位置
  if (left < scrollLeft) {
    container.scrollLeft = left
  } else if (left + width > scrollLeft + containerWidth) {
    // 如果激活标签在可视区域右侧，滚动到标签右侧对齐
    container.scrollLeft = left + width - containerWidth
  }
}

/**
 * 右键菜单相关函数
 */

/**
 * 打开右键菜单
 * @param e 鼠标事件
 * @param idx 标签索引
 */
function openContextMenu(e: MouseEvent, idx: number) {
  contextMenu.value.visible = true
  contextMenu.value.x = e.clientX
  contextMenu.value.y = e.clientY
  contextMenu.value.idx = idx
  document.addEventListener('click', closeContextMenu)
}

/**
 * 关闭右键菜单
 */
function closeContextMenu() {
  contextMenu.value.visible = false
  contextMenu.value.idx = -1
  document.removeEventListener('click', closeContextMenu)
}

/**
 * 关闭其他标签（保留首页和当前标签）
 * @param idx 当前标签索引
 */
function closeOtherTabs(idx: number) {
  const current = tabs.value[idx]
  if (!current) return

  // 保留当前标签和首页
  tabs.value = tabs.value.filter((tab, i) => i === idx || tab.path === HOME_PATH)
  // 保证当前标签激活
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeAllMenus()
}

/**
 * 关闭左侧标签（保留首页）
 * @param idx 当前标签索引
 */
function closeLeftTabs(idx: number) {
  const current = tabs.value[idx]
  if (!current) return

  // 保留当前标签及右侧标签，以及首页
  tabs.value = tabs.value.filter((tab, i) => i >= idx || tab.path === HOME_PATH)
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeAllMenus()
}

/**
 * 关闭右侧标签
 * @param idx 当前标签索引
 */
function closeRightTabs(idx: number) {
  const current = tabs.value[idx]
  if (!current) return

  // 保留当前标签及左侧标签，以及首页
  tabs.value = tabs.value.filter((tab, i) => i <= idx || tab.path === HOME_PATH)
  if (activePath.value !== current.path) {
    router.push(current.path)
  }
  closeAllMenus()
}

/**
 * 刷新标签页
 * 通过添加时间戳参数强制刷新页面
 * @param idx 标签索引
 */
function refreshTab(idx: number) {
  const tab = tabs.value[idx]
  if (!tab) return

  // 获取当前查询参数，移除旧的 _refresh 参数
  const currentQuery = { ...route.query }
  delete currentQuery._refresh

  // 使用路由替换，添加新的时间戳参数强制刷新
  // BasicLayout 中的 router-view 会根据 _refresh 参数变化重新渲染
  router.replace({
    path: tab.path,
    query: { ...currentQuery, _refresh: Date.now() }
  }).catch((err) => {
    // 忽略导航重复的错误（用户快速点击刷新时可能发生）
    if (err.name !== 'NavigationDuplicated') {
      console.error('刷新页面失败:', err)
    }
  })
  closeAllMenus()
}

/**
 * 刷新图标点击处理
 * 触发刷新动画和页面刷新
 * @param idx 标签索引
 */
function handleRefresh(idx: number) {
  spinningIdx.value = idx
  refreshTab(idx)
  // 动画结束后重置状态
  setTimeout(() => {
    if (spinningIdx.value === idx) {
      spinningIdx.value = -1
    }
  }, REFRESH_ANIMATION_DURATION)
}

/**
 * 右侧更多菜单
 */

/**
 * 切换更多菜单显示/隐藏
 */
function toggleMoreMenu() {
  moreMenuVisible.value = !moreMenuVisible.value
  if (moreMenuVisible.value && moreBtnRef.value) {
    const rect = moreBtnRef.value.getBoundingClientRect()
    moreMenuPos.value = {
      left: rect.right - MORE_MENU_WIDTH,
      top: rect.bottom
    }
    document.addEventListener('click', closeMoreMenu)
  }
}

/**
 * 关闭更多菜单
 */
function closeMoreMenu() {
  moreMenuVisible.value = false
  document.removeEventListener('click', closeMoreMenu)
}

/**
 * 计算溢出标签（防抖优化）
 * 找出超出可视区域的标签，用于显示溢出菜单
 */
const calcOverflowTabs = debounce(() => {
  const container = tabScrollRef.value
  if (!container) return

  const containerLeft = container.scrollLeft
  const containerRight = containerLeft + container.offsetWidth

  overflowTabs.value = tabs.value.filter((tab, idx) => {
    const el = tabRefs.value[idx]
    if (!el) return false

    const left = el.offsetLeft
    const right = left + el.offsetWidth
    // 如果标签的右侧小于可视区左侧，或左侧大于可视区右侧，则为溢出
    return right > containerRight || left < containerLeft
  })
}, DEBOUNCE_DELAY)

/**
 * 监听关闭当前标签页事件
 * 用于响应键盘快捷键 Ctrl+W / Cmd+W
 */
function handleCloseCurrentTab() {
  const currentIdx = activeTabIdx.value
  if (currentIdx >= 0 && tabs.value[currentIdx]?.path !== HOME_PATH) {
    closeTab(currentIdx)
  }
}

/**
 * 键盘快捷键处理
 * 支持 Ctrl+W/Cmd+W 关闭当前标签，Ctrl+1-9/Cmd+1-9 切换标签
 * @param e 键盘事件
 */
function handleKeydown(e: KeyboardEvent) {
  // Ctrl+W 或 Cmd+W 关闭当前标签
  if ((e.ctrlKey || e.metaKey) && e.key === 'w') {
    e.preventDefault()
    handleCloseCurrentTab()
  }
  // Ctrl+数字键切换标签（1-9）
  if ((e.ctrlKey || e.metaKey) && /^[1-9]$/.test(e.key)) {
    const idx = parseInt(e.key) - 1
    if (idx < tabs.value.length) {
      e.preventDefault()
      switchTab(tabs.value[idx].path)
    }
  }
}

/**
 * 溢出菜单显示/隐藏
 * @param e 鼠标事件（可选）
 */
function toggleOverflowMenu(e?: MouseEvent) {
  overflowMenuVisible.value = !overflowMenuVisible.value
  const btn = (e?.currentTarget as HTMLElement) || null
  if (btn) {
    const rect = btn.getBoundingClientRect()
    let left = rect.right - OVERFLOW_MENU_WIDTH
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

/**
 * 关闭溢出菜单
 */
function closeOverflowMenu() {
  overflowMenuVisible.value = false
  document.removeEventListener('click', closeOverflowMenu)
}

/**
 * 监听器
 */

// 监听路由变化，自动添加新标签页
watch(
  () => route.path,
  async (newPath) => {
    activePath.value = newPath
    const title = getTabTitle()
    updateOrAddTab(newPath, title)
    // 跳转后自动滚动到当前标签
    await nextTick()
    scrollToActiveTab()
  },
  { immediate: true } // 立即执行一次，确保初始路由也能正确添加标签
)

/**
 * 生命周期
 */

onMounted(() => {
  // 监听窗口大小变化，重新计算溢出标签
  window.addEventListener('resize', calcOverflowTabs)
  // 监听键盘事件，支持快捷键
  window.addEventListener('keydown', handleKeydown)
  // 监听滚动事件，重新计算溢出标签
  if (tabScrollRef.value) {
    tabScrollRef.value.addEventListener('scroll', calcOverflowTabs)
  }
  // 监听自定义事件：关闭当前标签
  window.addEventListener('close-current-tab', handleCloseCurrentTab)

  // 监听标签变化，重新计算溢出
  watch([tabs, activePath], async () => {
    await nextTick()
    calcOverflowTabs()
  }, { immediate: true })
})

onBeforeUnmount(() => {
  // 清理所有事件监听器
  window.removeEventListener('resize', calcOverflowTabs)
  window.removeEventListener('keydown', handleKeydown)
  if (tabScrollRef.value) {
    tabScrollRef.value.removeEventListener('scroll', calcOverflowTabs)
  }
  window.removeEventListener('close-current-tab', handleCloseCurrentTab)
  // 关闭所有菜单
  closeAllMenus()
})
</script>

<style scoped>
/* 标签栏主容器 */
.tag-tabs {
  display: flex;
  align-items: center;
  background: #fafbfc;
  padding: 0 12px 0 0;
  height: var(--tag-tabs-height);
  position: relative;
  min-width: 0;
  overflow: hidden;
  width: 100%;
  box-sizing: border-box;
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
  /* Firefox 隐藏滚动条 */
  -ms-overflow-style: none;
  /* IE/Edge 隐藏滚动条 */
  padding-bottom: -1px;
  margin-bottom: -1px;
}

/* Chrome/Safari 隐藏滚动条 */
.tabs-scroll-area::-webkit-scrollbar {
  display: none;
}

/* 单个标签 */
.tab {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  margin-right: 2px;
  height: var(--tag-tabs-height);
  line-height: var(--tag-tabs-height);
  background: #f7f8fa;
  color: #222;
  border: none;
  border-bottom: 1px solid #e5e6eb;
  border-right: 1px solid #f0f0f0;
  border-radius: 0;
  cursor: pointer;
  position: relative;
  transition: background 0.2s, color 0.2s;
  white-space: nowrap;
  min-width: 80px;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 0 0 auto;
  box-shadow: none;
  z-index: 1;
}

.tab:first-child {
  margin-left: 0;
  margin-right: 0;
}

.tab:last-child {
  border-right: none;
}

/* 标签悬浮效果 */
.tab:hover {
  background: #f0f5ff;
  color: #1890ff;
}

/* 激活状态的标签 */
.tab.active {
  background: #f0f2f5;
  color: #1890ff;
  border: none;
  border-bottom: none !important;
  border-radius: 0;
  box-shadow: none;
  z-index: 2;
  position: relative;
}

.tab.active span,
.tab.active .refresh-icon {
  color: #1890ff !important;
}

/* 标签聚焦效果 */
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
  color: #1890ff;
}

.refresh-icon:hover {
  color: #40a9ff;
}

/* 刷新动画 */
.refresh-icon.spinning {
  animation: spin 0.6s linear;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }

  100% {
    transform: rotate(360deg);
  }
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
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.10);
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
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.10);
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
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.10);
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
