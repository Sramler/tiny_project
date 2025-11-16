package com.tiny.oauthserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * OAuth2 授权数据清理监听器
 * 在应用启动时自动清理旧的授权数据，这些数据可能是用旧的 ObjectMapper 序列化的
 * 清理后，新的授权数据会用正确的 ObjectMapper 序列化
 */
@Component
@Order(1) // 确保在其他组件之前执行，特别是在 OAuth2AuthorizationService 创建之前
public class OAuth2AuthorizationCleanupListener implements ApplicationListener<ApplicationReadyEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationCleanupListener.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final boolean cleanupEnabled;
    
    public OAuth2AuthorizationCleanupListener(
            JdbcTemplate jdbcTemplate,
            @Value("${oauth2.authorization.cleanup-on-startup:true}") boolean cleanupEnabled) {
        this.jdbcTemplate = jdbcTemplate;
        this.cleanupEnabled = cleanupEnabled;
    }
    
    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        if (!cleanupEnabled) {
            logger.debug("OAuth2 授权数据清理已禁用");
            return;
        }
        
        try {
            logger.info("开始清理 OAuth2 授权表中的旧数据...");
            
            int deletedCount = jdbcTemplate.update("DELETE FROM oauth2_authorization WHERE id IS NOT NULL");
            
            logger.info("清理完成，删除了 {} 条旧的授权记录", deletedCount);
            logger.info("注意：用户需要重新登录以生成新的授权数据");
        } catch (Exception e) {
            logger.error("清理 OAuth2 授权数据时发生错误", e);
            // 不抛出异常，避免影响应用启动
        }
    }
}

