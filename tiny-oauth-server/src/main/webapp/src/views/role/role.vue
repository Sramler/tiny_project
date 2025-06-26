<template>
  <!-- 外层内容容器，统一风格 -->
  <div class="content-container" style="position: relative;">
    <div class="content-card">
      <!-- 查询表单，风格与用户管理一致 -->
      <div class="form-container">
        <a-form layout="inline" :model="query">
          <a-form-item label="角色名">
            <a-input v-model:value="query.name" placeholder="请输入角色名" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="throttledSearch">搜索</a-button>
            <a-button class="ml-2" @click="throttledReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>
      <!-- 工具栏，包含批量操作、新建、刷新、列设置等 -->
      <div class="toolbar-container">
        <div class="table-title">角色列表</div>
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
                <span style="font-weight: bold; color: #1677ff; cursor: pointer;" @click="resetColumnOrder">重置</span>
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
            :scroll="{ x: 900, y: tableBodyHeight }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'action'">
                <div class="action-buttons">
                  <a-button type="link" size="small" @click.stop="throttledEdit(record)" class="action-btn">编辑</a-button>
                  <a-button type="link" size="small" danger @click.stop="throttledDelete(record)" class="action-btn">删除</a-button>
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
    <!-- 抽屉表单，编辑/新建角色 -->
    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerMode === 'create' ? '新建角色' : '编辑角色'"
      width="400px"
      :get-container="false"
      :style="{ position: 'absolute' }"
      @close="handleDrawerClose"
    >
      <RoleForm
        v-if="drawerVisible"
        :mode="drawerMode"
        :role-data="currentRole"
        @submit="handleFormSubmit"
        @cancel="handleDrawerClose"
      />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
// 引入Vue相关API
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
// 引入角色API
import { roleList, createRole, updateRole, deleteRole } from '@/api/role'
// 引入Antd组件和图标
import { message, Modal } from 'ant-design-vue'
import { ReloadOutlined, SettingOutlined, HolderOutlined, PlusOutlined } from '@ant-design/icons-vue'
import VueDraggable from 'vuedraggable'
import RoleForm from './RoleForm.vue'

// 查询条件
const query = ref({ name: '' })
// 表格数据
const tableData = ref<any[]>([])
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
const allColumns = ref([
  { title: 'ID', dataIndex: 'id' },
  { title: '角色名', dataIndex: 'name' },
  { title: '描述', dataIndex: 'description' },
  { title: '操作', dataIndex: 'action', width: 160, fixed: 'right', align: 'center' }
])
// 可拖拽列
const draggableColumns = ref([...allColumns.value])
// 当前显示的列
const showColumnKeys = ref(
  allColumns.value.map(col => col.dataIndex).filter(key => typeof key === 'string' && key)
)
// 列变化时同步
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
    showColumnKeys.value = allColumns.value.map(col => col.dataIndex)
  } else {
    showColumnKeys.value = []
  }
}
// 重置列顺序
function resetColumnOrder() {
  draggableColumns.value = [...allColumns.value]
  showColumnKeys.value = allColumns.value.map(col => col.dataIndex)
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
  ...allColumns.value.filter(col => showColumnKeys.value.includes(col.dataIndex))
])
// 多选配置，所有 id 强制转字符串，保证类型一致
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (selectedKeys: (string|number)[]) => {
    // 全部转为字符串，防止类型不一致
    selectedRowKeys.value = selectedKeys.map(String);
  },
  checkStrictly: false,
  preserveSelectedRowKeys: true,
  fixed: true
}))
// 表格内容区高度自适应
const tableContentRef = ref<HTMLElement | null>(null)
const tableScrollContainerRef = ref<HTMLElement | null>(null)
const paginationRef = ref<HTMLElement | null>(null)
const tableBodyHeight = ref(400)
function updateTableBodyHeight() {
  nextTick(() => {
    if (tableContentRef.value && paginationRef.value) {
      const tableHeader = tableContentRef.value.querySelector('.ant-table-header') as HTMLElement;
      const containerHeight = tableContentRef.value.clientHeight;
      const paginationHeight = paginationRef.value.clientHeight;
      const tableHeaderHeight = tableHeader ? tableHeader.clientHeight : 55;
      const bodyHeight = containerHeight - paginationHeight - tableHeaderHeight;
      tableBodyHeight.value = Math.max(bodyHeight, 200);
    }
  });
}
// 加载数据
async function loadData() {
  loading.value = true
  try {
    const params = {
      name: query.value.name.trim(),
      page: (Number(pagination.value.current) || 1) - 1,
      size: Number(pagination.value.pageSize) || 10
    }
    const res = await roleList(params)
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
  query.value.name = ''
  pagination.value.current = 1
  loadData()
}
const throttledReset = handleReset
// 新建
function handleCreate() {
  drawerMode.value = 'create'
  currentRole.value = { id: '', name: '', description: '' }
  drawerVisible.value = true
}
const throttledCreate = handleCreate
// 刷新动画状态，控制刷新按钮旋转
const refreshing = ref(false)
async function handleRefresh() {
  refreshing.value = true      // 开始旋转刷新按钮
  loading.value = true         // 显示表格 loading
  await loadData().catch((error) => {
    console.error('刷新数据失败:', error)
  }).finally(() => {
    setTimeout(() => {
      refreshing.value = false
    }, 1000) // 1秒后停止旋转
    loading.value = false      // 关闭表格 loading
  })
}
const throttledRefresh = handleRefresh
// 清除选择
function clearSelection() { selectedRowKeys.value = [] }
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
    message.warning('请先选择要删除的角色')
    return
  }
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个角色吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return Promise.all(selectedRowKeys.value.map(id => deleteRole(id)))
        .then(() => {
          message.success('批量删除成功')
          selectedRowKeys.value = []
          loadData()
        })
        .catch((error: any) => {
          message.error('批量删除失败: ' + (error.message || '未知错误'))
          return Promise.reject(error);
        })
    }
  })
}
const throttledBatchDelete = handleBatchDelete
// 单条删除
function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除角色 ${record.name} 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return deleteRole(record.id).then(() => {
        message.success('删除成功')
        loadData()
      }).catch((error: any) => {
        message.error('删除角色失败: ' + (error.message || '未知错误'))
        return Promise.reject(error);
      });
    }
  })
}
const throttledDelete = handleDelete
// 编辑
function handleEdit(record: any) {
  drawerMode.value = 'edit'
  currentRole.value = { ...record }
  drawerVisible.value = true
}
const throttledEdit = handleEdit
// 抽屉关闭
function handleDrawerClose() {
  drawerVisible.value = false
  currentRole.value = null
}
// 保存（新建/编辑）
async function handleFormSubmit(formData: any) {
  try {
    if (drawerMode.value === 'edit' && formData.id) {
      await updateRole(formData.id, formData)
      message.success('更新成功')
    } else {
      await createRole(formData)
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
const currentRole = ref<any | null>(null)

// 行点击事件，单选/取消唯一选中，和用户管理完全一致，所有 id 统一用字符串
function onCustomRow(record: any) {
  return {
    onClick: (event: MouseEvent) => {
      // 如果点击的是复选框本身则不处理
      if ((event.target as HTMLElement).closest('.ant-checkbox-wrapper')) return;
      // 统一用字符串类型的 id
      const recordId = String(record.id)
      const isSelected = selectedRowKeys.value.includes(recordId)
      if (isSelected && selectedRowKeys.value.length === 1) {
        // 只选中当前行时，点击取消选中
        selectedRowKeys.value = []
      } else {
        // 只选中当前行
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
</script>

<style scoped>
/* 复用用户管理页面样式，保证风格一致 */
.content-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  /* background: #f0f2f5; */
  background: #fff; /* 与 user.vue 保持一致 */
}

.content-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0; /* 关键，防止撑开 */
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
  display: flex;              /* 启用flex布局 */
  flex-direction: column;     /* 垂直排列子元素 */
  flex: 1;                    /* 占满父容器剩余空间 */
  min-height: 0;              /* 关键，防止撑开 */
}

.table-scroll-container {
  /* 不要设置 flex: 1; */
  min-height: 0; /* 可选，防止撑开 */
  overflow: auto; /* 内容多时滚动 */
}

.pagination-container {
  display: flex;                /* 启用flex布局 */
  align-items: center;          /* 垂直居中 */
  justify-content: flex-end;    /* 右对齐 */
  background: #fff;             /* 可选，分页条背景 */
/* 不要设置height/line-height/padding-top/padding-bottom */
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