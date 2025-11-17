-- ============================================
-- 将所有用户的密码更新为 123456（BCrypt 编码）
-- 适用于 tiny-oauth-server 项目
-- ============================================
-- 
-- 使用方法：
-- 1. 先运行 Java 工具类生成 BCrypt 哈希（或使用在线工具）
-- 2. 将生成的哈希值替换下面的 {bcrypt}HASH_VALUE
-- 3. 执行此 SQL 脚本
-- 
-- 生成 BCrypt 哈希的方法：
-- - 使用 Spring Security 的 PasswordEncoder
-- - 使用在线工具：https://bcrypt-generator.com/
-- - 使用命令行工具：htpasswd -bnBC 10 "" 123456 | tr -d ':\n'
-- ============================================

-- 步骤 1: 查看当前所有需要更新的记录
-- 查看所有使用 {bcrypt} 格式密码的用户
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS current_password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 使用 BCrypt 格式（password 键）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL THEN '⚠️ 使用 passwordHash 键（旧格式）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL THEN '⚠️ 使用 password_hash 键（旧格式）'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '⚠️ 使用其他格式'
        ELSE '❌ 格式错误'
    END AS password_format,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND (
      JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
      OR JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL
      OR JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL
  )
ORDER BY uam.id;

-- 步骤 2: 更新所有用户的密码为 123456（BCrypt 编码）
-- 注意：BCrypt 哈希值每次生成都不同（因为包含随机盐），但都可以验证相同的密码
-- 这里使用一个固定的 BCrypt 哈希值（明文密码：123456）
-- 如果需要生成新的哈希值，可以运行 PasswordTest.java 或使用在线工具
-- 
-- 优先更新 password 键（新格式）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    COALESCE(authentication_configuration, JSON_OBJECT()),
    '$.password',
    '{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu' -- 123456
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND (
      JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
  );

-- 步骤 3: 删除旧的键名（passwordHash, password_hash），统一使用 password 键
-- 注意：如果 authentication_configuration 中已经有 password 键，则删除旧的键
UPDATE user_authentication_method
SET authentication_configuration = JSON_REMOVE(
    authentication_configuration,
    '$.passwordHash',
    '$.password_hash',
    '$.encodedPassword',
    '$.hash'
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL
  AND (
      JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.encodedPassword') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.hash') IS NOT NULL
  );

-- 步骤 4: 验证更新结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') AS passwordHash,
    JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') AS password_hash,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 密码已更新为 123456（使用 password 键）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL THEN '⚠️ 仍使用 passwordHash 键（旧格式）'
        WHEN JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL THEN '⚠️ 仍使用 password_hash 键（旧格式）'
        ELSE '❌ 密码格式错误'
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND (
      JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
      OR JSON_EXTRACT(uam.authentication_configuration, '$.passwordHash') IS NOT NULL
      OR JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') IS NOT NULL
  )
ORDER BY uam.id;

-- ============================================
-- 注意事项：
-- 1. 执行前请备份数据库
-- 2. 此脚本会将所有使用 BCrypt 格式密码的用户密码更新为 123456
-- 3. 建议在生产环境中修改密码后立即更改
-- 4. 密码哈希值：{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu
--    对应的明文密码：123456
-- 5. BCrypt 哈希值每次生成都不同（因为包含随机盐），但都可以验证相同的密码
-- 6. 如果需要生成新的哈希值，可以运行 PasswordTest.java 或使用在线工具
-- 7. 脚本会统一使用 password 键名（新格式），并删除旧的键名（passwordHash, password_hash 等）
-- ============================================

