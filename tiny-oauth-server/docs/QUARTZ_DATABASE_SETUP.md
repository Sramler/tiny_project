# Quartz 数据库表配置指南

## 问题描述

使用 `spring-boot-starter-quartz` 时，如果配置了 JDBC JobStore（数据库存储），Quartz 需要特定的数据库表来持久化 Job 和 Trigger 信息。这些表**不会自动创建**，需要手动初始化。

## 解决方案

### 方案 1：使用 schema.sql 自动初始化（推荐）

已在 `schema.sql` 中添加了 Quartz 所需的 11 张表：

1. `QRTZ_JOB_DETAILS` - Job 详情表
2. `QRTZ_TRIGGERS` - Trigger 表
3. `QRTZ_SIMPLE_TRIGGERS` - 简单触发器表
4. `QRTZ_CRON_TRIGGERS` - Cron 触发器表
5. `QRTZ_SIMPROP_TRIGGERS` - 属性触发器表
6. `QRTZ_BLOB_TRIGGERS` - Blob 触发器表
7. `QRTZ_CALENDARS` - 日历表
8. `QRTZ_PAUSED_TRIGGER_GRPS` - 暂停的触发器组表
9. `QRTZ_FIRED_TRIGGERS` - 正在执行的触发器表
10. `QRTZ_SCHEDULER_STATE` - 调度器状态表（集群模式）
11. `QRTZ_LOCKS` - 锁表（集群模式）

**配置说明：**

在 `application.yml` 中已配置：

```yaml
spring:
  sql:
    init:
      mode: always # 总是执行数据初始化脚本
      data-locations: classpath:data.sql # 指定数据初始化脚本位置
      continue-on-error: true # 遇到错误继续执行
```

**注意：** `spring.sql.init.mode: always` 会执行 `schema.sql`，但需要确保：
- `schema.sql` 文件位于 `src/main/resources/` 目录
- 数据库连接配置正确
- 数据库用户有 CREATE TABLE 权限

### 方案 2：手动执行 SQL 脚本

如果自动初始化失败，可以手动执行 `schema.sql` 中的 Quartz 表创建语句：

```bash
# 连接到 MySQL 数据库
mysql -u root -p tiny_web

# 执行 schema.sql（或只执行 Quartz 表部分）
source /path/to/schema.sql
```

或者使用 MySQL 客户端工具（如 Navicat、DBeaver）直接执行 SQL。

### 方案 3：使用 Flyway/Liquibase 迁移工具

如果项目使用数据库迁移工具，可以将 Quartz 表创建语句添加到迁移脚本中。

## 验证表是否创建成功

执行以下 SQL 查询，确认表已创建：

```sql
-- 查看所有 Quartz 表
SHOW TABLES LIKE 'QRTZ_%';

-- 应该看到 11 张表：
-- QRTZ_JOB_DETAILS
-- QRTZ_TRIGGERS
-- QRTZ_SIMPLE_TRIGGERS
-- QRTZ_CRON_TRIGGERS
-- QRTZ_SIMPROP_TRIGGERS
-- QRTZ_BLOB_TRIGGERS
-- QRTZ_CALENDARS
-- QRTZ_PAUSED_TRIGGER_GRPS
-- QRTZ_FIRED_TRIGGERS
-- QRTZ_SCHEDULER_STATE
-- QRTZ_LOCKS
```

## Quartz 配置说明

当前 Quartz 配置（`QuartzConfig.java`）已正确设置：

```java
props.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
props.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
props.put("org.quartz.jobStore.tablePrefix", "QRTZ_");
props.put("org.quartz.jobStore.isClustered", "true");
```

**关键配置项：**
- `tablePrefix`: `QRTZ_` - 表名前缀，必须与数据库表名一致
- `isClustered`: `true` - 启用集群模式（多实例部署）
- `driverDelegateClass`: `StdJDBCDelegate` - MySQL 驱动委托类

## 常见问题

### 1. 表未自动创建

**原因：**
- `spring.sql.init.mode` 未设置为 `always`
- `schema.sql` 文件路径不正确
- 数据库用户没有 CREATE TABLE 权限
- 表已存在，但结构不完整

**解决：**
- 检查 `application.yml` 配置
- 确认 `schema.sql` 在 `src/main/resources/` 目录
- 手动执行 SQL 脚本创建表

### 2. Quartz 启动报错：表不存在

**错误信息：**
```
Table 'tiny_web.QRTZ_JOB_DETAILS' doesn't exist
```

**解决：**
- 确认表已创建（使用 `SHOW TABLES LIKE 'QRTZ_%'`）
- 检查表名前缀是否匹配（`QRTZ_`）
- 检查数据库连接配置是否正确

### 3. 集群模式问题

**说明：**
- 集群模式需要 `QRTZ_SCHEDULER_STATE` 和 `QRTZ_LOCKS` 表
- 多个 Quartz 实例共享同一个数据库
- 通过数据库锁机制实现任务不重复执行

**验证：**
- 启动多个应用实例
- 查看 `QRTZ_SCHEDULER_STATE` 表，应该看到多个实例记录
- 查看 `QRTZ_LOCKS` 表，应该看到锁记录

## 表结构说明

### QRTZ_JOB_DETAILS
存储 Job 的详细信息，包括：
- Job 名称和组名
- Job 实现类
- 是否持久化、是否并发等属性
- Job 数据（BLOB）

### QRTZ_TRIGGERS
存储 Trigger 的详细信息，包括：
- Trigger 名称和组名
- 关联的 Job
- 触发时间、状态
- Trigger 类型（CRON/SIMPLE/BLOB）

### QRTZ_FIRED_TRIGGERS
运行时表，记录正在执行的触发器：
- 触发时间
- 执行状态
- 关联的 Job 和 Trigger

### QRTZ_SCHEDULER_STATE
集群模式使用，记录每个调度器实例的状态：
- 实例名称
- 最后检查时间
- 检查间隔

### QRTZ_LOCKS
集群模式使用，实现分布式锁：
- 锁名称
- 用于防止并发冲突

## 参考文档

- [Quartz Scheduler Documentation](http://www.quartz-scheduler.org/documentation/)
- [Spring Boot Quartz Support](https://docs.spring.io/spring-boot/docs/current/reference/html/io.html#io.quartz)
- [Quartz Database Schema](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/tutorial-lesson-09.html)

