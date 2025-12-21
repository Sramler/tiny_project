# 密码字段迁移指南

## 概述

将 `tiny_web` 项目的用户密码从 `user.password` 字段迁移到 `user_authentication_method` 表。

## 迁移步骤

### 1. 创建 user_authentication_method 表

执行 `schema.sql` 中的 `user_authentication_method` 表创建语句，或者如果表已存在，跳过此步骤。

### 2. 执行数据迁移

执行 `migrate_password_to_authentication_method.sql` 脚本，将现有用户的密码从 `user.password` 字段迁移到 `user_authentication_method` 表。

```sql
-- 执行迁移脚本
source migrate_password_to_authentication_method.sql;
```

### 3. 验证迁移结果

检查迁移脚本的输出，确认所有用户都已成功迁移。

### 3.1. 键名迁移（如果使用了 encodedPassword 或 passwordHash）

如果之前使用了 `encodedPassword` 或 `passwordHash` 作为键名，可以执行 `migrate_to_password_key.sql` 脚本，将键名迁移为 `password`。

```sql
-- 执行键名迁移脚本
source migrate_to_password_key.sql;
```

**注意**：代码中已经支持向后兼容，可以同时支持 `password`、`encodedPassword` 和 `passwordHash` 键名。但建议使用 `password`，因为：
1. 在 `authentication_configuration` 上下文中，键名 `password` 已经足够清晰
2. 更简洁、直接，符合 JSON 配置的最佳实践
3. 代码会优先使用 `password` 键名，其他键名作为向后兼容

### 4. 修改数据库结构（可选）

如果确认迁移成功，可以执行 `alter_user_table_password_nullable.sql` 脚本，将 `user.password` 字段修改为可空（NULL）。

```sql
-- 修改 password 字段为可空
source alter_user_table_password_nullable.sql;
```

### 5. 测试应用程序

1. 启动应用程序
2. 使用现有用户登录，确认认证功能正常
3. 测试密码验证功能
4. 检查日志，确认没有错误

### 6. 清空 user.password 字段（可选，谨慎执行）

如果确认应用程序正常运行，可以清空 `user.password` 字段：

```sql
-- 清空 password 字段
UPDATE user SET password = NULL WHERE password IS NOT NULL;
```

### 7. 删除 user.password 字段（可选，更谨慎）

只有在确认完全不再需要 `user.password` 字段时，才执行此步骤：

```sql
-- 删除 password 字段
ALTER TABLE user DROP COLUMN password;
```

## 代码变更

### 新增文件

1. `JsonStringConverter.java` - JSON 字段转换器
2. `UserAuthenticationMethod.java` - 用户认证方法实体类
3. `UserAuthenticationMethodRepository.java` - 用户认证方法仓库
4. `PasswordAuthenticationProvider.java` - 密码认证提供者

### 修改文件

1. `User.java` - 将 `password` 字段标记为 `@Deprecated`
2. `UserDetailsServiceImpl.java` - 从 `UserAuthenticationMethod` 表读取密码（支持 `password`、`encodedPassword` 和 `passwordHash` 键名）
3. `SecurityConfig.java` - 使用自定义的 `PasswordAuthenticationProvider`
4. `schema.sql` - 添加 `user_authentication_method` 表，修改 `user.password` 字段为可空
5. `data.sql` - 更新初始数据，使用 `password` 键名
6. `migrate_password_to_authentication_method.sql` - 使用 `password` 键名

## 关于键名的说明

### 为什么使用 `password` 作为键名？

1. **简洁明了**：
   - 在 `authentication_configuration` 上下文中，键名 `password` 已经足够清晰
   - 更简洁、直接，符合 JSON 配置的最佳实践
   - 避免了不必要的冗长命名

2. **上下文明确**：
   - `authentication_configuration` 是 `UserAuthenticationMethod` 的配置字段
   - `authentication_type` 已经明确是 `PASSWORD`
   - 所以在配置中使用 `password` 键名是合理的

3. **向后兼容**：
   - 代码中已经支持向后兼容，可以同时支持 `password`、`encodedPassword` 和 `passwordHash` 键名
   - 代码会优先使用 `password` 键名，其他键名作为向后兼容
   - 如果使用旧键名，会输出警告日志，建议迁移到 `password`

### 支持的键名（按优先级）

1. `password` - **推荐**，简洁明了，符合 JSON 配置最佳实践
2. `encodedPassword` - 向后兼容，会输出警告日志
3. `passwordHash` - 向后兼容，会输出警告日志
4. `hash` - 向后兼容

## 回滚方案

如果迁移过程中出现问题，可以按照以下步骤回滚：

1. 恢复 `user.password` 字段的数据（如果有备份）
2. 恢复代码到迁移前的版本
3. 删除 `user_authentication_method` 表（如果不需要保留）

## 注意事项

1. **备份数据库**：在执行迁移前，请务必备份数据库
2. **测试环境**：建议先在测试环境执行迁移，确认无误后再在生产环境执行
3. **逐步迁移**：建议分步骤执行迁移，每一步都进行验证
4. **监控日志**：迁移后，密切关注应用程序日志，确保没有错误
5. **保留字段**：建议保留 `user.password` 字段一段时间，确保系统稳定运行后再删除

## 验证清单

- [ ] 数据库备份已完成
- [ ] `user_authentication_method` 表已创建
- [ ] 数据迁移脚本已执行
- [ ] 迁移结果已验证
- [ ] 应用程序已更新
- [ ] 登录功能测试通过
- [ ] 密码验证功能测试通过
- [ ] 日志检查无错误
- [ ] `user.password` 字段已修改为可空（可选）
- [ ] `user.password` 字段已清空（可选）
- [ ] `user.password` 字段已删除（可选）

## 支持

如果遇到问题，请查看：
1. 应用程序日志
2. 数据库日志
3. Spring Security 日志
4. 迁移脚本的输出

