import request from '@/utils/request'

// 资源接口类型定义
export interface ResourceItem {
  id?: number
  name: string
  title: string
  path: string
  uri: string
  method: string
  icon: string
  showIcon: boolean
  sort: number
  component: string
  redirect: string
  hidden: boolean
  keepAlive: boolean
  permission: string
  type: number
  parentId?: number | null
}

// 查询参数类型
export interface ResourceQuery {
  name?: string
  uri?: string
  permission?: string
  page?: number
  size?: number
}

// 获取资源列表
export function resourceList(params?: ResourceQuery): Promise<any> {
  return request.get('/api/resources', { params })
}

// 创建资源
export function createResource(data: ResourceItem): Promise<any> {
  return request.post('/api/resources', data)
}

// 更新资源
export function updateResource(id: number | string, data: ResourceItem): Promise<any> {
  return request.put(`/api/resources/${id}`, data)
}

// 删除资源
export function deleteResource(id: number | string): Promise<any> {
  return request.delete(`/api/resources/${id}`)
}

// 获取资源详情
export function getResourceDetail(id: number | string): Promise<ResourceItem> {
  return request.get(`/api/resources/${id}`)
}

// 批量删除资源
export function batchDeleteResources(ids: (number | string)[]): Promise<any> {
  return request.post('/api/resources/batch/delete', ids)
}

// 更新资源排序
export function updateResourceSort(data: { id: number; sort: number }[]): Promise<any> {
  return request.put('/api/resources/sort', data)
}
