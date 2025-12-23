package com.tiny.core.dict.repository.jpa;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 字典审计日志 JPA 实体类
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Entity
@Table(name = "dict_audit_log", indexes = {
    @Index(name = "idx_dict_code", columnList = "dict_code"),
    @Index(name = "idx_dict_item_id", columnList = "dict_item_id"),
    @Index(name = "idx_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_operation_type", columnList = "operation_type"),
    @Index(name = "idx_operation_time", columnList = "operation_time")
})
public class JpaDictAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_code", nullable = false, length = 64)
    private String dictCode;

    @Column(name = "dict_item_id")
    private Long dictItemId;

    @Column(name = "operation_type", nullable = false, length = 32)
    private String operationType;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 0L;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "operator", length = 64)
    private String operator;

    @Column(name = "operator_ip", length = 64)
    private String operatorIp;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public Long getDictItemId() {
        return dictItemId;
    }

    public void setDictItemId(Long dictItemId) {
        this.dictItemId = dictItemId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperatorIp() {
        return operatorIp;
    }

    public void setOperatorIp(String operatorIp) {
        this.operatorIp = operatorIp;
    }

    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
}

