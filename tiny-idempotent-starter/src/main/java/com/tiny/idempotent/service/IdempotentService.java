package com.tiny.idempotent.service;

/**
 * 幂等性服务接口
 * 
 * <p>提供幂等性 token 的存储和验证功能。</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public interface IdempotentService {
    
    /**
     * 检查并设置幂等性 token
     * 
     * <p>如果 token 不存在，则设置并返回 true；如果 token 已存在，则返回 false。</p>
     * 
     * @param key 幂等性 key
     * @param expireTime 过期时间（秒）
     * @return true 表示首次请求，false 表示重复请求
     */
    boolean checkAndSet(String key, int expireTime);
    
    /**
     * 删除幂等性 token
     * 
     * @param key 幂等性 key
     */
    void delete(String key);
}

