<template>
  <div class="content-container" style="position: relative;">
    <a-page-header title="DAG 运行历史" @back="handleBack" />
    <div class="content-card">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'triggerType'">
            <a-tag>{{ record.triggerType }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看详情</a-button>
              <a-button type="link" size="small" @click="handleViewNodes(record)">节点记录</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 运行详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="运行详情"
      :width="900"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="运行ID">{{ currentRecord.id }}</a-descriptions-item>
        <a-descriptions-item label="运行编号">{{ currentRecord.runNo }}</a-descriptions-item>
        <a-descriptions-item label="DAG ID">{{ currentRecord.dagId }}</a-descriptions-item>
        <a-descriptions-item label="版本ID">{{ currentRecord.dagVersionId }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRecord.status)">
            {{ currentRecord.status }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="触发类型">{{ currentRecord.triggerType }}</a-descriptions-item>
        <a-descriptions-item label="触发人">{{ currentRecord.triggeredBy || '-' }}</a-descriptions-item>
        <a-descriptions-item label="开始时间">{{ currentRecord.startTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="结束时间">{{ currentRecord.endTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="指标" :span="2">
          <pre style="max-height: 200px; overflow: auto;">{{ formatJson(currentRecord.metrics) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 节点执行记录弹窗 -->
    <a-modal
      v-model:open="nodesVisible"
      title="节点执行记录"
      :width="1200"
      :footer="null"
    >
      <a-table
        :columns="nodeColumns"
        :data-source="nodeRecords"
        :loading="nodesLoading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewNodeDetail(record)">查看详情</a-button>
              <a-button type="link" size="small" @click="handleViewLog(record)">查看日志</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>

    <!-- 节点详情弹窗 -->
    <a-modal
      v-model:open="nodeDetailVisible"
      title="节点执行详情"
      :width="900"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentNodeRecord">
        <a-descriptions-item label="实例ID">{{ currentNodeRecord.id }}</a-descriptions-item>
        <a-descriptions-item label="节点编码">{{ currentNodeRecord.nodeCode }}</a-descriptions-item>
        <a-descriptions-item label="任务ID">{{ currentNodeRecord.taskId }}</a-descriptions-item>
        <a-descriptions-item label="尝试次数">{{ currentNodeRecord.attemptNo }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentNodeRecord.status)">
            {{ currentNodeRecord.status }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="调度时间">{{ currentNodeRecord.scheduledAt || '-' }}</a-descriptions-item>
        <a-descriptions-item label="锁定者">{{ currentNodeRecord.lockedBy || '-' }}</a-descriptions-item>
        <a-descriptions-item label="锁定时间">{{ currentNodeRecord.lockTime || '-' }}</a-descriptions-item>
        <a-descriptions-item label="参数" :span="2">
          <pre style="max-height: 200px; overflow: auto;">{{ formatJson(currentNodeRecord.params) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="结果" :span="2">
          <pre style="max-height: 200px; overflow: auto;">{{ formatJson(currentNodeRecord.result) }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 日志弹窗 -->
    <a-modal
      v-model:open="logVisible"
      title="执行日志"
      :width="900"
      :footer="null"
    >
      <pre style="max-height: 500px; overflow: auto; white-space: pre-wrap;">{{ logContent }}</pre>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter, useRoute } from 'vue-router'
import {
  getDagRuns,
  getDagRun,
  getDagRunNodes,
  getDagRunNode,
  getTaskInstanceLog,
} from '@/api/scheduling'

const router = useRouter()
const route = useRoute()

const dagId = computed(() => Number(route.query.dagId))
const loading = ref(false)
const dataSource = ref<any[]>([])
const detailVisible = ref(false)
const currentRecord = ref<any>(null)
const nodesVisible = ref(false)
const nodeRecords = ref<any[]>([])
const nodesLoading = ref(false)
const nodeDetailVisible = ref(false)
const currentNodeRecord = ref<any>(null)
const logVisible = ref(false)
const logContent = ref('')

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
})

const columns = [
  { title: '运行ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '运行编号', dataIndex: 'runNo', key: 'runNo', width: 200 },
  { title: '版本ID', dataIndex: 'dagVersionId', key: 'dagVersionId', width: 100 },
  { title: '状态', key: 'status', width: 120 },
  { title: '触发类型', key: 'triggerType', width: 120 },
  { title: '触发人', dataIndex: 'triggeredBy', key: 'triggeredBy', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

const nodeColumns = [
  { title: '实例ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '节点编码', dataIndex: 'nodeCode', key: 'nodeCode', width: 150 },
  { title: '任务ID', dataIndex: 'taskId', key: 'taskId', width: 100 },
  { title: '尝试次数', dataIndex: 'attemptNo', key: 'attemptNo', width: 100 },
  { title: '状态', key: 'status', width: 120 },
  { title: '调度时间', dataIndex: 'scheduledAt', key: 'scheduledAt', width: 180 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' },
]

const getStatusColor = (status: string) => {
  const map: Record<string, string> = {
    SCHEDULED: 'blue',
    RUNNING: 'processing',
    SUCCESS: 'success',
    FAILED: 'error',
    CANCELLED: 'default',
    PENDING: 'default',
    RESERVED: 'warning',
    SKIPPED: 'default',
  }
  return map[status] || 'default'
}

const formatJson = (str: string | null | undefined) => {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      pageSize: pagination.pageSize,
    }
    const res = await getDagRuns(dagId.value, params)
    dataSource.value = res.records
    pagination.total = res.total
  } catch (error: any) {
    message.error(error.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadData()
}

const handleBack = () => {
  router.back()
}

const handleView = async (record: any) => {
  try {
    currentRecord.value = await getDagRun(dagId.value, record.id)
    detailVisible.value = true
  } catch (error: any) {
    message.error(error.message || '获取详情失败')
  }
}

const handleViewNodes = async (record: any) => {
  nodesLoading.value = true
  try {
    nodeRecords.value = await getDagRunNodes(dagId.value, record.id)
    nodesVisible.value = true
  } catch (error: any) {
    message.error(error.message || '获取节点记录失败')
  } finally {
    nodesLoading.value = false
  }
}

const handleViewNodeDetail = async (record: any) => {
  try {
    currentNodeRecord.value = await getDagRunNode(dagId.value, record.dagRunId || route.query.runId, record.id)
    nodeDetailVisible.value = true
  } catch (error: any) {
    message.error(error.message || '获取节点详情失败')
  }
}

const handleViewLog = async (record: any) => {
  try {
    logContent.value = await getTaskInstanceLog(record.id)
    logVisible.value = true
  } catch (error: any) {
    message.error(error.message || '获取日志失败')
  }
}

onMounted(() => {
  if (dagId.value) {
    loadData()
  } else {
    message.error('缺少DAG ID参数')
    router.back()
  }
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
  margin-top: 16px;
}
</style>


