<template>
  <div class="content-container" style="position: relative;">
    <div class="content-card">
      <div class="form-container">
        <a-form layout="inline" :model="query">
          <a-form-item label="编码">
            <a-input v-model:value="query.code" placeholder="请输入编码" />
          </a-form-item>
          <a-form-item label="名称">
            <a-input v-model:value="query.name" placeholder="请输入名称" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button class="ml-2" @click="handleReset">重置</a-button>
          </a-form-item>
        </a-form>
      </div>

      <div class="toolbar-container">
        <div class="table-title">任务类型列表</div>
        <div class="table-actions">
          <a-button type="link" @click="handleCreate" class="toolbar-btn">
            <template #icon>
              <PlusOutlined />
            </template>
            新建
          </a-button>
          <a-tooltip title="刷新">
            <span class="action-icon" @click="handleRefresh">
              <ReloadOutlined :spin="refreshing" />
            </span>
          </a-tooltip>
        </div>
      </div>

      <div class="demo-card">
        <a-alert
          type="info"
          show-icon
          message="示例执行器快速体验"
          description="一键创建 logging/delay 示例任务类型，配合文档即可快速演示任务执行效果。"
        />
        <a-space class="mt-2">
          <a-button
            :loading="creatingDemoType === 'logging'"
            @click="handleCreateDemo('logging')"
          >
            创建 Logging 示例
          </a-button>
          <a-button
            :loading="creatingDemoType === 'delay'"
            @click="handleCreateDemo('delay')"
          >
            创建 Delay 示例
          </a-button>
          <a-button type="link" href="/docs/SCHEDULING_TASK_EXECUTOR_GUIDE.md" target="_blank">
            查看执行器指南
          </a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-selection="{ selectedRowKeys, onChange: onSelectChange }"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'red'">
              {{ record.enabled ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm title="确定要删除吗？" @confirm="handleDelete(record.id)">
                <a-button type="link" danger size="small">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 任务类型表单弹窗 -->
    <a-modal
      v-model:open="formVisible"
      :title="formTitle"
      :width="700"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form :model="formData" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="编码" required>
          <a-input v-model:value="formData.code" placeholder="请输入编码（唯一）" />
        </a-form-item>
        <a-form-item label="名称" required>
          <a-input v-model:value="formData.name" placeholder="请输入名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入描述" />
        </a-form-item>
        <a-form-item label="执行器">
          <a-input v-model:value="formData.executor" placeholder="请输入执行器标识" />
        </a-form-item>
        <a-form-item label="参数Schema">
          <a-textarea v-model:value="formData.paramSchema" :rows="4" placeholder="请输入JSON格式的参数Schema" />
        </a-form-item>
        <a-form-item label="默认超时(秒)">
          <a-input-number v-model:value="formData.defaultTimeoutSec" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="默认最大重试">
          <a-input-number v-model:value="formData.defaultMaxRetry" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="是否启用">
          <a-switch v-model:checked="formData.enabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { taskTypeList, createTaskType, updateTaskType, deleteTaskType } from '@/api/scheduling'
import { throttle } from '@/utils/debounce'

const loading = ref(false)
const refreshing = ref(false)
const formVisible = ref(false)
const formTitle = ref('新建任务类型')
const selectedRowKeys = ref<number[]>([])
const dataSource = ref<any[]>([])
const creatingDemoType = ref<string | null>(null)

const query = reactive({
  code: '',
  name: '',
})

const formData = reactive({
  id: undefined as number | undefined,
  tenantId: undefined as number | undefined,
  code: '',
  name: '',
  description: '',
  executor: '',
  paramSchema: '',
  defaultTimeoutSec: 0,
  defaultMaxRetry: 0,
  enabled: true,
  createdBy: '',
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '编码', dataIndex: 'code', key: 'code', width: 150 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '描述', dataIndex: 'description', key: 'description' },
  { title: '执行器', dataIndex: 'executor', key: 'executor', width: 150 },
  { title: '默认超时(秒)', dataIndex: 'defaultTimeoutSec', key: 'defaultTimeoutSec', width: 120 },
  { title: '默认最大重试', dataIndex: 'defaultMaxRetry', key: 'defaultMaxRetry', width: 120 },
  { title: '状态', key: 'enabled', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' },
]

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
      ...query,
    }
    const res = await taskTypeList(params)
    dataSource.value = res.records
    pagination.total = res.total
  } catch (error: any) {
    message.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = throttle(() => {
  pagination.current = 1
  loadData()
}, 500)

const handleReset = throttle(() => {
  query.code = ''
  query.name = ''
  pagination.current = 1
  loadData()
}, 500)

const handleRefresh = throttle(() => {
  refreshing.value = true
  loadData().finally(() => {
    refreshing.value = false
  })
}, 500)

const demoTaskTypes = {
  logging: {
    tenantId: 1,
    code: 'demo_logging',
    name: '示例-Logging 执行器',
    description: '使用 loggingTaskExecutor 打印参数并返回结果',
    executor: 'loggingTaskExecutor',
    paramSchema: JSON.stringify(
      {
        type: 'object',
        properties: {
          message: {
            type: 'string',
            description: '需要打印的消息',
          },
        },
      },
      null,
      2,
    ),
    defaultTimeoutSec: 60,
    defaultMaxRetry: 0,
    enabled: true,
    createdBy: 'demo',
  },
  delay: {
    tenantId: 1,
    code: 'demo_delay',
    name: '示例-Delay 执行器',
    description: '使用 delayTaskExecutor 支持延迟/失败模拟',
    executor: 'delayTaskExecutor',
    paramSchema: JSON.stringify(
      {
        type: 'object',
        properties: {
          delayMs: {
            type: 'integer',
            description: '延迟毫秒数',
            default: 1000,
          },
          fail: {
            type: 'boolean',
            description: '是否模拟失败',
            default: false,
          },
        },
        required: ['delayMs'],
      },
      null,
      2,
    ),
    defaultTimeoutSec: 120,
    defaultMaxRetry: 2,
    enabled: true,
    createdBy: 'demo',
  },
}

const handleCreateDemo = async (typeKey: keyof typeof demoTaskTypes) => {
  if (creatingDemoType.value) {
    return
  }
  creatingDemoType.value = typeKey
  try {
    await createTaskType(demoTaskTypes[typeKey])
    message.success(`已创建 ${demoTaskTypes[typeKey].name}`)
    loadData()
  } catch (error: any) {
    message.error(error.message || '创建示例失败')
  } finally {
    creatingDemoType.value = null
  }
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const onSelectChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const handleCreate = () => {
  formTitle.value = '新建任务类型'
  Object.assign(formData, {
    id: undefined,
    tenantId: undefined,
    code: '',
    name: '',
    description: '',
    executor: '',
    paramSchema: '',
    defaultTimeoutSec: 0,
    defaultMaxRetry: 0,
    enabled: true,
    createdBy: '',
  })
  formVisible.value = true
}

const handleEdit = (record: any) => {
  formTitle.value = '编辑任务类型'
  Object.assign(formData, {
    id: record.id,
    tenantId: record.tenantId,
    code: record.code,
    name: record.name,
    description: record.description || '',
    executor: record.executor || '',
    paramSchema: record.paramSchema || '',
    defaultTimeoutSec: record.defaultTimeoutSec || 0,
    defaultMaxRetry: record.defaultMaxRetry || 0,
    enabled: record.enabled !== undefined ? record.enabled : true,
    createdBy: record.createdBy || '',
  })
  formVisible.value = true
}

const handleSubmit = async () => {
  if (!formData.code) {
    message.error('请输入编码')
    return
  }
  if (!formData.name) {
    message.error('请输入名称')
    return
  }
  try {
    if (formData.id) {
      await updateTaskType(formData.id, formData)
      message.success('更新成功')
    } else {
      await createTaskType(formData)
      message.success('创建成功')
    }
    formVisible.value = false
    loadData()
  } catch (error: any) {
    message.error(error.message || '操作失败')
  }
}

const handleCancel = () => {
  formVisible.value = false
}

const handleDelete = async (id: number) => {
  try {
    await deleteTaskType(id)
    message.success('删除成功')
    loadData()
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.content-container {
  padding: 16px;
}

.content-card {
  background: #fff;
  border-radius: 4px;
  padding: 16px;
}

.form-container {
  margin-bottom: 16px;
}

.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.demo-card {
  margin-bottom: 16px;
  background: #f9fbff;
  border: 1px solid #e6f0ff;
  border-radius: 4px;
  padding: 12px 16px;
}

.table-title {
  font-size: 16px;
  font-weight: 500;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-btn {
  padding: 0;
}

.action-icon {
  cursor: pointer;
  font-size: 16px;
  color: #666;
  transition: color 0.3s;
}

.action-icon:hover {
  color: #1677ff;
}

.ml-2 {
  margin-left: 8px;
}

.mt-2 {
  margin-top: 12px;
}
</style>


