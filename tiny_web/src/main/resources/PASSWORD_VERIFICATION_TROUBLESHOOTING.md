# 密码验证失败问题排查指南

## 问题描述

修改密码后仍然验证失败。

## 排查步骤

### 步骤 1: 检查数据库中的数据

执行以下 SQL 查询，检查数据库中的密码数据：

```sql
-- 查看所有用户的密码配置
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 有 {bcrypt} 前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '⚠️ 有其他前缀'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '$2a$%' THEN '❌ 缺少 {bcrypt} 前缀'
        ELSE '⚠️ 其他格式'
    END AS password_format,
    LENGTH(JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password'))) AS password_length
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD';
```

**预期结果**：
- `password` 字段应该是：`{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu`
- `password_format` 应该是：`✅ 有 {bcrypt} 前缀`
- `password_length` 应该是：`60`（BCrypt 哈希长度）或 `67`（带 `{bcrypt}` 前缀）

### 步骤 2: 验证密码哈希值是否正确

使用 `PasswordVerificationTest.java` 验证密码哈希值：

```bash
cd tiny_web
mvn compile exec:java -Dexec.mainClass="com.tiny.web.PasswordVerificationTest" -Dexec.classpathScope=test
```

**预期结果**：所有测试应该通过（✅ 密码正确）

### 步骤 3: 检查应用程序日志

启动应用程序，尝试登录，查看日志输出。日志应该包含以下信息：

```
DEBUG - 用户 {} 的认证配置键: [password, ...]
DEBUG - 用户 {} 的认证配置内容: {password={bcrypt}$2a$10$...}
DEBUG - 用户 {} 从 password 键读取密码
DEBUG - 用户 {} 成功读取密码，密码长度: 67, 密码前缀: {bcrypt}$2a
DEBUG - 用户 {} 密码验证开始
DEBUG - 用户输入的密码长度: 6
DEBUG - 存储的密码哈希（原始）: {bcrypt}$2a$10$4EIhU0...
DEBUG - 存储的密码哈希（规范化后）: {bcrypt}$2a$10$4EIhU0...
DEBUG - 密码哈希是否有前缀: true
DEBUG - 用户 {} 密码验证结果: 成功
INFO - 用户 {} 密码认证成功
```

### 步骤 4: 检查 JSON 解析问题

如果日志显示 `config` 为空或 `password` 键不存在，可能是 JSON 解析问题。检查：

1. **JSON 格式是否正确**：
   ```sql
   SELECT 
       uam.id,
       u.username,
       JSON_VALID(uam.authentication_configuration) AS is_valid_json,
       uam.authentication_configuration
   FROM user_authentication_method uam
   WHERE uam.authentication_provider = 'LOCAL'
     AND uam.authentication_type = 'PASSWORD';
   ```

2. **检查 JSON 键名**：
   ```sql
   SELECT 
       uam.id,
       u.username,
       JSON_KEYS(uam.authentication_configuration) AS config_keys
   FROM user_authentication_method uam
   WHERE uam.authentication_provider = 'LOCAL'
     AND uam.authentication_type = 'PASSWORD';
   ```

**预期结果**：
- `is_valid_json` 应该是：`1`（true）
- `config_keys` 应该包含：`["password", ...]`

### 步骤 5: 手动更新密码

如果数据库中的数据不正确，手动执行以下 SQL 更新：

```sql
-- 更新密码为 123456（BCrypt 编码）
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    authentication_configuration,
    '$.password',
    '{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu'
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL;

-- 验证更新结果
SELECT 
    uam.id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD';
```

### 步骤 6: 检查应用程序配置

检查 `application.yaml` 中的日志配置：

```yaml
logging:
  level:
    com.tiny.web.sys.security.PasswordAuthenticationProvider: DEBUG
    com.tiny.web.sys.security.UserDetailsServiceImpl: DEBUG
    root: DEBUG
```

### 步骤 7: 清除缓存

如果使用了缓存，清除缓存后重试：

1. **重启应用程序**
2. **清除浏览器缓存**
3. **清除 Session**

## 常见问题

### 问题 1: 数据库中的数据没有更新

**症状**：日志显示密码哈希值与数据库中的不一致。

**解决方法**：
1. 检查 SQL 更新语句是否成功执行
2. 检查数据库连接是否正确
3. 检查事务是否提交

### 问题 2: JSON 解析失败

**症状**：日志显示 `config` 为空或 `password` 键不存在。

**解决方法**：
1. 检查 JSON 格式是否正确
2. 检查 `JsonStringConverter` 是否正确配置
3. 检查数据库中的 JSON 数据是否有特殊字符

### 问题 3: 密码哈希值不正确

**症状**：日志显示密码验证失败，但数据库中的哈希值看起来正确。

**解决方法**：
1. 使用 `PasswordVerificationTest.java` 验证哈希值
2. 检查哈希值是否有特殊字符或空格
3. 检查哈希值长度是否正确

### 问题 4: 类型转换问题

**症状**：日志显示 `password` 字段不是字符串类型。

**解决方法**：
1. 检查数据库中的 JSON 数据类型
2. 检查 `JsonStringConverter` 的类型转换逻辑
3. 确保密码值在 JSON 中是字符串类型

### 问题 5: 缓存问题

**症状**：更新密码后，仍然使用旧密码验证。

**解决方法**：
1. 重启应用程序
2. 清除 Session
3. 检查是否有缓存配置

## 诊断工具

### 1. 密码验证测试

```bash
mvn compile exec:java -Dexec.mainClass="com.tiny.web.PasswordVerificationTest" -Dexec.classpathScope=test
```

### 2. 数据库检查脚本

执行 `check_password_in_database.sql` 脚本：

```bash
mysql -u root -p tiny_web < check_password_in_database.sql
```

### 3. 密码生成工具

```bash
mvn compile exec:java -Dexec.mainClass="com.tiny.web.PasswordTest" -Dexec.classpathScope=test
```

## 联系支持

如果问题仍然存在，请提供以下信息：

1. **应用程序日志**（包含 DEBUG 级别的日志）
2. **数据库查询结果**（执行 `check_password_in_database.sql` 的结果）
3. **错误信息**（完整的异常堆栈）
4. **配置信息**（`application.yaml` 中的相关配置）

## 相关文件

- `PasswordAuthenticationProvider.java` - 密码认证提供者
- `UserDetailsServiceImpl.java` - 用户详情服务
- `JsonStringConverter.java` - JSON 转换器
- `PasswordVerificationTest.java` - 密码验证测试
- `check_password_in_database.sql` - 数据库检查脚本
- `update_password_to_123456.sql` - 密码更新脚本

