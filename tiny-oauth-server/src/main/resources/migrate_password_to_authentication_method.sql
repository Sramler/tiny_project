-- 将现有用户的密码从 user 表迁移到 user_authentication_method 表
-- 执行此脚本前，请确保已经创建 user_authentication_method 表并做好数据备份
-- 规范字段说明：
--   password             : 按 {encoder}hash 格式存储当前密码（此脚本统一为 {noop} 前缀）
--   password_changed_at  : ISO8601(UTC) 时间，方便密码过期/轮换策略
--   hash_algorithm       : 记录当前使用的编码器，便于后续迁移
--   password_version     : 口令版本号，可在升级算法时做兼容
--   created_by           : 记录本次迁移由谁触发，便于审计

-- 为每个用户创建 LOCAL + PASSWORD 认证方法
INSERT INTO user_authentication_method (
    user_id,
    authentication_provider,
    authentication_type,
    authentication_configuration,
    is_primary_method,
    is_method_enabled,
    authentication_priority,
    created_at,
    updated_at
)
SELECT
    u.id AS user_id,
    'LOCAL' AS authentication_provider,
    'PASSWORD' AS authentication_type,
    JSON_OBJECT(
        'password', CASE
                       WHEN u.username = 'admin' THEN '{noop}admin'
                       ELSE '{noop}123456'
                   END,
        'password_changed_at', DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%sZ'),
        'hash_algorithm', 'noop',
        'password_version', 1,
        'created_by', 'migration-script'
    ) AS authentication_configuration,
    TRUE AS is_primary_method,
    TRUE AS is_method_enabled,
    0 AS authentication_priority,
    NOW() AS created_at,
    NOW() AS updated_at
FROM user u
WHERE NOT EXISTS (
    -- 避免重复插入
    SELECT 1
    FROM user_authentication_method uam
    WHERE uam.user_id = u.id
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
