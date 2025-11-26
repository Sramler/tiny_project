# Quartz 调度系统表结构迁移指南

## 一、表结构变更概览

### 旧表 → 新表映射关系

| 旧表名 | 新表名 | 说明 |
|--------|--------|------|
| `task_type` | `scheduling_task_type` | 任务类型表，添加多租户支持 |
| `task` | `scheduling_task` | 任务定义表（模板），不再绑定 DAG |
| `dag` | `scheduling_dag` | DAG 主表，移除状态字段（状态在 run 中） |
| - | `scheduling_dag_version` | **新增**：DAG 版本表 |
| - | `scheduling_dag_task` | **新增**：DAG 版本节点表（替代 task 表的部分功能） |
| `task_dependency` | `scheduling_dag_edge` | DAG 边表，使用 node_code |
| - | `scheduling_dag_run` | **新增**：DAG 运行实例表 |
| - | `scheduling_task_instance` | **新增**：任务实例表（调度队列） |
| `task_history` | `scheduling_task_history` | 任务执行历史表 |
| - | `scheduling_audit` | **新增**：操作审计表 |
| - | `scheduling_task_param` | **新增**：任务参数表（可选） |

## 二、核心设计变更

### 1. 多租户支持
- ✅ 所有表添加 `tenant_id` 字段
- ✅ 唯一约束改为 `(tenant_id, code)`
- ✅ 支持 SaaS 多租户场景

### 2. DAG 版本化
- ✅ 引入 `scheduling_dag_version` 表
- ✅ 支持 DRAFT/ACTIVE/ARCHIVED 状态
- ✅ 可以回滚到历史版本

### 3. 任务模板与实例分离
- ✅ `scheduling_task` 作为可复用模板
- ✅ `scheduling_task_instance` 作为执行实例
- ✅ 任务可以跨 DAG 复用

### 4. 节点使用 code 而非 id
- ✅ `scheduling_dag_task` 和 `scheduling_dag_edge` 使用 `node_code`
- ✅ 版本化时节点关系更稳定

### 5. 分布式执行支持
- ✅ `scheduling_task_instance` 有 `locked_by` 和 `lock_time`
- ✅ 支持 Worker 抢占式执行

### 6. 更细粒度的状态
- ✅ `scheduling_dag_run.status`: SCHEDULED/RUNNING/SUCCESS/FAILED/CANCELLED/PARTIAL_FAILED
- ✅ `scheduling_task_instance.status`: PENDING/RESERVED/RUNNING/SUCCESS/FAILED/SKIPPED

## 三、字段映射关系

### task_type → scheduling_task_type

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `id` | `id` | 保持不变 |
| `name` | `name` | 保持不变 |
| `name` (UNIQUE) | `code` (UNIQUE with tenant_id) | 改为 code，支持多租户 |
| `description` | `description` | 保持不变 |
| `handler_class` | `executor` | 重命名 |
| `config_template` | `param_schema` | 改为 JSON Schema |
| - | `tenant_id` | **新增** |
| - | `default_timeout_sec` | **新增** |
| - | `default_max_retry` | **新增** |
| `enabled` | `enabled` | 保持不变 |
| `created_at` | `created_at` | 保持不变 |
| `updated_at` | `updated_at` | 保持不变 |

### task → scheduling_task (任务模板)

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `id` | `id` | 保持不变 |
| `dag_id` | - | **移除**：任务不再绑定 DAG |
| `task_type_id` | `type_id` | 重命名 |
| `name` | `name` | 保持不变 |
| - | `code` | **新增**：业务编码 |
| `params` | `params` | 保持不变 |
| `max_attempt` | `max_retry` | 重命名 |
| - | `tenant_id` | **新增** |
| - | `retry_policy` | **新增**：重试策略 JSON |
| - | `concurrency_policy` | **新增**：并发策略 |
| `status` | - | **移除**：状态在 instance 中 |
| `attempt` | - | **移除**：在 instance 中 |
| `started_at` | - | **移除**：在 instance 中 |
| `finished_at` | - | **移除**：在 instance 中 |
| `worker_id` | - | **移除**：在 instance 中 |

### dag → scheduling_dag

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `id` | `id` | 保持不变 |
| `name` | `name` | 保持不变 |
| - | `code` | **新增**：业务编码 |
| - | `tenant_id` | **新增** |
| `status` | - | **移除**：状态在 dag_run 中 |
| `trigger_type` | - | **移除**：在 dag_run 中 |
| `schedule_cron` | - | **移除**：需要单独的表或配置 |
| `quartz_job_key` | - | **移除**：需要单独的表或配置 |
| `quartz_trigger_key` | - | **移除**：需要单独的表或配置 |
| `started_at` | - | **移除**：在 dag_run 中 |
| `finished_at` | - | **移除**：在 dag_run 中 |
| `enabled` | `enabled` | 保持不变 |
| `created_by` | `created_by` | 保持不变 |
| `created_at` | `created_at` | 保持不变 |
| `updated_at` | `updated_at` | 保持不变 |

### task_dependency → scheduling_dag_edge

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `id` | `id` | 保持不变 |
| - | `dag_version_id` | **新增**：关联版本 |
| `from_task_id` | `from_node_code` | 改为 node_code |
| `to_task_id` | `to_node_code` | 改为 node_code |
| `condition` | `condition` | 保持不变 |
| `condition_type` | - | **移除**：在 condition JSON 中 |

### task_history → scheduling_task_history

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `id` | `id` | 保持不变 |
| `task_id` | `task_instance_id` | 改为关联实例 |
| `task_id` | `task_id` | 保留（冗余） |
| `dag_id` | `dag_id` | 保持不变 |
| - | `dag_run_id` | **新增** |
| - | `node_code` | **新增** |
| `status` | `status` | 保持不变 |
| `attempt` | `attempt_no` | 重命名 |
| `result` | `result` | 保持不变 |
| `error_message` | `error_message` | 保持不变 |
| `error_stack` | `stack_trace` | 重命名 |
| `duration_seconds` | `duration_ms` | 改为毫秒 |
| `worker_id` | `worker_id` | 保持不变 |
| `fired_at` | `start_time` | 重命名 |
| `finished_at` | `end_time` | 保持不变 |
| - | `log_path` | **新增** |
| - | `params` | **新增**：参数快照 |

## 四、新增表说明

### 1. scheduling_dag_version（DAG 版本表）
- 支持 DAG 版本管理和回滚
- `status`: DRAFT/ACTIVE/ARCHIVED
- `definition`: JSON 格式存储完整定义

### 2. scheduling_dag_task（DAG 节点表）
- 节点属于版本，而非 DAG
- 使用 `node_code` 作为标识
- 支持节点级参数覆盖

### 3. scheduling_dag_run（DAG 运行实例）
- 每次 DAG 触发创建一个 run
- 记录运行状态和指标
- 支持幂等（run_no）

### 4. scheduling_task_instance（任务实例表）
- 调度队列项
- 支持 Worker 抢占（locked_by）
- 状态：PENDING/RESERVED/RUNNING/SUCCESS/FAILED/SKIPPED

### 5. scheduling_audit（审计表）
- 记录所有操作
- 支持操作审计和追溯

## 五、迁移步骤建议

### 阶段 1：数据迁移（如果已有数据）

```sql
-- 1. 迁移 task_type
INSERT INTO scheduling_task_type (id, tenant_id, code, name, description, executor, enabled, created_at, updated_at)
SELECT id, NULL, name, name, description, handler_class, enabled, created_at, updated_at
FROM task_type;

-- 2. 迁移 dag
INSERT INTO scheduling_dag (id, tenant_id, code, name, description, enabled, created_by, created_at, updated_at)
SELECT id, NULL, CONCAT('dag_', id), name, description, enabled, created_by, created_at, updated_at
FROM dag;

-- 3. 为每个 DAG 创建版本
INSERT INTO scheduling_dag_version (dag_id, version_no, status, created_at)
SELECT id, 1, 'ACTIVE', created_at
FROM dag;

-- 4. 迁移 task 为 scheduling_task（模板）
INSERT INTO scheduling_task (id, tenant_id, type_id, code, name, params, max_retry, enabled, created_at, updated_at)
SELECT id, NULL, task_type_id, CONCAT('task_', id), name, params, max_attempt, TRUE, created_at, updated_at
FROM task;

-- 5. 迁移 task 为 scheduling_dag_task（节点）
INSERT INTO scheduling_dag_task (dag_version_id, node_code, task_id, name, override_params, created_at)
SELECT 
    dv.id,
    CONCAT('node_', t.id),
    t.id,
    t.name,
    t.params,
    t.created_at
FROM task t
JOIN dag d ON t.dag_id = d.id
JOIN scheduling_dag_version dv ON dv.dag_id = d.id AND dv.version_no = 1;

-- 6. 迁移依赖关系
INSERT INTO scheduling_dag_edge (dag_version_id, from_node_code, to_node_code, condition, created_at)
SELECT 
    dv.id,
    CONCAT('node_', td.from_task_id),
    CONCAT('node_', td.to_task_id),
    td.condition,
    td.created_at
FROM task_dependency td
JOIN task t ON td.from_task_id = t.id
JOIN dag d ON t.dag_id = d.id
JOIN scheduling_dag_version dv ON dv.dag_id = d.id AND dv.version_no = 1;
```

### 阶段 2：代码适配

1. 更新 Entity 类（已完成）
2. 更新 Repository 接口（已完成）
3. 更新 Service 层逻辑
4. 更新 Controller 层
5. 更新前端 API 和页面

## 六、注意事项

1. **旧表保留**：建议保留旧表一段时间，确保数据迁移成功后再删除
2. **Quartz 集成**：需要单独的表或配置存储 Quartz Job/Trigger 信息
3. **租户隔离**：如果不需要多租户，`tenant_id` 可以统一设为 NULL
4. **版本管理**：创建 DAG 时自动创建第一个版本（DRAFT），激活后变为 ACTIVE
5. **任务模板**：`scheduling_task` 是模板，可以被多个 DAG 版本引用

## 七、新设计的优势

1. ✅ **多租户支持** - 适合 SaaS 场景
2. ✅ **版本化管理** - 支持 DAG 版本和回滚
3. ✅ **任务模板复用** - 任务可以跨 DAG 复用
4. ✅ **分布式执行** - 锁定机制支持多 Worker
5. ✅ **并发控制** - 支持多种并发策略
6. ✅ **细粒度状态** - 更好的监控和调试
7. ✅ **操作审计** - 完整的操作记录


