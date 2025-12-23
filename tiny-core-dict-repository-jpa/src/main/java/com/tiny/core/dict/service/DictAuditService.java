package com.tiny.core.dict.service;

import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.jpa.JpaDictAuditLog;
import com.tiny.core.dict.repository.jpa.JpaDictAuditLogRepository;
import com.tiny.core.dict.runtime.DictRuntime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 字典审计日志服务
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Service
public class DictAuditService {
    
    private final JpaDictAuditLogRepository auditLogRepository;
    private final DictItemRepository dictItemRepository;
    private final DictRuntime dictRuntime;
    
    public DictAuditService(JpaDictAuditLogRepository auditLogRepository,
                           DictItemRepository dictItemRepository,
                           DictRuntime dictRuntime) {
        this.auditLogRepository = auditLogRepository;
        this.dictItemRepository = dictItemRepository;
        this.dictRuntime = dictRuntime;
    }
    
    /**
     * 记录字典变更日志
     */
    @Transactional
    public void logDictChange(String dictCode, Long dictItemId, String operationType,
                              Long tenantId, String oldValueJson, String newValueJson,
                              String operator, String operatorIp) {
        JpaDictAuditLog log = new JpaDictAuditLog();
        log.setDictCode(dictCode);
        log.setDictItemId(dictItemId);
        log.setOperationType(operationType);
        log.setTenantId(tenantId);
        log.setOldValue(oldValueJson);
        log.setNewValue(newValueJson);
        log.setOperator(operator);
        log.setOperatorIp(operatorIp);
        log.setOperationTime(LocalDateTime.now());
        auditLogRepository.save(log);
    }
    
    /**
     * 查询审计日志
     */
    public List<JpaDictAuditLog> getAuditLogs(String dictCode, Long tenantId,
                                               LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByDictCodeAndTenantIdAndOperationTimeBetween(
            dictCode, tenantId, startTime, endTime);
    }
    
    /**
     * 根据审计日志回滚
     */
    @Transactional
    public JpaDictAuditLog rollbackAuditLog(Long auditLogId) {
        JpaDictAuditLog log = auditLogRepository.findById(auditLogId)
            .orElseThrow(() -> new IllegalArgumentException("审计日志不存在"));
        
        // 根据日志恢复数据
        restoreFromAuditLog(log);
        
        return log;
    }
    
    private void restoreFromAuditLog(JpaDictAuditLog log) {
        if (log.getDictItemId() == null) {
            return;
        }
        
        DictItem item = dictItemRepository.findById(log.getDictItemId())
            .orElseThrow(() -> new IllegalArgumentException("字典项不存在"));
        
        // TODO: 解析 oldValue JSON 并恢复
        // 这里简化实现，实际需要解析 JSON 并恢复字段值
        
        dictItemRepository.save(item);
        dictRuntime.refreshCache(log.getDictCode(), log.getTenantId());
    }
}

