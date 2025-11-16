<template>
  <!-- 侧边栏容器 -->
  <div :class="['sider', { collapsed }]" v-bind="$attrs">
    <!-- Logo 区域 -->
    <div class="logo">
      <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
      <span v-if="!collapsed" class="logo-text">Tiny app</span>
    </div>
    <!-- 菜单列表，支持滚动 -->
    <div class="menu-scroll">
      <ul class="menu">
        <template v-for="(item, idx) in menuList" :key="item.url">
          <li :class="['menu-item', { active: isActive(item), open: openMenu === idx }]" @click="toggleMenu(idx, item)">
            <!-- 使用 Icon.vue 组件渲染图标 -->
            <Icon v-if="item.showIcon && item.icon" :icon="item.icon" className="icon" />
            <span v-if="!collapsed" class="text">{{ item.title }}</span>
            <!-- 使用 Icon 组件替换箭头 -->
            <Icon v-if="item.children && item.children.length > 0 && !collapsed"
              :icon="openMenu === idx ? 'DownOutlined' : 'RightOutlined'" class="arrow" />
          </li>
          <!-- 二级菜单 -->
          <ul v-if="item.children && item.children.length > 0 && openMenu === idx && !collapsed" class="submenu">
            <template v-for="(sub, subIdx) in item.children" :key="sub.url">
              <li :class="['submenu-item', { active: isActive(sub), open: openSubMenu === subIdx }]"
                @click.stop="toggleSubMenu(subIdx, sub)">
                <!-- 二级菜单不渲染图标 -->
                <span class="text">{{ sub.title }}</span>
                <!-- 使用 Icon 组件替换箭头 -->
                <Icon v-if="sub.children && sub.children.length > 0"
                  :icon="openSubMenu === subIdx ? 'DownOutlined' : 'RightOutlined'" class="arrow" />
              </li>
              <!-- 三级菜单 -->
              <ul v-if="sub.children && sub.children.length > 0 && openSubMenu === subIdx" class="submenu third">
                <li v-for="third in sub.children" :key="third.url || third.id || third.name"
                  :class="['submenu-item', { active: isActive(third) }]" @click.stop="third.url && goMenu(third.url)">
                  <!-- 三级菜单不渲染图标 -->
                  <span class="text">{{ third.title }}</span>
                </li>
              </ul>
            </template>
          </ul>
        </template>
      </ul>
    </div>


    <!-- 折叠按钮固定在底部 -->
    <div class="collapse-btn" @click="toggleCollapse">
      <Icon :icon="collapsed ? 'MenuUnfoldOutlined' : 'MenuFoldOutlined'" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { menuTree, type MenuItem } from '@/api/menu'
import { message } from 'ant-design-vue'
import Icon from '@/components/Icon.vue'

// 禁用自动属性继承，手动控制属性绑定
defineOptions({
  inheritAttrs: false
})

// 侧边栏是否折叠状态
const collapsed = ref(false)
// 菜单项列表，初始为空，后续通过接口加载
const menuList = ref<MenuItem[]>([])
// 当前打开的一级菜单索引，默认工作台（0）
const openMenu = ref(0)
// 当前打开的二级菜单索引
const openSubMenu = ref(-1)

// 加载菜单数据
async function loadMenu() {
  try {
    const data = await menuTree()
    console.log('menuTree 返回数据:', data);

    if (data && Array.isArray(data) && data.length > 0) {
      menuList.value = data
      console.log('菜单列表加载成功，数量:', menuList.value.length)
    } else {
      menuList.value = []
      message.warning('未加载到可用菜单')
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    message.error('加载菜单失败，请检查网络或联系管理员')
    menuList.value = []
  }
}

onMounted(loadMenu)

// 路由实例
const router = useRouter()
const route = useRoute()

// 切换折叠状态
function toggleCollapse() {
  collapsed.value = !collapsed.value
}
// 一级菜单点击，手风琴效果
function toggleMenu(idx: number, item: MenuItem) {
  // 如果有子菜单
  if (item.children && item.children.length > 0) {
    openMenu.value = openMenu.value === idx ? -1 : idx
    openSubMenu.value = -1
    // 如果有 redirect 字段，点击父菜单时跳转到 redirect
    if (item.redirect) {
      goMenu(item.redirect) // 跳转到父节点的 redirect
    }
  } else {
    // 叶子节点直接跳转到 path
    if (item.url) {
      goMenu(item.url)
      openMenu.value = idx
      openSubMenu.value = -1
    }
  }
}
// 二级菜单点击，手风琴效果
function toggleSubMenu(subIdx: number, sub: MenuItem) {
  if (sub.children && sub.children.length > 0) {
    openSubMenu.value = openSubMenu.value === subIdx ? -1 : subIdx
  } else {
    if (sub.url) {
      goMenu(sub.url)
      openSubMenu.value = subIdx
    }
  }
}
// 跳转菜单
function goMenu(path: string) {
  router.push(path)
}
// 判断当前路由是否激活
function isActive(item: MenuItem) {
  return route.path === item.url
}
// 路由变化时自动展开对应菜单
watch(() => route.path, (newPath) => {
  // 一级
  const idx = menuList.value.findIndex((m: MenuItem) => m.url === newPath || (m.children && m.children.some((sub: MenuItem) => sub.url === newPath || (sub.children && sub.children.some((third: MenuItem) => third.url === newPath)))))
  if (idx !== -1) openMenu.value = idx
  // 二级
  if (idx !== -1 && menuList.value[idx].children && menuList.value[idx].children.length > 0) {
    const subIdx = menuList.value[idx].children.findIndex((sub: MenuItem) => sub.url === newPath || (sub.children && sub.children.some((third: MenuItem) => third.url === newPath)))
    openSubMenu.value = subIdx
  } else {
    openSubMenu.value = -1
  }
}, { immediate: true })
</script>

<style scoped>
.sider {
  /* 使用 var() 函数从 theme.css 中读取并应用侧边栏展开宽度变量 */
  width: var(--sider-width-expanded);
  /* background: #001529; */
  color: #fff;
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
  background: #fafbfc;
}

.sider.collapsed {
  /* 使用 var() 函数从 theme.css 中读取并应用侧边栏折叠宽度变量 */
  width: var(--sider-width-collapsed);
}

.logo {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  height: 56px;
  padding: 0 12px;
  min-width: 0;
  overflow: hidden;
}

.logo-img {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: block;
}

.logo-text {
  margin-left: 10px;
  font-size: 20px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
  line-height: 1;
  display: flex;
  align-items: center;
  color: #333;
  /* logo区字体色与菜单一致 */
}

.sider.collapsed .logo {
  justify-content: center;
  padding: 0;
}

.sider.collapsed .logo-img {
  margin: 0 auto;
}

.sider.collapsed .logo-text,
.sider.collapsed .text,
.sider.collapsed .arrow {
  display: none;
}

.menu-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item,
.submenu-item {
  color: #333;
  /* 普通菜单字体色 */
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  user-select: none;
}

.menu-item.active {
  background: #1890ff;
  color: #fff;
  /* 激活时字体和图标都为白色 */
}

.menu-item.open {
  /* background: #112240; */
}

.menu-item .icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.menu-item .text {
  margin-left: 8px;
}

.menu-item .arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.2s;
  width: 12px;
  text-align: center;
  margin-right: 16px;
}

.submenu {
  /* background: #0d1a26; */
  padding-left: 24px;
  margin-top: 2px;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 8px 12px 8px 12px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
  color: #333;
  border-radius: 4px;
  margin: 1px 0;
}

.submenu-item.active {
  background: #1890ff;
  color: #fff;
}

.submenu-item:hover {
  background: #f0f5ff;
  color: #1890ff;
}

/* 选中状态下的悬浮效果 - 与普通悬浮一致 */
.submenu-item.active:hover {
  background: #f0f5ff;
  color: #1890ff;
}

.submenu-item .arrow {
  margin-left: auto;
  font-size: 12px;
  transition: transform 0.2s;
  width: 12px;
  text-align: center;
  margin-right: 16px;
}

.submenu.third {
  background: transparent;
  padding-left: 24px;
  margin-top: 4px;
  border-left: 1px solid #f0f0f0;
  margin-left: 0;
}

.submenu.third .submenu-item {
  padding: 8px 12px 8px 12px;
  font-size: 14px;
  color: #333;
  position: relative;
  margin-left: -8px;
  margin-right: -16px;
  border-radius: 4px;
  margin-top: 1px;
  margin-bottom: 1px;
}

.submenu.third .submenu-item:hover {
  background: #f0f5ff !important;
  color: #1890ff !important;
}

.submenu.third .submenu-item.active {
  background: #1890ff !important;
  color: #fff !important;
}

/* 确保三级菜单样式优先级最高 */
.submenu.third li.submenu-item:hover {
  background: #f0f5ff !important;
  color: #1890ff !important;
}

.submenu.third li.submenu-item.active {
  background: #1890ff !important;
  color: #fff !important;
}

/* 选中状态下的悬浮效果 - 与普通悬浮一致 */
.submenu.third li.submenu-item.active:hover {
  background: #f0f5ff !important;
  color: #1890ff !important;
}

.collapse-btn {
  cursor: pointer;
  padding: 12px 0;
  font-size: 22px;
  text-align: center;
  color: #333;
  /* 伸缩按钮字体色与菜单一致 */
  background: #fafbfc;
  /* 伸缩按钮背景色与侧边栏一致 */
  border-top: 1px solid #e5e6eb;
  /* 分割线更柔和 */
  transition: background 0.2s, color 0.2s;
  width: 100%;
}

.collapse-btn:hover {
  background: #f0f5ff;
  /* 悬浮背景色与菜单一致 */
  color: #1890ff;
  /* 悬浮字体色与菜单一致 */
}

.collapse-btn :deep(.anticon) {
  font-size: 18px;
  /* 与一级菜单 icon 大小一致 */
  width: 24px;
  /* 与一级菜单 icon 宽度一致 */
  vertical-align: middle;
  display: inline-block;
}
</style>