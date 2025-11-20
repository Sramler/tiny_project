<template>
  <!-- 使用单个根节点包装所有内容，并继承所有属性 -->
  <div v-bind="$attrs" class="header-bar-container">
    <!-- 顶部栏容器 -->
    <div class="header-bar">
      <!-- 左侧占位（可放面包屑等） -->
      <div class="left"></div>
      <!-- 右侧用户信息区域 -->
      <div class="right">
        <a-dropdown trigger="hover" placement="bottomRight">
          <div class="dropdown" @click.stop>
            <div class="user-info">
              <!-- 用户头像 -->
              <img v-if="avatarUrl" class="avatar" :src="avatarUrl" alt="avatar" @error="handleAvatarError" />
              <div v-else class="avatar-icon" :style="avatarStyle">
                <UserOutlined />
              </div>
              <!-- 用户名和下拉箭头 -->
              <span class="username">{{ username }}</span>
              <DownOutlined class="dropdown-icon" />
            </div>
          </div>
          <template #overlay>
            <a-menu class="dropdown-menu" @click="handleMenuSelect">
              <a-menu-item key="profile">
                <UserOutlined class="menu-icon" />
                个人中心
              </a-menu-item>
              <a-menu-item key="settings">
                <SettingOutlined class="menu-icon" />
                个人设置
              </a-menu-item>
              <a-menu-item key="logout">
                <LogoutOutlined class="menu-icon" />
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
    <!-- 标签页导航插槽 -->
    <slot name="tags"></slot>
  </div>
</template>

<script setup lang="ts">
// 引入 Ant Design Icons
import { UserOutlined, SettingOutlined, LogoutOutlined, DownOutlined } from '@ant-design/icons-vue'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/auth/auth'
import { userApi } from '@/api/process'
import { generateAvatarStyleObject } from '@/utils/avatar'
import type { MenuProps } from 'ant-design-vue'

// 禁用自动属性继承，手动控制属性绑定
defineOptions({
  inheritAttrs: false
})

// 路由和认证
const router = useRouter()
const { logout } = useAuth()

// 用户信息
const username = ref('管理员')
const avatarUrl = ref<string>('')
const userId = ref<string>('')

// 处理菜单项点击
const handleMenuClick = async (action: string) => {
  console.log('菜单项被点击:', action)

  // 根据不同的操作执行相应逻辑
  switch (action) {
    case 'profile':
      // 跳转到个人中心页面
      router.push('/profile/center')
      break
    case 'settings':
      // 跳转到个人设置页面
      router.push('/profile/setting')
      break
    case 'logout':
      // 执行退出登录逻辑
      try {
        await logout()
      } catch (error) {
        console.error('退出登录失败:', error)
      }
      break
    default:
      console.warn('未知的菜单操作:', action)
  }
}

const handleMenuSelect: MenuProps['onClick'] = (info) => {
  handleMenuClick(info.key as string)
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const data = await userApi.getCurrentUser()
    username.value = data.nickname || data.username || '用户'
    userId.value = data.id
    // 更新头像URL
    if (userId.value) {
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000'
      const baseUrl = apiBaseUrl.endsWith('/') ? apiBaseUrl.slice(0, -1) : apiBaseUrl
      avatarUrl.value = `${baseUrl}/sys/users/${userId.value}/avatar?t=${Date.now()}`
    }

  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 处理头像加载错误
const handleAvatarError = () => {
  // 头像加载失败时，使用默认图标和随机颜色
  avatarUrl.value = ''
}

// 计算头像样式（当没有头像时使用随机颜色）
const avatarStyle = computed(() => {
  if (avatarUrl.value) {
    // 有头像时不使用样式
    return {}
  }
  // 没有头像时使用基于用户ID的随机颜色
  return generateAvatarStyleObject(userId.value, username.value)
})

// 监听头像上传成功事件
const handleAvatarUploaded = (event: Event) => {
  const customEvent = event as CustomEvent
  // 如果事件中包含 userId，且与当前用户ID匹配，则更新头像
  if (!customEvent.detail?.userId || customEvent.detail.userId === userId.value) {
    // 重新加载头像（添加时间戳避免缓存）
    if (userId.value) {
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000'
      const baseUrl = apiBaseUrl.endsWith('/') ? apiBaseUrl.slice(0, -1) : apiBaseUrl
      avatarUrl.value = `${baseUrl}/sys/users/${userId.value}/avatar?t=${Date.now()}`
    }
  }
}

// 组件挂载时加载用户信息和监听事件
onMounted(() => {
  loadUserInfo()
  window.addEventListener('avatar-uploaded', handleAvatarUploaded)
})

// 组件卸载时移除事件监听
onUnmounted(() => {
  window.removeEventListener('avatar-uploaded', handleAvatarUploaded)
})
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
  object-fit: cover;
}

.avatar-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  margin-right: 8px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  /* 背景色由 avatarStyle 动态设置 */
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
  padding: 4px 0;
}

.dropdown-menu :deep(.ant-dropdown-menu-item) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  transition: background-color 0.3s, color 0.3s;
}

.dropdown-menu :deep(.ant-dropdown-menu-item:hover),
.dropdown-menu :deep(.ant-dropdown-menu-item-active) {
  color: #1890ff;
  background: #f0f5ff;
}

.menu-icon {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
  flex-shrink: 0;
}

.dropdown-menu :deep(.ant-dropdown-menu-item:hover .menu-icon) {
  color: #1890ff;
}
</style>