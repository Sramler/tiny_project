package com.tiny.core.dict.repository;

import com.tiny.core.dict.model.DictType;
import java.util.List;
import java.util.Optional;

/**
 * 字典类型 Repository 接口
 * 
 * <p>纯接口定义，无实现，不依赖 Spring Data JPA。
 * 具体实现将在 Repository 模块中提供。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public interface DictTypeRepository {
    /**
     * 根据字典编码查询字典类型
     * 
     * @param dictCode 字典编码
     * @return 字典类型
     */
    Optional<DictType> findByDictCode(String dictCode);

    /**
     * 根据字典编码和租户ID查询字典类型
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return 字典类型
     */
    Optional<DictType> findByDictCodeAndTenantId(String dictCode, Long tenantId);

    /**
     * 根据租户ID查询字典类型列表
     * 
     * @param tenantIds 租户ID列表（包含0表示平台字典）
     * @return 字典类型列表
     */
    List<DictType> findByTenantIdIn(List<Long> tenantIds);

    /**
     * 保存字典类型
     * 
     * @param dictType 字典类型
     * @return 保存后的字典类型
     */
    DictType save(DictType dictType);

    /**
     * 删除字典类型
     * 
     * @param id 字典类型ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询字典类型
     * 
     * @param id 字典类型ID
     * @return 字典类型
     */
    Optional<DictType> findById(Long id);
}

