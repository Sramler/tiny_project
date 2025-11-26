# 调度任务执行器开发指南

本文档介绍如何在 `tiny-oauth-server` 的调度模块中创建、注册和调试自定义 TaskExecutor。

## 1. 基本概念

执行器接口位于 `TaskExecutorService.TaskExecutor`：

```java
public interface TaskExecutor {
    Object execute(Map<String, Object> params) throws Exception;
}
```

- `params`：运行时传递的参数，已合并任务默认参数、节点覆盖参数、实例覆盖参数。
- 返回值会被持久化到 `scheduling_task_instance.result`。
- 抛出的异常会被 Worker 捕获并记录到历史表，自动进入重试/失败流程。

## 2. 注册方式

1. 创建实现类并使用 `@Component("xxx")` 注解，Bean 名称会作为 `executor` 的推荐值。
2. 执行器会在启动时由 `TaskExecutorRegistry` 自动发现并注册。
3. 在任务类型（`SchedulingTaskType`）中填写 `executor` 字段即可绑定。
   - 推荐填 Bean 名称：例如 `loggingTaskExecutor`
   - 也可填类全名，注册表找不到时会回退按类名从 Spring 容器获取。

## 3. 示例执行器

项目内置了两个示例，位于 `com.tiny.scheduling.executor`：

| Bean 名称 | 描述 | 参数示例 |
|-----------|------|---------|
| `loggingTaskExecutor` | 打印参数并返回 echo 结果 | 无特殊参数 |
| `delayTaskExecutor` | 按参数延迟并可模拟失败 | `{"delayMs":2000,"fail":false}` |

这些示例可用于联调前后端或作为模板。

## 4. 调试建议

1. 使用 Actuator 的 `/actuator/health` 确认应用启动正常。
2. 创建任务类型 → 任务定义 → DAG → 版本/节点 → 手动触发 DAG/节点。
3. 通过 `/scheduling/task-instance/{id}/log` 或数据库表查看执行结果。
4. 通过任务运行历史与日志快速验证结果，必要时查看数据库中 `scheduling_task_history`、`scheduling_task_instance`。

## 5. 一键脚本 & 前端演示

- `docs/scripts/scheduling-task-type-demo.http`：提供 VSCode Rest Client / IntelliJ HTTP Client 可直接执行的示例脚本，自动创建 `loggingTaskExecutor` 与 `delayTaskExecutor` 任务类型。
- 前端 `调度中心 -> 任务类型` 页面新增 “示例执行器快速体验” 区域，可一键创建示例类型并马上在 UI 中查看/编辑。

## 5. 最佳实践

- 明确输入/输出 Schema，并在 `paramSchema` 中声明必填字段。
- 处理幂等：同一任务可能因重试被多次执行。
- 对外部系统调用要设置超时，并在执行器内部进行异常捕获与语义化报错。
- 如需访问 Spring Bean，可注入依赖；执行器本身是普通 Spring 组件。

