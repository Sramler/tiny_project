# Tiny Idempotent Spring Boot Starter

幂等性功能的 Spring Boot Starter，提供基于 AOP 的接口幂等性控制。

## 功能特性

- ✅ 基于 AOP 的幂等性控制
- ✅ 支持 SpEL 表达式自定义 key
- ✅ 支持请求头 `X-Idempotency-Key`
- ✅ 支持数据库和 Redis 两种存储方式（Redis 需要自行实现）
- ✅ 自动配置，开箱即用
- ✅ 支持直接组合使用（`@PostMapping` + `@Idempotent`）

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>tiny-idempotent-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 使用数据库实现（默认），需要引入 JDBC -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

### 2. 配置（可选）

```yaml
tiny:
  idempotent:
    enabled: true                    # 是否启用（默认 true）
    storage-type: database           # 存储类型：database（默认）或 redis
    default-expire-time: 60          # 默认过期时间（秒，默认 60）
```

### 3. 使用

#### 方式 1：组合注解（推荐）

```java
@PostMapping("/users")
@Idempotent
public ResponseEntity<User> createUser(@RequestBody User user) {
    // ...
}
```

#### 方式 2：使用 SpEL 表达式

```java
@PostMapping("/orders")
@Idempotent(key = "#userId + ':' + #order.orderId", expireTime = 300)
public ResponseEntity<Order> createOrder(
        @RequestParam Long userId,
        @RequestBody Order order) {
    // ...
}
```

#### 方式 3：使用请求头

```bash
curl -X POST http://localhost:9000/api/users \
  -H "X-Idempotency-Key: unique-id-12345" \
  -H "Content-Type: application/json" \
  -d '{"username": "john"}'
```

## 详细文档

- [使用指南](../tiny-oauth-server/docs/IDEMPOTENT_USAGE_GUIDE.md)
- [接入流程](../tiny-oauth-server/docs/IDEMPOTENT_INTEGRATION_FLOW.md)
- [设计分析](../docs/idempotent-annotation-extension-analysis.md)

## 设计说明

### 依赖设计

- **不引入 JDBC/Redis 依赖**：这些运行时依赖由使用方根据需求引入
- **使用 `@ConditionalOnClass`**：自动检测类是否存在来决定使用哪个实现
- **最小化依赖**：只引入必要的核心依赖

### 存储方式

- **数据库（默认）**：使用 MySQL 表 `sys_idempotent_token` 存储
- **Redis（可选）**：需要使用时，由使用方自行实现 `IdempotentService`

## 许可证

Copyright © 2024 Tiny Project

