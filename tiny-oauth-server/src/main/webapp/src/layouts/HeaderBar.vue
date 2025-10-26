<template>
  <!-- 使用单个根节点包装所有内容，并继承所有属性 -->
  <div v-bind="$attrs" class="header-bar-container">
    <!-- 顶部栏容器 -->
    <div class="header-bar">
      <!-- 左侧占位（可放面包屑等） -->
      <div class="left"></div>
      <!-- 右侧用户信息区域 -->
      <div class="right">
        <!-- 用户信息下拉菜单 -->
        <div class="dropdown" ref="dropdownRef">
          <div class="user-info" @mouseenter="showDropdown" @mouseleave="hideDropdown">
            <!-- 用户头像 -->
            <img class="avatar" src="https://i.pravatar.cc/40" alt="avatar" />
            <!-- 用户名和下拉箭头 -->
            <span class="username">管理员</span>
            <DownOutlined class="dropdown-icon" :class="{ 'rotated': dropdownVisible }" />
          </div>
          <!-- 下拉菜单内容 -->
          <ul class="dropdown-menu" v-show="dropdownVisible" :style="dropdownStyle">
            <li>
              <UserOutlined class="menu-icon" />
              个人中心
            </li>
            <li>
              <SettingOutlined class="menu-icon" />
              个人设置
            </li>
            <li>
              <LogoutOutlined class="menu-icon" />
              退出登录
            </li>
          </ul>
        </div>
      </div>
    </div>
    <!-- 标签页导航插槽 -->
    <slot name="tags"></slot>
  </div>
</template>

<script setup lang="ts">
// 引入 Ant Design Icons
import { UserOutlined, SettingOutlined, LogoutOutlined, DownOutlined } from '@ant-design/icons-vue'
import { ref, computed } from 'vue'

// 禁用自动属性继承，手动控制属性绑定
defineOptions({
  inheritAttrs: false
})

// 下拉菜单状态
const dropdownVisible = ref(false)
const dropdownRef = ref<HTMLElement | null>(null)

// 计算下拉菜单位置
const dropdownStyle = computed(() => {
  if (!dropdownRef.value) return {}

  const rect = dropdownRef.value.getBoundingClientRect()
  return {
    top: rect.bottom + 4 + 'px',
    right: window.innerWidth - rect.right + 'px'
  }
})

// 显示下拉菜单
const showDropdown = () => {
  dropdownVisible.value = true
}

// 隐藏下拉菜单
const hideDropdown = () => {
  dropdownVisible.value = false
}
</script>

<style scoped>
/* 根容器样式 */
.header-bar-container {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.header-bar {
  /* 使用 var() 函数从 theme.css 中读取并应用顶部栏高度变量 */
  height: var(--header-height);
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  padding: 0;
  position: relative;
  z-index: 100;
  /* 使用与系统UI一致的字体 */
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
  font-size: 14px;
  line-height: 1.5715;
  /* 确保布局正确 */
  width: 100%;
  box-sizing: border-box;
  /* background-color: #1890ff; */
}

.left {
  flex: 1;
  min-width: 0;
  /* 确保左侧区域不会挤压右侧 */
  overflow: hidden;
}

.right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
  margin-left: auto;
  /* 确保右侧区域始终显示在最右侧 */
  position: relative;
  z-index: 1;
  /* 确保不被其他元素影响 */
  min-width: fit-content;
}

.dropdown {
  position: relative;
  cursor: pointer;
  /* 确保下拉菜单有正确的定位上下文 */
  z-index: 1000;
}

/* 用户信息区域样式 */
.user-info {
  display: flex;
  align-items: center;
  padding: 0 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
  height: 32px;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
}

.username {
  margin-right: 4px;
  font-weight: 400;
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
  line-height: 1.5715;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  transition: transform 0.3s;
  flex-shrink: 0;
}

/* 下拉菜单显示时箭头旋转 */
.dropdown-icon.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #d9d9d9;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
  min-width: 120px;
  z-index: 999999;
  list-style: none;
  padding: 4px 0;
  margin: 0;
  border-radius: 6px;
  /* 确保下拉菜单在最顶层显示 */
}

.dropdown-menu li {
  display: flex;
  align-items: center;
  padding: 5px 12px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
  color: rgba(0, 0, 0, 0.85);
  font-size: 14px;
  line-height: 1.5715;
  /* 使用与系统UI一致的字体和颜色 */
}

.dropdown-menu li:hover {
  background: #f5f5f5;
}

.menu-icon {
  font-size: 14px;
  margin-right: 8px;
  color: rgba(0, 0, 0, 0.45);
  flex-shrink: 0;
}

.dropdown-menu li:hover .menu-icon {
  color: #1890ff;
}

/* 普通菜单项字体色 */
:deep(.ant-dropdown-menu .ant-dropdown-menu-item),
:deep(.ant-menu .ant-menu-item) {
  color: rgba(0, 0, 0, 0.85);
  /* 普通状态字体色 */
}

/* 悬浮/选中时字体色为主色调 */
:deep(.ant-dropdown-menu .ant-dropdown-menu-item:hover),
:deep(.ant-menu .ant-menu-item:hover),
:deep(.ant-dropdown-menu .ant-dropdown-menu-item-active),
:deep(.ant-menu .ant-menu-item-active) {
  color: #1890ff !important;
  /* 主色调 */
  background: #f0f5ff !important;
  /* 主色调浅色背景 */
}
</style>