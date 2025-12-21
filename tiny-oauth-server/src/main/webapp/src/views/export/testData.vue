<template>
  <div class="content-container" style="position: relative;">
    <div class="content-card">
      <div class="form-container">
        <a-form layout="inline" :model="query">
          <a-form-item label="租户编码">
            <a-input v-model:value="query.tenantCode" placeholder="请输入租户编码" allow-clear />
          </a-form-item>
          <a-form-item label="产品编码">
            <a-input v-model:value="query.productCode" placeholder="请输入产品编码" allow-clear />
          </a-form-item>
          <a-form-item label="状态">
            <a-select v-model:value="query.status" allow-clear placeholder="全部状态" style="width: 160px">
              <a-select-option value="UNBILLED">UNBILLED</a-select-option>
              <a-select-option value="BILLED">BILLED</a-select-option>
              <a-select-option value="ADJUSTED">ADJUSTED</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="throttledSearch">搜索</a-button>
            <a-button class="ml-2" @click="throttledReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div class="toolbar-container">
        <div class="table-title">
          导出测试数据（demo_export_usage）
        </div>
        <div class="table-actions">
          <a-button type="link" @click="openCreateDrawer" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-button type="primary" @click="openGenerateDrawer" class="toolbar-btn">
            生成测试数据
          </a-button>
          <a-button danger @click="handleClearDemo" class="toolbar-btn">
            清空测试数据
          </a-button>
          <a-tooltip title="刷新">
            <span class="action-icon" @click="throttledRefresh">
              <ReloadOutlined :spin="refreshing" />
            </span>
          </a-tooltip>
          <a-tooltip :title="showSortTooltip ? '关闭排序提示' : '开启排序提示'">
            <PoweroffOutlined :class="['action-icon', { active: showSortTooltip }]"
              @click="showSortTooltip = !showSortTooltip" />
          </a-tooltip>
          <a-popover placement="bottomRight" trigger="click" :destroyTooltipOnHide="false">
            <template #content>
              <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                <div style="display: flex; align-items: center;">
                  <a-checkbox :checked="showColumnKeys.length === allColumns.length"
                    :indeterminate="showColumnKeys.length > 0 && showColumnKeys.length < allColumns.length"
                    @change="onCheckAllChange" />
                  <span style="font-weight: bold; margin-left: 8px;">列展示/排序</span>
                </div>
                <span style="font-weight: bold; color: #1677ff; cursor: pointer;" @click="resetColumnOrder">
                  重置
                </span>
              </div>
              <VueDraggable v-model="draggableColumns"
                :item-key="(item: any) => item?.dataIndex || `col_${Math.random()}`" handle=".drag-handle"
                @end="onDragEnd" class="draggable-columns" ghost-class="sortable-ghost" chosen-class="sortable-chosen"
                tag="div">
                <template #item="{ element: col }">
                  <div class="draggable-column-item">
                    <HolderOutlined class="drag-handle" />
                    <a-checkbox :checked="showColumnKeys.includes(col.dataIndex)"
                      @change="(e: any) => onCheckboxChange(col.dataIndex, e.target.checked)">
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

      <div class="table-container" ref="tableContentRef">
        <div class="table-scroll-container">
          <a-table :columns="columns" :data-source="tableData" :pagination="false"
            :row-key="(record: any) => String(record.id)" bordered :loading="loading"
            :scroll="{ x: 1500, y: tableBodyHeight }" :locale="tableLocale" :show-sorter-tooltip="showSortTooltip">
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'status'">
                <a-tag :color="statusColorMap[record.status] || 'default'">
                  {{ record.status }}
                </a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'billable'">
                <a-tag :color="record.billable ? 'green' : 'red'">
                  {{ record.billable ? '是' : '否' }}
                </a-tag>
              </template>
              <template v-else-if="column.dataIndex === 'action'">
                <div class="action-buttons">
                  <a-button type="link" size="small" @click.stop="openEditDrawer(record)" class="action-btn">
                    <template #icon>
                      <EditOutlined />
                    </template>
                    编辑
                  </a-button>
                  <a-button type="link" size="small" danger @click.stop="handleDelete(record)" class="action-btn">
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
          <a-pagination v-model:current="pagination.current" :page-size="pagination.pageSize" :total="paginationConfig.total"
            :show-size-changer="pagination.showSizeChanger" :page-size-options="paginationConfig.pageSizeOptions"
            :show-total="pagination.showTotal" @change="handlePageChange" @showSizeChange="handlePageSizeChange"
            :locale="{ items_per_page: '条/页' }" />
        </div>
      </div>
    </div>

    <a-drawer v-model:open="drawerVisible" :title="drawerMode === 'create' ? '新建测试数据' : '编辑测试数据'" width="50%"
      :get-container="false" :style="{ position: 'absolute' }" @close="handleDrawerClose">
      <a-form :model="formState" layout="vertical">
        <a-form-item label="租户编码">
          <a-input v-model:value="formState.tenantCode" />
        </a-form-item>
        <a-form-item label="用量日期">
          <a-date-picker v-model:value="formState.usageDate" style="width: 100%" />
        </a-form-item>
        <a-form-item label="产品编码">
          <a-input v-model:value="formState.productCode" />
        </a-form-item>
        <a-form-item label="产品名称">
          <a-input v-model:value="formState.productName" />
        </a-form-item>
        <a-form-item label="套餐档位">
          <a-input v-model:value="formState.planTier" />
        </a-form-item>
        <a-form-item label="区域">
          <a-input v-model:value="formState.region" />
        </a-form-item>
        <a-form-item label="用量数量">
          <a-input-number v-model:value="formState.usageQty" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="单位">
          <a-input v-model:value="formState.unit" />
        </a-form-item>
        <a-form-item label="单价">
          <a-input-number v-model:value="formState.unitPrice" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="金额">
          <a-input-number v-model:value="formState.amount" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="币种">
          <a-input v-model:value="formState.currency" />
        </a-form-item>
        <a-form-item label="税率">
          <a-input-number v-model:value="formState.taxRate" :min="0" :max="1" :step="0.0001" style="width: 100%" />
        </a-form-item>
        <a-form-item label="是否计费">
          <a-switch v-model:checked="formState.billable" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="formState.status">
            <a-select-option value="UNBILLED">UNBILLED</a-select-option>
            <a-select-option value="BILLED">BILLED</a-select-option>
            <a-select-option value="ADJUSTED">ADJUSTED</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
      <template #footer>
        <div style="text-align: right;">
          <a-button style="margin-right: 8px" @click="handleDrawerClose">取消</a-button>
          <a-button type="primary" @click="handleSubmit">保存</a-button>
        </div>
      </template>
    </a-drawer>

    <a-drawer v-model:open="generateDrawerVisible" title="生成测试数据" width="50%" :get-container="false"
      :style="{ position: 'absolute' }" @close="handleGenerateDrawerClose">
      <a-alert message="提示" description="将通过存储过程批量生成 demo_export_usage 测试数据，仅建议在开发/测试环境使用。" type="warning" show-icon
        style="margin-bottom: 24px" />
      <a-form :model="generateFormState" layout="vertical">
        <a-form-item label="生成天数" required>
          <a-input-number v-model:value="generateFormState.days" :min="1" :max="365" style="width: 100%"
            placeholder="请输入生成天数（1-365）" />
          <div style="color: #999; font-size: 12px; margin-top: 4px">
            指定生成多少天的数据，默认 7 天
          </div>
        </a-form-item>
        <a-form-item label="每天生成行数" required>
          <a-input-number v-model:value="generateFormState.rowsPerDay" :min="1" :max="10000" style="width: 100%"
            placeholder="请输入每天生成的行数（1-10000）" />
          <div style="color: #999; font-size: 12px; margin-top: 4px">
            指定每天生成多少条记录，默认 2000 条
          </div>
        </a-form-item>
        <a-form-item label="目标总行数（可选）">
          <a-input-number v-model:value="generateFormState.targetRows" :min="0" style="width: 100%"
            placeholder="请输入目标总行数（0 表示不限制）" />
          <div style="color: #999; font-size: 12px; margin-top: 4px">
            如果设置了目标总行数，将优先满足总行数要求。设置为 0 表示不限制，按天数 × 每天行数生成
          </div>
        </a-form-item>
        <a-form-item label="是否清空现有数据">
          <a-switch v-model:checked="generateFormState.clearExisting" />
          <div style="color: #999; font-size: 12px; margin-top: 4px">
            开启后将先清空表中的所有数据，再生成新数据，确保数据量与预期一致。关闭则保留现有数据，在现有数据基础上追加。
          </div>
        </a-form-item>
      </a-form>
      <template #footer>
        <div style="text-align: right;">
          <a-button style="margin-right: 8px" @click="handleGenerateDrawerClose">取消</a-button>
          <a-button type="primary" @click="handleGenerateSubmit" :loading="generating">
            生成
          </a-button>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { PlusOutlined, ReloadOutlined, EditOutlined, DeleteOutlined, PoweroffOutlined, SettingOutlined, HolderOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import dayjs, { Dayjs } from 'dayjs'
import VueDraggable from 'vuedraggable'
import {
  demoExportUsageList,
  createDemoExportUsage,
  updateDemoExportUsage,
  deleteDemoExportUsage,
  generateDemoExportUsage,
  clearDemoExportUsage,
} from '@/api/demoExportUsage'
import { useThrottle } from '@/utils/debounce'

interface DemoUsageFormState {
  id?: number
  tenantCode: string
  usageDate: Dayjs | null
  productCode: string
  productName: string
  planTier: string
  region: string
  usageQty: number | null
  unit: string
  unitPrice: number | null
  amount: number | null
  currency: string
  taxRate: number | null
  billable: boolean
  status: string
}

const statusColorMap: Record<string, string> = {
  UNBILLED: 'orange',
  BILLED: 'green',
  ADJUSTED: 'blue',
}

const query = ref({
  tenantCode: '',
  productCode: '',
  status: undefined as string | undefined,
})

const tableData = ref<any[]>([])
const loading = ref(false)
const refreshing = ref(false)
const showSortTooltip = ref(true)

const pagination = ref({
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '30', '40', '50'],
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`,
})

const paginationConfig = computed(() => {
  const current = Number(pagination.value.current) || 1
  const pageSize = Number(pagination.value.pageSize) || 10
  return {
    ...pagination.value,
    current,
    pageSize,
    total: Number(pagination.value.total) || 0,
  }
})

const INITIAL_COLUMNS = [
  { title: 'ID', dataIndex: 'id', width: 80, sorter: true },
  { title: '租户编码', dataIndex: 'tenantCode', width: 140 },
  { title: '用量日期', dataIndex: 'usageDate', width: 120, sorter: true },
  { title: '产品编码', dataIndex: 'productCode', width: 120 },
  { title: '产品名称', dataIndex: 'productName', width: 160 },
  { title: '用量', dataIndex: 'usageQty', width: 100, sorter: true },
  { title: '单位', dataIndex: 'unit', width: 80 },
  { title: '金额', dataIndex: 'amount', width: 100, sorter: true },
  { title: '币种', dataIndex: 'currency', width: 80 },
  { title: '是否计费', dataIndex: 'billable', width: 100 },
  { title: '状态', dataIndex: 'status', width: 120 },
  { title: '操作', dataIndex: 'action', width: 160, fixed: 'right' },
]

const allColumns = ref([...INITIAL_COLUMNS])
const draggableColumns = ref([...INITIAL_COLUMNS])
const showColumnKeys = ref(INITIAL_COLUMNS.map((c) => c.dataIndex))

const columns = computed(() => {
  const filtered = allColumns.value.filter(
    (col) => col && col.dataIndex && showColumnKeys.value.includes(col.dataIndex),
  )
  return filtered
})

const tableLocale = computed(() => {
  if (showSortTooltip.value) {
    return {
      triggerDesc: '点击降序',
      triggerAsc: '点击升序',
      cancelSort: '取消排序',
    }
  }
  return undefined
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      tenantCode: query.value.tenantCode.trim(),
      productCode: query.value.productCode.trim(),
      status: query.value.status,
      current: pagination.value.current,
      pageSize: pagination.value.pageSize,
    }
    const res = await demoExportUsageList(params)
    tableData.value = res.records || []
    pagination.value.total = Number(res.total) || 0
  } catch (error: any) {
    message.error('加载测试数据失败: ' + (error?.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

const throttledSearch = useThrottle(handleSearch, 800)

function handleReset() {
  query.value.tenantCode = ''
  query.value.productCode = ''
  query.value.status = undefined
  pagination.value.current = 1
  loadData()
}

const throttledReset = useThrottle(handleReset, 800)

async function handleRefresh() {
  refreshing.value = true
  loading.value = true
  await loadData().catch(() => { })
  setTimeout(() => {
    refreshing.value = false
  }, 800)
  loading.value = false
}

const throttledRefresh = useThrottle(handleRefresh, 1000)

function handlePageChange(page: number) {
  pagination.value.current = page || 1
  loadData()
}

function handlePageSizeChange(_current: number, size: number) {
  pagination.value.pageSize = size || 10
  pagination.value.current = 1
  loadData()
}

const drawerVisible = ref(false)
const drawerMode = ref<'create' | 'edit'>('create')
const formState = ref<DemoUsageFormState>({
  tenantCode: '',
  usageDate: null,
  productCode: '',
  productName: '',
  planTier: 'standard',
  region: '',
  usageQty: null,
  unit: '',
  unitPrice: null,
  amount: null,
  currency: 'CNY',
  taxRate: null,
  billable: true,
  status: 'UNBILLED',
})

const generateDrawerVisible = ref(false)
const generating = ref(false)
const generateFormState = ref({
  days: 7,
  rowsPerDay: 2000,
  targetRows: 0,
  clearExisting: false,
})

function openCreateDrawer() {
  drawerMode.value = 'create'
  formState.value = {
    tenantCode: '',
    usageDate: dayjs(),
    productCode: '',
    productName: '',
    planTier: 'standard',
    region: '',
    usageQty: null,
    unit: '',
    unitPrice: null,
    amount: null,
    currency: 'CNY',
    taxRate: null,
    billable: true,
    status: 'UNBILLED',
  }
  drawerVisible.value = true
}

function openEditDrawer(record: any) {
  drawerMode.value = 'edit'
  formState.value = {
    id: record.id,
    tenantCode: record.tenantCode,
    usageDate: record.usageDate ? dayjs(record.usageDate) : null,
    productCode: record.productCode,
    productName: record.productName,
    planTier: record.planTier,
    region: record.region,
    usageQty: record.usageQty,
    unit: record.unit,
    unitPrice: record.unitPrice,
    amount: record.amount,
    currency: record.currency,
    taxRate: record.taxRate,
    billable: record.billable,
    status: record.status,
  }
  drawerVisible.value = true
}

function handleDrawerClose() {
  drawerVisible.value = false
}

async function handleSubmit() {
  const payload: any = {
    tenantCode: formState.value.tenantCode,
    usageDate: formState.value.usageDate ? formState.value.usageDate.format('YYYY-MM-DD') : null,
    productCode: formState.value.productCode,
    productName: formState.value.productName,
    planTier: formState.value.planTier,
    region: formState.value.region,
    usageQty: formState.value.usageQty,
    unit: formState.value.unit,
    unitPrice: formState.value.unitPrice,
    amount: formState.value.amount,
    currency: formState.value.currency,
    taxRate: formState.value.taxRate,
    billable: formState.value.billable,
    status: formState.value.status,
  }
  try {
    if (drawerMode.value === 'create') {
      await createDemoExportUsage(payload)
      message.success('创建测试数据成功')
    } else if (drawerMode.value === 'edit' && formState.value.id != null) {
      await updateDemoExportUsage(formState.value.id, payload)
      message.success('更新测试数据成功')
    }
    drawerVisible.value = false
    loadData()
  } catch (error: any) {
    message.error('保存测试数据失败: ' + (error?.message || '未知错误'))
  }
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除记录 ${record.id} 吗？`,
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return deleteDemoExportUsage(record.id)
        .then(() => {
          message.success('删除成功')
          loadData()
        })
        .catch((error: any) => {
          message.error('删除失败: ' + (error?.message || '未知错误'))
          return Promise.reject(error)
        })
    },
  })
}

function openGenerateDrawer() {
  generateFormState.value = {
    days: 7,
    rowsPerDay: 2000,
    targetRows: 0,
    clearExisting: false,
  }
  generateDrawerVisible.value = true
}

function handleGenerateDrawerClose() {
  generateDrawerVisible.value = false
}

async function handleGenerateSubmit() {
  if (!generateFormState.value.days || generateFormState.value.days <= 0) {
    message.warning('请输入有效的生成天数')
    return
  }
  if (!generateFormState.value.rowsPerDay || generateFormState.value.rowsPerDay <= 0) {
    message.warning('请输入有效的每天生成行数')
    return
  }

  generating.value = true
  try {
    await generateDemoExportUsage(
      {
        days: generateFormState.value.days,
        rowsPerDay: generateFormState.value.rowsPerDay,
        targetRows: generateFormState.value.targetRows || 0,
        clearExisting: generateFormState.value.clearExisting || false,
      },
      // 单次调用将超时提高到 30 秒，覆盖 axios 实例默认 5 秒
      { timeout: 30000 },
    )
    message.success('已生成测试数据')
    generateDrawerVisible.value = false
    await loadData()
  } catch (error: any) {
    message.error('生成测试数据失败: ' + (error?.message || '未知错误'))
  } finally {
    generating.value = false
  }
}

async function handleClearDemo() {
  Modal.confirm({
    title: '清空测试数据',
    content: '此操作将删除 demo_export_usage 表中的所有记录，仅建议在开发/测试环境使用，确认继续？',
    okText: '确认清空',
    okButtonProps: { danger: true },
    cancelText: '取消',
    onOk: async () => {
      try {
        await clearDemoExportUsage()
        message.success('已清空测试数据')
        loadData()
      } catch (error: any) {
        message.error('清空测试数据失败: ' + (error?.message || '未知错误'))
        return Promise.reject(error)
      }
    },
  })
}

function onCheckAllChange(e: any) {
  if (e.target.checked) {
    showColumnKeys.value = INITIAL_COLUMNS.map((col) => col.dataIndex)
  } else {
    showColumnKeys.value = []
  }
}

function onCheckboxChange(dataIndex: string, checked: boolean) {
  if (!dataIndex) return
  if (checked) {
    if (!showColumnKeys.value.includes(dataIndex)) {
      showColumnKeys.value.push(dataIndex)
    }
  } else {
    showColumnKeys.value = showColumnKeys.value.filter((key) => key !== dataIndex)
  }
}

function resetColumnOrder() {
  allColumns.value = [...INITIAL_COLUMNS]
  draggableColumns.value = [...INITIAL_COLUMNS]
  showColumnKeys.value = INITIAL_COLUMNS.map((col) => col.dataIndex)
}

function onDragEnd(event: any) {
  // 拖拽结束后，同步 draggableColumns 到 allColumns
  allColumns.value = draggableColumns.value.filter((col) => col && typeof col.dataIndex === 'string')
  // showColumnKeys 只保留 allColumns 里存在的 dataIndex
  showColumnKeys.value = showColumnKeys.value.filter((key) =>
    allColumns.value.some((col) => col.dataIndex === key),
  )
}

// 监听 allColumns 变化，同步到 draggableColumns
watch(
  allColumns,
  (val) => {
    draggableColumns.value = val.filter((col) => col && typeof col.dataIndex === 'string')
  },
  { deep: true },
)

// 监听 draggableColumns 变化，同步到 allColumns
watch(
  draggableColumns,
  (val) => {
    allColumns.value = val.filter((col) => col && typeof col.dataIndex === 'string')
    // showColumnKeys 只保留 allColumns 里存在的 dataIndex
    showColumnKeys.value = showColumnKeys.value.filter((key) =>
      allColumns.value.some((col) => col.dataIndex === key),
    )
  },
  { deep: true },
)

const tableContentRef = ref<HTMLElement | null>(null)
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

onMounted(() => {
  loadData()
  updateTableBodyHeight()
  window.addEventListener('resize', updateTableBodyHeight)
})

watch(
  () => pagination.value.pageSize,
  () => {
    updateTableBodyHeight()
  },
)
</script>

<style scoped>
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
}

.toolbar-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  padding: 8px 24px;
}

.table-container {
  display: flex;
  /* 启用flex布局 */
  flex-direction: column;
  /* 垂直排列子元素 */
  flex: 1;
  /* 占满父容器剩余空间 */
  min-height: 0;
  /* 关键，防止撑开 */
}

.table-scroll-container {
  /* 不要设置 flex: 1; */
  min-height: 0;
  /* 可选，防止撑开 */
  overflow: auto;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  background: #fff;
}

/* 禁止表格单元格换行，超出内容使用省略号，避免“跨行”撑高行高 */
:deep(.ant-table-cell) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ml-2 {
  margin-left: 8px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #222;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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
</style>
