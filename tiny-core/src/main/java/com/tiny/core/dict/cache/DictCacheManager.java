package com.tiny.core.dict.cache;

/**
 * 字典缓存管理接口
 * 
 * <p>提供字典缓存的加载、获取、刷新能力。
 * 具体实现可以是内存缓存或 Redis 缓存。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public interface DictCacheManager {
    /**
     * 获取字典缓存
     * 
     * <p>如果缓存不存在，则从数据库加载并缓存。
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     * @return 字典缓存
     */
    DictCache getDictCache(String dictCode, Long tenantId);

    /**
     * 刷新字典缓存
     * 
     * <p>从数据库重新加载字典数据并更新缓存。
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     */
    void refreshDictCache(String dictCode, Long tenantId);

    /**
     * 异步刷新字典缓存
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     */
    void refreshDictCacheAsync(String dictCode, Long tenantId);

    /**
     * 清除字典缓存
     * 
     * @param dictCode 字典编码
     * @param tenantId 租户ID
     */
    void evictDictCache(String dictCode, Long tenantId);

    /**
     * 清除所有字典缓存
     */
    void evictAllDictCache();
}

