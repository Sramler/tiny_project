package com.tiny.core.dict.repository;

import com.tiny.core.dict.model.DictItem;
import java.util.List;
import java.util.Optional;

/**
 * 字典项 Repository 接口
 * 
 * <p>纯接口定义，无实现，不依赖 Spring Data JPA。
 * 具体实现将在 Repository 模块中提供。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public interface DictItemRepository {
    /**
     * 根据字典类型ID和租户ID列表查询字典项（优化查询）
     * 
     * <p>一次性查询平台字典（tenant_id=0）和租户字典，避免多次查询。
     * 
     * @param dictTypeId 字典类型ID
     * @param tenantIds 租户ID列表（包含0表示平台字典）
     * @return 字典项列表，按 sortOrder 排序
     */
    List<DictItem> findByDictTypeIdAndTenantIdInOrderBySortOrder(Long dictTypeId, List<Long> tenantIds);

    /**
     * 根据字典类型ID查询字典项
     * 
     * @param dictTypeId 字典类型ID
     * @return 字典项列表
     */
    List<DictItem> findByDictTypeId(Long dictTypeId);

    /**
     * 根据字典类型ID和租户ID查询字典项
     * 
     * @param dictTypeId 字典类型ID
     * @param tenantId 租户ID
     * @return 字典项列表
     */
    List<DictItem> findByDictTypeIdAndTenantId(Long dictTypeId, Long tenantId);

    /**
     * 根据字典类型ID、值和租户ID查询字典项
     * 
     * @param dictTypeId 字典类型ID
     * @param value 字典值
     * @param tenantId 租户ID
     * @return 字典项
     */
    Optional<DictItem> findByDictTypeIdAndValueAndTenantId(Long dictTypeId, String value, Long tenantId);

    /**
     * 保存字典项
     * 
     * @param dictItem 字典项
     * @return 保存后的字典项
     */
    DictItem save(DictItem dictItem);

    /**
     * 删除字典项
     * 
     * @param id 字典项ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询字典项
     * 
     * @param id 字典项ID
     * @return 字典项
     */
    Optional<DictItem> findById(Long id);
}

