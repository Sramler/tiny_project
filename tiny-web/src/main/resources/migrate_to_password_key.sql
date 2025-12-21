-- ============================================
-- 数据迁移脚本：将 authentication_configuration 中的键名迁移为 password
-- 支持从 password_hash, passwordHash, encodedPassword 迁移到 password
-- 执行此脚本前，请确保已经执行了基本的密码迁移脚本
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

-- 步骤 5: 为 BCrypt 格式的密码添加 {bcrypt} 前缀（如果缺少）
-- 更新 password 值，如果它是 BCrypt 格式（$2a$, $2b$, $2y$ 开头）但没有前缀
-- 注意：JSON_EXTRACT 返回的是带引号的 JSON 字符串，需要使用 JSON_UNQUOTE 去掉引号才能使用 LIKE
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    authentication_configuration,
    '$.password',
    CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')))
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL
  AND (
      JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2a$%'
      OR JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2b$%'
      OR JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2y$%'
  )
  AND JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) NOT LIKE '{%';

-- 步骤 6: 验证迁移结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    CASE 
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL 
             AND JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 已迁移（使用 password，有 {bcrypt} 前缀）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL 
             AND JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '✅ 已迁移（使用 password，有其他前缀）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL 
             AND JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '⚠️ 已迁移（使用 password，但缺少 {bcrypt} 前缀）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL 
             AND JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '⚠️ 已迁移（使用 password，但缺少 {bcrypt} 前缀）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL 
             AND JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '⚠️ 已迁移（使用 password，但缺少 {bcrypt} 前缀）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL THEN '✅ 已迁移（使用 password）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL THEN '❌ 未迁移（仍使用 password_hash）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL THEN '❌ 未迁移（仍使用 passwordHash）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.encodedPassword') IS NOT NULL THEN '❌ 未迁移（仍使用 encodedPassword）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.hash') IS NOT NULL THEN '❌ 未迁移（仍使用 hash）'
        ELSE '❌ 配置错误（缺少密码字段）'
    END AS migration_status,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
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

-- 步骤 7: 可选 - 清理旧的键名（谨慎执行！）
-- 注意：只有在确认所有记录都已成功迁移后，才执行此步骤
-- 此步骤会删除 authentication_configuration 中除了 password 之外的所有旧键名
-- 如果需要保留其他配置字段，请修改此脚本
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
-- 1. 执行步骤 1-4（键名迁移）后，执行步骤 5（添加 {bcrypt} 前缀）
-- 2. 执行步骤 6，验证迁移结果
-- 3. 确认所有记录都已成功迁移，且密码值都有正确的 {bcrypt} 前缀
-- 4. 应用程序已经更新为优先使用 password 键名（但代码中已支持向后兼容）
-- 5. 代码中已经添加了 normalizePasswordHash 方法，会自动处理缺少前缀的情况
--    但如果数据库中的密码已经有正确的前缀，可以提高验证效率
-- 6. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 7. 执行迁移前，请备份数据库
-- 8. 如果验证结果中还有未迁移的记录，检查原因并手动处理
-- 9. 确认所有记录都已成功迁移后，可以考虑执行步骤 7 清理旧键名
-- ============================================

