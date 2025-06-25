import request from '@/utils/request'

export function roleList(params: any) {
  return request.get('/sys/roles', { params })
}

export function getRoleById(id: string) {
  return request.get(`/sys/roles/${id}`)
}

export function createRole(data: any) {
  return request.post('/sys/roles', data)
}

export function updateRole(id: string, data: any) {
  return request.put(`/sys/roles/${id}`, data)
}

export function deleteRole(id: string) {
  return request.delete(`/sys/roles/${id}`)
}
