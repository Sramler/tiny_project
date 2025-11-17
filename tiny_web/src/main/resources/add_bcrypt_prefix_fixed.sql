-- ============================================
-- 为 authentication_configuration 中的 password 字段添加 {bcrypt} 前缀
-- 修复版本：正确处理 JSON_EXTRACT 返回值（带引号的字符串）
-- ============================================
-- 
-- 问题说明：
-- JSON_EXTRACT 返回的是带引号的 JSON 字符串（例如："$2a$10$..."），
-- 需要使用 JSON_UNQUOTE 去掉引号才能正确使用 LIKE 进行匹配。
-- 
-- 使用方法：
-- 1. 先执行"步骤 1：检查需要更新的记录"，查看哪些记录需要更新
-- 2. 确认无误后，执行"步骤 2：添加 {bcrypt} 前缀"
-- 3. 最后执行"步骤 3：验证更新结果"，确认所有记录都已正确更新
-- ============================================

-- 步骤 1: 检查需要更新的记录
-- 查看哪些记录的密码是 BCrypt 格式但没有 {bcrypt} 前缀
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS current_password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 已有 {bcrypt} 前缀（无需更新）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '✅ 已有其他前缀（无需更新）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '⚠️ 需要添加 {bcrypt} 前缀（BCrypt $2a$ 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '⚠️ 需要添加 {bcrypt} 前缀（BCrypt $2b$ 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '⚠️ 需要添加 {bcrypt} 前缀（BCrypt $2y$ 格式）'
        ELSE '❓ 其他格式（可能不是 BCrypt）'
    END AS status,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

-- 步骤 2: 添加 {bcrypt} 前缀
-- 为缺少前缀的 BCrypt 密码添加 {bcrypt} 前缀
-- 注意：此 UPDATE 语句只会更新 BCrypt 格式（$2a$, $2b$, $2y$ 开头）且没有前缀的密码
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    authentication_configuration,
    '$.password',
    CONCAT('{bcrypt}', JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')))
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL
  -- 检查是否是 BCrypt 格式（$2a$, $2b$, $2y$ 开头）
  AND (
      JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2a$%'
      OR JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2b$%'
      OR JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2y$%'
  )
  -- 检查是否没有前缀（不以 { 开头）
  AND JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) NOT LIKE '{%';

-- 步骤 3: 验证更新结果
-- 检查所有记录的密码是否都有正确的 {bcrypt} 前缀
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 已有 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '✅ 已有其他前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '❌ 缺少前缀（BCrypt $2a$ 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2b$%' THEN '❌ 缺少前缀（BCrypt $2b$ 格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2y$%' THEN '❌ 缺少前缀（BCrypt $2y$ 格式）'
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
-- 2. 确认无误后，执行步骤 2，为 BCrypt 格式的密码添加 {bcrypt} 前缀
-- 3. 执行步骤 3，验证更新结果，确保所有记录都已正确更新
-- 4. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 5. 执行迁移前，请备份数据库
-- 6. 代码中已经添加了 normalizePasswordHash 方法，会自动处理缺少前缀的情况
--    但如果数据库中的密码已经有正确的前缀，可以提高验证效率
-- ============================================

