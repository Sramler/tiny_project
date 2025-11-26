# 调度模块问题分析

## 一、未实现的功能（TODO）

### 1. 节点级别的操作（高优先级）

**位置**: `SchedulingService.java` (642-660行)

```java
// TODO: 实现节点单独触发逻辑
public void triggerNode(Long dagId, Long nodeId)

// TODO: 实现节点重试逻辑
public void retryNode(Long dagId, Long nodeId)

// TODO: 实现节点暂停逻辑
public void pauseNode(Long dagId, Long nodeId)

// TODO: 实现节点恢复逻辑
public void resumeNode(Long dagId, Long nodeId)
```

**影响**: 
- 无法对单个节点进行操作
- 前端调用这些接口会抛出异常

**建议**: 实现节点级别的操作，允许对单个任务实例进行控制

---

### 2. 任务重试逻辑不完整（高优先级）

**位置**: `TaskWorkerService.java` (179行)

```java
int maxRetry = instance.getAttemptNo(); // TODO: 从任务定义中获取
```

**问题**:
- 当前使用 `instance.getAttemptNo()` 作为最大重试次数，这是错误的
- 应该从 `SchedulingTask` 或 `SchedulingDagTask` 中获取 `maxRetry`
- 重试逻辑无法正确工作

**修复建议**:
```java
// 从任务定义中获取最大重试次数
SchedulingTask task = taskRepository.findById(instance.getTaskId())
    .orElseThrow(() -> new RuntimeException("任务不存在"));
int maxRetry = task.getMaxRetry() != null && task.getMaxRetry() > 0 
    ? task.getMaxRetry() 
    : taskType.getDefaultMaxRetry();
```

---

### 3. JSON 参数解析未实现（中优先级）

**位置**: `TaskExecutorService.java` (110-114行)

```java
private Map<String, Object> parseParams(String paramsJson) {
    // TODO: 使用 JSON 解析库解析参数
    // 这里简化处理
    return Map.of();
}
```

**问题**:
- 参数解析返回空 Map，任务无法获取参数
- 任务执行器无法接收参数

**修复建议**:
- 使用 Jackson 或 Gson 解析 JSON
- 处理参数合并（任务默认参数 + 节点覆盖参数）

---

### 4. 下游任务检查逻辑需要完善（中优先级）

**位置**: `TaskWorkerService.java` (231行)

```java
// TODO: 根据 DAG Edge 检查是否是下游任务
```

**问题**:
- 当前实现使用了 `DependencyCheckerService.isDownstreamTask()`，但逻辑可能不够完善
- 需要确保正确识别所有下游任务

**建议**: 验证当前实现是否正确，必要时优化

---

## 二、潜在问题

### 1. 任务执行结果存储格式问题（中优先级）

**位置**: `TaskWorkerService.java` (135行, 144行)

```java
instance.setResult(result.getResult() != null ? result.getResult().toString() : null);
history.setResult(result.getResult() != null ? result.getResult().toString() : null);
```

**问题**:
- 使用 `toString()` 存储结果，可能丢失复杂对象信息
- 数据库字段是 JSON 类型，应该存储 JSON 格式

**修复建议**:
```java
// 使用 JSON 序列化
ObjectMapper objectMapper = new ObjectMapper();
String resultJson = result.getResult() != null 
    ? objectMapper.writeValueAsString(result.getResult()) 
    : null;
instance.setResult(resultJson);
```

---

### 2. 缺少任务超时处理（高优先级）

**问题**:
- 任务定义中有 `timeoutSec` 字段，但没有超时检查逻辑
- 长时间运行的任务不会被自动取消

**影响**:
- 任务可能无限期运行
- 资源浪费

**修复建议**:
- 在 `TaskWorkerService.executeTask()` 中添加超时检查
- 使用 `Future` 和 `ExecutorService` 实现超时控制

---

### 3. 缺少并发策略实现（中优先级）

**问题**:
- 任务定义中有 `concurrencyPolicy` 字段（PARALLEL/SEQUENTIAL/SINGLETON/KEYED）
- 但没有实现并发控制逻辑

**影响**:
- 无法控制任务的并发执行
- SINGLETON 和 KEYED 策略无法生效

**修复建议**:
- 实现并发策略检查
- SINGLETON: 同一任务只能有一个实例运行
- KEYED: 基于 key 的并发控制

---

### 4. 缺少任务执行器注册机制（中优先级）

**问题**:
- `TaskExecutorService.getExecutor()` 通过 Bean 名称或类名查找执行器
- 没有统一的执行器注册机制
- 执行器查找可能失败

**建议**:
- 创建执行器注册表
- 支持动态注册执行器
- 提供执行器发现机制

---

### 5. 缺少审计日志记录（低优先级）

**问题**:
- 有 `SchedulingAudit` 表，但很多操作没有记录审计日志
- 无法追踪操作历史

**建议**:
- 在关键操作（创建、更新、删除、触发）时记录审计日志
- 使用 AOP 或统一的服务层记录

---

### 6. 错误处理不完善（中优先级）

**问题**:
- 很多地方使用 `RuntimeException`，错误信息不够详细
- 缺少统一的异常处理机制
- 错误堆栈可能丢失

**建议**:
- 定义自定义异常类型
- 统一异常处理
- 记录详细的错误信息

---

### 7. 数据一致性问题（高优先级）

**问题**:
- `DagExecutionJob` 中创建任务实例和更新 DAG Run 状态可能不在同一事务中
- 如果创建任务实例失败，DAG Run 状态可能不一致

**建议**:
- 确保关键操作在同一事务中
- 添加事务回滚处理
- 实现补偿机制

---

### 8. 缺少任务执行器示例（低优先级）

**问题**:
- 没有提供任务执行器的示例实现
- 开发者不知道如何创建自定义执行器

**建议**:
- 提供示例执行器实现
- 编写执行器开发文档

---

### 9. 缺少任务参数验证（中优先级）

**问题**:
- 任务类型有 `paramSchema`（JSON Schema），但没有参数验证逻辑
- 无效参数可能导致任务执行失败

**建议**:
- 实现 JSON Schema 验证
- 在任务执行前验证参数

---

### 10. 缺少任务执行监控（低优先级）

**问题**:
- 没有任务执行性能监控
- 无法统计任务执行时间、成功率等指标

**建议**:
- 添加指标收集
- 集成监控系统（如 Prometheus）

---

## 三、性能问题

### 1. 定时任务扫描效率（中优先级）

**问题**:
- `TaskWorkerService.processPendingTasks()` 每 5 秒扫描一次所有 PENDING 任务
- `DagRunMonitorService.monitorDagRuns()` 每 10 秒扫描一次所有 RUNNING 的 DAG Run
- 数据量大时可能影响性能

**建议**:
- 使用索引优化查询
- 限制每次扫描的数量
- 考虑使用消息队列

---

### 2. 数据库查询优化（中优先级）

**问题**:
- 某些查询可能没有使用索引
- N+1 查询问题

**建议**:
- 检查查询性能
- 添加必要的索引
- 使用 JOIN 查询减少数据库访问

---

## 四、安全性问题

### 1. 缺少权限控制（高优先级）

**问题**:
- Controller 层没有权限检查
- 任何用户都可以触发、暂停、删除 DAG

**建议**:
- 添加权限注解（如 `@PreAuthorize`）
- 实现租户级别的权限控制

---

### 2. 任务执行器安全（中优先级）

**问题**:
- 任务执行器可以执行任意代码
- 没有执行器白名单机制

**建议**:
- 实现执行器白名单
- 限制可执行的执行器类型
- 添加执行器权限验证

---

## 五、建议的修复优先级

### 高优先级（必须修复）
1. ✅ 节点级别的操作实现
2. ✅ 任务重试逻辑修复
3. ✅ 任务超时处理
4. ✅ 数据一致性问题
5. ✅ 权限控制

### 中优先级（应该修复）
1. ✅ JSON 参数解析
2. ✅ 任务执行结果存储格式
3. ✅ 并发策略实现
4. ✅ 任务参数验证
5. ✅ 错误处理完善

### 低优先级（可以后续优化）
1. ✅ 审计日志记录
2. ✅ 任务执行器注册机制
3. ✅ 任务执行监控
4. ✅ 性能优化

---

## 六、总结

当前调度模块的核心功能已基本实现，但还存在以下主要问题：

1. **功能缺失**: 节点级别操作、超时处理、并发策略等
2. **逻辑错误**: 重试逻辑、参数解析等
3. **安全问题**: 权限控制、执行器安全等
4. **性能问题**: 查询优化、扫描效率等

建议按照优先级逐步修复这些问题，确保系统的稳定性和安全性。

