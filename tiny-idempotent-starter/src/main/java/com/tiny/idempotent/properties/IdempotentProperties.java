package com.tiny.idempotent.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等性配置属性
 */
@ConfigurationProperties(prefix = "tiny.idempotent")
public class IdempotentProperties {
    
    /**
     * 是否启用幂等性功能
     */
    private boolean enabled = true;
    
    /**
     * 存储类型：database 或 redis
     */
    private String storageType = "database";
    
    /**
     * 默认过期时间（秒）
     */
    private int defaultExpireTime = 60;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getStorageType() {
        return storageType;
    }
    
    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }
    
    public int getDefaultExpireTime() {
        return defaultExpireTime;
    }
    
    public void setDefaultExpireTime(int defaultExpireTime) {
        this.defaultExpireTime = defaultExpireTime;
    }
}

