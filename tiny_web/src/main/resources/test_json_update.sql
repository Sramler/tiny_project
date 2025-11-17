-- ============================================
-- 测试 JSON 更新脚本
-- 用于测试和调试 JSON 字段的更新操作
-- ============================================

-- 测试 1: 查看当前数据
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    uam.authentication_configuration,
    JSON_EXTRACT(uam.authentication_configuration, '$.password') AS password_extracted,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password_unquoted,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' AS is_bcrypt_format,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' AS has_prefix
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- 测试 2: 检查哪些记录需要更新
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS current_password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '已有前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '需要添加 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '需要添加 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '需要添加 {bcrypt} 前缀'
        ELSE '其他格式'
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

