# 数据库同步分析报告

## 分析时间
2025-12-20

## 数据库概览

- **数据库名**: `tiny_web`
- **表总数**: 91 个表
- **主要表分类**:
  - 业务表: user, role, resource, user_role, role_resource, user_authentication_method, user_avatar 等
  - Camunda 工作流表: act_* (约 50 个表)
  - Quartz 调度表: QRTZ_* (11 个表)
  - OAuth2 表: oauth2_* (3 个表)
  - Liquibase 表: databasechangelog, databasechangeloglock
  - 其他: export_task, http_request_log, scheduling_* 等

---

## 关键表结构差异分析

### 1. user 表

#### 当前数据库结构
- **字段数**: 16 个
- **字段列表**:
  - id, username, nickname, enabled, account_non_expired, account_non_locked
  - credentials_non_expired, create_time, update_time, last_login_at
  - last_login_ip, email, failed_login_count, last_failed_login_at
  - last_login_device, phone

#### schema.sql 中的定义
- **字段数**: 18 个
- **字段列表**:
  - id, username, nickname, enabled, account_non_expired, account_non_locked
  - credentials_non_expired, email, phone, last_login_at
  - last_login_ip, last_login_device, failed_login_count, last_failed_login_at
  - **created_at**, **updated_at**

#### 差异
1. ✅ **password 字段**: 数据库中没有，schema.sql 中也没有（正确）
2. ⚠️ **时间字段命名不一致**:
   - 数据库: `create_time`, `update_time`
   - schema.sql: `created_at`, `updated_at`

#### 建议
- 需要统一时间字段命名
- 选项 1: 修改 schema.sql 使用 `create_time`, `update_time`
- 选项 2: 创建 Liquibase changelog 将数据库字段重命名为 `created_at`, `updated_at`

---

### 2. resource 表

#### 当前数据库结构
- **字段数**: 19 个
- **关键字段**: `url` (不是 `path`) ✅
- **额外字段**: `enabled` (schema.sql 中没有)

#### schema.sql 中的定义
- **字段数**: 18 个
- **关键字段**: `url` ✅
- **缺少字段**: `enabled`

#### 差异
1. ✅ **url 字段**: 数据库和 schema.sql 都使用 `url`（正确）
2. ⚠️ **enabled 字段**: 数据库中有，但 schema.sql 中没有

#### 建议
- 在 schema.sql 中添加 `enabled` 字段定义

---

### 3. user_avatar 表

#### 当前数据库结构
- **字段数**: 8 个
- **字段**: user_id, content_hash, content_type, data, file_size, filename, updated_at, uploaded_at

#### schema.sql 中的定义
- **字段数**: 8 个
- **CHECK 约束**: `CONSTRAINT chk_file_size CHECK (file_size <= 1048576)` ✅（已修复 COMMENT 问题）

#### 差异
- ✅ 字段结构基本一致
- ✅ CHECK 约束已修复（无 COMMENT）

---

## 其他发现

### 1. 数据库中存在但 schema.sql 中没有的表

以下表在数据库中存在，但不在 schema.sql 中：

#### Camunda 工作流表（约 50 个）
- `act_ge_*`, `act_hi_*`, `act_id_*`, `act_re_*`, `act_ru_*`
- **说明**: 这些表由 Camunda 自动创建，不需要在 schema.sql 中定义

#### OAuth2 表（3 个）
- `oauth2_authorization`
- `oauth2_authorization_consent`
- `oauth2_registered_client`
- **说明**: 这些表由 Spring Security OAuth2 自动创建，不需要在 schema.sql 中定义

#### 其他业务表
- `menu` - 菜单表（可能是旧表，已迁移到 resource）
- `resource_backup` - 资源备份表
- `export_demo_order` - 导出演示订单表
- `export_demo_record` - 导出演示记录表

#### 建议
- 如果这些表是业务需要的，应该添加到 schema.sql
- 如果是临时表或备份表，可以忽略

---

### 2. schema.sql 中存在但数据库中没有的表

检查 schema.sql 中定义的表，确认是否都在数据库中：

#### 需要验证的表
- `user` ✅
- `role` ✅
- `user_role` ✅
- `resource` ✅
- `role_resource` ✅
- `user_authentication_method` ✅
- `user_authentication_audit` ✅
- `user_avatar` ✅
- `http_request_log` ✅
- `export_task` ✅
- `demo_export_usage` ✅
- `scheduling_*` (11 个表) ✅
- `QRTZ_*` (11 个表) ✅

**结论**: schema.sql 中定义的表都在数据库中存在 ✅

---

## 关键差异总结

### 需要修复的差异

1. **user 表时间字段命名不一致**
   - 数据库: `create_time`, `update_time`
   - schema.sql: `created_at`, `updated_at`
   - **影响**: 可能导致 Liquibase 执行时创建新字段或报错

2. **resource 表缺少 enabled 字段**
   - 数据库中有 `enabled` 字段
   - schema.sql 中没有定义
   - **影响**: Liquibase 执行时不会创建该字段

### 已正确的部分

1. ✅ user 表没有 password 字段（已迁移）
2. ✅ resource 表使用 url 字段（不是 path）
3. ✅ user_avatar 表的 CHECK 约束已修复

---

## 建议的修复方案

### 方案 1: 更新 schema.sql 以匹配当前数据库（推荐）

#### 步骤 1: 修复 user 表时间字段
```sql
-- 在 schema.sql 中，将 created_at 改为 create_time，updated_at 改为 update_time
```

#### 步骤 2: 添加 resource 表的 enabled 字段
```sql
-- 在 schema.sql 的 resource 表定义中添加：
`enabled` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否启用'
```

### 方案 2: 创建 Liquibase changelog 记录差异

如果选择保持 schema.sql 不变，需要创建 changelog 记录差异：

#### 1. user 表时间字段重命名
```yaml
# db/changelog/004-rename-user-time-fields.yaml
databaseChangeLog:
  - changeSet:
      id: rename-user-time-fields
      author: tiny
      comment: 将 user 表的 create_time/update_time 重命名为 created_at/updated_at
      changes:
        - renameColumn:
            tableName: user
            oldColumnName: create_time
            newColumnName: created_at
            columnDataType: TIMESTAMP
        - renameColumn:
            tableName: user
            oldColumnName: update_time
            newColumnName: updated_at
            columnDataType: TIMESTAMP
```

#### 2. resource 表添加 enabled 字段
```yaml
# db/changelog/005-add-resource-enabled-field.yaml
databaseChangeLog:
  - changeSet:
      id: add-resource-enabled-field
      author: tiny
      comment: 为 resource 表添加 enabled 字段
      changes:
        - addColumn:
            tableName: resource
            columns:
              - column:
                  name: enabled
                  type: TINYINT(1)
                  defaultValueNumeric: 0
                  remarks: 是否启用
```

---

## 执行建议

### 立即执行（高优先级）

1. **修复 schema.sql**
   - 将 user 表的 `created_at`/`updated_at` 改为 `create_time`/`update_time`
   - 在 resource 表中添加 `enabled` 字段

2. **验证修复**
   - 重新运行同步脚本
   - 确认差异已消除

### 后续维护

1. **建立同步流程**
   - 每次数据库变更后，同步更新 schema.sql
   - 通过 Liquibase changelog 管理所有变更

2. **定期检查**
   - 每月执行一次同步检查
   - 确保 schema.sql 与实际数据库一致

---

## 总结

### 发现的问题
1. ⚠️ user 表时间字段命名不一致
2. ⚠️ resource 表缺少 enabled 字段

### 正确的部分
1. ✅ user 表没有 password 字段
2. ✅ resource 表使用 url 字段
3. ✅ user_avatar 表的 CHECK 约束已修复

### 下一步行动
1. 修复 schema.sql 中的差异
2. 重新验证数据库结构一致性
3. 确保 Liquibase 可以正常执行

