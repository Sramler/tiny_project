-- ============================================
-- 完整迁移：将 user 表的 password 字段迁移到 user_authentication_method 表
-- 执行此脚本后，user.password 字段将不再使用
-- ============================================

-- 步骤 1: 将现有用户的密码迁移到 user_authentication_method 表
-- 只迁移 user 表中有密码但 user_authentication_method 表中没有的记录
INSERT INTO user_authentication_method 
    (user_id, authentication_provider, authentication_type, authentication_configuration, is_primary_method, is_method_enabled, authentication_priority, created_at, updated_at)
SELECT 
    u.id AS user_id,
    'LOCAL' AS authentication_provider,
    'PASSWORD' AS authentication_type,
    JSON_OBJECT('passwordHash', u.password) AS authentication_configuration,
    TRUE AS is_primary_method,
    TRUE AS is_method_enabled,
    0 AS authentication_priority,
    NOW() AS created_at,
    NOW() AS updated_at
FROM user u
WHERE u.password IS NOT NULL 
  AND u.password != ''
  AND NOT EXISTS (
      SELECT 1 
      FROM user_authentication_method uam 
      WHERE uam.user_id = u.id 
        AND uam.authentication_provider = 'LOCAL' 
        AND uam.authentication_type = 'PASSWORD'
  );

-- 步骤 2: 验证迁移结果
SELECT 
    u.id AS user_id,
    u.username,
    CASE WHEN u.password IS NOT NULL THEN '有密码（待清空）' ELSE '无密码（已废弃）' END AS user_password_status,
    CASE WHEN uam.id IS NOT NULL THEN '已迁移' ELSE '未迁移' END AS migration_status,
    uam.authentication_provider,
    uam.authentication_type,
    uam.is_method_enabled
FROM user u
LEFT JOIN user_authentication_method uam ON u.id = uam.user_id 
    AND uam.authentication_provider = 'LOCAL' 
    AND uam.authentication_type = 'PASSWORD'
ORDER BY u.id;

-- 步骤 3: 可选 - 清空 user.password 字段（谨慎执行！确保迁移成功后再执行）
-- UPDATE user SET password = NULL WHERE password IS NOT NULL;

-- 步骤 4: 可选 - 删除 user.password 字段（需要修改数据库结构，更谨慎！）
-- ALTER TABLE user DROP COLUMN password;

-- ============================================
-- 注意事项：
-- 1. 执行步骤 1 后，检查步骤 2 的验证结果
-- 2. 确认所有用户都已成功迁移后再执行步骤 3
-- 3. 步骤 4 需要修改 Java 代码中的 User 实体类（已完成标记为 @Deprecated）
-- 4. 建议先在测试环境执行，确认无误后再在生产环境执行
-- ============================================

