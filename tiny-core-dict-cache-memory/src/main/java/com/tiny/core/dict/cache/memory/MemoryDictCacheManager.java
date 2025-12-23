package com.tiny.core.dict.cache.memory;

import com.tiny.core.dict.cache.DictCache;
import com.tiny.core.dict.cache.DictCacheManager;
import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存字典缓存管理器
 * 
 * <p>使用 ConcurrentHashMap 实现内存缓存，支持多租户隔离。
 * 缓存 Key 格式：tenantId:dictCode
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class MemoryDictCacheManager implements DictCacheManager {
    
    private final DictItemRepository dictItemRepository;
    private final DictTypeRepository dictTypeRepository;
    
    /**
     * 缓存结构：Map<tenantId, Map<dictCode, DictCache>>
     */
    private final Map<Long, Map<String, DictCache>> cache = new ConcurrentHashMap<>();
    
    public MemoryDictCacheManager(DictItemRepository dictItemRepository,
                                  DictTypeRepository dictTypeRepository) {
        this.dictItemRepository = dictItemRepository;
        this.dictTypeRepository = dictTypeRepository;
    }
    
    @Override
    public DictCache getDictCache(String dictCode, Long tenantId) {
        // 确保租户缓存 Map 存在
        cache.computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>());
        
        // 如果缓存不存在，则从数据库加载
        Map<String, DictCache> tenantCache = cache.get(tenantId);
        return tenantCache.computeIfAbsent(dictCode, code -> loadDict(dictCode, tenantId));
    }
    
    /**
     * 从数据库加载字典数据
     * 
     * <p>优化查询：一次性查询平台字典（tenant_id=0）和租户字典，
     * 租户值自动覆盖平台值（相同 value）。
     */
    private DictCache loadDict(String dictCode, Long tenantId) {
        // 1. 查询字典类型
        Long dictTypeId = dictTypeRepository.findByDictCode(dictCode)
            .orElseThrow(() -> new IllegalArgumentException("Dictionary not found: " + dictCode))
            .getId();
        
        // 2. 查询平台字典（tenant_id=0）和租户字典
        List<Long> tenantIds = List.of(0L, tenantId);
        List<DictItem> items = dictItemRepository
            .findByDictTypeIdAndTenantIdInOrderBySortOrder(dictTypeId, tenantIds);
        
        // 3. 构建 value -> label 映射，租户字典覆盖平台字典
        Map<String, String> map = items.stream()
            .filter(item -> Boolean.TRUE.equals(item.getEnabled())) // 只包含启用的项
            .collect(Collectors.toMap(
                DictItem::getValue,
                DictItem::getLabel,
                (existing, replacement) -> replacement  // 租户覆盖平台
            ));
        
        DictCache dictCache = new DictCache();
        dictCache.setValueLabelMap(map);
        dictCache.setLastUpdateTime(LocalDateTime.now());
        return dictCache;
    }
    
    @Override
    public void refreshDictCache(String dictCode, Long tenantId) {
        Map<String, DictCache> tenantCache = cache.get(tenantId);
        if (tenantCache != null) {
            tenantCache.put(dictCode, loadDict(dictCode, tenantId));
        }
    }
    
    @Override
    @Async
    public void refreshDictCacheAsync(String dictCode, Long tenantId) {
        refreshDictCache(dictCode, tenantId);
    }
    
    @Override
    public void evictDictCache(String dictCode, Long tenantId) {
        Map<String, DictCache> tenantCache = cache.get(tenantId);
        if (tenantCache != null) {
            tenantCache.remove(dictCode);
        }
    }
    
    @Override
    public void evictAllDictCache() {
        cache.clear();
    }
}

