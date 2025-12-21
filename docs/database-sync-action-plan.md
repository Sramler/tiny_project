# 数据库同步行动计划

## 目标

确保 `schema.sql` 和 `data.sql` 与当前 `tiny_web` 数据库完全一致，为 Liquibase 迁移做好准备。

---

## 执行步骤

### 步骤 1: 导出当前数据库结构

```bash
# 方式 1: 使用提供的脚本（推荐）
./scripts/sync-database-schema.sh

# 方式 2: 手动导出
mysqldump -u root -p \
  --no-data \
  --skip-triggers \
  --skip-routines \
  --skip-events \
  --single-transaction \
  --routines=false \
  --compact \
  tiny_web > docs/database-sync/current_schema.sql
```

### 步骤 2: 检查数据库结构

```bash
# 执行 SQL 检查脚本
mysql -u root -p tiny_web < scripts/check-database-structure.sql > docs/database-sync/structure_check.txt

# 查看检查结果
cat docs/database-sync/structure_check.txt
```

### 步骤 3: 对比差异

```bash
# 查看差异报告
cat docs/database-sync/schema_diff_*.txt

# 或使用可视化工具
opendiff tiny-oauth-server/src/main/resources/schema.sql docs/database-sync/current_schema_*.sql
```

### 步骤 4: 处理差异

根据差异报告，需要处理的情况：

#### 情况 A: schema.sql 缺少表或字段

**操作**:
1. 从 `current_schema.sql` 中提取缺失的表/字段定义
2. 添加到 `schema.sql` 的适当位置
3. 创建 Liquibase changelog 记录此次添加

#### 情况 B: schema.sql 有表但数据库中没有

**操作**:
1. 这是正常的，Liquibase 会创建这些表
2. 确保 schema.sql 中的定义是正确的
3. 无需额外操作

#### 情况 C: 字段类型或约束不一致

**操作**:
1. 分析差异原因（可能是历史迁移导致）
2. 更新 schema.sql 以反映最新结构
3. 创建 Liquibase changelog 记录此次变更

---

## 重点关注项

### 1. user 表的 password 字段

**检查**: schema.sql 中 `user` 表是否还包含 `password` 字段

**预期**: 不应该包含（已迁移到 `user_authentication_method` 表）

**如果发现差异**:
- 从 schema.sql 中移除 `password` 字段
- 创建 Liquibase changelog 记录此次移除

### 2. resource 表的字段名

**检查**: 当前数据库中是 `path` 还是 `url`

**预期**: 应该是 `url`（已迁移）

**如果发现差异**:
- 更新 schema.sql 使用 `url`
- 创建 Liquibase changelog 记录此次重命名

### 3. user_avatar 表的 CHECK 约束

**检查**: CHECK 约束是否正确（不应包含 COMMENT）

**预期**: `CONSTRAINT chk_file_size CHECK (file_size <= 1048576)`

**如果发现差异**:
- 更新 schema.sql 移除 COMMENT
- ✅ 已完成修复

---

## 创建差异 Changelog

对于发现的每个差异，创建对应的 Liquibase changelog：

### 示例 1: 移除 password 字段

```yaml
# db/changelog/004-remove-user-password-field.yaml
databaseChangeLog:
  - changeSet:
      id: remove-user-password-field
      author: tiny
      comment: 移除 user 表的 password 字段（已迁移到 user_authentication_method）
      changes:
        - dropColumn:
            tableName: user
            columnName: password
```

### 示例 2: 重命名 path 为 url

```yaml
# db/changelog/004-rename-resource-path-to-url.yaml
databaseChangeLog:
  - changeSet:
      id: rename-resource-path-to-url
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

## 验证清单

执行同步后，验证以下内容：

### Schema 验证

- [ ] 所有表都存在
- [ ] 所有字段类型一致
- [ ] 所有索引一致
- [ ] 所有外键一致
- [ ] 所有约束一致
- [ ] user 表不包含 password 字段
- [ ] resource 表使用 url 字段（不是 path）
- [ ] user_avatar 表的 CHECK 约束正确

### 数据验证

- [ ] 基础用户数据存在（admin, user）
- [ ] 基础角色数据存在（ROLE_ADMIN, ROLE_USER）
- [ ] 基础资源数据存在
- [ ] 关联关系数据正确

### Liquibase 验证

- [ ] 所有历史迁移都已转换为 changelog
- [ ] changelog 执行顺序正确
- [ ] 无重复的 changeSet ID

---

## 执行建议

### 立即执行（高优先级）

1. **运行同步脚本**
   ```bash
   ./scripts/sync-database-schema.sh
   ```

2. **检查差异报告**
   ```bash
   cat docs/database-sync/schema_diff_*.txt
   ```

3. **处理关键差异**
   - user 表的 password 字段
   - resource 表的 path/url 字段
   - user_avatar 表的 CHECK 约束

### 后续维护（中优先级）

1. **创建差异 Changelog**
   - 为每个差异创建对应的 changelog
   - 更新 master changelog

2. **验证 Liquibase 执行**
   - 在测试环境验证
   - 确保所有变更正确执行

---

## 风险提示

⚠️ **重要**: 

1. **备份数据库**: 执行任何操作前先备份
   ```bash
   mysqldump -u root -p tiny_web > backup_$(date +%Y%m%d).sql
   ```

2. **测试环境验证**: 先在测试环境验证所有变更

3. **逐步执行**: 不要一次性修改所有内容，逐步处理

4. **记录变更**: 所有差异都应该通过 Liquibase changelog 记录

---

## 总结

✅ **应该执行同步和 diff**，原因：

1. **确保一致性**: schema.sql 应该反映当前数据库的实际状态
2. **避免冲突**: 防止 Liquibase 执行时与现有数据库冲突
3. **历史追踪**: 通过 diff 可以发现遗漏的迁移脚本
4. **文档化**: schema.sql 作为数据库结构的文档，应该准确

🎯 **下一步**:

1. 运行 `./scripts/sync-database-schema.sh`
2. 分析差异报告
3. 根据差异更新 schema.sql 和创建 Liquibase changelog
4. 验证所有变更

