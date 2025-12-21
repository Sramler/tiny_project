# 数据库同步和 Diff 处理指南

## 问题背景

在将数据库管理迁移到 Liquibase 后，需要确保：
1. **schema.sql** 与实际数据库表结构一致
2. **data.sql** 与实际数据库初始数据一致
3. 所有历史迁移脚本都已反映在 schema.sql 中

---

## 当前状态分析

### 已知的迁移脚本

1. **migration_path_to_url.sql** (2024-06-27)
   - 将 `resource` 表的 `path` 字段重命名为 `url`
   - ✅ 已在 data.sql 中修复（`path` → `url`）
   - ✅ schema.sql 中已使用 `url` 字段

2. **complete_migration_password_field.sql**
   - 将 `user.password` 迁移到 `user_authentication_method`
   - ⚠️ 需要确认 schema.sql 中 `user` 表是否还包含 `password` 字段

3. **migrate_password_to_authentication_method.sql**
   - 密码字段迁移脚本
   - ⚠️ 需要确认是否已反映在 schema.sql 中

---

## 同步和 Diff 方案

### 方案 1: 使用 mysqldump 导出当前数据库结构（推荐）

#### 步骤 1: 导出表结构

```bash
# 导出表结构（不包含数据）
mysqldump -u root -p \
  --no-data \
  --skip-triggers \
  --skip-routines \
  --skip-events \
  --single-transaction \
  --routines=false \
  tiny_web > current_schema.sql

# 清理导出文件（移除数据库创建语句、USE 语句等）
sed -i '' '/^CREATE DATABASE/d' current_schema.sql
sed -i '' '/^USE /d' current_schema.sql
sed -i '' '/^\/\*!40101 SET/d' current_schema.sql
sed -i '' '/^\/\*!40000 SET/d' current_schema.sql
sed -i '' '/^\/\*!50001 SET/d' current_schema.sql
```

#### 步骤 2: 对比差异

```bash
# 使用 diff 工具对比
diff -u schema.sql current_schema.sql > schema_diff.txt

# 或使用更友好的工具
# macOS: 使用 opendiff
opendiff schema.sql current_schema.sql

# Linux: 使用 meld 或 vimdiff
meld schema.sql current_schema.sql
```

#### 步骤 3: 导出初始数据

```bash
# 导出特定表的初始数据（仅基础数据，不包含业务数据）
mysqldump -u root -p \
  --no-create-info \
  --skip-triggers \
  --single-transaction \
  tiny_web \
  user role user_role resource role_resource user_authentication_method \
  > current_data.sql
```

---

### 方案 2: 使用 SQL 查询检查差异

#### 检查表结构差异

```sql
-- 1. 列出所有表
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'tiny_web' 
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

-- 2. 检查特定表的结构
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT,
    EXTRA
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'tiny_web' 
  AND TABLE_NAME = 'user'
ORDER BY ORDINAL_POSITION;

-- 3. 检查索引
SELECT 
    INDEX_NAME,
    COLUMN_NAME,
    NON_UNIQUE,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'tiny_web' 
  AND TABLE_NAME = 'user'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;

-- 4. 检查外键
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'tiny_web' 
  AND TABLE_NAME = 'user_role'
  AND REFERENCED_TABLE_NAME IS NOT NULL;
```

#### 检查数据差异

```sql
-- 检查初始数据是否存在
SELECT COUNT(*) as user_count FROM user;
SELECT COUNT(*) as role_count FROM role;
SELECT COUNT(*) as resource_count FROM resource;
SELECT COUNT(*) as user_role_count FROM user_role;
SELECT COUNT(*) as role_resource_count FROM role_resource;
SELECT COUNT(*) as user_auth_method_count FROM user_authentication_method;

-- 检查关键数据
SELECT id, username, nickname FROM user ORDER BY id;
SELECT id, code, name FROM role ORDER BY id;
SELECT id, name, url FROM resource ORDER BY id LIMIT 10;
```

---

### 方案 3: 使用 Liquibase diffChangelog（推荐用于后续维护）

Liquibase 提供了 `diffChangelog` 命令，可以自动生成差异 changelog：

```bash
# 安装 Liquibase CLI（如果未安装）
# macOS: brew install liquibase
# 或下载: https://www.liquibase.org/download

# 生成差异 changelog
liquibase \
  --changeLogFile=db/changelog/004-diff-from-current.yaml \
  --url=jdbc:mysql://localhost:3306/tiny_web \
  --username=root \
  --password=Tianye0903. \
  --referenceUrl=jdbc:mysql://localhost:3306/tiny_web \
  --referenceUsername=root \
  --referencePassword=Tianye0903. \
  diffChangelog
```

**注意**: 这个命令需要配置 `liquibase.properties` 文件。

---

## 具体操作步骤

### 步骤 1: 导出当前数据库结构

创建导出脚本：

```bash
#!/bin/bash
# scripts/export-current-schema.sh

DB_NAME="tiny_web"
DB_USER="root"
DB_PASS="Tianye0903."
OUTPUT_DIR="docs/database-sync"

mkdir -p $OUTPUT_DIR

# 导出表结构
mysqldump -u $DB_USER -p$DB_PASS \
  --no-data \
  --skip-triggers \
  --skip-routines \
  --skip-events \
  --single-transaction \
  --routines=false \
  --compact \
  $DB_NAME > $OUTPUT_DIR/current_schema.sql

# 清理导出文件
sed -i '' '/^CREATE DATABASE/d' $OUTPUT_DIR/current_schema.sql
sed -i '' '/^USE /d' $OUTPUT_DIR/current_schema.sql
sed -i '' '/^\/\*!40/d' $OUTPUT_DIR/current_schema.sql
sed -i '' '/^\/\*!50/d' $OUTPUT_DIR/current_schema.sql

echo "Schema exported to $OUTPUT_DIR/current_schema.sql"
```

### 步骤 2: 对比差异

```bash
# 生成差异报告
diff -u \
  tiny-oauth-server/src/main/resources/schema.sql \
  docs/database-sync/current_schema.sql \
  > docs/database-sync/schema_diff.txt

# 查看差异
cat docs/database-sync/schema_diff.txt
```

### 步骤 3: 处理差异

根据差异报告，需要处理的情况：

#### 情况 1: schema.sql 缺少表

如果当前数据库有表但 schema.sql 中没有：
- 将表结构添加到 schema.sql
- 创建新的 Liquibase changelog 文件

#### 情况 2: schema.sql 有表但数据库中没有

如果 schema.sql 有表但数据库中没有：
- 这是正常的，Liquibase 会创建这些表
- 确保 schema.sql 是正确的

#### 情况 3: 表结构不一致

如果表结构不一致：
- 分析差异原因
- 如果是历史迁移导致的，需要：
  1. 将迁移脚本转换为 Liquibase changelog
  2. 更新 schema.sql 以反映最新结构

---

## 迁移脚本处理

### 需要处理的迁移脚本

1. **migration_path_to_url.sql**
   - ✅ 已处理：data.sql 中已使用 `url` 字段
   - ✅ schema.sql 中已使用 `url` 字段
   - ⚠️ 需要：创建 Liquibase changelog 记录此次迁移

2. **complete_migration_password_field.sql**
   - ⚠️ 需要检查：schema.sql 中 `user` 表是否还包含 `password` 字段
   - ⚠️ 需要：创建 Liquibase changelog 记录此次迁移

3. **migrate_password_to_authentication_method.sql**
   - ⚠️ 需要检查：是否已反映在 schema.sql 中

### 创建迁移 Changelog

对于历史迁移脚本，应该创建对应的 Liquibase changelog：

```yaml
# db/changelog/004-migrate-path-to-url.yaml
databaseChangeLog:
  - changeSet:
      id: migrate-resource-path-to-url
      author: tiny
      comment: 将 resource 表的 path 字段重命名为 url
      changes:
        - renameColumn:
            tableName: resource
            oldColumnName: path
            newColumnName: url
            columnDataType: VARCHAR(200)
            remarks: 前端路由路径
```

---

## 推荐的同步流程

### 1. 立即执行（首次同步）

```bash
# 1. 导出当前数据库结构
./scripts/export-current-schema.sh

# 2. 对比差异
diff -u schema.sql docs/database-sync/current_schema.sql > schema_diff.txt

# 3. 分析差异
# - 检查是否有表缺失
# - 检查是否有字段差异
# - 检查是否有索引差异

# 4. 更新 schema.sql
# - 根据差异更新 schema.sql
# - 确保所有历史迁移都已反映

# 5. 验证
# - 重新导出数据库结构
# - 再次对比，确保一致
```

### 2. 创建差异 Changelog

对于发现的差异，创建对应的 Liquibase changelog：

```yaml
# db/changelog/005-sync-with-current-database.yaml
databaseChangeLog:
  - changeSet:
      id: sync-table-structure-2024-12-20
      author: tiny
      comment: 同步表结构与当前数据库一致
      changes:
        # 添加缺失的字段
        - addColumn:
            tableName: user
            columns:
              - column:
                  name: new_field
                  type: VARCHAR(100)
                  remarks: 新字段说明
        # 修改字段类型
        - modifyDataType:
            tableName: user
            columnName: existing_field
            newDataType: VARCHAR(200)
```

### 3. 更新 Master Changelog

```yaml
# db.changelog-master.yaml
databaseChangeLog:
  # ... 现有 changelog ...
  
  # 历史迁移脚本（按时间顺序）
  - include:
      file: db/changelog/004-migrate-path-to-url.yaml
      relativeToChangelogFile: true
  
  - include:
      file: db/changelog/005-migrate-password-field.yaml
      relativeToChangelogFile: true
  
  # 同步差异
  - include:
      file: db/changelog/006-sync-with-current-database.yaml
      relativeToChangelogFile: true
```

---

## 自动化脚本

创建自动化同步脚本：

```bash
#!/bin/bash
# scripts/sync-database-schema.sh

set -e

DB_NAME="tiny_web"
DB_USER="root"
DB_PASS="Tianye0903."
SCHEMA_FILE="tiny-oauth-server/src/main/resources/schema.sql"
OUTPUT_DIR="docs/database-sync"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=== 数据库 Schema 同步工具 ==="
echo "时间: $(date)"
echo ""

# 创建输出目录
mkdir -p $OUTPUT_DIR

# 1. 导出当前数据库结构
echo "1. 导出当前数据库结构..."
mysqldump -u $DB_USER -p$DB_PASS \
  --no-data \
  --skip-triggers \
  --skip-routines \
  --skip-events \
  --single-transaction \
  --routines=false \
  --compact \
  $DB_NAME > $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql

# 清理导出文件
sed -i '' '/^CREATE DATABASE/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^USE /d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^\/\*!40/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql
sed -i '' '/^\/\*!50/d' $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql

echo "   ✓ 导出完成: $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql"
echo ""

# 2. 对比差异
echo "2. 对比差异..."
diff -u $SCHEMA_FILE $OUTPUT_DIR/current_schema_${TIMESTAMP}.sql > $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt || true

if [ -s $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt ]; then
    echo "   ⚠️  发现差异，请查看: $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt"
    echo ""
    echo "   差异摘要:"
    grep "^[+-]" $OUTPUT_DIR/schema_diff_${TIMESTAMP}.txt | head -20
else
    echo "   ✓ 无差异，schema.sql 与当前数据库一致"
fi

echo ""
echo "=== 完成 ==="
```

---

## 检查清单

### Schema 一致性检查

- [ ] 导出当前数据库结构
- [ ] 对比 schema.sql 与当前数据库结构
- [ ] 检查所有表是否存在
- [ ] 检查所有字段是否一致
- [ ] 检查所有索引是否一致
- [ ] 检查所有外键是否一致
- [ ] 检查所有约束是否一致

### 数据一致性检查

- [ ] 导出初始数据
- [ ] 对比 data.sql 与当前数据库数据
- [ ] 检查基础用户数据（admin, user）
- [ ] 检查基础角色数据（ROLE_ADMIN, ROLE_USER）
- [ ] 检查基础资源数据
- [ ] 检查关联关系数据

### 迁移脚本检查

- [ ] migration_path_to_url.sql → 已反映在 schema.sql
- [ ] complete_migration_password_field.sql → 已反映在 schema.sql
- [ ] migrate_password_to_authentication_method.sql → 已反映在 schema.sql
- [ ] 所有迁移脚本都已转换为 Liquibase changelog

---

## 建议

### 短期（立即执行）

1. **导出当前数据库结构**
   ```bash
   mysqldump -u root -p --no-data tiny_web > current_schema.sql
   ```

2. **对比差异**
   ```bash
   diff -u schema.sql current_schema.sql > schema_diff.txt
   ```

3. **分析差异并更新**
   - 根据差异更新 schema.sql
   - 创建对应的 Liquibase changelog 记录差异

### 长期（持续维护）

1. **建立同步流程**
   - 每次数据库变更后，同步更新 schema.sql
   - 通过 Liquibase changelog 管理所有变更

2. **定期检查**
   - 每月或每个版本发布前，执行一次同步检查
   - 确保 schema.sql 与实际数据库一致

3. **自动化**
   - 使用 CI/CD 流程自动检查
   - 在部署前验证数据库结构一致性

---

## 总结

✅ **应该执行同步和 diff**，原因：

1. **确保一致性**: schema.sql 和 data.sql 应该反映当前数据库的实际状态
2. **避免冲突**: 防止 Liquibase 执行时与现有数据库结构冲突
3. **历史追踪**: 通过 diff 可以发现遗漏的迁移脚本
4. **文档化**: schema.sql 作为数据库结构的文档，应该准确

⚠️ **注意事项**:

1. **备份数据库**: 执行任何操作前先备份
2. **测试环境验证**: 先在测试环境验证同步结果
3. **逐步迁移**: 不要一次性修改所有内容，逐步处理差异
4. **记录变更**: 所有差异都应该通过 Liquibase changelog 记录

