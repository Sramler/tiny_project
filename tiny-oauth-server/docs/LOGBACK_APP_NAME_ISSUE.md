# Logback 日志文件名 APP_NAME_IS_UNDEFINED 问题解决

## 问题描述

日志文件名显示为 `APP_NAME_IS_UNDEFINED.2025-11-26.0.log.gz`，而不是预期的 `oauth-server.2025-11-26.0.log.gz`。

同时，还出现了 `LOG_PATH_IS_UNDEFINED` 目录，说明 `LOG_PATH` 也没有正确读取。

## 问题原因

### 根本原因

1. **Logback 初始化时机早于 Spring 环境**
   - Logback 在 Spring 应用上下文初始化之前就开始初始化
   - 此时 `springProperty` 可能无法读取到 Spring 配置的值
   - 如果 `spring.application.name` 未配置或无法读取，`springProperty` 会返回占位符字符串（如 `${spring.application.name}`）

2. **占位符字符串被当作实际值**
   - 当 `APP_NAME` 变量包含占位符字符串时，logback 会将其作为字面值使用
   - 导致日志文件名变成 `APP_NAME_IS_UNDEFINED.log`

3. **多模块项目配置不一致**
   - 项目包含多个模块：`tiny_web`、`tiny-oauth-server`、`tiny-oauth-client`、`tiny-oauth-resource`
   - 如果某个模块没有正确配置 `spring.application.name`，就会导致问题

## 解决方案

### 方案：符合 Spring Boot 官方指南的混合配置（已实施）

**Spring Boot 官方指南**：使用 `springProperty` 标签可以从 `application.yaml` 中读取配置。

**问题**：logback 在 Spring 环境初始化之前就开始初始化，此时 `springProperty` 可能返回占位符字符串。

**解决方案**：使用混合配置，既支持在 `application.yaml` 中配置（符合官方指南），又解决了初始化时机问题。

修改 `logback-spring.xml`，使用多级备选方案：

```xml
<!-- LOG_PATH: 支持在 application.yaml 中配置，同时支持系统属性/环境变量覆盖 -->
<springProperty scope="context" name="LOG_PATH_SPRING" source="logging.file.path" defaultValue="logs"/>
<!-- 优先使用系统属性/环境变量，如果不存在则使用 Spring 配置的值，最后使用默认值 -->
<property name="LOG_PATH" value="${LOG_PATH:-${LOG_PATH_SPRING:-logs}}"/>

<!-- APP_NAME: 支持在 application.yaml 中配置，同时支持系统属性/环境变量覆盖 -->
<springProperty scope="context" name="APP_NAME_SPRING" source="spring.application.name" defaultValue="oauth-server"/>
<!-- 优先使用系统属性/环境变量，如果不存在则使用 Spring 配置的值，最后使用默认值 -->
<property name="APP_NAME" value="${APP_NAME:-${APP_NAME_SPRING:-oauth-server}}"/>
```

**配置优先级**：
1. 系统属性（`-DAPP_NAME=xxx` 或 `-DLOG_PATH=xxx`）
2. 环境变量（`APP_NAME=xxx` 或 `LOG_PATH=xxx`）
3. `application.yaml` 中的配置（`spring.application.name` 或 `logging.file.path`，通过 `springProperty` 读取）
4. 默认值（`oauth-server` 或 `logs`）

**优势**：
- ✅ 符合 Spring Boot 官方指南（支持在 `application.yaml` 中配置）
- ✅ 解决了初始化时机问题（即使 Spring 环境未初始化，也能使用默认值）
- ✅ 支持系统属性/环境变量覆盖（便于不同环境部署）

## 验证方法

### 1. 检查日志文件名

重启应用后，检查 `logs/` 目录下的日志文件名：

```bash
ls -la logs/
```

应该看到：
- ✅ `oauth-server.log`
- ✅ `oauth-server-error.log`
- ✅ `oauth-server-audit.log`
- ❌ 不应该有 `APP_NAME_IS_UNDEFINED.log`

### 2. 检查应用配置

确认所有模块的 `application.yaml` 中都配置了 `spring.application.name`：

```bash
# 检查 tiny-oauth-server
grep -A 2 "spring:" tiny-oauth-server/src/main/resources/application.yaml

# 检查 tiny_web
grep -A 2 "spring:" tiny_web/src/main/resources/application.yaml

# 检查 tiny-oauth-client
grep -A 2 "spring:" tiny-oauth-client/src/main/resources/application.yml

# 检查 tiny-oauth-resource
grep -A 2 "spring:" tiny-oauth-resource/src/main/resources/application.yml
```

### 3. 启动时设置系统属性

如果使用 IDE 启动，在运行配置中添加 VM 参数：

```
-DAPP_NAME=oauth-server
-DLOG_PATH=logs
```

## 各模块应用名配置

| 模块 | 应用名 | 配置文件位置 |
|------|--------|------------|
| tiny-oauth-server | `oauth-server` | `tiny-oauth-server/src/main/resources/application.yaml` |
| tiny_web | `tiny_web` | `tiny_web/src/main/resources/application.yaml` |
| tiny-oauth-client | `spring-oauth-client` | `tiny-oauth-client/src/main/resources/application.yml` |
| tiny-oauth-resource | `spring-oauth-resource` | `tiny-oauth-resource/src/main/resources/application.yml` |

## 最佳实践

1. **统一配置管理**
   - 所有模块都应该在 `application.yaml` 中配置 `spring.application.name`
   - 使用统一的命名规范（如：`模块名` 或 `服务名`）

2. **启动脚本标准化**
   - 在启动脚本中设置系统属性，确保日志文件名正确
   - 示例：
     ```bash
     #!/bin/bash
     APP_NAME=${APP_NAME:-oauth-server}
     LOG_PATH=${LOG_PATH:-logs}
     java -DAPP_NAME=$APP_NAME -DLOG_PATH=$LOG_PATH -jar app.jar
     ```

3. **Docker 部署**
   - 在 Dockerfile 或 docker-compose.yml 中设置环境变量
   - 示例：
     ```yaml
     environment:
       - APP_NAME=oauth-server
       - LOG_PATH=/app/logs
     ```

4. **Kubernetes 部署**
   - 在 Deployment 或 ConfigMap 中设置环境变量
   - 示例：
     ```yaml
     env:
       - name: APP_NAME
         value: "oauth-server"
       - name: LOG_PATH
         value: "/app/logs"
     ```

## 相关文件

- `tiny-oauth-server/src/main/resources/logback-spring.xml` - Logback 配置文件
- `tiny-oauth-server/src/main/resources/application.yaml` - Spring Boot 配置文件

## 参考文档

- [Logback 官方文档 - Property](http://logback.qos.ch/manual/configuration.html#property)
- [Spring Boot 官方文档 - Logging](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.logging)

