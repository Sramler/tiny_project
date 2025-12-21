package com.tiny.idempotent.service.impl;

import com.tiny.idempotent.service.IdempotentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 基于数据库的幂等性服务实现
 * 
 * <p>使用数据库表存储幂等性 token，适合没有 Redis 的环境。</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class DatabaseIdempotentService implements IdempotentService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseIdempotentService.class);
    
    private final JdbcTemplate jdbcTemplate;
    
    public DatabaseIdempotentService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initTable();
    }
    
    /**
     * 初始化幂等性表（如果不存在）
     */
    private void initTable() {
        try {
            String sql = """
                CREATE TABLE IF NOT EXISTS sys_idempotent_token (
                    id VARCHAR(255) PRIMARY KEY,
                    expire_time DATETIME NOT NULL,
                    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_expire_time (expire_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='幂等性token表'
                """;
            jdbcTemplate.execute(sql);
            log.info("幂等性表初始化成功");
        } catch (Exception e) {
            log.warn("幂等性表可能已存在，跳过初始化: {}", e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public boolean checkAndSet(String key, int expireTime) {
        // 先清理过期数据
        cleanupExpiredTokens();
        
        // 尝试插入
        try {
            LocalDateTime expireTimeValue = LocalDateTime.now().plusSeconds(expireTime);
            String insertSql = """
                INSERT INTO sys_idempotent_token (id, expire_time, created_time)
                VALUES (?, ?, ?)
                """;
            jdbcTemplate.update(insertSql, key, expireTimeValue, LocalDateTime.now());
            log.debug("幂等性token设置成功: key={}, expireTime={}秒", key, expireTime);
            return true;
        } catch (Exception e) {
            // 主键冲突，说明 token 已存在
            log.debug("幂等性token已存在: key={}", key);
            return false;
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            String deleteSql = "DELETE FROM sys_idempotent_token WHERE id = ?";
            jdbcTemplate.update(deleteSql, key);
            log.debug("幂等性token删除成功: key={}", key);
        } catch (Exception e) {
            log.warn("删除幂等性token失败: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 清理过期的 token
     */
    private void cleanupExpiredTokens() {
        try {
            String cleanupSql = "DELETE FROM sys_idempotent_token WHERE expire_time < ?";
            int deleted = jdbcTemplate.update(cleanupSql, LocalDateTime.now());
            if (deleted > 0) {
                log.debug("清理过期幂等性token: {} 条", deleted);
            }
        } catch (Exception e) {
            log.warn("清理过期幂等性token失败: {}", e.getMessage());
        }
    }
}
