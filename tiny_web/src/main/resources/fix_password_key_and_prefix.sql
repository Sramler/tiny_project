-- ============================================
-- 一键修复脚本：同时处理键名迁移和前缀添加
-- 1. 将 password_hash, passwordHash, encodedPassword 等键名迁移为 password
-- 2. 为 BCrypt 格式的密码添加 {bcrypt} 前缀（如果缺少）
-- ============================================

-- 步骤 1: 将 password_hash 键名迁移为 password，并添加 {bcrypt} 前缀（如果需要）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.password_hash'),
    '$.password',
    CASE 
        -- 如果值以 { 开头，说明已有前缀，直接使用（去掉引号）
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')) LIKE '{%' THEN 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash'))
        -- 如果是 BCrypt 格式（$2a$, $2b$, $2y$ 开头），添加 {bcrypt} 前缀
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')) LIKE '$2a$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')) LIKE '$2b$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')) LIKE '$2y$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash')))
        -- 其他格式，直接使用（去掉引号）
        ELSE 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password_hash'))
    END
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 2: 将 passwordHash 键名迁移为 password，并添加 {bcrypt} 前缀（如果需要）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.passwordHash'),
    '$.password',
    CASE 
        -- 如果值以 { 开头，说明已有前缀，直接使用（去掉引号）
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')) LIKE '{%' THEN 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash'))
        -- 如果是 BCrypt 格式（$2a$, $2b$, $2y$ 开头），添加 {bcrypt} 前缀
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')) LIKE '$2a$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')) LIKE '$2b$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')) LIKE '$2y$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash')))
        -- 其他格式，直接使用（去掉引号）
        ELSE 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.passwordHash'))
    END
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 3: 将 encodedPassword 键名迁移为 password，并添加 {bcrypt} 前缀（如果需要）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.encodedPassword'),
    '$.password',
    CASE 
        -- 如果值以 { 开头，说明已有前缀，直接使用（去掉引号）
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')) LIKE '{%' THEN 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword'))
        -- 如果是 BCrypt 格式（$2a$, $2b$, $2y$ 开头），添加 {bcrypt} 前缀
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')) LIKE '$2a$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')) LIKE '$2b$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')) LIKE '$2y$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword')))
        -- 其他格式，直接使用（去掉引号）
        ELSE 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.encodedPassword'))
    END
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.encodedPassword') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 4: 将 hash 键名迁移为 password，并添加 {bcrypt} 前缀（如果需要）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.hash'),
    '$.password',
    CASE 
        -- 如果值以 { 开头，说明已有前缀，直接使用（去掉引号）
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')) LIKE '{%' THEN 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash'))
        -- 如果是 BCrypt 格式（$2a$, $2b$, $2y$ 开头），添加 {bcrypt} 前缀
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')) LIKE '$2a$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')) LIKE '$2b$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')))
        WHEN JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')) LIKE '$2y$%' THEN 
            CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash')))
        -- 其他格式，直接使用（去掉引号）
        ELSE 
            JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.hash'))
    END
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.hash') IS NOT NULL
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL;

-- 步骤 5: 为已存在 password 键但缺少前缀的 BCrypt 密码添加 {bcrypt} 前缀
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
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password_hash')) AS password_hash,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash')) AS passwordHash,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.encodedPassword')) AS encodedPassword,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.hash')) AS hash
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- ============================================
-- 注意事项：
-- 1. 此脚本会同时处理键名迁移和前缀添加
-- 2. 执行步骤 1-5 后，检查步骤 6 的验证结果
-- 3. 确认所有记录都已成功迁移，且密码值都有正确的 {bcrypt} 前缀
-- 4. 代码中已经添加了 normalizePasswordHash 方法，会自动处理缺少前缀的情况
--    但如果数据库中的密码已经有正确的前缀，可以提高验证效率
-- 5. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 6. 执行迁移前，请备份数据库
-- ============================================

