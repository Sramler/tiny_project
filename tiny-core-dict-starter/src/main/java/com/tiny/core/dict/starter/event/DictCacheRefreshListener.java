package com.tiny.core.dict.starter.event;

import com.tiny.core.dict.cache.DictCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 字典缓存刷新监听器
 * 
 * <p>监听字典变更事件，自动刷新缓存。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@Component
public class DictCacheRefreshListener {
    
    private static final Logger logger = LoggerFactory.getLogger(DictCacheRefreshListener.class);
    
    private final DictCacheManager cacheManager;
    
    public DictCacheRefreshListener(DictCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
    
    @EventListener
    @Async
    public void handleDictChange(DictChangeEvent event) {
        logger.debug("收到字典变更事件: dictCode={}, tenantId={}, changeType={}",
            event.getDictCode(), event.getTenantId(), event.getChangeType());
        
        try {
            cacheManager.refreshDictCacheAsync(event.getDictCode(), event.getTenantId());
            logger.debug("字典缓存刷新完成: dictCode={}, tenantId={}",
                event.getDictCode(), event.getTenantId());
        } catch (Exception e) {
            logger.error("字典缓存刷新失败: dictCode={}, tenantId={}",
                event.getDictCode(), event.getTenantId(), e);
        }
    }
}

