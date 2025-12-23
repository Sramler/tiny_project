package com.tiny.core.dict.starter.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 字典变更通知器
 * 
 * <p>发布字典变更事件，用于触发缓存刷新等操作。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Component
public class DictChangeNotifier {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public DictChangeNotifier(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * 通知字典变更
     */
    public void notifyDictChanged(String dictCode, Long tenantId, DictChangeEvent.DictChangeType changeType) {
        DictChangeEvent event = new DictChangeEvent(this, dictCode, tenantId, changeType);
        eventPublisher.publishEvent(event);
    }
}

