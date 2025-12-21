-- ============================================
-- 为 authentication_configuration 中的 password 字段添加 {bcrypt} 前缀
-- 如果密码值是 BCrypt 格式（$2a$, $2b$, $2y$ 开头）但没有前缀，则添加 {bcrypt} 前缀
-- ============================================

-- 步骤 1: 查找需要添加前缀的记录
-- 检查 password 值是否是 BCrypt 格式但没有前缀
-- 注意：JSON_EXTRACT 返回的是带引号的 JSON 字符串，需要使用 JSON_UNQUOTE 去掉引号才能使用 LIKE
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS current_password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '需要添加 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '需要添加 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '需要添加 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '已有 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '已有其他前缀'
        ELSE '不是 BCrypt 格式'
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

-- 步骤 2: 为 BCrypt 格式的密码添加 {bcrypt} 前缀
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

-- 步骤 3: 验证更新结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 已有 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '✅ 已有其他前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '❌ 缺少前缀（BCrypt 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '❌ 缺少前缀（BCrypt 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '❌ 缺少前缀（BCrypt 格式）'
        ELSE '⚠️ 其他格式'
    END AS status,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

-- ============================================
-- 注意事项：
-- 1. 执行步骤 1 后，检查哪些记录需要添加前缀
-- 2. 执行步骤 2，为 BCrypt 格式的密码添加 {bcrypt} 前缀
-- 3. 执行步骤 3，验证更新结果
-- 4. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 5. 执行迁移前，请备份数据库
-- 6. 代码中已经添加了 normalizePasswordHash 方法，会自动处理缺少前缀的情况
--    但如果数据库中的密码已经有正确的前缀，可以提高验证效率
-- ============================================

