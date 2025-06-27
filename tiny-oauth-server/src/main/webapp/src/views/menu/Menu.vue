<template>
  <!-- 外层内容容器，统一风格 -->
  <div class="content-container" style="position: relative;">
    <div class="content-card">
      <!-- 查询表单，风格与用户管理一致 -->
      <div class="form-container">
        <a-form layout="inline" :model="query">
          <a-form-item label="菜单名称">
            <a-input v-model:value="query.name" placeholder="请输入菜单名称" />
          </a-form-item>
          <a-form-item label="菜单标题">
            <a-input v-model:value="query.title" placeholder="请输入菜单标题" />
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
      
      <!-- 工具栏，包含批量操作、新建、刷新、列设置等 -->
      <div class="toolbar-container">
        <div class="table-title">菜单列表</div>
        <div class="table-actions">
          <div v-if="selectedRowKeys.length > 0" class="batch-actions">
            <a-button type="primary" danger @click="throttledBatchDelete" class="toolbar-btn">
              批量删除 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button @click="clearSelection" class="toolbar-btn">取消选择</a-button>
          </div>
          <a-button type="link" @click="throttledCreate" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-tooltip title="刷新">
            <span class="action-icon" @click="throttledRefresh">
              <ReloadOutlined :spin="refreshing" />
            </span>
          </a-tooltip>
          <a-popover placement="bottomRight" trigger="click">
            <template #content>
              <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                <div style="display: flex; align-items: center;">
                  <a-checkbox
                    :checked="showColumnKeys.length === allColumns.length"
                    :indeterminate="showColumnKeys.length > 0 && showColumnKeys.length < allColumns.length"
                    @change="onCheckAllChange"
                  />
                  <span style="font-weight: bold; margin-left: 8px;">列展示/排序</span>
                </div>
                <span
                  style="font-weight: bold; color: #1677ff; cursor: pointer;"
                  @click="resetColumnOrder"
                >
                  重置
                </span>
              </div>
              <VueDraggable
                v-model="draggableColumns"
                :item-key="(item: any) => item?.dataIndex || ('col_' + Math.random())"
                handle=".drag-handle"
                @end="onDragEnd"
                class="draggable-columns"
                ghost-class="sortable-ghost"
                chosen-class="sortable-chosen"
                tag="div"
              >
                <template #item="{ element }">
                  <div class="draggable-column-item">
                    <HolderOutlined class="drag-handle" />
                    <a-checkbox
                      :checked="showColumnKeys.includes(element.dataIndex)"
                      @change="(e: any) => onCheckboxChange(element.dataIndex, e.target.checked)"
                    >
                      {{ element.title }}
                    </a-checkbox>
                  </div>
                </template>
              </VueDraggable>
            </template>
            <a-tooltip title="列设置">
              <SettingOutlined class="action-icon" />
            </a-tooltip>
          </a-popover>
        </div>
      </div>
      
      <!-- 表格区域，支持多选、动态列、分页 -->
      <div class="table-container" ref="tableContentRef">
        <div class="table-scroll-container" ref="tableScrollContainerRef">
          <a-table
            :columns="columns"
            :data-source="tableData"
            :pagination="false"
            :row-key="(record: any) => String(record.id)"
            bordered
            :loading="loading"
            :row-selection="rowSelection"
            :custom-row="onCustomRow"
            :row-class-name="getRowClassName"
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
        <div class="pagination-container" ref="paginationRef">
          <a-pagination
            v-model:current="pagination.current"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            :show-size-changer="pagination.showSizeChanger"
            :page-size-options="paginationConfig.pageSizeOptions"
            :show-total="pagination.showTotal"
            @change="handlePageChange"
            @showSizeChange="handlePageSizeChange"
            :locale="{ items_per_page: '条/页' }"
          />
        </div>
      </div>
    </div>
    
    <!-- 抽屉表单，编辑/新建菜单 -->
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
// 引入菜单API
import { menuList, createMenu, updateMenu, deleteMenu, batchDeleteMenus, type MenuItem, type MenuQuery } from '@/api/menu'
// 引入Antd组件和图标
import { message, Modal } from 'ant-design-vue'
import { 
  ReloadOutlined, 
  PlusOutlined, 
  EditOutlined, 
  DeleteOutlined,
  SettingOutlined,
  HolderOutlined,
  // 常用菜单图标
  HomeOutlined,
  UserOutlined,
  DashboardOutlined,
  TeamOutlined,
  FileOutlined,
  FolderOutlined,
  AppstoreOutlined,
  MenuOutlined,
  ApiOutlined
} from '@ant-design/icons-vue'
import VueDraggable from 'vuedraggable'
import MenuForm from './MenuForm.vue'

// 查询条件
const query = ref<MenuQuery>({ 
  name: '', 
  title: '', 
  permission: '' 
})

// 表格数据
const tableData = ref<MenuItem[]>([])

// 加载状态
const loading = ref(false)

// 选中行key
const selectedRowKeys = ref<string[]>([])

// 分页配置
const pagination = ref({
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '30', '40', '50'],
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`
})

const paginationConfig = computed(() => ({
  current: Number(pagination.value.current) || 1,
  pageSize: Number(pagination.value.pageSize) || 10,
  showSizeChanger: pagination.value.showSizeChanger,
  pageSizeOptions: pagination.value.pageSizeOptions,
  total: Number(pagination.value.total) || 0,
  showTotal: pagination.value.showTotal
}))

// 所有列定义
const INITIAL_COLUMNS = [
  { title: '菜单名称', dataIndex: 'name', width: 150 },
  { title: '菜单标题', dataIndex: 'title', width: 150 },
  { title: '图标', dataIndex: 'icon', width: 80, align: 'center' },
  { title: '路径', dataIndex: 'path', width: 200 },
  { title: '权限标识', dataIndex: 'permission', width: 200 },
  { title: '排序', dataIndex: 'sort', width: 80, align: 'center' },
  { title: '显示', dataIndex: 'hidden', width: 80, align: 'center' },
  { title: '缓存', dataIndex: 'keepAlive', width: 80, align: 'center' },
  { title: '操作', dataIndex: 'action', width: 200, fixed: 'right', align: 'center' }
]

const allColumns = ref([...INITIAL_COLUMNS])
const draggableColumns = ref([...INITIAL_COLUMNS])
const showColumnKeys = ref(
  INITIAL_COLUMNS.map(col => col.dataIndex).filter(key => typeof key === 'string' && key)
)

watch(allColumns, (val) => {
  showColumnKeys.value = showColumnKeys.value.filter(key => val.some(col => col.dataIndex === key))
})

watch(draggableColumns, (val) => {
  allColumns.value = val.filter(col => typeof col.dataIndex === 'string')
  showColumnKeys.value = showColumnKeys.value.filter(key => allColumns.value.some(col => col.dataIndex === key))
})

// 列复选框变化
function onCheckboxChange(dataIndex: string, checked: boolean) {
  if (!dataIndex) return
  if (checked) {
    if (!showColumnKeys.value.includes(dataIndex)) showColumnKeys.value.push(dataIndex)
  } else {
    showColumnKeys.value = showColumnKeys.value.filter(key => key !== dataIndex)
  }
}

// 列全选
function onCheckAllChange(e: any) {
  if (e.target.checked) {
    showColumnKeys.value = INITIAL_COLUMNS.map(col => col.dataIndex)
  } else {
    showColumnKeys.value = []
  }
}

const DEFAULT_COLUMN_KEYS = INITIAL_COLUMNS
  .filter(col => typeof col.dataIndex === 'string' && col.dataIndex)
  .map(col => col.dataIndex)

function resetColumnOrder() {
  allColumns.value = [...INITIAL_COLUMNS]
  draggableColumns.value = [...INITIAL_COLUMNS]
  showColumnKeys.value = [...DEFAULT_COLUMN_KEYS]
}

// 拖拽结束
function onDragEnd() {}

// 计算最终表格列
const columns = computed(() => [
  {
    title: '序号',
    dataIndex: 'index',
    width: 80,
    align: 'center',
    fixed: 'left',
    customRender: ({ index }: { index?: number }) => {
      const current = Number(pagination.value.current) || 1
      const pageSize = Number(pagination.value.pageSize) || 10
      return (current - 1) * pageSize + (typeof index === 'number' ? index : 0) + 1
    }
  },
  ...INITIAL_COLUMNS.filter(col => showColumnKeys.value.includes(col.dataIndex))
])

// 多选配置
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (selectedKeys: (string|number)[]) => {
    selectedRowKeys.value = selectedKeys.map(String)
  },
  checkStrictly: false,
  preserveSelectedRowKeys: true,
  fixed: true
}))

// 展开配置
const expandableConfig = {
  expandedRowKeys: expandedRowKeys.value,
  onExpand: (expanded: boolean, record: any) => {
    if (expanded) {
      expandedRowKeys.value.push(String(record.id))
    } else {
      expandedRowKeys.value = expandedRowKeys.value.filter(key => key !== String(record.id))
    }
  },
  onExpandedRowsChange: (expandedRows: any[]) => {
    expandedRowKeys.value = expandedRows.map(row => String(row.id))
  }
}

// 表格内容区高度自适应
const tableContentRef = ref<HTMLElement | null>(null)
const tableScrollContainerRef = ref<HTMLElement | null>(null)
const paginationRef = ref<HTMLElement | null>(null)
const tableBodyHeight = ref(400)

function updateTableBodyHeight() {
  nextTick(() => {
    if (tableContentRef.value && paginationRef.value) {
      const tableHeader = tableContentRef.value.querySelector('.ant-table-header') as HTMLElement
      const containerHeight = tableContentRef.value.clientHeight
      const paginationHeight = paginationRef.value.clientHeight
      const tableHeaderHeight = tableHeader ? tableHeader.clientHeight : 55
      const bodyHeight = containerHeight - paginationHeight - tableHeaderHeight
      tableBodyHeight.value = Math.max(bodyHeight, 200)
    }
  })
}

// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {
      name: query.value.name?.trim(),
      title: query.value.title?.trim(),
      permission: query.value.permission?.trim(),
      page: (Number(pagination.value.current) || 1) - 1,
      size: Number(pagination.value.pageSize) || 10
    }
    const res = await menuList(params)
    tableData.value = Array.isArray(res.content) ? res.content : []
    pagination.value.total = res.totalElements || 0
  } catch (error) {
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 查询
function handleSearch() {
  pagination.value.current = 1
  loadData()
}
const throttledSearch = handleSearch

// 重置
function handleReset() {
  query.value = { name: '', title: '', permission: '' }
  pagination.value.current = 1
  loadData()
}
const throttledReset = handleReset

// 新建
function handleCreate() {
  drawerMode.value = 'create'
  currentMenu.value = { name: '', title: '', sort: 0, showIcon: true, hidden: false, keepAlive: false }
  parentMenu.value = null
  drawerVisible.value = true
}
const throttledCreate = handleCreate

// 刷新动画状态
const refreshing = ref(false)
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

// 清除选择
function clearSelection() { 
  selectedRowKeys.value = [] 
}

// 分页变化
function handlePageChange(page: number) {
  pagination.value.current = page || 1
  loadData()
}

function handlePageSizeChange(current: number, size: number) {
  pagination.value.pageSize = size || 10
  pagination.value.current = 1
  loadData()
}

// 批量删除
function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的菜单')
    return
  }
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个菜单吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return batchDeleteMenus(selectedRowKeys.value)
        .then(() => {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          loadData()
        })
        .catch((error: any) => {
          message.error('批量删除失败: ' + (error.message || '未知错误'))
          return Promise.reject(error)
        })
    }
  })
}
const throttledBatchDelete = handleBatchDelete

// 单条删除
function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除菜单 ${record.title} 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return deleteMenu(record.id).then(() => {
        message.success('删除成功')
        loadData()
      }).catch((error: any) => {
        message.error('删除菜单失败: ' + (error.message || '未知错误'))
        return Promise.reject(error)
      })
    }
  })
}
const throttledDelete = handleDelete

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
  currentMenu.value = { name: '', title: '', sort: 0, showIcon: true, hidden: false, keepAlive: false }
  parentMenu.value = record
  drawerVisible.value = true
}
const throttledAddChild = handleAddChild

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

watch(() => pagination.value.pageSize, () => {
  updateTableBodyHeight()
})

// 抽屉相关
const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('edit')
const currentMenu = ref<MenuItem | null>(null)
const parentMenu = ref<MenuItem | null>(null)

// 展开的节点keys
const expandedRowKeys = ref<string[]>([])

// 行点击事件
function onCustomRow(record: any) {
  return {
    onClick: (event: MouseEvent) => {
      if ((event.target as HTMLElement).closest('.ant-checkbox-wrapper')) return
      const recordId = String(record.id)
      const isSelected = selectedRowKeys.value.includes(recordId)
      if (isSelected && selectedRowKeys.value.length === 1) {
        selectedRowKeys.value = []
      } else {
        selectedRowKeys.value = [recordId]
      }
    }
  }
}

function getRowClassName(record: any) {
  if (selectedRowKeys.value.includes(String(record.id))) {
    return 'checkbox-selected-row'
  }
  return ''
}

// 图标组件映射
const iconMap: Record<string, any> = {
  'HomeOutlined': HomeOutlined,
  'UserOutlined': UserOutlined,
  'DashboardOutlined': DashboardOutlined,
  'TeamOutlined': TeamOutlined,
  'FileOutlined': FileOutlined,
  'FolderOutlined': FolderOutlined,
  'AppstoreOutlined': AppstoreOutlined,
  'MenuOutlined': MenuOutlined,
  'ApiOutlined': ApiOutlined,
  'SettingOutlined': SettingOutlined
}

function getIconComponent(iconName: string) {
  return iconMap[iconName] || MenuOutlined
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

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #fff;
}

:deep(.ant-pagination) {
  min-height: 32px !important;
  height: 32px !important;
  line-height: 32px !important;
  display: flex !important;
  flex-direction: row !important;
  align-items: center !important;
}

:deep(.ant-pagination-item),
:deep(.ant-pagination-item-link),
:deep(.ant-pagination-prev),
:deep(.ant-pagination-next),
:deep(.ant-pagination-jump-next),
:deep(.ant-pagination-jump-prev) {
  height: 32px !important;
  min-width: 32px !important;
  line-height: 32px !important;
  box-sizing: border-box;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0 !important;
}

:deep(.ant-pagination-item-ellipsis) {
  line-height: 32px !important;
  vertical-align: middle !important;
  display: inline-block !important;
  font-size: 16px !important;
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

.batch-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  border-radius: 0;
  padding: 0;
  margin-right: 0;
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

.action-icon.active {
  color: #1890ff;
  background: #e6f7ff;
}

.draggable-columns {
  max-height: 300px;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.draggable-columns::-webkit-scrollbar {
  display: none;
}

.draggable-column-item {
  display: flex;
  align-items: center;
  padding: 4px 2px;
  margin-bottom: 4px;
  background: transparent;
  border-radius: 4px;
  transition: background-color 0.2s ease;
  cursor: default;
}

.draggable-column-item:hover {
  background-color: #f5f5f5;
}

.draggable-column-item.sortable-ghost {
  opacity: 0.5;
  background: #e6f7ff;
}

.draggable-column-item.sortable-chosen {
  background: #e6f7ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.drag-handle {
  margin-right: 8px;
  color: #bfbfbf;
  font-size: 16px;
  cursor: move;
  transition: color 0.2s;
}

.drag-handle:hover {
  color: #1890ff;
}

.sortable-ghost .drag-handle {
  color: #1890ff;
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

/* 复用user/index.vue的隔行换色和高亮样式 */
:deep(.ant-table-tbody > tr:nth-child(odd)) {
  background-color: #fafbfc;
}
:deep(.ant-table-tbody > tr:nth-child(even)) {
  background-color: #fff;
}
:deep(.ant-table-tbody > tr.checkbox-selected-row) {
  background-color: #e6f7ff !important;
}
:deep(.ant-table-tbody > tr.checkbox-selected-row:hover) {
  background-color: #bae7ff !important;
}
</style> 