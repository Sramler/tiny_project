# Liquibase 路径配置修复

## 问题描述

启动应用时，Liquibase 报错：
```
The file db/changelog/db/changelog/002-create-schema.yaml was not found
```

## 根本原因

在 `db.changelog-master.yaml` 中，当使用 `relativeToChangelogFile: true` 时，路径是相对于当前 changelog 文件的。

由于 master changelog 文件本身就在 `db/changelog/` 目录下，所以：
- ❌ 错误路径: `db/changelog/002-create-schema.yaml` → 解析为 `db/changelog/db/changelog/002-create-schema.yaml`
- ✅ 正确路径: `002-create-schema.yaml` → 解析为 `db/changelog/002-create-schema.yaml`

## 修复方案

### 修复前
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/002-create-schema.yaml
      relativeToChangelogFile: true
```

### 修复后
```yaml
databaseChangeLog:
  - include:
      file: 002-create-schema.yaml
      relativeToChangelogFile: true
```

## 修复内容

1. **修复 include 路径**
   - `002-create-schema.yaml` - 使用相对路径
   - `003-insert-initial-data.yaml` - 使用相对路径

2. **修复 sqlFile 路径**
   - `001-create-demo-export-usage-procedure.sql` - 使用 `classpath:` 前缀

## 路径配置规则

### relativeToChangelogFile: true
- 路径相对于当前 changelog 文件所在目录
- 示例：如果 master changelog 在 `db/changelog/`，则 `002-create-schema.yaml` 指向同目录下的文件

### relativeToChangelogFile: false
- 路径相对于 classpath 根目录
- 需要使用 `classpath:` 前缀
- 示例：`classpath:db/changelog/001-create-demo-export-usage-procedure.sql`

## 验证

修复后，Liquibase 应该能够正确找到所有 changelog 文件：
- ✅ `002-create-schema.yaml`
- ✅ `003-insert-initial-data.yaml`
- ✅ `001-create-demo-export-usage-procedure.sql`

## 文件位置

- Master changelog: `tiny-oauth-server/src/main/resources/db/changelog/db.changelog-master.yaml`
- Schema changelog: `tiny-oauth-server/src/main/resources/db/changelog/002-create-schema.yaml`
- Data changelog: `tiny-oauth-server/src/main/resources/db/changelog/003-insert-initial-data.yaml`

