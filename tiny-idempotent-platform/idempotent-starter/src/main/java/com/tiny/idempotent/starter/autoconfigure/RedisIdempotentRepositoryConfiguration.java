package com.tiny.idempotent.starter.autoconfigure;

import com.tiny.idempotent.core.repository.IdempotentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 幂等性存储配置
 * 
 * <p>当 classpath 中存在 StringRedisTemplate 且配置为 redis 存储时生效</p>
 * <p>使用字符串形式的类名避免直接 import，防止在没有 Redis 依赖时类加载失败</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
@ConditionalOnProperty(prefix = "tiny.idempotent", name = "store", havingValue = "redis")
public class RedisIdempotentRepositoryConfiguration {
    
    /**
     * Redis 实现的幂等性存储
     * 
     * <p>注意：由于使用了字符串形式的 @ConditionalOnClass，当 Redis 类不存在时，
     * 此配置类不会被加载，因此方法参数可以使用正常的类型（通过反射获取）</p>
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentRepository.class)
    @SuppressWarnings("unchecked")
    public IdempotentRepository redisIdempotentRepository(Object redisTemplate) throws Exception {
        // 使用反射方式创建 RedisIdempotentRepository，避免直接 import
        Class<?> repositoryClass = Class.forName("com.tiny.idempotent.repository.redis.RedisIdempotentRepository");
        return (IdempotentRepository) repositoryClass
            .getConstructor(redisTemplate.getClass())
            .newInstance(redisTemplate);
    }
}

