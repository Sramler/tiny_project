# 数据库同步修复记录

## 修复时间
2025-12-20

## 已修复的差异

### 1. user 表时间字段命名 ✅

**问题**: schema.sql 中使用 `created_at`/`updated_at`，但数据库中实际是 `create_time`/`update_time`

**修复**: 更新 schema.sql 中的字段名
- `created_at` → `create_time`
- `updated_at` → `update_time`
- 同时将类型从 `TIMESTAMP` 改为 `DATETIME`（与数据库一致）

**文件**: `tiny-oauth-server/src/main/resources/schema.sql` (第17-18行)

---

### 2. resource 表添加 enabled 字段 ✅

**问题**: 数据库中有 `enabled` 字段，但 schema.sql 中没有定义

**修复**: 在 resource 表定义中添加 `enabled` 字段
```sql
`enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用'
```

**文件**: `tiny-oauth-server/src/main/resources/schema.sql` (第69行)

---

## 验证结果

### 已确认正确的部分

1. ✅ **user 表没有 password 字段** - 已迁移到 user_authentication_method
2. ✅ **resource 表使用 url 字段** - 不是 path
3. ✅ **user_avatar 表的 CHECK 约束** - 已修复 COMMENT 问题

---

## 下一步

1. **重新验证**: 运行同步脚本确认差异已消除
2. **测试 Liquibase**: 确保 Liquibase 可以正常执行
3. **更新文档**: 记录所有修复

---

## 注意事项

- 修复后的 schema.sql 现在与当前数据库结构一致
- 如果数据库中有其他表（如 Camunda、OAuth2 表），它们由框架自动管理，不需要在 schema.sql 中定义
- 建议定期执行同步检查，确保 schema.sql 与实际数据库保持一致

