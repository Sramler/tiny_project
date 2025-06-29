<template>
  <div class="menu-data-import">
    <a-card title="菜单数据导入" :bordered="false">
      <a-alert
        v-if="importResult"
        :type="importResult.success ? 'success' : 'error'"
        :message="importResult.message"
        :description="importResult.importedCount ? `共导入 ${importResult.importedCount} 个菜单项` : ''"
        show-icon
        closable
        @close="importResult = null"
        style="margin-bottom: 16px"
      />
      
      <a-descriptions title="导入说明" :column="1" bordered>
        <a-descriptions-item label="功能描述">
          从 menu.json 文件导入菜单结构数据到数据库
        </a-descriptions-item>
        <a-descriptions-item label="数据来源">
          src/main/resources/menu.json
        </a-descriptions-item>
        <a-descriptions-item label="导入内容">
          工作台、系统管理、个人页、异常页等菜单结构
        </a-descriptions-item>
        <a-descriptions-item label="注意事项">
          <ul style="margin: 0; padding-left: 16px;">
            <li>导入前请确保数据库表结构已正确创建</li>
            <li>如果菜单已存在，可能会产生重复数据</li>
            <li>建议在开发环境或测试环境中使用</li>
            <li>导入后可以通过菜单管理页面查看和管理</li>
          </ul>
        </a-descriptions-item>
      </a-descriptions>
      
      <div style="margin-top: 24px; text-align: center;">
        <a-space>
          <a-button 
            type="primary" 
            size="large"
            :loading="importing"
            @click="handleImport"
          >
            <template #icon>
              <UploadOutlined />
            </template>
            开始导入菜单数据
          </a-button>
          
          <a-button 
            size="large"
            @click="handleViewMenuData"
          >
            <template #icon>
              <EyeOutlined />
            </template>
            查看菜单数据
          </a-button>
          
          <a-button 
            size="large"
            @click="handleCheckStatus"
          >
            <template #icon>
              <CheckCircleOutlined />
            </template>
            检查服务状态
          </a-button>
        </a-space>
      </div>
      
      <!-- 菜单数据预览 -->
      <a-collapse v-model:activeKey="activeKeys" style="margin-top: 24px;">
        <a-collapse-panel key="preview" header="菜单数据预览">
          <pre style="background: #f5f5f5; padding: 16px; border-radius: 4px; overflow: auto; max-height: 400px;">{{ menuDataPreview }}</pre>
        </a-collapse-panel>
      </a-collapse>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined, EyeOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import request from '@/utils/request'

// 响应式数据
const importing = ref(false)
const importResult = ref<any>(null)
const activeKeys = ref<string[]>([])
const menuDataPreview = ref('')

// 菜单数据预览
const menuData = [
  {
    title: "工作台",
    url: "/",
    icon: "HomeOutlined",
    showIcon: true,
    component: "/views/Dashboard.vue"
  },
  {
    title: "系统管理",
    url: "/system",
    icon: "SettingOutlined",
    showIcon: true,
    redirect: "/system/role",
    children: [
      { title: "角色管理", url: "/system/role", showIcon: false, component: "/views/role/role.vue" },
      { title: "菜单管理", url: "/system/menu", showIcon: false, component: "/views/menu/Menu.vue" },
      { title: "资源管理", url: "/system/resource", showIcon: false, component: "/views/resource/resource.vue" },
      { title: "用户管理", url: "/system/user", showIcon: false, component: "/views/user/User.vue" }
    ]
  },
  {
    title: "个人页",
    url: "/profile",
    icon: "UserOutlined",
    showIcon: true,
    redirect: "/profile/center",
    children: [
      { title: "个人中心", url: "/profile/center", showIcon: false, component: "/views/Profile.vue" },
      { title: "个人设置", url: "/profile/setting", showIcon: false, component: "/views/Setting.vue" }
    ]
  },
  {
    title: "异常页",
    url: "/exception",
    icon: "WarningOutlined",
    showIcon: true,
    redirect: "/exception/403",
    children: [
      { title: "403", url: "/exception/403", showIcon: false, component: "/views/403.vue" },
      { title: "404", url: "/exception/404", showIcon: false, component: "/views/404.vue" },
      { title: "500", url: "/exception/500", showIcon: false, component: "/views/500.vue" }
    ]
  }
]

// 处理导入
async function handleImport() {
  importing.value = true
  try {
    const response = await request.post('/sys/menu-data/import')
    importResult.value = response
    if (response.success) {
      message.success(`菜单数据导入成功，共导入 ${response.importedCount} 个菜单项`)
    } else {
      message.error(response.message || '导入失败')
    }
  } catch (error: any) {
    console.error('导入失败:', error)
    message.error('导入失败: ' + (error.message || '未知错误'))
    importResult.value = {
      success: false,
      message: '导入失败: ' + (error.message || '未知错误')
    }
  } finally {
    importing.value = false
  }
}

// 查看菜单数据
function handleViewMenuData() {
  activeKeys.value = ['preview']
  menuDataPreview.value = JSON.stringify(menuData, null, 2)
}

// 检查服务状态
async function handleCheckStatus() {
  try {
    const response = await request.get('/sys/menu-data/status')
    if (response.success) {
      message.success('菜单数据导入服务正常运行')
    } else {
      message.warning('菜单数据导入服务异常')
    }
  } catch (error: any) {
    console.error('检查状态失败:', error)
    message.error('检查服务状态失败: ' + (error.message || '未知错误'))
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 可以在这里做一些初始化工作
})
</script>

<style scoped>
.menu-data-import {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

pre {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style> 