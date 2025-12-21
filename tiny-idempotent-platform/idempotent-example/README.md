# idempotent-example

幂等平台使用示例 - 快速开始和最佳实践

## 一、简介

`idempotent-example` 是 `tiny-idempotent-platform` 的使用示例项目，展示如何在实际业务中使用幂等平台。

### 包含内容

1. **HTTP 接口幂等示例**
   - 订单创建、支付、取消等接口的幂等实现
   - 展示 `@Idempotent` 注解的使用方法

2. **配置示例**
   - 轻量模式（内存存储）
   - 数据库模式
   - Redis 模式

3. **最佳实践**
   - Key 生成策略
   - TTL 设置
   - 异常处理

## 二、快速开始

### 1. 启动应用

```bash
# 轻量模式（内存存储，无需额外依赖）
mvn spring-boot:run

# 或使用数据库模式
mvn spring-boot:run -Dspring-boot.run.profiles=database

# 或使用 Redis 模式
mvn spring-boot:run -Dspring-boot.run.profiles=redis
```

应用启动后访问：http://localhost:8080

### 2. 测试幂等接口

#### 创建订单（幂等接口）

```bash
# 第一次请求
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORDER-20241221-001",
    "userId": 1001,
    "productId": 2001,
    "quantity": 2,
    "amount": 19900
  }'

# 重复请求（相同 orderNo）
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORDER-20241221-001",
    "userId": 1001,
    "productId": 2001,
    "quantity": 2,
    "amount": 19900
  }'
```

**预期结果**：
- 第一次请求：创建新订单，返回订单信息
- 第二次请求：返回相同的订单信息（幂等性保证）

#### 支付订单（幂等接口）

```bash
curl -X POST http://localhost:8080/api/orders/1/pay \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-20241221-001",
    "amount": 19900
  }'
```

### 3. 使用 HTTP API 接口

#### 获取幂等 Token

```bash
curl -X GET http://localhost:8080/idempotent/token?scope=order
```

#### 验证 Token

```bash
curl -X POST http://localhost:8080/idempotent/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "http:order:abc123..."
  }'
```

#### 执行幂等操作

```bash
curl -X POST http://localhost:8080/idempotent/execute \
  -H "Content-Type: application/json" \
  -d '{
    "key": "http:order:ORDER-20241221-001",
    "ttl": 300,
    "failOpen": false,
    "payload": {
      "message": "执行成功"
    }
  }'
```

## 三、代码示例

### 1. 使用 @Idempotent 注解

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping
    @Idempotent(
        key = "#request.orderNo",    // 使用订单号作为幂等 Key
        timeout = 300,                // 超时时间 5 分钟
        failOpen = false              // 失败时拒绝请求
    )
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        // 业务逻辑
        return ResponseEntity.ok(orderService.createOrder(request));
    }
}
```

### 2. Key 生成策略示例

```java
// 使用单个字段
@Idempotent(key = "#request.orderNo")

// 使用多个字段组合
@Idempotent(key = "#orderId + ':' + #paymentRequest.paymentId")

// 使用路径参数
@Idempotent(key = "#orderId + ':cancel'")
```

### 3. 配置说明

#### 轻量模式（默认）

```yaml
tiny:
  idempotent:
    enabled: true
    store: memory          # 使用内存存储
    ttl: 300               # 默认 TTL 5 分钟
    fail-open: false       # 失败策略：拒绝请求
```

#### 数据库模式

```yaml
tiny:
  idempotent:
    store: database        # 使用数据库存储
    ttl: 600               # TTL 10 分钟
```

需要配置数据源：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: password
```

#### Redis 模式

```yaml
tiny:
  idempotent:
    store: redis           # 使用 Redis 存储
    ttl: 900               # TTL 15 分钟
```

需要配置 Redis：
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 四、最佳实践

### 1. Key 生成策略

- ✅ **使用业务唯一标识**：订单号、支付ID 等
- ✅ **避免使用时间戳**：时间戳会导致每次请求 Key 都不同
- ✅ **组合 Key**：多个字段组合确保唯一性

### 2. TTL 设置

- **短期操作**（如支付）：300 秒（5 分钟）
- **中期操作**（如订单创建）：600 秒（10 分钟）
- **长期操作**（如退款）：1800 秒（30 分钟）

### 3. 失败策略

- **fail-open: false**（推荐）：幂等检查失败时拒绝请求，更安全
- **fail-open: true**：幂等检查失败时允许请求通过，适合高可用场景

### 4. 异常处理

示例应用已配置 `ExampleExceptionHandler`，统一处理异常并返回标准错误响应格式。

## 五、测试

### 单元测试

```bash
mvn test
```

### 集成测试

启动应用后，使用 curl 或 Postman 测试各个接口。

## 六、常见问题

### 1. 为什么重复请求返回不同的订单？

检查幂等 Key 是否正确生成。确保 `@Idempotent` 注解的 `key` 属性能够唯一标识请求。

### 2. 如何查看幂等执行日志？

设置日志级别：
```yaml
logging:
  level:
    com.tiny.idempotent: DEBUG
```

### 3. 内存模式数据会丢失吗？

是的，内存模式的数据在应用重启后会丢失。生产环境建议使用数据库或 Redis 模式。

## 七、更多信息

- [tiny-idempotent-platform README](../README.md)
- [幂等平台设计文档](../../docs/)

## 八、许可证

与 idempotent-platform 保持一致

