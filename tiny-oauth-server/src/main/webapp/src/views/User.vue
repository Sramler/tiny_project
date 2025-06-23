<template>
  <div class="page-container">
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
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button class="ml-2" @click="handleReset">重置</a-button>
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
              @click="handleBatchDelete" 
              class="toolbar-btn"
            >
              <template #icon>
                <DeleteOutlined />
              </template>
              批量删除 ({{ selectedRowKeys.length }})
            </a-button>
            <a-button 
              type="primary" 
              @click="handleBatchEnable" 
              class="toolbar-btn"
            >
              <template #icon>
                <CheckCircleOutlined />
              </template>
              批量启用
            </a-button>
            <a-button 
              @click="handleBatchDisable" 
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
          
          <a-button type="primary" @click="handleCreate" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-tooltip title="刷新">
            <ReloadOutlined class="action-icon" @click="handleRefresh" />
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
                :item-key="(item: any) => item.dataIndex"
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
          row-key="id"
          bordered
          :loading="loading"
          @change="handleTableChange"
          :row-selection="rowSelection"
          :custom-row="onCustomRow"
          :row-class-name="getRowClassName"
          :scroll="{ y: tableBodyHeight }"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="['enabled','account_non_expired','account_non_locked','credentials_non_expired'].includes(column.dataIndex)">
              <a-tag :color="record[column.dataIndex] ? 'green' : 'red'">
                {{ record[column.dataIndex] ? '是' : '否' }}
              </a-tag>
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
          :page-size-options="pagination.pageSizeOptions"
          :show-total="pagination.showTotal"
          @change="handlePageChange"
          @showSizeChange="handlePageSizeChange"
          :locale="{ items_per_page: '条/页' }"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, h, onBeforeUnmount, nextTick } from 'vue'
import { userList } from '@/api/user'
import { PlusOutlined, ReloadOutlined, SettingOutlined, HolderOutlined, DeleteOutlined, CheckCircleOutlined, StopOutlined, CloseOutlined } from '@ant-design/icons-vue'
import VueDraggable from 'vuedraggable'

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
  pageSizeOptions: ['10', '20', '30', '40', '50'],
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`
})

const paginationConfig = computed(() => ({
  ...pagination.value
}))

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
    ellipsis: true,
    sorter: true
  }
])

const draggableColumns = ref([...allColumns.value])

const showColumnKeys = ref(allColumns.value.map(col => col.dataIndex))

watch(draggableColumns, (val) => {
  allColumns.value = [...val]
})

function onCheckboxChange(dataIndex: string, checked: boolean) {
  console.log('复选框变化:', dataIndex, checked)
  if (checked) {
    if (!showColumnKeys.value.includes(dataIndex)) {
      showColumnKeys.value.push(dataIndex)
    }
  } else {
    showColumnKeys.value = showColumnKeys.value.filter(key => key !== dataIndex)
  }
  console.log('当前显示的列:', showColumnKeys.value)
}

const columns = computed(() => [
  {
    title: '序号',
    dataIndex: 'index',
    width: 80,
    align: 'center',
    customRender: ({ index, record }: { index: number, record: any }) => {
      return h('div', {
        class: 'cell-content',
        onClick: () => handleCellClick(record, 'index')
      }, (pagination.value.current - 1) * pagination.value.pageSize + index + 1)
    }
  },
  ...allColumns.value.filter(col => showColumnKeys.value.includes(col.dataIndex))
])

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (selectedKeys: string[], selectedRows: any[]) => {
    selectedRowKeys.value = selectedKeys;
    console.log('复选框选中变化后的行:', selectedRowKeys.value)
  },
  checkStrictly: false,
  preserveSelectedRowKeys: true
}))

async function loadData() {
  loading.value = true
  try {
    const params = {
      username: query.value.username.trim(),
      nickname: query.value.nickname.trim(),
      current: pagination.value.current,
      pageSize: pagination.value.pageSize
    }
    const res = await userList(params)
    tableData.value = res.records
    pagination.value.total = res.total
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  query.value.username = ''
  query.value.nickname = ''
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any, filters: any, sorter: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

function handleCreate() {
  alert('新建功能待实现')
}

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

function onCheckAllChange(e: any) {
  console.log('全选复选框变化:', e.target.checked)
  if (e.target.checked) {
    showColumnKeys.value = allColumns.value.map(col => col.dataIndex)
  } else {
    showColumnKeys.value = []
  }
  console.log('当前显示的列:', showColumnKeys.value)
}

function resetColumnOrder() {
  draggableColumns.value = [...allColumns.value]
  showColumnKeys.value = allColumns.value.map(col => col.dataIndex)
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
    alert('请先选择要删除的用户')
    return
  }
  
  if (confirm(`确定要删除选中的 ${selectedRowKeys.value.length} 个用户吗？`)) {
    console.log('批量删除用户:', selectedRowKeys.value)
    alert('批量删除功能待实现')
  }
}

function handleBatchEnable() {
  if (selectedRowKeys.value.length === 0) {
    alert('请先选择要启用的用户')
    return
  }
  
  console.log('批量启用用户:', selectedRowKeys.value)
  alert('批量启用功能待实现')
}

function handleBatchDisable() {
  if (selectedRowKeys.value.length === 0) {
    alert('请先选择要禁用的用户')
    return
  }
  
  console.log('批量禁用用户:', selectedRowKeys.value)
  alert('批量禁用功能待实现')
}

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
  pagination.value.current = page;
  loadData();
}

function handlePageSizeChange(current: number, size: number) {
  pagination.value.pageSize = size;
  pagination.value.current = 1;
  loadData();
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 90px);
  background: #f0f2f5;
  padding: 16px;
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
</style> 