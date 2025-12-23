package com.tiny.core.dict.starter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 数据字典配置属性
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@ConfigurationProperties(prefix = "tiny.core.dict")
public class DictProperties {
    
    /**
     * 是否启用数据字典功能
     */
    private boolean enabled = true;
    
    /**
     * 缓存配置
     */
    private Cache cache = new Cache();
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    /**
     * 缓存配置
     */
    public static class Cache {
        /**
         * 缓存类型：memory（默认）或 redis
         */
        private String type = "memory";
        
        /**
         * 缓存刷新间隔（秒）
         */
        private long refreshInterval = 300;
        
        /**
         * 缓存过期时间（秒），0 表示不过期
         */
        private long expireTime = 0;
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public long getRefreshInterval() {
            return refreshInterval;
        }
        
        public void setRefreshInterval(long refreshInterval) {
            this.refreshInterval = refreshInterval;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
    }
}

