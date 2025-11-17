<template>
  <div class="profile-page">
    <a-row :gutter="24">
      <!-- 左侧：用户信息卡片 -->
      <a-col :xs="24" :md="8">
        <a-card class="user-info-card" :bordered="false">
          <div class="user-avatar-section">
            <a-avatar :size="120" class="user-avatar">
              <template #icon>
                <UserOutlined />
              </template>
            </a-avatar>
            <h2 class="user-nickname">{{ userInfo.nickname || userInfo.username || '未设置昵称' }}</h2>
            <p class="user-username">@{{ userInfo.username }}</p>
          </div>
          <a-divider />
          <div class="user-stats">
            <div class="stat-item">
              <div class="stat-label">用户ID</div>
              <div class="stat-value">{{ userInfo.id || '-' }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">账户状态</div>
              <div class="stat-value">
                <a-tag :color="userInfo.enabled ? 'green' : 'red'">
                  {{ userInfo.enabled ? '已启用' : '已禁用' }}
                </a-tag>
              </div>
            </div>
            <div class="stat-item" v-if="userInfo.lastLoginAt">
              <div class="stat-label">最后登录</div>
              <div class="stat-value">{{ formatDateTime(userInfo.lastLoginAt) }}</div>
            </div>
          </div>
        </a-card>
      </a-col>

      <!-- 右侧：详细信息 -->
      <a-col :xs="24" :md="16">
        <a-card title="基本信息" :bordered="false">
          <a-descriptions :column="1" bordered>
            <a-descriptions-item label="用户名">
              {{ userInfo.username }}
            </a-descriptions-item>
            <a-descriptions-item label="昵称">
              {{ userInfo.nickname || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="用户ID">
              {{ userInfo.id || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="账户状态">
              <a-tag :color="userInfo.enabled ? 'green' : 'red'">
                {{ userInfo.enabled ? '已启用' : '已禁用' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="账号未过期">
              <a-tag :color="userInfo.accountNonExpired ? 'green' : 'red'">
                {{ userInfo.accountNonExpired ? '是' : '否' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="账号未锁定">
              <a-tag :color="userInfo.accountNonLocked ? 'green' : 'red'">
                {{ userInfo.accountNonLocked ? '是' : '否' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="密码未过期">
              <a-tag :color="userInfo.credentialsNonExpired ? 'green' : 'red'">
                {{ userInfo.credentialsNonExpired ? '是' : '否' }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="最后登录时间" v-if="userInfo.lastLoginAt">
              {{ formatDateTime(userInfo.lastLoginAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="创建时间" v-if="userInfo.createdAt">
              {{ formatDateTime(userInfo.createdAt) }}
            </a-descriptions-item>
            <a-descriptions-item label="更新时间" v-if="userInfo.updatedAt">
              {{ formatDateTime(userInfo.updatedAt) }}
            </a-descriptions-item>
          </a-descriptions>

          <div class="action-buttons" style="margin-top: 24px;">
            <a-button type="primary" @click="goToSettings">
              <template #icon>
                <SettingOutlined />
              </template>
              编辑个人设置
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, SettingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { userApi } from '@/api/process'

// 路由
const router = useRouter()

// 用户信息
const userInfo = ref<any>({
  id: '',
  username: '',
  nickname: '',
  enabled: true,
  accountNonExpired: true,
  accountNonLocked: true,
  credentialsNonExpired: true,
  lastLoginAt: null,
  createdAt: null,
  updatedAt: null
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const data = await userApi.getCurrentUser()
    userInfo.value = data
  } catch (error) {
    console.error('加载用户信息失败:', error)
    message.error('加载用户信息失败')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime: string | null | undefined): string => {
  if (!dateTime) return '-'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch (error) {
    return dateTime
  }
}

// 跳转到个人设置
const goToSettings = () => {
  router.push('/profile/setting')
}

// 组件挂载时加载数据
onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.user-info-card {
  margin-bottom: 24px;
}

.user-avatar-section {
  text-align: center;
  padding: 24px 0;
}

.user-avatar {
  margin-bottom: 16px;
  background: #1890ff;
}

.user-nickname {
  margin: 16px 0 8px 0;
  font-size: 24px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}

.user-username {
  margin: 0;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.user-stats {
  padding: 16px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.stat-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.stat-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.stat-value {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
  font-weight: 500;
}

.action-buttons {
  text-align: right;
}
</style>