package com.tiny.core.dict.cache.redis;

import com.tiny.core.dict.cache.DictCacheManager;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import com.tiny.core.dict.starter.properties.DictProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis 字典缓存自动配置
 * 
 * <p>当配置为 redis 缓存类型时，自动配置 RedisDictCacheManager。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnProperty(prefix = "tiny.core.dict.cache", name = "type", havingValue = "redis")
@ConditionalOnBean({DictItemRepository.class, DictTypeRepository.class, RedisTemplate.class})
@EnableConfigurationProperties(DictProperties.class)
public class RedisDictCacheAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean(DictCacheManager.class)
    public DictCacheManager redisDictCacheManager(DictItemRepository dictItemRepository,
                                                   DictTypeRepository dictTypeRepository,
                                                   @Qualifier("dictRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                                   DictProperties dictProperties) {
        long ttlSeconds = dictProperties.getCache().getExpireTime() > 0 
            ? dictProperties.getCache().getExpireTime() 
            : 3600; // 默认 1 小时
        
        return new RedisDictCacheManager(dictItemRepository, dictTypeRepository, redisTemplate, ttlSeconds);
    }
}

