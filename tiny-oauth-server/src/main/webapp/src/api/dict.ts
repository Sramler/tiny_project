import request from '@/utils/request';

/**
 * 字典类型查询参数
 */
export interface DictTypeQuery {
  dictCode?: string;
  dictName?: string;
  categoryId?: number;
}

/**
 * 字典类型 DTO
 */
export interface DictTypeDTO {
  id?: number;
  dictCode: string;
  dictName: string;
  description?: string;
  tenantId?: number;
  categoryId?: number;
  enabled?: boolean;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 字典类型创建 DTO
 */
export interface DictTypeCreateDTO {
  dictCode: string;
  dictName: string;
  description?: string;
  categoryId?: number;
}

/**
 * 字典类型更新 DTO
 */
export interface DictTypeUpdateDTO {
  dictName: string;
  description?: string;
  categoryId?: number;
  enabled?: boolean;
}

/**
 * 字典项查询参数
 */
export interface DictItemQuery {
  dictCode: string;
  value?: string;
  enabled?: boolean;
}

/**
 * 字典项 DTO
 */
export interface DictItemDTO {
  id?: number;
  dictTypeId?: number;
  dictCode: string;
  value: string;
  label: string;
  description?: string;
  tenantId?: number;
  enabled?: boolean;
  sortOrder?: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 字典项创建 DTO
 */
export interface DictItemCreateDTO {
  dictCode: string;
  value: string;
  label: string;
  description?: string;
  sortOrder?: number;
  enabled?: boolean;
}

/**
 * 字典项更新 DTO
 */
export interface DictItemUpdateDTO {
  label: string;
  description?: string;
  sortOrder?: number;
  enabled?: boolean;
}

/**
 * 批量获取标签请求
 */
export interface BatchLabelRequest {
  dictCode: string;
  values: string[];
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * 获取字典标签
 */
export function getDictLabel(dictCode: string, value: string, tenantId: number): Promise<string> {
  return request.get('/api/dict/label', {
    params: { dictCode, value },
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 获取字典所有项
 */
export function getDict(dictCode: string, tenantId: number): Promise<Record<string, string>> {
  return request.get(`/api/dict/${dictCode}`, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 批量获取字典标签
 */
export function getDictLabelsBatch(
  request: BatchLabelRequest,
  tenantId: number
): Promise<Record<string, string>> {
  return request.post('/api/dict/labels/batch', request, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 分页查询字典类型
 */
export function dictTypeList(
  query: DictTypeQuery,
  page: number = 0,
  size: number = 10,
  tenantId: number
): Promise<PageResponse<DictTypeDTO>> {
  return request.get('/api/dict/types', {
    params: { ...query, page, size },
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 创建字典类型
 */
export function createDictType(
  dto: DictTypeCreateDTO,
  tenantId: number
): Promise<DictTypeDTO> {
  return request.post('/api/dict/types', dto, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 更新字典类型
 */
export function updateDictType(
  id: number,
  dto: DictTypeUpdateDTO,
  tenantId: number
): Promise<DictTypeDTO> {
  return request.put(`/api/dict/types/${id}`, dto, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 删除字典类型
 */
export function deleteDictType(id: number, tenantId: number): Promise<void> {
  return request.delete(`/api/dict/types/${id}`, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 分页查询字典项
 */
export function dictItemList(
  query: DictItemQuery,
  page: number = 0,
  size: number = 20,
  tenantId: number
): Promise<PageResponse<DictItemDTO>> {
  return request.get('/api/dict/items', {
    params: { ...query, page, size },
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 创建字典项
 */
export function createDictItem(
  dto: DictItemCreateDTO,
  tenantId: number
): Promise<DictItemDTO> {
  return request.post('/api/dict/items', dto, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 批量创建字典项
 */
export function createDictItemsBatch(
  dtos: DictItemCreateDTO[],
  tenantId: number
): Promise<DictItemDTO[]> {
  return request.post('/api/dict/items/batch', dtos, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 更新字典项
 */
export function updateDictItem(
  id: number,
  dto: DictItemUpdateDTO,
  tenantId: number
): Promise<DictItemDTO> {
  return request.put(`/api/dict/items/${id}`, dto, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 删除字典项
 */
export function deleteDictItem(id: number, tenantId: number): Promise<void> {
  return request.delete(`/api/dict/items/${id}`, {
    headers: { 'X-Tenant-ID': tenantId },
  });
}

/**
 * 刷新字典缓存
 */
export function refreshDictCache(
  dictCode?: string,
  tenantId?: number
): Promise<void> {
  return request.post(
    '/api/dict/cache/refresh',
    {},
    {
      params: dictCode ? { dictCode } : {},
      headers: tenantId ? { 'X-Tenant-ID': tenantId } : {},
    }
  );
}

