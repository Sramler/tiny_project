# data.sql 重复插入问题修复

## 修复时间
2025-12-20

## 问题描述

启动应用时，Liquibase 执行 `data.sql` 报错：
```
Duplicate entry 'admin' for key 'user.username'
```

## 根本原因

数据库中已经存在初始数据（admin、user 等），但 Liquibase 每次启动都会尝试执行 `data.sql`，导致重复插入数据，触发唯一约束冲突。

## 修复方案

在所有 INSERT 语句中使用 `INSERT IGNORE`，避免重复插入数据。

### 修复内容

1. **user 表**: `INSERT` → `INSERT IGNORE`
2. **role 表**: `INSERT` → `INSERT IGNORE`
3. **user_role 表**: `INSERT` → `INSERT IGNORE`
4. **resource 表**: `INSERT` → `INSERT IGNORE`（所有资源插入语句）
5. **role_resource 表**: `INSERT` → `INSERT IGNORE`
6. **user_authentication_method 表**: `INSERT` → `INSERT IGNORE` + 添加 `NOT EXISTS` 条件
7. **demo_export_usage 表**: `INSERT` → `INSERT IGNORE`

### 特殊处理

#### 1. user_authentication_method 表

由于使用了 `INSERT ... SELECT`，除了使用 `INSERT IGNORE`，还添加了 `NOT EXISTS` 条件：

```sql
INSERT IGNORE INTO `user_authentication_method` 
    ...
FROM user u
WHERE u.username IN ('admin', 'user')
  AND NOT EXISTS (
      SELECT 1 FROM user_authentication_method uam 
      WHERE uam.user_id = u.id 
        AND uam.authentication_provider = 'LOCAL' 
        AND uam.authentication_type = 'PASSWORD'
  );
```

#### 2. 调度中心资源（使用 LAST_INSERT_ID）

由于调度中心资源使用了 `LAST_INSERT_ID()`，需要改为使用 `SELECT` 查询：

```sql
-- 修复前
SET @scheduling_dir_id = LAST_INSERT_ID();

-- 修复后
SET @scheduling_dir_id = (SELECT id FROM `resource` WHERE `name` = 'scheduling' LIMIT 1);
```

**原因**: `INSERT IGNORE` 在数据已存在时不会插入，`LAST_INSERT_ID()` 会返回 0 或上次插入的 ID，导致后续插入的 `parent_id` 错误。

## INSERT IGNORE 说明

`INSERT IGNORE` 的作用：
- 如果插入的数据违反唯一约束或主键约束，**忽略错误**，不插入数据
- 如果插入的数据不违反约束，**正常插入**
- 适用于：初始数据插入，允许数据已存在的情况

## 修复后的效果

- ✅ 数据库已存在数据时，不会报错
- ✅ 数据库不存在数据时，正常插入
- ✅ 可以安全地多次执行 `data.sql`
- ✅ 保持数据一致性

## 文件位置

- `tiny-oauth-server/src/main/resources/data.sql`

## 验证

修复后，Liquibase 应该能够成功执行 `data.sql`，即使数据库中已存在初始数据也不会报错。

