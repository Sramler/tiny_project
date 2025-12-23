package com.tiny.core.dict.cache.redis;

import com.tiny.core.dict.cache.DictCache;
import com.tiny.core.dict.cache.DictCacheManager;
import com.tiny.core.dict.model.DictItem;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 字典缓存管理器
 * 
 * <p>使用 Redis 实现字典缓存，支持多租户隔离。
 * 缓存 Key 格式：dict:{tenantId}:{dictCode}
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class RedisDictCacheManager implements DictCacheManager {
    
    private static final String CACHE_KEY_PREFIX = "dict:";
    private static final long DEFAULT_TTL_SECONDS = 3600; // 默认 1 小时过期
    
    private final DictItemRepository dictItemRepository;
    private final DictTypeRepository dictTypeRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long ttlSeconds;
    
    public RedisDictCacheManager(DictItemRepository dictItemRepository,
                                 DictTypeRepository dictTypeRepository,
                                 RedisTemplate<String, Object> redisTemplate) {
        this(dictItemRepository, dictTypeRepository, redisTemplate, DEFAULT_TTL_SECONDS);
    }
    
    public RedisDictCacheManager(DictItemRepository dictItemRepository,
                                 DictTypeRepository dictTypeRepository,
                                 RedisTemplate<String, Object> redisTemplate,
                                 long ttlSeconds) {
        this.dictItemRepository = dictItemRepository;
        this.dictTypeRepository = dictTypeRepository;
        this.redisTemplate = redisTemplate;
        this.ttlSeconds = ttlSeconds;
    }
    
    @Override
    public DictCache getDictCache(String dictCode, Long tenantId) {
        String cacheKey = buildCacheKey(dictCode, tenantId);
        
        // 尝试从 Redis 获取缓存
        // 注意：需要确保 RedisTemplate 配置了正确的序列化器
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        DictCache cache = null;
        
        if (cached instanceof DictCache) {
            cache = (DictCache) cached;
        } else if (cached instanceof Map) {
            // 如果 Redis 返回的是 Map（可能是序列化后的结果），需要手动转换
            // 这里简化处理，直接重新加载
            cache = null;
        }
        
        if (cache == null) {
            // 缓存不存在或反序列化失败，从数据库加载
            cache = loadDict(dictCode, tenantId);
            // 写入 Redis，设置过期时间
            redisTemplate.opsForValue().set(cacheKey, cache, ttlSeconds, TimeUnit.SECONDS);
        }
        
        return cache;
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
        String cacheKey = buildCacheKey(dictCode, tenantId);
        DictCache cache = loadDict(dictCode, tenantId);
        // 更新 Redis 缓存，设置过期时间
        redisTemplate.opsForValue().set(cacheKey, cache, ttlSeconds, TimeUnit.SECONDS);
    }
    
    @Override
    @Async
    public void refreshDictCacheAsync(String dictCode, Long tenantId) {
        refreshDictCache(dictCode, tenantId);
    }
    
    @Override
    public void evictDictCache(String dictCode, Long tenantId) {
        String cacheKey = buildCacheKey(dictCode, tenantId);
        redisTemplate.delete(cacheKey);
    }
    
    @Override
    public void evictAllDictCache() {
        // 删除所有以 dict: 开头的 key
        // 注意：在生产环境中，应该使用更精确的 key 模式，避免误删其他数据
        redisTemplate.delete(redisTemplate.keys(CACHE_KEY_PREFIX + "*"));
    }
    
    /**
     * 构建缓存 Key
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return 缓存 Key
     */
    private String buildCacheKey(String dictCode, Long tenantId) {
        return CACHE_KEY_PREFIX + tenantId + ":" + dictCode;
    }
}

