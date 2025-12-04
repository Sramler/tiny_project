# Quartz 集群状态检查指南

## 概述

本文档说明如何确认当前 Quartz 调度器是以集群模式还是单机模式运行。

## 配置说明

在 `application.yaml` 中，Quartz 集群配置如下：

```yaml
spring:
  quartz:
    properties:
      org.quartz.jobStore.isClustered: true  # 启用集群
      org.quartz.jobStore.clusterCheckinInterval: 20000  # 集群实例心跳频率 (ms)
```

- `isClustered: true` 表示启用集群模式
- `isClustered: false` 或未配置表示单机模式

## 检查方法

### 方法 1: 通过 REST API 检查（推荐）

调用以下 API 端点获取 Quartz 集群状态：

```bash
GET /scheduling/quartz/cluster-status
```

**响应示例（集群模式）：**
```json
{
  "schedulerName": "DagScheduler",
  "schedulerInstanceId": "hostname_1234567890123",
  "isClustered": true,
  "isStarted": true,
  "isInStandbyMode": false,
  "numberOfJobsExecuted": 42,
  "schedulerStarted": 1701234567890,
  "clusterMode": "集群模式",
  "status": "运行中"
}
```

**响应示例（单机模式）：**
```json
{
  "schedulerName": "DagScheduler",
  "schedulerInstanceId": "hostname_1234567890123",
  "isClustered": false,
  "isStarted": true,
  "isInStandbyMode": false,
  "numberOfJobsExecuted": 42,
  "schedulerStarted": 1701234567890,
  "clusterMode": "单机模式",
  "status": "运行中"
}
```

**关键字段说明：**
- `isClustered`: `true` 表示集群模式，`false` 表示单机模式
- `schedulerInstanceId`: 调度器实例 ID，集群模式下每个节点有唯一 ID
- `clusterMode`: 中文描述，便于快速识别

### 方法 2: 通过应用启动日志检查

在应用启动时，Quartz 会输出调度器初始化信息。查看日志文件或控制台输出：

**集群模式日志特征：**
```
INFO  org.quartz.core.QuartzScheduler - Scheduler DagScheduler_<instanceId> started.
INFO  org.quartz.impl.jdbcjobstore.JobStoreSupport - ClusterManager: detected 1 other instance(s).
```

**单机模式日志特征：**
```
INFO  org.quartz.core.QuartzScheduler - Scheduler DagScheduler_<instanceId> started.
```

### 方法 3: 通过数据库表检查

查询 Quartz 的调度器状态表，查看集群中的实例数量：

```sql
SELECT 
    SCHED_NAME,
    INSTANCE_NAME,
    LAST_CHECKIN_TIME,
    CHECKIN_INTERVAL
FROM QRTZ_SCHEDULER_STATE
ORDER BY LAST_CHECKIN_TIME DESC;
```

**说明：**
- 集群模式：如果有多条记录（不同 `INSTANCE_NAME`），说明有多个节点在运行
- 单机模式：通常只有一条记录

**示例结果（集群模式，2个节点）：**
```
SCHED_NAME      | INSTANCE_NAME              | LAST_CHECKIN_TIME | CHECKIN_INTERVAL
----------------|----------------------------|-------------------|------------------
DagScheduler    | hostname1_1234567890123    | 1701234567890     | 20000
DagScheduler    | hostname2_1234567890123    | 1701234567891     | 20000
```

### 方法 4: 通过代码检查

在代码中直接调用 `QuartzSchedulerService.getClusterStatus()` 方法：

```java
@Autowired
private QuartzSchedulerService quartzSchedulerService;

public void checkClusterStatus() {
    QuartzSchedulerService.ClusterStatusInfo status = 
        quartzSchedulerService.getClusterStatus();
    
    if (status.isClustered()) {
        System.out.println("Quartz 运行在集群模式");
        System.out.println("实例 ID: " + status.getSchedulerInstanceId());
    } else {
        System.out.println("Quartz 运行在单机模式");
    }
}
```

## 集群模式 vs 单机模式

### 集群模式特点

1. **高可用性**：多个节点可以共享任务调度，一个节点故障时其他节点可以接管
2. **负载均衡**：任务会在集群中的不同节点上执行
3. **数据共享**：所有节点共享同一个数据库（QRTZ_* 表）
4. **实例隔离**：每个节点有唯一的 `instanceId`

### 单机模式特点

1. **简单部署**：只需要一个节点运行
2. **无协调开销**：不需要集群协调机制
3. **单点故障**：节点故障会导致调度停止

## 注意事项

1. **配置一致性**：集群模式下，所有节点的 Quartz 配置必须一致（特别是 `isClustered` 和 `clusterCheckinInterval`）
2. **数据库连接**：集群模式下，所有节点必须连接到同一个数据库
3. **时钟同步**：集群节点之间的系统时钟应该同步（建议使用 NTP）
4. **心跳检查**：`clusterCheckinInterval` 设置过小会增加数据库压力，设置过大会影响故障检测速度

## 故障排查

### 问题：配置了集群但检测到单机模式

**可能原因：**
1. 配置未生效，检查 `application.yaml` 中的 `org.quartz.jobStore.isClustered` 是否为 `true`
2. 数据库连接问题，无法访问共享数据库
3. 表结构问题，QRTZ_* 表未正确创建

**解决方法：**
1. 确认配置文件中的 `isClustered: true`
2. 检查数据库连接配置
3. 验证 QRTZ_SCHEDULER_STATE 表是否存在

### 问题：集群模式下只有一个节点

**可能原因：**
1. 其他节点未启动
2. 其他节点配置错误
3. 网络问题导致节点无法通信

**解决方法：**
1. 检查其他节点的启动日志
2. 验证所有节点的配置一致性
3. 检查网络连接和防火墙设置

## 相关文档

- [Quartz 数据库表设计文档](./DAG_TASK_TABLE_DESIGN_REVIEW.md)
- [Quartz 集成状态文档](./QUARTZ_INTEGRATION_STATUS.md)
- [Quartz 数据库设置文档](./QUARTZ_DATABASE_SETUP.md)


