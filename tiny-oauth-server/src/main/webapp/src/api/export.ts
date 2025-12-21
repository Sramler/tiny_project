import request from '@/utils/request'

export interface ExportTask {
  taskId: string
  userId: string
  username?: string
  status: string
  progress?: number
  totalRows?: number
  processedRows?: number
  sheetCount?: number
  filePath?: string
  downloadUrl?: string
  errorMsg?: string
  errorCode?: string
  workerId?: string
  attempt?: number
  lastHeartbeat?: string
  expireAt?: string
  createdAt?: string
  updatedAt?: string
  id?: number
}

export const exportApi = {
  listTasks: () => request.get<ExportTask[]>('/export/task'),
  getTask: (taskId: string) => request.get<ExportTask>(`/export/task/${taskId}`),
}
