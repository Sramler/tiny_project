package com.tiny.core.dict.starter.event;

import org.springframework.context.ApplicationEvent;

/**
 * 字典变更事件
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictChangeEvent extends ApplicationEvent {
    
    private final String dictCode;
    private final Long tenantId;
    private final DictChangeType changeType;
    
    public DictChangeEvent(Object source, String dictCode, Long tenantId, DictChangeType changeType) {
        super(source);
        this.dictCode = dictCode;
        this.tenantId = tenantId;
        this.changeType = changeType;
    }
    
    public String getDictCode() {
        return dictCode;
    }
    
    public Long getTenantId() {
        return tenantId;
    }
    
    public DictChangeType getChangeType() {
        return changeType;
    }
    
    /**
     * 字典变更类型
     */
    public enum DictChangeType {
        CREATE,   // 创建
        UPDATE,   // 更新
        DELETE    // 删除
    }
}

