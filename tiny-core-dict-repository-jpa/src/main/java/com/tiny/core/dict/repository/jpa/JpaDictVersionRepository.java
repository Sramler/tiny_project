package com.tiny.core.dict.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 字典版本 Repository
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Repository
public interface JpaDictVersionRepository extends JpaRepository<JpaDictVersion, Long> {
    
    Optional<JpaDictVersion> findByDictCodeAndVersionAndTenantId(String dictCode, String version, Long tenantId);
    
    List<JpaDictVersion> findByDictCodeAndTenantIdOrderByCreatedAtDesc(String dictCode, Long tenantId);
}

