-- ============================================
-- 修改 user 表的 password 字段为可空（NULL）
-- 执行此脚本前，请确保已经执行了数据迁移脚本
-- ============================================

-- 修改 password 字段为可空
ALTER TABLE user 
MODIFY COLUMN password VARCHAR(100) NULL 
COMMENT '加密密码（已废弃，保留用于兼容，实际密码存储在 user_authentication_method 表中）';

-- 验证修改结果
DESCRIBE user;

-- ============================================
-- 注意事项：
-- 1. 执行此脚本前，请确保已经执行了 migrate_password_to_authentication_method.sql
-- 2. 确认所有用户的密码都已迁移到 user_authentication_method 表
-- 3. 应用程序已经更新为使用 user_authentication_method 表
-- 4. 建议先在测试环境执行，确认无误后再在生产环境执行
-- ============================================

