# DAG 任务调度系统表设计评审

> **系统架构：** 基于 Quartz + Spring Boot 的分布式任务调度系统
> 
> **设计说明：**
> - 使用 Quartz 作为底层调度引擎，负责定时触发和任务分发
> - 业务层实现 DAG（有向无环图）任务编排，支持任务依赖和条件分支
> - 表结构已合并到 `schema.sql` 中，与系统其他表统一管理
> - 通过 `quartz_job_key` 和 `quartz_trigger_key` 字段关联 Quartz Job 和 Trigger

## 一、原设计存在的问题

### 1. ❌ 索引缺失（严重问题）

**问题描述：**
- `task` 表的 `dag_id`、`task_type_id`、`status` 字段没有索引
- `task_dependency` 表的 `from_task_id`、`to_task_id` 没有索引
- `task_history` 表的 `task_id`、`dag_id`、`status`、`fired_at` 没有索引

**影响：**
- 查询性能差，特别是关联查询和状态筛选
- 外键字段没有索引会导致外键约束检查性能低下
- 无法高效查询任务依赖关系

**优化方案：**
- 为所有外键字段添加索引
- 为常用查询字段（status、created_at等）添加索引
- 为复合查询场景添加联合索引

### 2. ⚠️ 外键约束策略

**设计决策：**
- **不设置数据库外键约束**，由应用层代码控制关联更新和删除
- 保留索引以优化查询性能

**原因：**
- 外键约束会影响数据库性能（特别是在高并发场景）
- 在分布式系统中，外键约束可能带来问题
- 业务逻辑需要更灵活的控制（如软删除、批量操作等）
- 便于数据迁移和分库分表

**应用层需要确保：**
- 删除 DAG 时，应用层代码需要删除相关任务、任务依赖和历史记录
- 删除任务时，应用层代码需要删除相关依赖和历史记录
- 删除任务类型时，应用层代码需要检查是否有任务在使用
- 使用事务保证数据一致性

### 3. ⚠️ 状态字段缺乏约束

**问题描述：**
- 状态字段使用 `VARCHAR(32)`，没有枚举约束
- 容易输入错误的状态值

**影响：**
- 数据一致性风险
- 业务逻辑可能出错

**优化方案：**
- 在应用层使用枚举类控制
- 考虑使用 MySQL 8.0+ 的 CHECK 约束
- 添加状态值说明注释

### 4. ⚠️ 唯一性约束缺失

**问题描述：**
- `task` 表没有唯一性约束，同一 DAG 内可能创建同名任务
- `task_dependency` 表可能产生重复依赖关系

**影响：**
- 数据重复风险
- 业务逻辑混乱

**优化方案：**
```sql
UNIQUE KEY `uk_task_dag_name` (`dag_id`, `name`)
UNIQUE KEY `uk_task_dependency` (`from_task_id`, `to_task_id`)
```

### 5. ⚠️ 字段设计不完善

**问题描述：**
- 缺少执行时间追踪字段（started_at、finished_at、duration）
- 缺少 worker_id 用于分布式追踪
- 缺少扩展字段（handler_class、config_template）
- error_stack 和 error_message 没有分离

**影响：**
- 无法追踪任务执行时间
- 无法定位执行任务的 worker
- 扩展性差

**优化方案：**
- 添加时间追踪字段
- 添加 worker_id 字段
- 添加扩展配置字段
- 分离错误信息和堆栈

### 6. ⚠️ 表结构规范不一致

**问题描述：**
- 没有使用反引号包裹表名和字段名
- 缺少详细的中文注释
- 没有指定 ENGINE 和 CHARSET
- 时间字段混用 DATETIME 和 TIMESTAMP

**影响：**
- 不符合项目编码规范
- 可维护性差

**优化方案：**
- 统一使用反引号
- 添加详细注释
- 指定 ENGINE=InnoDB 和 CHARSET=utf8mb4
- 统一时间字段类型

### 7. ⚠️ 缺少业务约束

**问题描述：**
- `task_dependency` 表没有防止任务依赖自身的约束
- 缺少条件类型字段

**影响：**
- 可能产生循环依赖
- 依赖条件不明确

**优化方案：**
```sql
CONSTRAINT `chk_task_dependency_no_self` CHECK (`from_task_id` != `to_task_id`)
```

## 二、优化后的设计亮点

### 1. ✅ 完整的索引体系

```sql
-- 单列索引
KEY `idx_task_dag_id` (`dag_id`)
KEY `idx_task_status` (`status`)

-- 联合索引（优化复合查询）
KEY `idx_task_status_retry` (`status`, `next_retry_at`)
KEY `idx_dag_status_created` (`status`, `created_at`)
```

### 2. ✅ 应用层控制关联关系

- **不设置数据库外键约束**：由应用层代码控制关联更新和删除
- **保留索引**：优化查询性能，特别是关联查询
- **事务保证**：使用数据库事务确保数据一致性
- **灵活控制**：支持软删除、批量操作等复杂业务场景

### 3. ✅ 完善的唯一性约束

- 任务类型名称唯一
- 任务在 DAG 内名称唯一
- 任务依赖关系唯一

### 4. ✅ 扩展性设计

- 支持任务类型配置模板
- 支持条件分支依赖
- 支持多种触发类型
- 支持调度表达式（Cron）

### 5. ✅ 完整的审计追踪

- 执行时间追踪
- Worker 实例追踪
- 创建人追踪
- 执行历史完整记录

## 三、性能优化建议

### 1. 查询场景索引设计

**场景1：查询 DAG 下的所有任务**
```sql
-- 需要索引：idx_task_dag_id
SELECT * FROM task WHERE dag_id = ?
```

**场景2：查询待重试的任务**
```sql
-- 需要联合索引：idx_task_status_retry
SELECT * FROM task 
WHERE status = 'FAILED' 
  AND next_retry_at <= NOW()
```

**场景3：查询任务执行历史**
```sql
-- 需要联合索引：idx_task_history_task_fired
SELECT * FROM task_history 
WHERE task_id = ? 
ORDER BY fired_at DESC
```

### 2. 分区建议（大数据量场景）

如果任务历史表数据量很大，可以考虑按时间分区：

```sql
-- 按月分区（示例）
PARTITION BY RANGE (YEAR(fired_at) * 100 + MONTH(fired_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    ...
)
```

### 3. 归档策略

- 定期归档历史数据（如保留3个月）
- 使用 `task_history` 表存储详细历史
- 考虑使用归档表存储更久远的数据

## 四、Quartz 集成说明

### 1. Quartz Job 与 DAG 的关联

**设计思路：**
- DAG 作为业务层的任务编排单元，通过 Quartz 进行定时调度
- 每个需要定时执行的 DAG 对应一个 Quartz Job
- 通过 `quartz_job_key` 和 `quartz_trigger_key` 字段关联 Quartz 的 Job 和 Trigger

**字段说明：**
- `quartz_job_key`: 格式为 `group.name`，如 `dag-group.dag-001`
- `quartz_trigger_key`: 格式为 `group.name`，如 `dag-trigger-group.dag-001-trigger`
- `schedule_cron`: Cron 表达式，用于创建 Quartz CronTrigger
- `enabled`: 控制 DAG 是否启用，禁用时暂停 Quartz 调度

**集成流程：**
```java
// 1. 创建 DAG 时，如果 trigger_type = SCHEDULE，创建对应的 Quartz Job
@Transactional
public Dag createDag(Dag dag) {
    dag = dagMapper.insert(dag);
    
    if ("SCHEDULE".equals(dag.getTriggerType()) && dag.getScheduleCron() != null) {
        String jobKey = "dag-group." + dag.getName();
        String triggerKey = "dag-trigger-group." + dag.getName() + "-trigger";
        
        // 创建 Quartz Job
        JobDetail jobDetail = JobBuilder.newJob(DagExecutionJob.class)
            .withIdentity(jobKey)
            .usingJobData("dagId", dag.getId())
            .build();
        
        // 创建 CronTrigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(CronScheduleBuilder.cronSchedule(dag.getScheduleCron()))
            .build();
        
        scheduler.scheduleJob(jobDetail, trigger);
        
        // 更新 DAG 的 Quartz Key
        dag.setQuartzJobKey(jobKey);
        dag.setQuartzTriggerKey(triggerKey);
        dagMapper.updateById(dag);
    }
    
    return dag;
}

// 2. Quartz Job 执行时，触发 DAG 执行
public class DagExecutionJob implements Job {
    @Override
    public void execute(JobExecutionContext context) {
        Long dagId = context.getJobDetail().getJobDataMap().getLong("dagId");
        dagExecutionService.executeDag(dagId);
    }
}

// 3. 更新 DAG 调度时，同步更新 Quartz Trigger
@Transactional
public void updateDagSchedule(Long dagId, String cron) {
    Dag dag = dagMapper.selectById(dagId);
    if (dag.getQuartzTriggerKey() != null) {
        TriggerKey triggerKey = TriggerKey.triggerKey(dag.getQuartzTriggerKey());
        CronTrigger newTrigger = TriggerBuilder.newTrigger()
            .withIdentity(triggerKey)
            .withSchedule(CronScheduleBuilder.cronSchedule(cron))
            .build();
        scheduler.rescheduleJob(triggerKey, newTrigger);
    }
}

// 4. 禁用/启用 DAG 时，暂停/恢复 Quartz Job
public void toggleDagEnabled(Long dagId, boolean enabled) {
    Dag dag = dagMapper.selectById(dagId);
    if (dag.getQuartzJobKey() != null) {
        JobKey jobKey = JobKey.jobKey(dag.getQuartzJobKey());
        if (enabled) {
            scheduler.resumeJob(jobKey);
        } else {
            scheduler.pauseJob(jobKey);
        }
    }
    dag.setEnabled(enabled);
    dagMapper.updateById(dag);
}
```

### 2. Quartz 配置建议

**application.yaml 配置：**
```yaml
spring:
  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: never  # 使用自定义表结构，不自动初始化
    properties:
      org.quartz.scheduler.instanceName: DagScheduler
      org.quartz.scheduler.instanceId: AUTO
      org.quartz.jobStore.class: org.quartz.impl.jdbcjobstore.JobStoreTX
      org.quartz.jobStore.driverDelegateClass: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      org.quartz.jobStore.tablePrefix: QRTZ_
      org.quartz.jobStore.isClustered: true
      org.quartz.threadPool.threadCount: 10
```

## 五、使用建议

### 1. 应用层数据完整性控制

由于表设计中没有使用数据库外键约束，需要在应用层代码中确保数据完整性：

**删除 DAG 时的处理：**
```java
@Transactional
public void deleteDag(Long dagId) {
    // 1. 删除任务依赖关系
    taskDependencyMapper.deleteByDagId(dagId);
    
    // 2. 删除任务执行历史
    taskHistoryMapper.deleteByDagId(dagId);
    
    // 3. 删除任务
    taskMapper.deleteByDagId(dagId);
    
    // 4. 删除 DAG
    dagMapper.deleteById(dagId);
}
```

**删除任务时的处理：**
```java
@Transactional
public void deleteTask(Long taskId) {
    // 1. 删除任务依赖关系（作为上游或下游）
    taskDependencyMapper.deleteByTaskId(taskId);
    
    // 2. 删除任务执行历史
    taskHistoryMapper.deleteByTaskId(taskId);
    
    // 3. 删除任务
    taskMapper.deleteById(taskId);
}
```

**删除任务类型时的处理：**
```java
@Transactional
public void deleteTaskType(Long taskTypeId) {
    // 1. 检查是否有任务在使用该类型
    long count = taskMapper.countByTaskTypeId(taskTypeId);
    if (count > 0) {
        throw new BusinessException("该任务类型正在被使用，无法删除");
    }
    
    // 2. 删除任务类型
    taskTypeMapper.deleteById(taskTypeId);
}
```

**注意事项：**
- 所有删除操作必须使用 `@Transactional` 保证原子性
- 删除顺序要正确（先删除依赖，再删除主体）
- 删除前要检查业务约束（如任务类型是否被使用）
- 考虑软删除场景，可能需要额外的状态字段

### 2. 状态枚举定义

```java
public enum TaskStatus {
    PENDING,      // 待执行
    RUNNING,      // 执行中
    SUCCESS,      // 成功
    FAILED,       // 失败
    SKIPPED,      // 跳过
    CANCELED      // 取消
}

public enum DagStatus {
    PENDING,          // 待执行
    RUNNING,           // 执行中
    SUCCESS,           // 全部成功
    PARTIAL_FAILED,    // 部分失败
    FAILED,            // 全部失败
    CANCELED           // 取消
}
```

### 3. 常用查询示例

**查询待执行的任务：**
```sql
SELECT t.* 
FROM task t
WHERE t.dag_id = ?
  AND t.status = 'PENDING'
  AND NOT EXISTS (
      SELECT 1 FROM task_dependency td
      JOIN task pt ON td.from_task_id = pt.id
      WHERE td.to_task_id = t.id
        AND pt.status NOT IN ('SUCCESS', 'SKIPPED')
  )
ORDER BY t.created_at;
```

**查询任务依赖关系：**
```sql
SELECT 
    ft.name AS from_task,
    tt.name AS to_task,
    td.condition_type
FROM task_dependency td
JOIN task ft ON td.from_task_id = ft.id
JOIN task tt ON td.to_task_id = tt.id
WHERE ft.dag_id = ?;
```

## 六、总结

### 原设计评分：6/10

**优点：**
- 表结构清晰，关系明确
- 基本满足业务需求

**缺点：**
- 索引缺失严重
- 外键约束不完整
- 缺少业务约束
- 不符合项目规范

### 优化后设计评分：9/10

**改进：**
- ✅ 完整的索引体系
- ✅ 应用层控制关联关系（移除外键约束，提升性能）
- ✅ 完善的唯一性约束
- ✅ 扩展性设计
- ✅ 符合项目规范
- ✅ 完整的审计追踪

**建议：**
- 根据实际业务场景调整索引
- 考虑大数据量场景的分区策略
- 定期归档历史数据

