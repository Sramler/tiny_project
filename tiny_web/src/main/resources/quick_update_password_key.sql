-- ============================================
-- 快速更新脚本：直接将 authentication_configuration 中的 password_hash 键名改为 password
-- 适用于需要快速修复单个或少量记录的情况
-- ============================================

-- 方式 1: 更新单个用户的 authentication_configuration（根据用户ID）
-- 将 user_id = 1 的用户的 password_hash 键名改为 password
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.password_hash'),
    '$.password',
    JSON_EXTRACT(authentication_configuration, '$.password_hash')
)
WHERE user_id = 1
  AND authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL;

-- 方式 2: 更新所有用户的 authentication_configuration（批量更新）
-- 将所有用户的 password_hash 键名改为 password
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

-- 方式 3: 更新所有用户的 authentication_configuration（支持多种键名）
-- 按优先级处理：password_hash -> passwordHash -> encodedPassword -> hash -> password
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    JSON_REMOVE(authentication_configuration, '$.password_hash', '$.passwordHash', '$.encodedPassword', '$.hash'),
    '$.password',
    COALESCE(
        JSON_EXTRACT(authentication_configuration, '$.password_hash'),
        JSON_EXTRACT(authentication_configuration, '$.passwordHash'),
        JSON_EXTRACT(authentication_configuration, '$.encodedPassword'),
        JSON_EXTRACT(authentication_configuration, '$.hash'),
        JSON_EXTRACT(authentication_configuration, '$.password')
    )
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NULL
  AND (
      JSON_EXTRACT(authentication_configuration, '$.password_hash') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.passwordHash') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.encodedPassword') IS NOT NULL
      OR JSON_EXTRACT(authentication_configuration, '$.hash') IS NOT NULL
  );

-- 验证更新结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_EXTRACT(uam.authentication_configuration, '$.password') AS password,
    JSON_EXTRACT(uam.authentication_configuration, '$.password_hash') AS password_hash,
    uam.authentication_configuration AS full_configuration
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
ORDER BY uam.id;

-- ============================================
-- 使用说明：
-- 1. 方式 1: 适用于更新单个用户（修改 user_id = 1 为实际用户ID）
-- 2. 方式 2: 适用于批量更新所有用户（只处理 password_hash 键名）
-- 3. 方式 3: 适用于批量更新所有用户（支持多种键名，推荐使用）
-- 4. 执行后，运行验证查询，确认更新结果
-- 5. 建议先在测试环境执行，确认无误后再在生产环境执行
-- 6. 执行前请备份数据库
-- ============================================

