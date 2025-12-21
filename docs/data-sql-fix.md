# data.sql 修复记录

## 修复时间
2025-12-20

## 问题描述

启动应用时，Liquibase 执行 `data.sql` 报错：
```
Field 'failed_login_count' doesn't have a default value
```

## 根本原因

在 `data.sql` 的 INSERT 语句中，没有包含 `failed_login_count` 字段，但该字段在数据库中是 `NOT NULL` 且没有默认值。

### 数据库中的字段定义
```sql
failed_login_count INT NOT NULL  -- 没有默认值
```

### schema.sql 中的定义
```sql
failed_login_count INT NOT NULL DEFAULT 0  -- 有默认值
```

**注意**: schema.sql 中定义了默认值，但当前数据库中的表可能是在之前创建的，没有默认值。

## 修复方案

在 `data.sql` 的 INSERT 语句中显式包含 `failed_login_count` 字段。

### 修复前
```sql
INSERT INTO `user` (`username`, `nickname`, `enabled`, `account_non_expired`, `account_non_locked`, `credentials_non_expired`) VALUES
('admin', '管理员', true, true, true, true),
('user', '普通用户', true, true, true, true);
```

### 修复后
```sql
INSERT INTO `user` (`username`, `nickname`, `enabled`, `account_non_expired`, `account_non_locked`, `credentials_non_expired`, `failed_login_count`) VALUES
('admin', '管理员', true, true, true, true, 0),
('user', '普通用户', true, true, true, true, 0);
```

## 修复内容

- ✅ 在 INSERT 语句中添加了 `failed_login_count` 字段
- ✅ 为两个用户都设置了默认值 `0`

## 文件位置

- `tiny-oauth-server/src/main/resources/data.sql` (第2-4行)

## 验证

修复后，Liquibase 应该能够成功执行 `data.sql`，插入初始用户数据。

## 后续建议

1. **统一字段定义**: 确保 schema.sql 中的字段定义与数据库一致
2. **添加默认值**: 如果可能，为数据库中的 `failed_login_count` 字段添加默认值
3. **验证其他表**: 检查其他表的 INSERT 语句是否也有类似问题

