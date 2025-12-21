# java.lang.Exception 异常分析报告

## 分析时间

2025-12-20

## 概述

日志中出现的 `java.lang.Exception` 主要来源于**数据库初始化脚本执行失败**，这些异常发生在应用启动阶段，但不会阻止应用启动（因为配置了 `continue-on-error: true`）。

## ⚠️ 重要发现：配置冲突

**问题**: 项目中同时启用了两种数据库管理方式，导致冲突：

1. **Liquibase** (已启用): `liquibase.enabled: true`

   - 当前只管理存储过程 (`sp_generate_demo_export_usage`)
   - 未管理表结构和初始数据

2. **Spring SQL 初始化** (已启用): `spring.sql.init.mode: always`
   - 管理所有表结构 (`schema.sql` - 19 个表)
   - 管理初始数据 (`data.sql`)

**当前状态**:

- Liquibase 和 Spring SQL 初始化同时执行
- Liquibase 执行存储过程创建
- Spring SQL 初始化执行表结构和数据初始化
- 异常日志中的 SQL 错误来自 Spring SQL 初始化

**发现**:

- 存在迁移脚本 `migration_path_to_url.sql`，显示 `resource` 表的 `path` 字段已重命名为 `url`
- 但 `data.sql` 中仍使用 `path` 字段，导致 `Unknown column 'path'` 错误

**建议**:

1. **短期**: 修复 Spring SQL 初始化脚本中的错误
2. **长期**: 将表结构和数据迁移到 Liquibase，统一管理数据库变更

---

## 异常类型汇总

### 1. SQLSyntaxErrorException: CHECK 约束 COMMENT 语法错误

**异常类型**: `java.sql.SQLSyntaxErrorException`  
**出现时间**: `2025-12-20 22:45:01.512`  
**出现次数**: 2 次  
**严重程度**: ⚠️ 中等（表创建失败，但应用仍可启动）

#### 异常信息

```
You have an error in your SQL syntax; check the manual that corresponds to your MySQL server version
for the right syntax to use near 'COMMENT '文件大小限制：最大1MB' ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4' at line 1
```

#### 问题 SQL

```sql
CREATE TABLE IF NOT EXISTS `user_avatar` (
  ...
  CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576) COMMENT '文件大小限制：最大1MB'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```

#### 根本原因

**MySQL 版本限制**:

- MySQL 8.0.16+ 才支持 CHECK 约束
- **CHECK 约束不支持 COMMENT 子句**
- 即使 MySQL 8.0.16+ 支持 CHECK 约束，也不能在约束定义后添加 COMMENT

#### 异常堆栈

```
java.sql.SQLSyntaxErrorException: ...
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(...)
	at com.mysql.cj.jdbc.StatementImpl.executeInternal(...)
	at org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(...)
	at org.springframework.boot.sql.init.AbstractScriptDatabaseInitializer.initializeDatabase(...)
```

#### 影响

- ⚠️ **表创建失败**: `user_avatar` 表未能创建
- ✅ **应用仍可启动**: 由于配置了 `continue-on-error: true`
- ⚠️ **功能受影响**: 如果代码尝试访问 `user_avatar` 表，会抛出表不存在异常

#### 解决方案

**修复 SQL 语法**（移除 CHECK 约束的 COMMENT）:

```sql
-- 错误的写法（当前）
CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576) COMMENT '文件大小限制：最大1MB'

-- 正确的写法（方案 1：移除 COMMENT）
CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576)

-- 正确的写法（方案 2：使用表级 COMMENT）
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户头像表，文件大小限制：最大1MB'
```

**文件位置**: `tiny-oauth-server/src/main/resources/schema.sql`

---

### 2. SQLSyntaxErrorException: Unknown column 'path'

**异常类型**: `java.sql.SQLSyntaxErrorException`  
**出现时间**: `2025-12-20 22:45:01.551`  
**出现次数**: 10+ 次  
**严重程度**: ⚠️ 中等（数据插入失败，但应用仍可启动）

#### 异常信息

```
Unknown column 'path' in 'field list'
```

#### 问题 SQL

```sql
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES ('system', '/system', '', '', 'SettingOutlined', 1, 1, '', '', 0, 0, '系统管理', 'system', 0, NULL)
```

#### 根本原因

**表结构不匹配**:

- `data.sql` 脚本尝试向 `resource` 表插入数据
- 插入语句包含 `path` 列
- 但 `resource` 表的实际结构中 `path` 列已被重命名为 `url`
- 存在迁移脚本 `migration_path_to_url.sql` 记录了此次迁移
- `data.sql` 未同步更新，仍使用旧的 `path` 字段名

#### 异常堆栈

```
java.sql.SQLSyntaxErrorException: Unknown column 'path' in 'field list'
	at com.mysql.cj.jdbc.exceptions.SQLError.createSQLException(...)
	at com.mysql.cj.jdbc.StatementImpl.executeInternal(...)
	at org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(...)
	at org.springframework.boot.sql.init.AbstractScriptDatabaseInitializer.applyDataScripts(...)
```

#### 影响

- ⚠️ **数据插入失败**: `resource` 表的初始数据未能插入
- ✅ **应用仍可启动**: 由于配置了 `continue-on-error: true`
- ⚠️ **功能受影响**: 如果应用依赖这些初始数据，相关功能可能无法正常工作

#### 解决方案

**方案 1: 修复 data.sql（推荐）**

检查 `resource` 表的实际结构，移除或修改 `path` 列：

```sql
-- 检查表结构
DESC resource;
-- 或
SHOW CREATE TABLE resource;

-- 根据实际表结构修改 INSERT 语句
-- 如果 path 列不存在，移除它：
INSERT INTO `resource` (`name`, `uri`, `method`, ...) VALUES (...)
```

**方案 2: 修复表结构**

如果 `path` 列应该存在，添加该列：

```sql
ALTER TABLE `resource` ADD COLUMN `path` VARCHAR(255) NULL COMMENT '路径';
```

**文件位置**: `tiny-oauth-server/src/main/resources/data.sql`

---

## 异常传播路径

### 异常捕获机制

这些异常**不会被** `GlobalExceptionHandler` 捕获，因为：

1. **发生时机**: 异常发生在应用启动阶段（`@PostConstruct` / `afterPropertiesSet`）
2. **处理位置**: Spring Boot 的 `AbstractScriptDatabaseInitializer` 内部处理
3. **日志记录**: 异常被记录为 DEBUG 级别，但不会抛出到应用层

### GlobalExceptionHandler 的作用范围

`GlobalExceptionHandler` 中的 `@ExceptionHandler(Exception.class)` 方法**只处理**：

- HTTP 请求处理过程中抛出的异常
- Controller 层抛出的异常
- 业务逻辑层抛出的异常

**不处理**：

- 应用启动阶段的异常
- 数据库初始化脚本执行失败
- Bean 初始化失败

---

## 配置说明

### continue-on-error 配置

在 `application.yaml` 中配置了：

```yaml
spring:
  sql:
    init:
      continue-on-error: true # 遇到错误继续执行
```

**作用**:

- ✅ 允许数据库初始化脚本部分失败
- ✅ 应用仍可正常启动
- ⚠️ 但可能导致部分功能不可用

**建议**:

- 开发环境可以保持 `true`，便于快速启动
- 生产环境应该设为 `false`，确保数据库初始化完整

---

## 异常统计

| 异常类型                                        | 出现次数 | 严重程度 | 影响范围                  | 是否需要修复 |
| ----------------------------------------------- | -------- | -------- | ------------------------- | ------------ |
| SQLSyntaxErrorException (CHECK COMMENT)         | 2        | ⚠️ 中等  | `user_avatar` 表创建失败  | ✅ 是        |
| SQLSyntaxErrorException (Unknown column 'path') | 10+      | ⚠️ 中等  | `resource` 表数据插入失败 | ✅ 是        |

---

## 修复建议

### 方案 1: 禁用 Spring SQL 初始化，完全使用 Liquibase（推荐）

**步骤**:

1. **禁用 Spring SQL 初始化**

   ```yaml
   # application.yaml
   spring:
     sql:
       init:
         mode: never # 禁用 Spring SQL 初始化
   ```

2. **将 schema.sql 和 data.sql 迁移到 Liquibase**

   - 创建新的 changelog 文件，包含所有表结构
   - 创建数据初始化 changelog，包含初始数据
   - 更新 `db.changelog-master.yaml` 引用新的 changelog

3. **修复 SQL 语法错误**
   - 在 Liquibase changelog 中修复 CHECK 约束语法
   - 修复 `resource` 表的 `path` 列问题

### 方案 2: 禁用 Liquibase，使用 Spring SQL 初始化

**步骤**:

1. **禁用 Liquibase**

   ```yaml
   # application.yaml
   liquibase:
     enabled: false
   ```

2. **修复 schema.sql 和 data.sql**
   - 修复 CHECK 约束语法错误
   - 修复 `resource` 表的 `path` 列问题

### 立即修复（高优先级）

如果暂时保留 Spring SQL 初始化，需要修复：

1. **修复 schema.sql 中的 CHECK 约束语法**

   - 文件: `tiny-oauth-server/src/main/resources/schema.sql`
   - 移除 CHECK 约束后的 COMMENT

2. **修复 data.sql 中的列名问题**
   - 文件: `tiny-oauth-server/src/main/resources/data.sql`
   - 将 `path` 字段名改为 `url`（根据 `migration_path_to_url.sql` 迁移脚本）
   - 或检查 `resource` 表的实际结构，使用正确的字段名

### 长期优化

1. **统一数据库管理方式**

   - ✅ **推荐**: 使用 Liquibase 统一管理所有数据库变更
   - 将 schema.sql 和 data.sql 迁移到 Liquibase changelog
   - 禁用 Spring SQL 初始化

2. **启动验证**

   - 添加启动后健康检查
   - 验证关键表是否存在
   - 验证关键数据是否已初始化

3. **环境配置**
   - 开发环境: `continue-on-error: true`（如果使用 Spring SQL 初始化）
   - 生产环境: `continue-on-error: false`（如果使用 Spring SQL 初始化）
   - Liquibase 会自动处理变更，无需此配置

---

## 验证步骤

修复后，验证步骤：

1. **清理数据库**（可选）:

   ```sql
   DROP TABLE IF EXISTS user_avatar;
   DELETE FROM resource;
   ```

2. **重启应用**:

   ```bash
   mvn spring-boot:run
   ```

3. **检查日志**:

   - 确认没有 SQLSyntaxErrorException
   - 确认表创建成功
   - 确认数据插入成功

4. **验证功能**:
   - 测试用户头像上传功能
   - 测试资源管理功能

---

## 相关文件

- SQL 脚本: `tiny-oauth-server/src/main/resources/schema.sql`
- 数据脚本: `tiny-oauth-server/src/main/resources/data.sql`
- 应用配置: `tiny-oauth-server/src/main/resources/application.yaml`
- 异常处理器: `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/controller/GlobalExceptionHandler.java`

---

## 总结

日志中出现的 `java.lang.Exception` 主要来源于：

1. **数据库初始化脚本执行失败**

   - CHECK 约束语法错误（MySQL 不支持 CHECK 约束的 COMMENT）
   - 表结构不匹配（`resource` 表缺少 `path` 列）

2. **异常处理机制**

   - 异常被 Spring Boot 内部捕获
   - 由于 `continue-on-error: true`，应用仍可启动
   - 不会被 `GlobalExceptionHandler` 捕获（发生在启动阶段）

3. **影响**
   - 部分表和数据未正确初始化
   - 相关功能可能不可用
   - 需要修复 SQL 脚本以确保完整初始化
