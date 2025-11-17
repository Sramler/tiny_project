# 将用户密码更新为 123456

## 概述

此文档说明如何将所有用户的密码更新为 `123456`（BCrypt 编码）。

## 方法 1: 使用 SQL 脚本（推荐）

### 步骤 1: 备份数据库

```bash
mysqldump -u root -p tiny_web > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 步骤 2: 执行 SQL 脚本

```bash
mysql -u root -p tiny_web < update_password_to_123456.sql
```

或者在 MySQL 客户端中执行：

```sql
-- 步骤 1: 查看当前所有需要更新的记录
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS current_password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 使用 BCrypt 格式'
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{%' THEN '⚠️ 使用其他格式'
        ELSE '❌ 格式错误'
    END AS password_format
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;

-- 步骤 2: 更新所有用户的密码为 123456
UPDATE user_authentication_method
SET authentication_configuration = JSON_SET(
    authentication_configuration,
    '$.password',
    '{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu' -- 123456
)
WHERE authentication_provider = 'LOCAL'
  AND authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL;

-- 步骤 3: 验证更新结果
SELECT 
    uam.id AS method_id,
    uam.user_id,
    u.username,
    JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) AS password,
    CASE 
        WHEN JSON_UNQUOTE(JSON_EXTRACT(uam.authentication_configuration, '$.password')) LIKE '{bcrypt}%' THEN '✅ 密码已更新为 123456'
        ELSE '❌ 密码格式错误'
    END AS status
FROM user_authentication_method uam
INNER JOIN user u ON u.id = uam.user_id
WHERE uam.authentication_provider = 'LOCAL'
  AND uam.authentication_type = 'PASSWORD'
  AND JSON_EXTRACT(uam.authentication_configuration, '$.password') IS NOT NULL
ORDER BY uam.id;
```

### 步骤 3: 验证更新结果

登录系统，使用用户名和密码 `123456` 验证是否可以正常登录。

## 方法 2: 使用 Java 工具类生成新的 BCrypt 哈希

### 步骤 1: 运行 PasswordTest.java

```bash
cd tiny_web
mvn compile exec:java -Dexec.mainClass="com.tiny.web.PasswordTest" -Dexec.classpathScope=test
```

### 步骤 2: 复制生成的 BCrypt 哈希值

输出示例：
```
123456 密码加密结果：$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu
123456 密码（带前缀）：{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu
```

### 步骤 3: 更新 SQL 脚本

将生成的哈希值替换 `update_password_to_123456.sql` 中的哈希值。

### 步骤 4: 执行 SQL 脚本

参考方法 1 的步骤 2。

## 方法 3: 使用在线工具生成 BCrypt 哈希

### 步骤 1: 访问在线工具

访问 https://bcrypt-generator.com/ 或类似工具。

### 步骤 2: 生成 BCrypt 哈希

1. 输入密码：`123456`
2. 选择 rounds（强度）：`10`（推荐）
3. 点击生成
4. 复制生成的哈希值（例如：`$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu`）

### 步骤 3: 更新 SQL 脚本

将生成的哈希值添加到 `{bcrypt}` 前缀后，更新 SQL 脚本。

### 步骤 4: 执行 SQL 脚本

参考方法 1 的步骤 2。

## 注意事项

1. **备份数据库**：执行 SQL 脚本前，请务必备份数据库。
2. **测试环境**：建议先在测试环境执行，确认无误后再在生产环境执行。
3. **安全性**：密码 `123456` 是弱密码，仅用于开发/测试环境。生产环境请使用强密码。
4. **BCrypt 哈希值**：BCrypt 哈希值每次生成都不同（因为包含随机盐），但都可以验证相同的密码。
5. **密码格式**：确保密码哈希值包含 `{bcrypt}` 前缀，否则 Spring Security 的 `DelegatingPasswordEncoder` 可能无法正确识别。

## 验证密码

更新密码后，可以使用以下方法验证：

1. **登录系统**：使用用户名和密码 `123456` 登录系统。
2. **Java 代码验证**：
   ```java
   BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
   String hash = "{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu";
   boolean matches = encoder.matches("123456", hash);
   System.out.println("密码验证结果：" + matches); // 应该输出 true
   ```

## 相关文件

- `update_password_to_123456.sql` - SQL 更新脚本
- `PasswordTest.java` - Java 工具类，用于生成 BCrypt 哈希
- `UserDetailsServiceImpl.java` - 用户详情服务，包含密码规范化逻辑
- `PasswordAuthenticationProvider.java` - 密码认证提供者

## 故障排除

### 问题 1: 更新后无法登录

**原因**：密码哈希值格式错误或缺少 `{bcrypt}` 前缀。

**解决方法**：
1. 检查 `authentication_configuration` 中的 `password` 字段是否包含 `{bcrypt}` 前缀。
2. 使用 `normalizePasswordHash` 方法规范化密码哈希。

### 问题 2: SQL 脚本执行失败

**原因**：JSON 函数使用不当或数据格式错误。

**解决方法**：
1. 检查 MySQL 版本是否支持 JSON 函数（MySQL 5.7+）。
2. 检查 `authentication_configuration` 字段是否为有效的 JSON 格式。
3. 使用 `JSON_VALID` 函数验证 JSON 格式。

### 问题 3: BCrypt 哈希值验证失败

**原因**：密码哈希值不正确或格式错误。

**解决方法**：
1. 使用 `PasswordTest.java` 重新生成 BCrypt 哈希。
2. 确保哈希值包含 `{bcrypt}` 前缀。
3. 检查哈希值长度（BCrypt 哈希值通常为 60 个字符）。

## 参考资料

- [Spring Security PasswordEncoder](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
- [BCrypt Password Hashing](https://en.wikipedia.org/wiki/Bcrypt)
- [MySQL JSON Functions](https://dev.mysql.com/doc/refman/8.0/en/json-functions.html)

