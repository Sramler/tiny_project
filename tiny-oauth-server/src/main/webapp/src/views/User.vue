<template>
  <div class="page-container" style="position: relative;">
    <div class="card">
      <div class="form-card">
        <a-form layout="inline" :model="query">
          <a-form-item label="用户名">
            <a-input v-model:value="query.username" placeholder="请输入用户名" />
          </a-form-item>
          <a-form-item label="昵称">
            <a-input v-model:value="query.nickname" placeholder="请输入昵称" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="throttledSearch">搜索</a-button>
            <a-button class="ml-2" @click="throttledReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>
      
      <div class="table-toolbar">
        <div class="table-title">
          用户列表
        </div>
        <div class="table-actions">
          <div v-if="selectedRowKeys.length > 0" class="batch-actions">
            <a-button 
              type="primary" 
              danger 
              @click="throttledBatchDelete" 
              class="toolbar-btn"
            >
              <template #icon>
                <DeleteOutlined />
              </template>
              批量删除 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button 
              type="primary" 
              @click="throttledBatchEnable" 
              class="toolbar-btn"
            >
              <template #icon>
                <CheckCircleOutlined />
              </template>
              批量启用
            </a-button>
            <a-button 
              @click="throttledBatchDisable" 
              class="toolbar-btn"
            >
              <template #icon>
                <StopOutlined />
              </template>
              批量禁用
            </a-button>
            <a-button 
              @click="clearSelection" 
              class="toolbar-btn"
            >
              <template #icon>
                <CloseOutlined />
              </template>
              取消选择
            </a-button>
          </div>
          
          <a-button type="primary" @click="throttledCreate" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-tooltip title="刷新">
            <ReloadOutlined class="action-icon" @click="throttledRefresh" />
          </a-tooltip>
          <a-tooltip :title="showSortTooltip ? '关闭排序提示' : '开启排序提示'">
            <PoweroffOutlined
              :class="['action-icon', { active: showSortTooltip }]"
              @click="showSortTooltip = !showSortTooltip"
            />
          </a-tooltip>
          <a-popover
            placement="bottomRight"
            trigger="click"
            :destroyTooltipOnHide="false"
          >
            <template #content>
              <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                <div style="display: flex; align-items: center;">
                  <a-checkbox
                    :checked="showColumnKeys.length === allColumns.length"
                    :indeterminate="showColumnKeys.length > 0 && showColumnKeys.length < allColumns.length"
                    @change="(e: any) => onCheckAllChange(e)"
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
                :item-key="(item: any) => item?.dataIndex || `col_${Math.random()}`"
                handle=".drag-handle"
                @end="onDragEnd"
                class="draggable-columns"
                ghost-class="sortable-ghost"
                chosen-class="sortable-chosen"
                tag="div"
              >
                <template #item="{ element: col }">
                  <div class="draggable-column-item">
                    <HolderOutlined class="drag-handle" />
                    <a-checkbox
                      :checked="showColumnKeys.includes(col.dataIndex)"
                      @change="(e: any) => onCheckboxChange(col.dataIndex, e.target.checked)"
                    >
                      {{ col.title }}
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
      <div class="table-content" ref="tableContentRef">
        <a-table
          :columns="columns"
          :data-source="tableData"
          :pagination="false"
          :row-key="(record: any) => record?.id || `row_${Math.random()}`"
          bordered
          :loading="loading"
          @change="handleTableChange"
          :row-selection="rowSelection"
          :custom-row="onCustomRow"
          :row-class-name="getRowClassName"
          :scroll="{ x: 1500, y: tableBodyHeight }"
          :locale="tableLocale"
          :show-sorter-tooltip="showSortTooltip"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="['enabled','account_non_expired','account_non_locked','credentials_non_expired'].includes(column.dataIndex)">
              <a-tag :color="record[column.dataIndex] ? 'green' : 'red'">
                {{ record[column.dataIndex] ? '是' : '否' }}
              </a-tag>
            </template>
            <template v-else-if="column.dataIndex === 'action'">
              <div class="action-buttons">
                <a-button 
                  type="link" 
                  size="small" 
                  @click="throttledEdit(record)"
                  class="action-btn"
                >
                  <template #icon>
                    <EditOutlined />
                  </template>
                  编辑
                </a-button>
                <a-button 
                  type="link" 
                  size="small" 
                  danger 
                  @click="throttledDelete(record)"
                  class="action-btn"
                >
                  <template #icon>
                    <DeleteOutlined />
                  </template>
                  删除
                </a-button>
                <a-button 
                  type="link" 
                  size="small" 
                  @click="handleView(record)"
                  class="action-btn"
                >
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看
                </a-button>
              </div>
            </template>
          </template>
        </a-table>
      </div>
      <div class="table-pagination" ref="paginationRef">
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

    <a-drawer
      v-model:open="drawerVisible"
      :title="drawerMode === 'create' ? '新建用户' : drawerMode === 'edit' ? '编辑用户' : '查看用户'"
      width="50%"
      :get-container="false"
      :style="{ position: 'absolute' }"
      @close="handleDrawerClose"
    >
      <UserEditModal
        v-if="drawerVisible"
        :mode="drawerMode"
        :user-data="currentUser"
        @submit="handleDrawerClose"
        @cancel="handleDrawerClose"
      />
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, h, onBeforeUnmount, nextTick } from 'vue'
import { userList, updateUser, deleteUser, batchDeleteUsers, batchEnableUsers, batchDisableUsers } from '@/api/user'
import { PlusOutlined, ReloadOutlined, SettingOutlined, HolderOutlined, DeleteOutlined, CheckCircleOutlined, StopOutlined, CloseOutlined, EditOutlined, EyeOutlined, InfoCircleOutlined, QuestionCircleOutlined, PoweroffOutlined } from '@ant-design/icons-vue'
import VueDraggable from 'vuedraggable'
import UserEditModal from '@/views/Login/UserEditModal.vue'
import UserViewModal from '@/components/UserViewModal.vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useThrottleFn } from '@/utils/throttle'

const query = ref({
  username: '',
  nickname: ''
})

const tableData = ref<any[]>([])

const loading = ref(false)

const selectedRowKeys = ref<string[]>([])

const pagination = ref({
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '30', '40', '50'].filter(option => option && typeof option === 'string'),
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`
})

const paginationConfig = computed(() => {
  const config = {
    current: Number(pagination.value.current) || 1,
    pageSize: Number(pagination.value.pageSize) || 10,
    showSizeChanger: pagination.value.showSizeChanger,
    pageSizeOptions: pagination.value.pageSizeOptions.filter(option => option && typeof option === 'string'),
    total: Number(pagination.value.total) || 0,
    showTotal: pagination.value.showTotal
  }
  console.log('paginationConfig computed:', config)
  return config
})

const allColumns = ref([
  { title: 'ID', dataIndex: 'id', sorter: true },
  { title: '用户名', dataIndex: 'username', sorter: true },
  { title: '密码', dataIndex: 'password' },
  { title: '昵称', dataIndex: 'nickname', sorter: true },
  { title: '是否启用', dataIndex: 'enabled', sorter: true },
  { title: '账号未过期', dataIndex: 'account_non_expired', sorter: true },
  { title: '账号未锁定', dataIndex: 'account_non_locked', sorter: true },
  { title: '密码未过期', dataIndex: 'credentials_non_expired', sorter: true },
  {
    title: '最后登录时间',
    dataIndex: 'last_login_at',
    width: 180,
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 200,
    fixed: 'right',
    align: 'center'
  }
]);

const draggableColumns = ref([...allColumns.value])

const showColumnKeys = ref(
  allColumns.value
    .map(col => col.dataIndex)
    .filter(key => typeof key === 'string' && key !== undefined && key !== null)
);

watch(allColumns, (val) => {
  showColumnKeys.value = showColumnKeys.value.filter(key =>
    val.some(col => col.dataIndex === key)
  );
});

watch(draggableColumns, (val) => {
  // 只保留合法列
  allColumns.value = val.filter(col => col && typeof col.dataIndex === 'string')
  // showColumnKeys 只保留 allColumns 里存在的 dataIndex
  showColumnKeys.value = showColumnKeys.value.filter(key =>
    allColumns.value.some(col => col.dataIndex === key)
  )
})

function onCheckboxChange(dataIndex: string, checked: boolean) {
  console.log('复选框变化:', dataIndex, checked)
  if (!dataIndex || typeof dataIndex !== 'string') {
    console.warn('无效的 dataIndex:', dataIndex)
    return
  }
  if (checked) {
    if (!showColumnKeys.value.includes(dataIndex)) {
      showColumnKeys.value.push(dataIndex)
    }
  } else {
    showColumnKeys.value = showColumnKeys.value.filter(key => key !== dataIndex)
  }
  console.log('当前显示的列:', showColumnKeys.value)
}

console.log('allColumns.value:', allColumns.value)
console.log('showColumnKeys.value:', showColumnKeys.value)
const columns = computed(() => {
  console.log('columns computed 触发，allColumns:', allColumns.value)
  console.log('columns computed 触发，showColumnKeys:', showColumnKeys.value)
  
  // 只保留合法列
  const filtered = allColumns.value.filter(
    col =>
      col &&
      typeof col.dataIndex === 'string' &&
      col.dataIndex &&
      showColumnKeys.value.includes(col.dataIndex)
  )
  // 打印调试最终columns
  console.log('columns for table:', filtered)
  return [
    {
      title: '序号',
      dataIndex: 'index',
      width: 80,
      align: 'center',
      fixed: 'left',
      customRender: ({ index }: { index?: number }) => {
        const safeIndex = typeof index === 'number' && !isNaN(index) ? index : 0
        const current = Number(pagination.value.current) || 1
        const pageSize = Number(pagination.value.pageSize) || 10
        return (current - 1) * pageSize + safeIndex + 1
      }
    },
    ...filtered
  ]
})

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (selectedKeys: string[], selectedRows: any[]) => {
    selectedRowKeys.value = selectedKeys;
    console.log('复选框选中变化后的行:', selectedRowKeys.value)
  },
  checkStrictly: false,
  preserveSelectedRowKeys: true,
  fixed: true
}))

async function loadData() {
  loading.value = true
  try {
    const params = {
      username: query.value.username.trim(),
      nickname: query.value.nickname.trim(),
      current: Number(pagination.value.current) || 1,
      pageSize: Number(pagination.value.pageSize) || 10
    }
    const res = await userList(params)
    tableData.value = Array.isArray(res.records) ? res.records : []
    pagination.value.total = res.total || 0
  } catch (error) {
    tableData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

const throttledSearch = useThrottleFn(handleSearch, 1000)

function handleReset() {
  query.value.username = ''
  query.value.nickname = ''
  pagination.value.current = 1
  loadData()
}

const throttledReset = useThrottleFn(handleReset, 1000)

function handleTableChange(pag: any, filters: any, sorter: any) {
  console.log('表格变化事件:', { pag, filters, sorter })
  console.log('变化前的 pagination:', pagination.value)
  console.log('变化前的 paginationConfig:', paginationConfig.value)
  console.log('变化前的 columns:', columns.value)
  
  // 添加安全检查
  if (pag && typeof pag.current === 'number') {
    pagination.value.current = pag.current
  }
  if (pag && typeof pag.pageSize === 'number') {
    pagination.value.pageSize = pag.pageSize
  }
  
  console.log('变化后的 pagination:', pagination.value)
  loadData()
  
  console.log('变化后的 columns:', columns.value)
}

function handleCreate() {
  drawerMode.value = 'create'
  currentUser.value = {
    username: '',
    nickname: '',
    enabled: true,
    account_non_expired: true,
    account_non_locked: true,
    credentials_non_expired: true
  }
  drawerVisible.value = true
}

const throttledCreate = useThrottleFn(handleCreate, 500)

function handleRefresh() {
  console.log('刷新按钮被点击')
  query.value.username = ''
  query.value.nickname = ''
  pagination.value.current = 1
  loading.value = true
  loadData().catch((error) => {
    console.error('刷新数据失败:', error)
  }).finally(() => {
    console.log('刷新完成')
  })
}

const throttledRefresh = useThrottleFn(handleRefresh, 1000)

function onCheckAllChange(e: any) {
  console.log('全选复选框变化:', e.target.checked)
  if (e.target.checked) {
    showColumnKeys.value = allColumns.value
      .filter(col => col && typeof col.dataIndex === 'string' && col.dataIndex)
      .map(col => col.dataIndex)
  } else {
    showColumnKeys.value = []
  }
  console.log('当前显示的列:', showColumnKeys.value)
}

function resetColumnOrder() {
  draggableColumns.value = [...allColumns.value]
  showColumnKeys.value = allColumns.value
    .filter(col => col && typeof col.dataIndex === 'string' && col.dataIndex)
    .map(col => col.dataIndex)
}

function onDragEnd(event: any) {
  console.log('拖拽结束，新顺序:', draggableColumns.value.map(col => col.title))
  console.log('拖拽事件详情:', event)
}

function handleCellClick(record: any, dataIndex: string) {
  console.log('单元格被点击:', record, dataIndex)
  
  const recordId = record.id
  const isSelected = selectedRowKeys.value.includes(recordId)

  if (isSelected && selectedRowKeys.value.length === 1) {
    selectedRowKeys.value = []
    console.log('单元格点击: 取消唯一选中行', recordId)
  } else {
    selectedRowKeys.value = [recordId]
    console.log('单元格点击: 仅选中当前行', recordId)
  }
  
  console.log('更新后的复选框选中行:', selectedRowKeys.value)
}

function onCustomRow(record: any) {
  return {
    onClick: (event: MouseEvent) => {
      if ((event.target as HTMLElement).closest('.ant-checkbox-wrapper')) return;
      
      const recordId = record.id
      const isSelected = selectedRowKeys.value.includes(recordId)

      if (isSelected && selectedRowKeys.value.length === 1) {
        selectedRowKeys.value = []
        console.log('行点击: 取消唯一选中行', recordId)
      } else {
        selectedRowKeys.value = [recordId]
        console.log('行点击: 仅选中当前行', recordId)
      }
      
      console.log('点击行后，当前复选框选中:', selectedRowKeys.value)
    }
  }
}

const tableContentRef = ref<HTMLElement | null>(null)
const paginationRef = ref<HTMLElement | null>(null)
const tableBodyHeight = ref(400)

function updateTableBodyHeight() {
  nextTick(() => {
    if (tableContentRef.value && paginationRef.value) {
      const paginationHeight = paginationRef.value.clientHeight
      tableBodyHeight.value = tableContentRef.value.clientHeight - paginationHeight
      if (tableBodyHeight.value < 200) tableBodyHeight.value = 200
    }
  })
}

onMounted(() => {
  updateTableBodyHeight()
  window.addEventListener('resize', updateTableBodyHeight)
  loadData()
})
onBeforeUnmount(() => {
  window.removeEventListener('resize', updateTableBodyHeight)
})

watch(() => pagination.value.pageSize, () => {
  updateTableBodyHeight()
})

function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要删除的用户')
    return
  }
  
  if (confirm(`确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`)) {
    console.log('批量删除用户:', selectedRowKeys.value)
    
    batchDeleteUsers(selectedRowKeys.value)
      .then(() => {
        message.success('批量删除成功')
        selectedRowKeys.value = [] // 清空选择
        loadData() // 重新加载数据
      })
      .catch((error: any) => {
        console.error('批量删除失败:', error)
        message.error('批量删除失败: ' + (error.message || '未知错误'))
      })
  }
}

const throttledBatchDelete = useThrottleFn(handleBatchDelete, 1000)

function handleBatchEnable() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要启用的用户')
    return
  }
  
  console.log('批量启用用户:', selectedRowKeys.value)
  
  batchEnableUsers(selectedRowKeys.value)
    .then(() => {
      message.success('批量启用成功')
      selectedRowKeys.value = [] // 清空选择
      loadData() // 重新加载数据
    })
    .catch((error: any) => {
      console.error('批量启用失败:', error)
      message.error('批量启用失败: ' + (error.message || '未知错误'))
    })
}

const throttledBatchEnable = useThrottleFn(handleBatchEnable, 1000)

function handleBatchDisable() {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请先选择要禁用的用户')
    return
  }
  
  console.log('批量禁用用户:', selectedRowKeys.value)
  
  batchDisableUsers(selectedRowKeys.value)
    .then(() => {
      message.success('批量禁用成功')
      selectedRowKeys.value = [] // 清空选择
      loadData() // 重新加载数据
    })
    .catch((error: any) => {
      console.error('批量禁用失败:', error)
      message.error('批量禁用失败: ' + (error.message || '未知错误'))
    })
}

const throttledBatchDisable = useThrottleFn(handleBatchDisable, 1000)

function clearSelection() {
  selectedRowKeys.value = []
  console.log('清除选择')
}

function getRowClassName(record: any) {
  if (selectedRowKeys.value.includes(record.id)) {
    return 'checkbox-selected-row'
  }
  
  return ''
}

function handlePageChange(page: number) {
  console.log('页码变化:', page)
  pagination.value.current = page || 1
  loadData()
}

function handlePageSizeChange(current: number, size: number) {
  console.log('每页条数变化:', { current, size })
  pagination.value.pageSize = size || 10
  pagination.value.current = 1
  loadData()
}

function handleDelete(record: any) {
  if (confirm(`确定要删除用户 ${record.username} 吗？`)) {
    console.log('删除用户:', record.id)
    
    deleteUser(record.id)
      .then(() => {
        message.success('用户删除成功')
        loadData() // 重新加载数据
      })
      .catch((error: any) => {
        console.error('删除用户失败:', error)
        message.error('删除用户失败: ' + (error.message || '未知错误'))
      })
  }
}

const throttledDelete = useThrottleFn(handleDelete, 500)

function handleView(record: any) {
  drawerMode.value = 'view'
  currentUser.value = record
  drawerVisible.value = true
}

const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit' | 'view'>('edit')
const currentUser = ref<any | null>(null)

function handleEdit(record: any) {
  drawerMode.value = 'edit'
  currentUser.value = record
  drawerVisible.value = true
}

const throttledEdit = useThrottleFn(handleEdit, 500)

function handleDrawerClose() {
  drawerVisible.value = false
  currentUser.value = null
}

const showSortTooltip = ref(true)

// 表格 locale 配置的计算属性
const tableLocale = computed(() => {
  if (showSortTooltip.value) {
    return {
      triggerDesc: '点击降序',
      triggerAsc: '点击升序', 
      cancelSort: '取消排序'
    }
  }
  return undefined // 不显示任何排序提示
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 90px);
  background: #f0f2f5;
  padding: 0;
}

.card {
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  padding: 24px 24px 0 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.form-card {
  padding: 24px 0 24px 0;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  margin-bottom: 0;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}

.table-content {
  flex: 1 1 0%;
  min-height: 0;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  display: flex;
  flex-direction: column;
  padding-bottom: 16px;
}

.table-pagination {
  background: #fff;
  padding: 0 0 8px 0;
  text-align: right;
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

:deep(.ant-table-tbody > tr) {
  cursor: pointer;
  transition: background-color 0.2s ease;
  user-select: none;
}

:deep(.ant-table-tbody > tr:hover) {
  background-color: #f5f5f5 !important;
}

:deep(.ant-table-tbody > tr.checkbox-selected-row) {
  background-color: #e6f7ff !important;
}

:deep(.ant-table-tbody > tr.checkbox-selected-row:hover) {
  background-color: #bae7ff !important;
}

:deep(.ant-table-tbody > tr .ant-checkbox-wrapper) {
  pointer-events: auto;
}

:deep(.ant-table-tbody > tr td) {
  pointer-events: auto;
}

.cell-content {
  width: 100%;
  height: 100%;
  cursor: pointer;
  padding: 4px 0;
  transition: background-color 0.2s ease;
}

.cell-content:hover {
  background-color: rgba(24, 144, 255, 0.1);
  border-radius: 4px;
}

:deep(.ant-pagination) {
  margin-top: 0 !important;
  padding-top: 8px;
  background: transparent;
}

:deep(.ant-table-tbody > tr:nth-child(odd)) {
  /* 奇数行背景色，浅灰色 */
  background-color: #fafbfc;
}

:deep(.ant-table-tbody > tr:nth-child(even)) {
  /* 偶数行背景色，白色 */
  background-color: #fff;
}

/* 隐藏表格内容区的滚动条，但保留滚动功能 */
:deep(.ant-table-body) {
  scrollbar-width: none;           /* Firefox 隐藏滚动条 */
  -ms-overflow-style: none;        /* IE 10+ 隐藏滚动条 */
}
:deep(.ant-table-body::-webkit-scrollbar) {
  display: none;                   /* Chrome/Safari/Edge 隐藏滚动条 */
}

/* 操作按钮样式 */
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

/* 模态框样式优化 */
:deep(.ant-modal-header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-modal-footer) {
  border-top: 1px solid #f0f0f0;
  padding: 16px 24px;
}

/* 表单样式优化 */
:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #262626;
}

:deep(.ant-switch) {
  min-width: 44px;
}

/* 描述列表样式 */
:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  color: #262626;
  background-color: #fafafa;
}

:deep(.ant-descriptions-item-content) {
  color: #595959;
}

/* 保证表头永远单行显示，不出现省略号，不换行 */
:deep(.ant-table-thead > tr > th) {
  white-space: nowrap; /* 不换行，单行显示 */
  text-overflow: clip; /* 不显示省略号 */
  overflow: visible;   /* 超出内容不隐藏 */
}

.toolbar-switch {
  margin-left: 8px;
  vertical-align: middle;
}
</style> 