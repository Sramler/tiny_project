package com.tiny.idempotent.config;

import com.tiny.idempotent.aspect.IdempotentAspect;
import com.tiny.idempotent.properties.IdempotentProperties;
import com.tiny.idempotent.service.IdempotentService;
import com.tiny.idempotent.service.impl.DatabaseIdempotentService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 幂等性自动配置类
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@AutoConfiguration
@EnableConfigurationProperties(IdempotentProperties.class)
@ConditionalOnProperty(prefix = "tiny.idempotent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IdempotentAutoConfiguration {
    
    /**
     * 数据库实现的幂等性服务
     * 
     * 条件：
     * 1. 配置为 database 或未配置（默认）
     * 2. JdbcTemplate 类存在（由使用方引入 spring-boot-starter-jdbc）
     * 3. 不存在其他 IdempotentService Bean
     */
    @Bean
    @ConditionalOnMissingBean(IdempotentService.class)
    @ConditionalOnProperty(prefix = "tiny.idempotent", name = "storage-type", havingValue = "database", matchIfMissing = true)
    @ConditionalOnClass(JdbcTemplate.class)
    public IdempotentService databaseIdempotentService(JdbcTemplate jdbcTemplate) {
        return new DatabaseIdempotentService(jdbcTemplate);
    }
    
    /**
     * Redis 实现的幂等性服务（预留，需要使用时由使用方实现）
     * 
     * 注意：Redis 实现不在 starter 中提供，因为：
     * 1. 避免强制引入 Redis 依赖
     * 2. 使用方可以根据需求自行实现
     * 3. 如需使用，可以在应用模块中创建实现并标记为 @Primary
     */
    
    /**
     * 幂等性切面
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect(IdempotentService idempotentService) {
        return new IdempotentAspect(idempotentService);
    }
}

