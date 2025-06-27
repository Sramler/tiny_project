<template>
  <!-- 外层内容容器，统一风格 -->
  <div class="content-container" style="position: relative;">
    <div class="content-card">
      <!-- 查询表单 -->
      <div class="form-container">
        <a-form layout="inline" :model="query">
          <a-form-item label="菜单名称">
            <a-input v-model:value="query.title" placeholder="请输入菜单名称" />
          </a-form-item>
          <a-form-item label="权限标识">
            <a-input v-model:value="query.permission" placeholder="请输入权限标识" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="throttledSearch">搜索</a-button>
            <a-button class="ml-2" @click="throttledReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>
      
      <!-- 工具栏 -->
      <div class="toolbar-container">
        <div class="table-title">菜单管理</div>
        <div class="table-actions">
          <a-button type="link" @click="throttledCreate" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建菜单
          </a-button>
          <a-tooltip title="刷新">
            <span class="action-icon" @click="throttledRefresh">
              <ReloadOutlined :spin="refreshing" />
            </span>
          </a-tooltip>
          <a-tooltip title="展开全部">
            <span class="action-icon" @click="expandAll">
              <ExpandOutlined />
            </span>
          </a-tooltip>
          <a-tooltip title="折叠全部">
            <span class="action-icon" @click="collapseAll">
              <CompressOutlined />
            </span>
          </a-tooltip>
        </div>
      </div>
      
      <!-- 表格区域 -->
      <div class="table-container" ref="tableContentRef">
        <div class="table-scroll-container" ref="tableScrollContainerRef">
          <a-table
            :columns="columns"
            :data-source="tableData"
            :pagination="false"
            :row-key="(record: any) => String(record.id)"
            bordered
            :loading="loading"
            :scroll="{ x: 1200, y: tableBodyHeight }"
            :expandable="expandableConfig"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'icon'">
                <div class="icon-cell">
                  <component 
                    v-if="record.icon && record.showIcon" 
                    :is="getIconComponent(record.icon)" 
                    class="menu-icon"
                  />
                  <span v-else class="no-icon">-</span>
                </div>
              </template>
              <template v-else-if="column.dataIndex === 'hidden'">
                <a-tag :color="record.hidden ? 'red' : 'green'">
                  {{ record.hidden ? '隐藏' : '显示' }}
                </a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'keepAlive'">
                <a-tag :color="record.keepAlive ? 'blue' : 'default'">
                  {{ record.keepAlive ? '缓存' : '不缓存' }}
                </a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'type'">
                <a-tag :color="getTypeColor(record.type)">
                  {{ getTypeText(record.type) }}
                </a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'action'">
                <div class="action-buttons">
                  <a-button type="link" size="small" @click.stop="throttledEdit(record)" class="action-btn">
                    <template #icon>
                      <EditOutlined />
                    </template>
                    编辑
                  </a-button>
                  <a-button type="link" size="small" @click.stop="throttledAddChild(record)" class="action-btn">
                    <template #icon>
                      <PlusOutlined />
                    </template>
                    添加子菜单
                  </a-button>
                  <a-button type="link" size="small" danger @click.stop="throttledDelete(record)" class="action-btn">
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                    删除
                  </a-button>
                </div>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>
    
    <!-- 抽屉表单 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerMode === 'create' ? '新建菜单' : '编辑菜单'"
      width="600px"
      :get-container="false"
      :style="{ position: 'absolute' }"
      @close="handleDrawerClose"
    >
      <MenuForm
        v-if="drawerVisible"
        :mode="drawerMode"
        :menu-data="currentMenu"
        :parent-menu="parentMenu"
        @submit="handleFormSubmit"
        @cancel="handleDrawerClose"
      />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
// 引入Vue相关API
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
// 引入菜单API（需要创建）
import { menuList, createMenu, updateMenu, deleteMenu } from '@/api/menu'
// 引入Antd组件和图标
import { message, Modal } from 'ant-design-vue'
import { 
  ReloadOutlined, 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined,
  ExpandOutlined,
  CompressOutlined,
  // 常用菜单图标
  HomeOutlined,
  UserOutlined,
  SettingOutlined,
  DashboardOutlined,
  TeamOutlined,
  FileOutlined,
  FolderOutlined,
  AppstoreOutlined
} from '@ant-design/icons-vue'
import MenuForm from './MenuForm.vue'

// 查询条件
const query = ref({ 
  title: '', 
  permission: '' 
})

// 表格数据
const tableData = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 刷新动画状态
const refreshing = ref(false)

// 展开的节点keys
const expandedRowKeys = ref<string[]>([])

// 表格内容区高度自适应
const tableContentRef = ref<HTMLElement | null>(null)
const tableScrollContainerRef = ref<HTMLElement | null>(null)
const tableBodyHeight = ref(400)

// 初始列定义
const INITIAL_COLUMNS = [
  { title: '菜单名称', dataIndex: 'title', width: 200 },
  { title: '图标', dataIndex: 'icon', width: 80, align: 'center' },
  { title: '路径', dataIndex: 'path', width: 200 },
  { title: '权限标识', dataIndex: 'permission', width: 200 },
  { title: '类型', dataIndex: 'type', width: 100, align: 'center' },
  { title: '排序', dataIndex: 'sort', width: 80, align: 'center' },
  { title: '显示', dataIndex: 'hidden', width: 80, align: 'center' },
  { title: '缓存', dataIndex: 'keepAlive', width: 80, align: 'center' },
  { title: '操作', dataIndex: 'action', width: 200, fixed: 'right', align: 'center' }
]

// 表格列配置
const columns = computed(() => INITIAL_COLUMNS)

// 展开配置
const expandableConfig = computed(() => ({
  expandedRowKeys: expandedRowKeys.value,
  onExpand: (expanded: boolean, record: any) => {
    const key = String(record.id)
    if (expanded) {
      expandedRowKeys.value = [...expandedRowKeys.value, key]
    } else {
      expandedRowKeys.value = expandedRowKeys.value.filter(k => k !== key)
    }
  }
}))

// 图标组件映射
const iconMap: Record<string, any> = {
  'HomeOutlined': HomeOutlined,
  'UserOutlined': UserOutlined,
  'SettingOutlined': SettingOutlined,
  'DashboardOutlined': DashboardOutlined,
  'TeamOutlined': TeamOutlined,
  'FileOutlined': FileOutlined,
  'FolderOutlined': FolderOutlined,
  'AppstoreOutlined': AppstoreOutlined
}

// 获取图标组件
function getIconComponent(iconName: string) {
  return iconMap[iconName] || AppstoreOutlined
}

// 获取类型颜色
function getTypeColor(type: number) {
  const colorMap: Record<number, string> = {
    0: 'blue',    // 目录
    1: 'green',   // 菜单
    2: 'orange'   // 按钮
  }
  return colorMap[type] || 'default'
}

// 获取类型文本
function getTypeText(type: number) {
  const textMap: Record<number, string> = {
    0: '目录',
    1: '菜单', 
    2: '按钮'
  }
  return textMap[type] || '未知'
}

// 更新表格高度
function updateTableBodyHeight() {
  nextTick(() => {
    if (tableContentRef.value) {
      const containerHeight = tableContentRef.value.clientHeight
      tableBodyHeight.value = Math.max(containerHeight - 100, 200)
    }
  })
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {
      title: query.value.title.trim(),
      permission: query.value.permission.trim()
    }
    const res = await menuList(params)
    tableData.value = Array.isArray(res) ? res : []
  } catch (error) {
    console.error('加载菜单数据失败:', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// 查询
function handleSearch() {
  loadData()
}
const throttledSearch = handleSearch

// 重置
function handleReset() {
  query.value.title = ''
  query.value.permission = ''
  loadData()
}
const throttledReset = handleReset

// 刷新
async function handleRefresh() {
  refreshing.value = true
  loading.value = true
  await loadData().catch((error) => {
    console.error('刷新数据失败:', error)
  }).finally(() => {
    setTimeout(() => {
      refreshing.value = false
    }, 1000)
    loading.value = false
  })
}
const throttledRefresh = handleRefresh

// 展开全部
function expandAll() {
  const allKeys = getAllKeys(tableData.value)
  expandedRowKeys.value = allKeys
}

// 折叠全部
function collapseAll() {
  expandedRowKeys.value = []
}

// 获取所有节点key（递归）
function getAllKeys(data: any[]): string[] {
  const keys: string[] = []
  data.forEach(item => {
    keys.push(String(item.id))
    if (item.children && item.children.length > 0) {
      keys.push(...getAllKeys(item.children))
    }
  })
  return keys
}

// 新建
function handleCreate() {
  drawerMode.value = 'create'
  currentMenu.value = {
    name: '',
    title: '',
    path: '',
    uri: '',
    method: 'GET',
    icon: '',
    showIcon: true,
    sort: 0,
    component: '',
    redirect: '',
    hidden: false,
    keepAlive: false,
    permission: '',
    type: 1,
    parentId: null
  }
  parentMenu.value = null
  drawerVisible.value = true
}
const throttledCreate = handleCreate

// 编辑
function handleEdit(record: any) {
  drawerMode.value = 'edit'
  currentMenu.value = { ...record }
  parentMenu.value = null
  drawerVisible.value = true
}
const throttledEdit = handleEdit

// 添加子菜单
function handleAddChild(record: any) {
  drawerMode.value = 'create'
  currentMenu.value = {
    name: '',
    title: '',
    path: '',
    uri: '',
    method: 'GET',
    icon: '',
    showIcon: true,
    sort: 0,
    component: '',
    redirect: '',
    hidden: false,
    keepAlive: false,
    permission: '',
    type: 1,
    parentId: record.id
  }
  parentMenu.value = record
  drawerVisible.value = true
}
const throttledAddChild = handleAddChild

// 删除
function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除菜单 "${record.title}" 吗？${record.children && record.children.length > 0 ? '删除后其子菜单也会被删除！' : ''}`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return deleteMenu(record.id).then(() => {
        message.success('删除成功')
        loadData()
      }).catch((error: any) => {
        message.error('删除失败: ' + (error.message || '未知错误'))
        return Promise.reject(error)
      })
    }
  })
}
const throttledDelete = handleDelete

// 抽屉相关
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const currentMenu = ref<any | null>(null)
const parentMenu = ref<any | null>(null)

// 抽屉关闭
function handleDrawerClose() {
  drawerVisible.value = false
  currentMenu.value = null
  parentMenu.value = null
}

// 保存（新建/编辑）
async function handleFormSubmit(formData: any) {
  try {
    if (drawerMode.value === 'edit' && formData.id) {
      await updateMenu(formData.id, formData)
      message.success('更新成功')
    } else {
      await createMenu(formData)
      message.success('创建成功')
    }
    handleDrawerClose()
    loadData()
  } catch (error: any) {
    message.error('保存失败: ' + (error.message || '未知错误'))
  }
}

// 生命周期钩子
onMounted(() => {
  loadData()
  updateTableBodyHeight()
  window.addEventListener('resize', updateTableBodyHeight)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateTableBodyHeight)
})
</script>

<style scoped>
/* 复用用户管理页面样式，保证风格一致 */
.content-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.content-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.form-container {
  padding: 24px;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.toolbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 8px 24px 8px 24px;
}

.table-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
}

.table-scroll-container {
  min-height: 0;
  overflow: auto;
}

.ml-2 { margin-left: 8px; }

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #222;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-btn {
  border-radius: 4px;
  height: 32px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.action-icon {
  font-size: 18px;
  cursor: pointer;
  color: #595959;
  border-radius: 4px;
  padding: 8px;
  transition: color 0.2s, background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 32px;
  min-height: 32px;
}

.action-icon:hover {
  color: #1890ff;
  background: #f5f5f5;
}

.icon-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-icon {
  font-size: 16px;
  color: #1890ff;
}

.no-icon {
  color: #bfbfbf;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.action-btn {
  padding: 2px 4px;
  height: auto;
  line-height: 1.2;
  font-size: 12px;
}

.action-btn:hover {
  background-color: #f5f5f5;
  border-radius: 4px;
}

/* 表格样式 */
:deep(.ant-table-tbody > tr:nth-child(odd)) {
  background-color: #fafbfc;
}

:deep(.ant-table-tbody > tr:nth-child(even)) {
  background-color: #fff;
}

:deep(.ant-table-tbody > tr:hover) {
  background-color: #f5f5f5 !important;
}
</style> 