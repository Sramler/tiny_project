package com.tiny.core.dict.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 字典项 JPA Repository
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Repository
public interface JpaDictItemRepository extends JpaRepository<JpaDictItem, Long> {
    
    /**
     * 根据字典类型ID和租户ID列表查询字典项（优化查询）
     * 
     * <p>一次性查询平台字典（tenant_id=0）和租户字典，避免多次查询。
     * 按 sortOrder 排序。
     */
    @Query("SELECT d FROM JpaDictItem d WHERE d.dictTypeId = :dictTypeId AND d.tenantId IN :tenantIds ORDER BY d.sortOrder ASC")
    List<JpaDictItem> findByDictTypeIdAndTenantIdInOrderBySortOrder(
        @Param("dictTypeId") Long dictTypeId, 
        @Param("tenantIds") List<Long> tenantIds);
    
    /**
     * 根据字典类型ID查询字典项
     */
    List<JpaDictItem> findByDictTypeId(Long dictTypeId);
    
    /**
     * 根据字典类型ID和租户ID查询字典项
     */
    List<JpaDictItem> findByDictTypeIdAndTenantId(Long dictTypeId, Long tenantId);
    
    /**
     * 根据字典类型ID、值和租户ID查询字典项
     */
    Optional<JpaDictItem> findByDictTypeIdAndValueAndTenantId(Long dictTypeId, String value, Long tenantId);
}

