<template>
  <!-- 顶部栏容器 -->
  <div class="header-bar">
    <!-- 左侧占位（可放面包屑等） -->
    <div class="left"></div>
    <!-- 右侧用户信息区域 -->
    <div class="right">
      <!-- 用户信息下拉菜单 -->
      <div class="dropdown">
        <div class="user-info">
          <!-- 用户头像 -->
          <img class="avatar" src="https://i.pravatar.cc/40" alt="avatar" />
          <!-- 用户名和下拉箭头 -->
          <span class="username">管理员</span>
          <DownOutlined class="dropdown-icon" />
        </div>
        <!-- 下拉菜单内容 -->
        <ul class="dropdown-menu">
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
</template>

<script setup lang="ts">
// 引入 Ant Design Icons
import { UserOutlined, SettingOutlined, LogoutOutlined, DownOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.header-bar {
  height: 50px; /* 顶部栏高度 */
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  padding: 0 24px;
  position: relative;
  z-index: 10;
  /* background-color: #1890ff; */
}

.left {
  flex: 1;
}

.right {
  display: flex;
  align-items: center;
}

.dropdown {
  position: relative;
  cursor: pointer;
}

/* 用户信息区域样式 */
.user-info {
  display: flex;
  align-items: center;
  padding: 0 4px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: rgba(0, 0, 0, 0.025);
}

.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  margin-right: 8px;
}

.username {
  margin-right: 8px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.dropdown-icon {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  transition: transform 0.3s;
}

/* 下拉菜单悬停时箭头旋转 */
.dropdown:hover .dropdown-icon {
  transform: rotate(180deg);
}

.dropdown-menu {
  display: none;
  position: absolute;
  right: 0;
  top: 100%;
  background: #fff;
  border: 1px solid #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  min-width: 120px;
  z-index: 10001;
  list-style: none;
  padding: 4px 0;
  margin: 4px 0 0 0;
  border-radius: 4px;
}

.dropdown:hover .dropdown-menu {
  display: block;
}

.dropdown-menu li {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
  color: #333; /* 默认字体色为深色，与主内容一致 */
}

.dropdown-menu li:hover {
  background: #f5f5f5;
}

.menu-icon {
  font-size: 16px;
  margin-right: 8px;
  color: #595959;
}

.dropdown-menu li:hover .menu-icon {
  color: #1890ff;
}

/* 普通菜单项字体色 */
:deep(.ant-dropdown-menu .ant-dropdown-menu-item),
:deep(.ant-menu .ant-menu-item) {
  color: #333; /* 普通状态字体色 */
}

/* 悬浮/选中时字体色为主色调 */
:deep(.ant-dropdown-menu .ant-dropdown-menu-item:hover),
:deep(.ant-menu .ant-menu-item:hover),
:deep(.ant-dropdown-menu .ant-dropdown-menu-item-active),
:deep(.ant-menu .ant-menu-item-active) {
  color: #1890ff !important; /* 主色调 */
  background: #f0f5ff !important; /* 主色调浅色背景 */
}
</style> 