import request from '@/utils/request'

// 菜单接口类型定义
export interface MenuItem {
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
  children?: MenuItem[]
}

// 查询参数类型
export interface MenuQuery {
  title?: string
  permission?: string
}

// 获取菜单列表
export function menuList(params?: MenuQuery): Promise<MenuItem[]> {
  return request.get('/api/resources', { params })
}

// 获取菜单树
export function getMenuTree(): Promise<MenuItem[]> {
  return request.get('/api/resources/tree')
}

// 创建菜单
export function createMenu(data: MenuItem): Promise<any> {
  return request.post('/api/resources', data)
}

// 更新菜单
export function updateMenu(id: number | string, data: MenuItem): Promise<any> {
  return request.put(`/api/resources/${id}`, data)
}

// 删除菜单
export function deleteMenu(id: number | string): Promise<any> {
  return request.delete(`/api/resources/${id}`)
}

// 获取菜单详情
export function getMenuDetail(id: number | string): Promise<MenuItem> {
  return request.get(`/api/resources/${id}`)
}

// 批量删除菜单
export function batchDeleteMenus(ids: (number | string)[]): Promise<any> {
  return request.post('/api/resources/batch/delete', ids)
}

// 更新菜单排序
export function updateMenuSort(data: { id: number; sort: number }[]): Promise<any> {
  return request.put('/api/resources/sort', data)
}
