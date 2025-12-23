package com.tiny.core.dict.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典审计日志 Repository
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Repository
public interface JpaDictAuditLogRepository extends JpaRepository<JpaDictAuditLog, Long> {
    
    @Query("SELECT a FROM JpaDictAuditLog a WHERE a.dictCode = :dictCode " +
           "AND a.tenantId = :tenantId AND a.operationTime BETWEEN :startTime AND :endTime " +
           "ORDER BY a.operationTime DESC")
    List<JpaDictAuditLog> findByDictCodeAndTenantIdAndOperationTimeBetween(
        @Param("dictCode") String dictCode,
        @Param("tenantId") Long tenantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}

