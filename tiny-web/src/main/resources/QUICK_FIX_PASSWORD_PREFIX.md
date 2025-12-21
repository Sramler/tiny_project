# 快速修复密码前缀指南

## 问题描述

如果 `authentication_configuration` 中的 `password` 值是 `$2a$10$...` 格式（BCrypt 哈希），但缺少 `{bcrypt}` 前缀，Spring Security 的 `DelegatingPasswordEncoder` 可能无法正确识别和验证密码。

**重要提示**：MySQL 的 `JSON_EXTRACT` 函数返回的是带引号的 JSON 字符串（例如：`"$2a$10$..."`），在 WHERE 子句中使用 LIKE 时必须使用 `JSON_UNQUOTE(JSON_EXTRACT(...))` 去掉引号，才能正确匹配。

## 问题说明

Spring Security 的 `DelegatingPasswordEncoder` 需要密码哈希值带有编码前缀（如 `{bcrypt}`），才能正确识别并验证密码。

如果数据库中的 BCrypt 密码缺少 `{bcrypt}` 前缀，虽然代码中的 `normalizePasswordHash` 方法会自动处理，但为了数据库的一致性和验证效率，建议直接在数据库中修复。

## 解决方案

### 方案 1: 代码自动处理（推荐用于开发环境）

代码中已经添加了 `normalizePasswordHash` 方法，会自动为缺少前缀的 BCrypt 密码添加 `{bcrypt}` 前缀。

**优点**：

- 不需要修改数据库
- 向后兼容
- 自动处理

**缺点**：

- 每次验证都需要检查和处理
- 数据库中的值仍然缺少前缀

### 方案 2: 直接修改数据库（推荐用于生产环境）

执行 SQL 脚本，直接在数据库中为 BCrypt 密码添加 `{bcrypt}` 前缀。

**优点**：

- 数据库中的值格式正确
- 验证时不需要额外处理
- 更高效

**缺点**：

- 需要执行 SQL 脚本
- 需要备份数据库

## 快速修复 SQL

### 方式 1: 使用修复后的 SQL 脚本（推荐）

执行 `add_bcrypt_prefix_fixed.sql` 脚本（已修复 JSON_EXTRACT 使用问题）：

```sql
-- 步骤 1: 检查需要更新的记录
-- 注意：使用 JSON_UNQUOTE 去掉引号，才能正确使用 LIKE 匹配
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
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

-- 步骤 2: 添加 {bcrypt} 前缀
-- 注意：WHERE 子句中使用 JSON_UNQUOTE 去掉引号，才能正确使用 LIKE 匹配
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
-- 注意：使用 JSON_UNQUOTE 去掉引号，才能正确使用 LIKE 匹配
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
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;
```

### 方式 2: 同时处理键名迁移和前缀添加

执行 `fix_password_key_and_prefix.sql` 脚本（已修复），它会：

1. 将 `password_hash`, `passwordHash`, `encodedPassword` 等键名迁移为 `password`
2. 为 BCrypt 格式的密码添加 `{bcrypt}` 前缀

```sql
-- 执行综合修复脚本
source fix_password_key_and_prefix.sql;
```

### 方式 3: 单独处理前缀添加

执行 `add_bcrypt_prefix.sql` 脚本（已修复）：

```sql
-- 执行前缀添加脚本
source add_bcrypt_prefix.sql;
```

## 正确的格式

### 推荐格式

```json
{
  "password": "{bcrypt}$2a$10$aXNtZGVtb1VzZXIuLi5QQVNT"
}
```

### 错误格式

```json
{
  "password": "$2a$10$aXNtZGVtb1VzZXIuLi5QQVNT" // ❌ 缺少 {bcrypt} 前缀
}
```

## 支持的编码器前缀

- `{bcrypt}` - BCrypt 编码器（推荐）
- `{noop}` - 明文密码（不推荐，仅用于开发环境）
- `{pbkdf2}` - PBKDF2 编码器
- `{scrypt}` - SCrypt 编码器
- `{argon2}` - Argon2 编码器

## 常见问题

### Q1: 为什么 SQL 语句需要使用 `JSON_UNQUOTE(JSON_EXTRACT(...))`？

**答案**：MySQL 的 `JSON_EXTRACT` 函数返回的是带引号的 JSON 字符串（例如：`"$2a$10$..."`），而不是原始字符串值。在 WHERE 子句中使用 LIKE 时，必须使用 `JSON_UNQUOTE` 去掉引号，才能正确匹配。

**错误示例**：

```sql
-- ❌ 错误：这样无法正确匹配，因为 JSON_EXTRACT 返回的是带引号的字符串
WHERE JSON_EXTRACT(authentication_configuration, '$.password') LIKE '$2a$%'
```

**正确示例**：

```sql
-- ✅ 正确：使用 JSON_UNQUOTE 去掉引号后，才能正确匹配
WHERE JSON_UNQUOTE(JSON_EXTRACT(authentication_configuration, '$.password')) LIKE '$2a$%'
```

### Q2: 为什么需要 {bcrypt} 前缀？

Spring Security 的 `DelegatingPasswordEncoder` 通过前缀来选择不同的密码编码器。如果没有前缀，`DelegatingPasswordEncoder` 可能无法正确识别密码格式，导致验证失败。

### Q3: 如果我不修复数据库，代码还能正常工作吗？

**答案**：可以。代码中的 `normalizePasswordHash` 方法会自动为缺少前缀的 BCrypt 密码添加 `{bcrypt}` 前缀。但为了数据库的一致性和验证效率，建议直接在数据库中修复。

## 注意事项

1. **备份数据库**：执行 SQL 脚本前，请务必备份数据库
2. **测试环境**：建议先在测试环境执行，确认无误后再在生产环境执行
3. **代码兼容性**：代码中已经添加了 `normalizePasswordHash` 方法，会自动处理缺少前缀的情况
4. **性能考虑**：如果数据库中的密码已经有正确的前缀，可以提高验证效率

## 相关文件

- `add_bcrypt_prefix_fixed.sql` - 修复后的前缀添加脚本（推荐）
- `add_bcrypt_prefix.sql` - 前缀添加脚本（已修复）
- `migrate_to_password_key.sql` - 键名迁移 + 前缀添加（已修复）
- `fix_password_key_and_prefix.sql` - 综合修复脚本（已修复，推荐）
