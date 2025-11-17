-- ============================================
-- 数据迁移脚本：将 user 表的 password 字段迁移到 user_authentication_method 表
-- 执行此脚本前，请确保已经创建了 user_authentication_method 表
-- ============================================

-- 步骤 1: 将现有用户的密码迁移到 user_authentication_method 表
-- 只迁移 user 表中有密码但 user_authentication_method 表中没有的记录
INSERT INTO user_authentication_method 
    (user_id, authentication_provider, authentication_type, authentication_configuration, is_primary_method, is_method_enabled, authentication_priority, created_at, updated_at)
SELECT 
    u.id AS user_id,
    'LOCAL' AS authentication_provider,
    'PASSWORD' AS authentication_type,
    JSON_OBJECT('password', u.password) AS authentication_configuration,
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
-- 注意：在执行此步骤前，请确保：
-- 1. 步骤 2 的验证结果显示所有用户都已成功迁移
-- 2. 应用程序已经更新为使用 user_authentication_method 表
-- 3. 已经进行充分测试，确认认证功能正常
-- UPDATE user SET password = NULL WHERE password IS NOT NULL;

-- 步骤 4: 可选 - 修改 user.password 字段为 NULL（如果数据库支持，可以执行此步骤）
-- 注意：此步骤会修改数据库结构，需要谨慎执行
-- ALTER TABLE user MODIFY COLUMN password VARCHAR(100) NULL COMMENT '加密密码（已废弃，保留用于兼容）';

-- 步骤 5: 可选 - 删除 user.password 字段（更谨慎！）
-- 注意：只有在确认完全不再需要 user.password 字段时，才执行此步骤
-- 建议先保留字段一段时间，确保系统稳定运行后再删除
-- ALTER TABLE user DROP COLUMN password;

-- ============================================
-- 注意事项：
-- 1. 执行步骤 1 后，检查步骤 2 的验证结果
-- 2. 确认所有用户都已成功迁移后再执行步骤 3
-- 3. 步骤 4 和步骤 5 需要修改数据库结构，更谨慎
-- 4. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 5. 执行迁移前，请备份数据库
-- ============================================

