-- ============================================
-- 直接修改 user_authentication_method 表中的 authentication_configuration JSON 字段
-- 将 password_hash 键名改为 password
-- 支持多种键名迁移：password_hash, passwordHash, encodedPassword -> password
-- ============================================

-- 步骤 1: 将 password_hash 键名迁移为 password
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.password_hash'),
    '$.password',
    JSON_EXTRACT(authentication_configuration, '$.password_hash')
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 2: 将 passwordHash 键名迁移为 password
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.passwordHash'),
    '$.password',
    JSON_EXTRACT(authentication_configuration, '$.passwordHash')
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 3: 将 encodedPassword 键名迁移为 password
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.encodedPassword'),
    '$.password',
    JSON_EXTRACT(authentication_configuration, '$.encodedPassword')
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.encodedPassword') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 4: 将 hash 键名迁移为 password（如果有的话）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.hash'),
    '$.password',
    JSON_EXTRACT(authentication_configuration, '$.hash')
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.hash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 5: 验证迁移结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    CASE 
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL THEN '✅ 已迁移（使用 password）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL THEN '❌ 未迁移（仍使用 password_hash）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL THEN '❌ 未迁移（仍使用 passwordHash）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.encodedPassword') IS NOT NULL THEN '❌ 未迁移（仍使用 encodedPassword）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.hash') IS NOT NULL THEN '❌ 未迁移（仍使用 hash）'
        ELSE '❌ 配置错误（缺少密码字段）'
    END AS migration_status,
    JSON_EXTRACT(uam.authentication_configuration, '$.password') AS password,
    JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') AS password_hash,
    JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') AS passwordHash,
    JSON_EXTRACT(uam.authentication_configuration, '$.encodedPassword') AS encodedPassword,
    JSON_EXTRACT(uam.authentication_configuration, '$.hash') AS hash,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- ============================================
-- 使用说明：
-- 1. 执行步骤 1-4，将各种键名迁移为 password
-- 2. 执行步骤 5，验证迁移结果
-- 3. 如果验证结果中还有未迁移的记录，检查原因并手动处理
-- 4. 确认所有记录都已成功迁移后，可以删除旧的键名
-- ============================================

-- 步骤 6: 可选 - 清理旧的键名（谨慎执行！）
-- 注意：只有在确认所有记录都已成功迁移后，才执行此步骤
-- 此步骤会删除 authentication_configuration 中除了 password 之外的所有旧键名
-- 
-- UPDATE user_authentication_method
-- SET authentication_configuration = JSON_OBJECT(
--     'password', JSON_EXTRACT(authentication_configuration, '$.password')
-- )
-- WHERE authentication_provider = 'LOCAL'
--   AND authentication_type = 'PASSWORD'
--   AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL
--   AND (
--       JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
--       OR JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
--       OR JSON_EXTRACT(authentication_configuration, '$.encodedPassword') IS NOT NULL
--       OR JSON_EXTRACT(authentication_configuration, '$.hash') IS NOT NULL
--   );

-- ============================================
-- 注意事项：
-- 1. 执行前请备份数据库
-- 2. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 3. 执行步骤 1-4 后，检查步骤 5 的验证结果
-- 4. 确认所有记录都已成功迁移后，可以考虑执行步骤 6 清理旧键名
-- 5. 应用程序已经更新为优先使用 password 键名（但代码中已支持向后兼容）
-- ============================================

