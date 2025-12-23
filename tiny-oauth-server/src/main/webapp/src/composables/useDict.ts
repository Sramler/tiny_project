import { ref, computed } from 'vue';
import {
  getDictLabel,
  getDict,
  getDictLabelsBatch,
  type DictTypeDTO,
  type DictItemDTO,
  type DictTypeQuery,
  type DictItemQuery,
  type DictTypeCreateDTO,
  type DictTypeUpdateDTO,
  type DictItemCreateDTO,
  type DictItemUpdateDTO,
  type PageResponse,
} from '@/api/dict';
import {
  dictTypeList,
  createDictType,
  updateDictType,
  deleteDictType,
  dictItemList,
  createDictItem,
  createDictItemsBatch,
  updateDictItem,
  deleteDictItem,
  refreshDictCache,
} from '@/api/dict';

/**
 * 字典 Composable
 * 
 * 提供字典相关的常用功能：
 * - 字典标签翻译
 * - 字典类型管理
 * - 字典项管理
 */
export function useDict(tenantId: number) {
  // 字典类型列表
  const typeList = ref<DictTypeDTO[]>([]);
  const typeLoading = ref(false);
  const typePagination = ref({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  // 字典项列表
  const itemList = ref<DictItemDTO[]>([]);
  const itemLoading = ref(false);
  const itemPagination = ref({
    current: 1,
    pageSize: 20,
    total: 0,
  });

  // 当前选中的字典类型
  const selectedDictType = ref<DictTypeDTO | null>(null);

  /**
   * 获取字典标签
   */
  const translateLabel = async (dictCode: string, value: string): Promise<string> => {
    try {
      return await getDictLabel(dictCode, value, tenantId);
    } catch (error) {
      console.error('获取字典标签失败:', error);
      return value; // 失败时返回原值
    }
  };

  /**
   * 获取字典所有项
   */
  const getDictItems = async (dictCode: string): Promise<Record<string, string>> => {
    try {
      return await getDict(dictCode, tenantId);
    } catch (error) {
      console.error('获取字典项失败:', error);
      return {};
    }
  };

  /**
   * 批量获取字典标签
   */
  const translateLabels = async (
    dictCode: string,
    values: string[]
  ): Promise<Record<string, string>> => {
    try {
      return await getDictLabelsBatch({ dictCode, values }, tenantId);
    } catch (error) {
      console.error('批量获取字典标签失败:', error);
      return {};
    }
  };

  /**
   * 加载字典类型列表
   */
  const loadDictTypes = async (query?: DictTypeQuery) => {
    typeLoading.value = true;
    try {
      const response: PageResponse<DictTypeDTO> = await dictTypeList(
        query || {},
        typePagination.value.current - 1,
        typePagination.value.pageSize,
        tenantId
      );
      typeList.value = response.content;
      typePagination.value.total = response.totalElements;
    } catch (error) {
      console.error('加载字典类型列表失败:', error);
    } finally {
      typeLoading.value = false;
    }
  };

  /**
   * 创建字典类型
   */
  const createType = async (dto: DictTypeCreateDTO): Promise<DictTypeDTO> => {
    const result = await createDictType(dto, tenantId);
    await loadDictTypes();
    return result;
  };

  /**
   * 更新字典类型
   */
  const updateType = async (id: number, dto: DictTypeUpdateDTO): Promise<DictTypeDTO> => {
    const result = await updateDictType(id, dto, tenantId);
    await loadDictTypes();
    return result;
  };

  /**
   * 删除字典类型
   */
  const removeType = async (id: number): Promise<void> => {
    await deleteDictType(id, tenantId);
    await loadDictTypes();
  };

  /**
   * 加载字典项列表
   */
  const loadDictItems = async (dictCode: string, query?: DictItemQuery) => {
    if (!dictCode) {
      itemList.value = [];
      return;
    }

    itemLoading.value = true;
    try {
      const response: PageResponse<DictItemDTO> = await dictItemList(
        { dictCode, ...query },
        itemPagination.value.current - 1,
        itemPagination.value.pageSize,
        tenantId
      );
      itemList.value = response.content;
      itemPagination.value.total = response.totalElements;
    } catch (error) {
      console.error('加载字典项列表失败:', error);
    } finally {
      itemLoading.value = false;
    }
  };

  /**
   * 创建字典项
   */
  const createItem = async (dto: DictItemCreateDTO): Promise<DictItemDTO> => {
    const result = await createDictItem(dto, tenantId);
    if (selectedDictType.value) {
      await loadDictItems(selectedDictType.value.dictCode);
    }
    return result;
  };

  /**
   * 批量创建字典项
   */
  const createItemsBatch = async (dtos: DictItemCreateDTO[]): Promise<DictItemDTO[]> => {
    const result = await createDictItemsBatch(dtos, tenantId);
    if (selectedDictType.value) {
      await loadDictItems(selectedDictType.value.dictCode);
    }
    return result;
  };

  /**
   * 更新字典项
   */
  const updateItem = async (id: number, dto: DictItemUpdateDTO): Promise<DictItemDTO> => {
    const result = await updateDictItem(id, dto, tenantId);
    if (selectedDictType.value) {
      await loadDictItems(selectedDictType.value.dictCode);
    }
    return result;
  };

  /**
   * 删除字典项
   */
  const removeItem = async (id: number): Promise<void> => {
    await deleteDictItem(id, tenantId);
    if (selectedDictType.value) {
      await loadDictItems(selectedDictType.value.dictCode);
    }
  };

  /**
   * 刷新字典缓存
   */
  const refreshCache = async (dictCode?: string): Promise<void> => {
    await refreshDictCache(dictCode, tenantId);
  };

  /**
   * 选择字典类型
   */
  const selectDictType = async (dictType: DictTypeDTO) => {
    selectedDictType.value = dictType;
    await loadDictItems(dictType.dictCode);
  };

  return {
    // 状态
    typeList,
    typeLoading,
    typePagination,
    itemList,
    itemLoading,
    itemPagination,
    selectedDictType,

    // 字典翻译
    translateLabel,
    getDictItems,
    translateLabels,

    // 字典类型管理
    loadDictTypes,
    createType,
    updateType,
    removeType,

    // 字典项管理
    loadDictItems,
    createItem,
    createItemsBatch,
    updateItem,
    removeItem,

    // 其他
    refreshCache,
    selectDictType,
  };
}

