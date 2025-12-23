package com.tiny.core.dict.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 字典类型 JPA Repository
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Repository
public interface JpaDictTypeRepository extends JpaRepository<JpaDictType, Long> {
    
    /**
     * 根据字典编码查询字典类型
     */
    Optional<JpaDictType> findByDictCode(String dictCode);
    
    /**
     * 根据字典编码和租户ID查询字典类型
     */
    Optional<JpaDictType> findByDictCodeAndTenantId(String dictCode, Long tenantId);
    
    /**
     * 根据租户ID列表查询字典类型列表
     */
    @Query("SELECT d FROM JpaDictType d WHERE d.tenantId IN :tenantIds")
    List<JpaDictType> findByTenantIdIn(@Param("tenantIds") List<Long> tenantIds);
}

