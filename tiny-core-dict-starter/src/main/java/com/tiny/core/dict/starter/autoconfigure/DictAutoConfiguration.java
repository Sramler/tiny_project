package com.tiny.core.dict.starter.autoconfigure;

import com.tiny.core.dict.cache.DictCacheManager;
import com.tiny.core.dict.repository.DictItemRepository;
import com.tiny.core.dict.repository.DictTypeRepository;
import com.tiny.core.dict.starter.properties.DictProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 数据字典自动配置
 * 
 * <p>支持条件装配，实现轻引入：
 * - 默认使用内存缓存（轻量模式）
 * - 可选 Redis 缓存（生产环境）
 * - 支持通过配置开关控制
 * 
 * <p>注意：需要 Repository 实现（tiny-core-dict-repository-jpa）才能正常工作
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(DictProperties.class)
@ConditionalOnProperty(prefix = "tiny.core.dict", name = "enabled", matchIfMissing = true)
public class DictAutoConfiguration {

    /**
     * 内存缓存实现（默认，轻量模式）
     * 
     * <p>条件：
     * 1. 不存在其他 DictCacheManager Bean
     * 2. 配置为 memory 或未配置（默认）
     * 3. Repository Bean 存在（由 Repository 模块提供）
     */
    @Bean
    @ConditionalOnMissingBean(DictCacheManager.class)
    @ConditionalOnProperty(prefix = "tiny.core.dict.cache", name = "type",
                          havingValue = "memory", matchIfMissing = true)
    @ConditionalOnBean({DictItemRepository.class, DictTypeRepository.class})
    public DictCacheManager memoryDictCacheManager(DictItemRepository dictItemRepository,
                                                   DictTypeRepository dictTypeRepository) {
        return new com.tiny.core.dict.cache.memory.MemoryDictCacheManager(
            dictItemRepository, dictTypeRepository);
    }

    /**
     * Redis 缓存实现（可选，生产环境）
     * 
     * <p>注意：Redis 缓存的实际实现由 tiny-core-dict-cache-redis 模块提供，
     * 通过 RedisDictCacheAutoConfiguration 自动配置。
     * 这里不需要手动配置，只需要确保 Starter 模块不阻止 Redis 缓存的自动配置即可。
     */
}

