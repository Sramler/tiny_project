# Liquibase 数据库管理重构总结

## 重构时间
2025-12-20

## 重构目标

将数据库管理从 Spring SQL 初始化迁移到 Liquibase，统一管理所有数据库变更。

---

## 已完成的修改

### 1. 修复 SQL 语法错误

#### 1.1 修复 schema.sql 中的 CHECK 约束 COMMENT 问题

**问题**: MySQL 不支持在 CHECK 约束后添加 COMMENT

**修复前**:
```sql
CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576) COMMENT '文件大小限制：最大1MB'
```

**修复后**:
```sql
CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576)
-- 注释移到表级 COMMENT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户头像表，文件大小限制：最大1MB';
```

**文件**: `tiny-oauth-server/src/main/resources/schema.sql` (第143行)

#### 1.2 修复 data.sql 中的字段名问题

**问题**: `resource` 表的 `path` 字段已迁移为 `url`，但 `data.sql` 仍使用 `path`

**修复**: 将所有 `path` 字段名改为 `url`（共11处）

**文件**: `tiny-oauth-server/src/main/resources/data.sql`

---

### 2. 创建 Liquibase Changelog 文件

#### 2.1 表结构 Changelog

**文件**: `tiny-oauth-server/src/main/resources/db/changelog/002-create-schema.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: create-schema-tables
      author: tiny
      comment: 创建所有业务表和 Quartz 表结构
      changes:
        - sqlFile:
            path: classpath:schema.sql
            relativeToChangelogFile: false
            splitStatements: true
            stripComments: false
            endDelimiter: ;
            dbms: mysql
```

#### 2.2 初始数据 Changelog

**文件**: `tiny-oauth-server/src/main/resources/db/changelog/003-insert-initial-data.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: insert-initial-data
      author: tiny
      comment: 插入初始数据（用户、角色、资源等）
      changes:
        - sqlFile:
            path: classpath:data.sql
            relativeToChangelogFile: false
            splitStatements: true
            stripComments: false
            endDelimiter: ;
            dbms: mysql
```

---

### 3. 更新 Master Changelog

**文件**: `tiny-oauth-server/src/main/resources/db/changelog/db.changelog-master.yaml`

**更新后结构**:
```yaml
databaseChangeLog:
  # 表结构创建（包含所有业务表和 Quartz 表）
  - include:
      file: db/changelog/002-create-schema.yaml
      relativeToChangelogFile: true

  # 初始数据插入
  - include:
      file: db/changelog/003-insert-initial-data.yaml
      relativeToChangelogFile: true

  # 存储过程创建
  - changeSet:
      id: create-demo-export-usage-procedure
      author: tiny
      changes:
        - sql:
            dbms: mysql
            sql: "DROP PROCEDURE IF EXISTS sp_generate_demo_export_usage"
        - sqlFile:
            path: db/changelog/001-create-demo-export-usage-procedure.sql
            relativeToChangelogFile: false
            splitStatements: false
            stripComments: false
```

**执行顺序**:
1. 创建表结构（002-create-schema.yaml）
2. 插入初始数据（003-insert-initial-data.yaml）
3. 创建存储过程（001-create-demo-export-usage-procedure.sql）

---

### 4. 禁用 Spring SQL 初始化

**文件**: `tiny-oauth-server/src/main/resources/application.yaml`

**修改前**:
```yaml
sql:
  init:
    mode: always # 总是执行数据初始化脚本
    schema-locations: classpath:schema.sql
    data-locations: classpath:data.sql
    continue-on-error: true
```

**修改后**:
```yaml
sql:
  init:
    mode: never # 禁用 Spring SQL 初始化，由 Liquibase 统一管理数据库变更
```

---

## 重构后的架构

### 数据库管理流程

```
应用启动
    ↓
Liquibase 初始化
    ↓
执行 db.changelog-master.yaml
    ↓
├─ 002-create-schema.yaml → 执行 schema.sql（创建表结构）
├─ 003-insert-initial-data.yaml → 执行 data.sql（插入初始数据）
└─ 001-create-demo-export-usage-procedure.sql → 创建存储过程
```

### 优势

1. **统一管理**: 所有数据库变更由 Liquibase 统一管理
2. **版本控制**: 每个变更都有版本记录，便于追踪和回滚
3. **环境一致性**: 确保所有环境的数据库结构一致
4. **变更追踪**: Liquibase 会记录已执行的变更，避免重复执行
5. **错误处理**: Liquibase 提供更好的错误处理和回滚机制

---

## 文件清单

### 修改的文件

1. `tiny-oauth-server/src/main/resources/schema.sql`
   - 修复 CHECK 约束 COMMENT 语法错误

2. `tiny-oauth-server/src/main/resources/data.sql`
   - 修复 `path` → `url` 字段名问题（11处）

3. `tiny-oauth-server/src/main/resources/application.yaml`
   - 禁用 Spring SQL 初始化（`mode: never`）

### 新建的文件

1. `tiny-oauth-server/src/main/resources/db/changelog/002-create-schema.yaml`
   - 表结构创建 changelog

2. `tiny-oauth-server/src/main/resources/db/changelog/003-insert-initial-data.yaml`
   - 初始数据插入 changelog

### 更新的文件

1. `tiny-oauth-server/src/main/resources/db/changelog/db.changelog-master.yaml`
   - 添加表结构和数据的 changelog 引用

---

## 验证步骤

### 1. 清理数据库（可选，用于测试）

```sql
-- 删除所有表（谨慎操作）
DROP DATABASE IF EXISTS tiny_web;
CREATE DATABASE tiny_web CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 重启应用

```bash
mvn spring-boot:run
```

### 3. 检查日志

确认以下内容：
- ✅ Liquibase 成功执行所有 changelog
- ✅ 没有 SQLSyntaxErrorException
- ✅ 所有表创建成功
- ✅ 初始数据插入成功

### 4. 验证数据库

```sql
-- 检查表是否存在
SHOW TABLES;

-- 检查 user_avatar 表结构（确认 CHECK 约束正确）
SHOW CREATE TABLE user_avatar;

-- 检查 resource 表数据（确认 url 字段正确）
SELECT id, name, url FROM resource LIMIT 5;
```

---

## 注意事项

### 1. 首次执行

- Liquibase 会在数据库中创建 `DATABASECHANGELOG` 和 `DATABASECHANGELOGLOCK` 表
- 这些表用于追踪已执行的变更

### 2. 后续变更

- 所有数据库变更都应通过 Liquibase changelog 管理
- 创建新的 changelog 文件，添加到 `db.changelog-master.yaml`
- 不要直接修改数据库结构

### 3. 回滚

如果需要回滚某个变更：
```bash
# 查看变更历史
liquibase history

# 回滚到指定变更
liquibase rollback <changeSet-id>
```

### 4. 生产环境

- 确保 Liquibase 配置正确
- 建议在生产环境首次部署前，先在测试环境验证
- 备份数据库后再执行变更

---

## 后续优化建议

1. **拆分大型 Changelog**
   - 如果 schema.sql 太大，可以拆分成多个 changelog 文件
   - 按模块组织（用户模块、调度模块、Quartz 模块等）

2. **使用 Liquibase 原生语法**
   - 逐步将 SQL 文件转换为 Liquibase 原生语法（createTable、addColumn 等）
   - 更好的跨数据库兼容性

3. **添加变更前验证**
   - 在 changelog 中添加 preConditions，确保变更条件满足

4. **文档化变更**
   - 为每个 changeSet 添加详细的注释
   - 说明变更原因和影响范围

---

## 总结

✅ **已完成**:
- 修复 SQL 语法错误（CHECK 约束、字段名）
- 创建 Liquibase changelog 文件
- 更新 master changelog
- 禁用 Spring SQL 初始化

✅ **优势**:
- 统一数据库管理
- 版本控制和变更追踪
- 更好的错误处理
- 环境一致性保证

🎯 **下一步**:
- 重启应用验证
- 检查数据库结构
- 确认功能正常

