import request from '@/utils/request'

// 菜单接口类型定义
export interface MenuItem {
  id?: number
  name: string
  title: string
  url?: string
  uri?: string
  method?: string
  icon?: string
  showIcon?: boolean
  sort?: number
  component?: string
  redirect?: string
  hidden?: boolean
  keepAlive?: boolean
  permission?: string
  parentId?: number | null
  children?: MenuItem[]
  createdAt?: string
  updatedAt?: string
}

// 查询参数类型
export interface MenuQuery {
  name?: string
  title?: string
  url?: string
  permission?: string
  parentId?: number
  hidden?: boolean
  enabled?: boolean
}

// 创建/更新菜单参数
export interface MenuCreateUpdateDto {
  id?: number
  name: string
  title: string
  url?: string
  uri?: string
  method?: string
  icon?: string
  showIcon?: boolean
  sort?: number
  component?: string
  redirect?: string
  hidden?: boolean
  keepAlive?: boolean
  permission?: string
  parentId?: number | null
}

// 菜单排序参数
export interface MenuSortDto {
  id: number
  sort: number
  parentId?: number | null
}

// 分页响应类型
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
}

// 获取菜单列表（返回list结构）
export function menuList(params: {
  name?: string
  title?: string
  permission?: string
  parentId?: number
  enabled?: boolean
}) {
  return request.get('/sys/menus', { params })
}

// 获取菜单树
export function menuTree() {
  return request.get('/sys/menus/tree')
}

// 获取用户菜单树
export function getUserMenuTree(userId: number): Promise<MenuItem[]> {
  return request.get(`/sys/menus/user/${userId}/tree`)
}

// 创建菜单
export function createMenu(data: {
  name: string
  title: string
  url: string
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
  type: number // 0-目录，1-菜单
  parentId?: number
}) {
  return request.post('/sys/menus', data)
}

// 更新菜单
export function updateMenu(
  id: string | number,
  data: {
    id?: number
    name: string
    title: string
    url: string
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
    type: number // 0-目录，1-菜单
    parentId?: number
  },
) {
  return request.put(`/sys/menus/${id}`, data)
}

// 删除菜单
export function deleteMenu(id: string | number) {
  return request.delete(`/sys/menus/${id}`)
}

// 获取菜单详情
export function getMenuDetail(id: number | string): Promise<MenuItem> {
  return request.get(`/sys/menus/${id}`)
}

// 批量删除菜单
export function batchDeleteMenus(ids: (string | number)[]) {
  return request.post('/sys/resources/menus/batch/delete', ids)
}

// 更新菜单排序
export function updateMenuSort(id: string | number, sort: number) {
  return request.put(`/sys/resources/menus/${id}/sort`, null, { params: { sort } })
}

// 批量更新菜单排序
export function batchUpdateMenuSort(
  data: MenuSortDto[],
): Promise<{ success: boolean; message: string }> {
  return request.put('/sys/menus/batch/sort', data)
}

// 根据父级ID获取子菜单
export function getMenusByParentId(parentId: number): Promise<MenuItem[]> {
  return request.get(`/sys/menus/parent/${parentId}`)
}

// 获取顶级菜单
export function getTopLevelMenus(): Promise<MenuItem[]> {
  return request.get('/sys/menus/top-level')
}

// 根据权限标识获取菜单
export function getMenusByPermission(permission: string): Promise<MenuItem[]> {
  return request.get(`/sys/menus/permission/${permission}`)
}

// 根据是否隐藏获取菜单
export function getMenusByHidden(hidden: boolean): Promise<MenuItem[]> {
  return request.get(`/sys/menus/hidden/${hidden}`)
}

// 检查菜单名称是否存在
export function checkMenuNameExists(name: string, excludeId?: string | number) {
  return request.get('/sys/resources/check-name', { params: { name, excludeId } })
}

// 检查菜单路径是否存在
export function checkMenuUrlExists(url: string, excludeId?: string | number) {
  return request.get('/sys/resources/check-url', { params: { url, excludeId } })
}

// 获取菜单选项列表（用于下拉选择）
export function getMenuOptions(): Promise<any[]> {
  return request.get('/sys/menus/options')
}

// 获取菜单类型选项
export function getMenuTypeOptions() {
  return [
    { label: '目录', value: 0 },
    { label: '菜单', value: 1 },
  ]
}
