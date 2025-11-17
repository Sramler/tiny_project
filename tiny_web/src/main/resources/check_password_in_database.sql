-- ============================================
-- 检查数据库中的密码数据
-- ============================================

-- 步骤 1: 查看所有用户的密码配置
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    uam.authentication_provider,
    uam.authentication_type,
    uam.authentication_configuration AS full_configuration,
    JSON_EXTRACT(uam.authentication_configuration, '$.password') AS password_json_extract,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password_unquoted,
    JSON_VALID(uam.authentication_configuration) AS is_valid_json,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 有 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '⚠️ 有其他前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '❌ 缺少 {bcrypt} 前缀（BCrypt 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '❌ 缺少 {bcrypt} 前缀（BCrypt 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '❌ 缺少 {bcrypt} 前缀（BCrypt 格式）'
        ELSE '⚠️ 其他格式'
    END AS password_format,
    LENGTH(JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password'))) AS password_length,
    uam.is_method_enabled,
    uam.created_at,
    uam.updated_at
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- 步骤 2: 检查 JSON 配置中的所有键
SELECT 
    uam.id AS method_id,
    u.username,
    JSON_KEYS(uam.authentication_configuration) AS config_keys,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- 步骤 3: 检查密码是否包含特殊字符或空格
SELECT 
    uam.id AS method_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    HEX(JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password'))) AS password_hex,
    LENGTH(JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password'))) AS password_length,
    CHAR_LENGTH(JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password'))) AS password_char_length
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- 步骤 4: 验证密码是否是 123456 的 BCrypt 哈希
-- 注意：这需要在应用程序中验证，但我们可以检查格式
SELECT 
    uam.id AS method_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) = '{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu' THEN '✅ 是 123456 的哈希值'
        ELSE '❌ 不是 123456 的哈希值'
    END AS is_123456_hash
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

