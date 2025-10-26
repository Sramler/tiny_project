-- 将现有用户的密码从 user 表迁移到 user_authentication_method 表
-- 执行此脚本前，请确保已经创建了 user_authentication_method 表

-- 为每个用户创建 LOCAL + PASSWORD 认证方法
INSERT INTO user_authentication_method 
    (user_id, authentication_provider, authentication_type, authentication_configuration, is_primary_method, is_method_enabled, authentication_priority, created_at, updated_at)
SELECT 
    id AS user_id,
    'LOCAL' AS authentication_provider,
    'PASSWORD' AS authentication_type,
    JSON_OBJECT('passwordHash', password) AS authentication_configuration,
    TRUE AS is_primary_method,
    TRUE AS is_method_enabled,
    0 AS authentication_priority,
    NOW() AS created_at,
    NOW() AS updated_at
FROM user
WHERE password IS NOT NULL AND password != ''
  AND NOT EXISTS (
      -- 避免重复插入
      SELECT 1 
      FROM user_authentication_method uam 
      WHERE uam.user_id = user.id 
        AND uam.authentication_provider = 'LOCAL' 
        AND uam.authentication_type = 'PASSWORD'
  );

-- 查看迁移结果
SELECT 
    u.id AS user_id,
    u.username,
    u.nickname,
    uam.authentication_provider,
    uam.authentication_type,
    uam.is_primary_method,
    uam.is_method_enabled,
    uam.authentication_priority
FROM user u
LEFT JOIN user_authentication_method uam ON u.id = uam.user_id
ORDER BY u.id, uam.authentication_priority;
