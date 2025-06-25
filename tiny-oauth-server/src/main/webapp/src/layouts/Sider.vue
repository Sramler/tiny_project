<template>
  <!-- 侧边栏容器 -->
  <div :class="['sider', { collapsed }]">
    <!-- Logo 区域 -->
    <div class="logo">
      <img src="@/assets/logo.svg" alt="logo" class="logo-img" />
      <span v-if="!collapsed" class="logo-text">Tiny app</span>
    </div>
    <!-- 菜单列表，支持滚动 -->
    <div class="menu-scroll">
      <ul class="menu">
        <template v-for="(item, idx) in menuList" :key="item.path">
          <li
            :class="['menu-item', { active: isActive(item), open: openMenu === idx }]"
            @click="toggleMenu(idx, item)"
          >
            <!-- 一级菜单图标动态渲染 -->
            <component
              v-if="item.showIcon && item.icon"
              :is="getIconComponent(item.icon)"
              class="icon"
            />
            <span v-if="!collapsed" class="text">{{ item.title }}</span>
            <!-- 有子菜单时显示箭头 -->
            <span v-if="item.children && !collapsed" class="arrow">{{ openMenu === idx ? '▼' : '▶' }}</span>
          </li>
          <!-- 二级菜单 -->
          <ul v-if="item.children && openMenu === idx && !collapsed" class="submenu">
            <template v-for="(sub, subIdx) in item.children" :key="sub.path">
              <li
                :class="['submenu-item', { active: isActive(sub), open: openSubMenu === subIdx }]"
                @click.stop="toggleSubMenu(subIdx, sub)"
              >
                <!-- 二级菜单不渲染图标 -->
                <span class="text">{{ sub.title }}</span>
                <!-- 有三级菜单时显示箭头 -->
                <span v-if="sub.children" class="arrow">{{ openSubMenu === subIdx ? '▼' : '▶' }}</span>
              </li>
              <!-- 三级菜单 -->
              <ul v-if="sub.children && openSubMenu === subIdx" class="submenu third">
                <li
                  v-for="third in sub.children"
                  :key="third.path"
                  :class="['submenu-item', { active: isActive(third) }]"
                  @click.stop="goMenu(third.path)"
                >
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
      <MenuUnfoldOutlined v-if="collapsed" />
      <MenuFoldOutlined v-else />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
// 引入所有 ant-design-vue 图标
import * as Icons from '@ant-design/icons-vue'
import { MenuUnfoldOutlined, MenuFoldOutlined } from '@ant-design/icons-vue'

// 侧边栏是否折叠状态
const collapsed = ref(false)
// 菜单项列表，初始为空，后续通过接口加载
const menuList = ref<any[]>([])
// 当前打开的一级菜单索引，默认工作台（0）
const openMenu = ref(0)
// 当前打开的二级菜单索引
const openSubMenu = ref(-1)

// 加载菜单数据
async function loadMenu() {
  const res = await axios.get('/menu.json')
  menuList.value = res.data
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
function toggleMenu(idx: number, item: any) {
  // 如果有子菜单
  if (item.children) {
    openMenu.value = openMenu.value === idx ? -1 : idx
    openSubMenu.value = -1
    // 如果有 redirect 字段，点击父菜单时跳转到 redirect
    if (item.redirect) {
      goMenu(item.redirect) // 跳转到父节点的 redirect
    }
  } else {
    // 叶子节点直接跳转到 path
    goMenu(item.path)
    openMenu.value = idx
    openSubMenu.value = -1
  }
}
// 二级菜单点击，手风琴效果
function toggleSubMenu(subIdx: number, sub: any) {
  if (sub.children) {
    openSubMenu.value = openSubMenu.value === subIdx ? -1 : subIdx
  } else {
    goMenu(sub.path)
    openSubMenu.value = subIdx
  }
}
// 跳转菜单
function goMenu(path: string) {
  router.push(path)
}
// 判断当前路由是否激活
function isActive(item: any) {
  return route.path === item.path
}
// 路由变化时自动展开对应菜单
watch(() => route.path, (newPath) => {
  // 一级
  const idx = menuList.value.findIndex((m: any) => m.path === newPath || (m.children && m.children.some((sub: any) => sub.path === newPath || (sub.children && sub.children.some((third: any) => third.path === newPath)))))
  if (idx !== -1) openMenu.value = idx
  // 二级
  if (idx !== -1 && menuList.value[idx].children) {
    const subIdx = menuList.value[idx].children.findIndex((sub: any) => sub.path === newPath || (sub.children && sub.children.some((third: any) => third.path === newPath)))
    openSubMenu.value = subIdx
  } else {
    openSubMenu.value = -1
  }
}, { immediate: true })

// 获取图标组件的方法
function getIconComponent(iconName: string) {
  // 如果 iconName 存在于 Icons 中，则返回对应组件，否则返回 null
  return (Icons as any)[iconName] || null
}
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
  color: #333; /* logo区字体色与菜单一致 */
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
  color: #333; /* 普通菜单字体色 */
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
}
.submenu {
  /* background: #0d1a26; */
  padding-left: 24px;
}
.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 0 10px 8px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}
.submenu-item.active {
  background: #1890ff;
  color: #fff;
}
.submenu.third {
  background: #09111a;
  padding-left: 24px;
}
.collapse-btn {
  cursor: pointer;
  padding: 12px 0;
  font-size: 22px;
  text-align: center;
  color: #333; /* 伸缩按钮字体色与菜单一致 */
  background: #fafbfc; /* 伸缩按钮背景色与侧边栏一致 */
  border-top: 1px solid #e5e6eb; /* 分割线更柔和 */
  transition: background 0.2s, color 0.2s;
  width: 100%;
}
.collapse-btn:hover {
  background: #f0f5ff; /* 悬浮背景色与菜单一致 */
  color: #1890ff;      /* 悬浮字体色与菜单一致 */
}
.collapse-btn :deep(.anticon) {
  font-size: 18px;      /* 与一级菜单 icon 大小一致 */
  width: 24px;          /* 与一级菜单 icon 宽度一致 */
  vertical-align: middle;
  display: inline-block;
}
</style> 