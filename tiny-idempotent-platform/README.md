# tiny-idempotent-platform

平台级幂等基础设施 - 企业级幂等性解决方案

---

## Part 1：平台目标与设计哲学 + Maven 多模块结构

### 一、平台目标与设计哲学

`tiny-idempotent-platform` 是一个"平台级、可裁剪、可插拔"的幂等基础设施，目标是：

1. **统一解决 HTTP / MQ / Job / 分布式任务 的幂等问题**
2. **对业务开发者"易用、低心智负担"**
3. **对平台架构"清晰分层、长期可演进"**
4. **支持从【单体轻量应用】到【中台 / SaaS 平台】的平滑升级**

#### 核心哲学

- **幂等是基础设施，不是业务代码**
- **Core 定义"幂等是什么"，Infra 定义"幂等怎么落地"**
- **默认安全、显式开启、可观测、可治理**

### 二、Maven 多模块总体结构

```
tiny-idempotent-platform/
├── pom.xml                         # 父 POM（dependencyManagement）
├── idempotent-core/                # 幂等核心引擎（纯 Java）
├── idempotent-sdk/                 # 给业务使用的 SDK
├── idempotent-starter/             # Spring Boot 自动装配
├── idempotent-repository/          # 存储层实现（infra）
│   ├── redis/
│   ├── database/
│   ├── memory/
│   └── custom/                     # 用户自定义实现
├── idempotent-mq/                  # MQ 幂等实现（infra）
│   ├── kafka/
│   ├── rabbitmq/
│   ├── rocketmq/
│   └── in-memory/
├── idempotent-control/             # 平台治理能力
└── idempotent-console/             # Web 管控台
```

---

## Part 2：package 级目录规范 + Repository & MQ 抽象

### 三、package 级目录规范（关键）

#### 3.1 idempotent-core（最严格）

```
com.tiny.idempotent.core
├── engine/        # 幂等执行引擎 & 状态流转
├── key/           # 幂等 Key 规范
├── record/        # 幂等状态模型
├── repository/    # Repository 抽象接口
├── mq/            # MQ 幂等抽象接口
├── context/       # 执行上下文
├── strategy/      # Key / TTL / Retry 策略
├── exception/     # 核心异常
└── spi/           # SPI 扩展点（非 Spring）
```

**禁止出现：Spring / Redis / MQ Client / JDBC / Servlet / HTTP / JSON**

#### 3.2 idempotent-sdk

```
com.tiny.idempotent.sdk
├── annotation/    # @Idempotent / @IdempotentConsume
├── aspect/        # AOP 拦截
├── facade/        # 幂等统一入口
├── resolver/      # 幂等 Key 解析
├── extractor/     # 请求 / 消息上下文提取
└── support/       # 内部工具
```

#### 3.3 idempotent-starter

```
com.tiny.idempotent.starter
├── autoconfigure/
├── condition/
├── properties/
├── registrar/
└── initializer/
```

**职责：装配 Bean + classpath 裁剪 + 不写业务逻辑**

### 四、Repository & MQ 抽象归属原则

- **Repository 抽象必须在 core**
- **MQ 幂等抽象必须在 core**
- **所有实现只能在 infra（repository / mq 模块）**

---

## Part 3：Starter 裁剪策略 + 幂等 Key 规范

### 五、Starter 自动裁剪（轻量 vs 全量）

#### 轻量模式（默认）

- **InMemoryRepository + HTTP 幂等 + 无 MQ**
- 适合：开发环境、测试、轻量应用

#### 全量模式

- **Redis / MQ 存在时自动装配 + Console + Platform Governance**
- 适合：生产环境、中台 / SaaS 平台

### 六、HTTP / MQ / Job 幂等 Key 规范

统一 Key 结构：**IdempotentKey = namespace : scope : uniqueKey**

#### HTTP

```
namespace: http
scope: method+path (如: POST:/api/orders)
uniqueKey: userId+bodyHash 或 X-Idempotency-Key
示例: http:POST:/api/orders:userId123:abc123def456
```

#### MQ

```
namespace: mq
scope: topic (如: order.create)
uniqueKey: partition+offset 或 messageId
示例: mq:kafka:order.create:partition0:offset12345
或: mq:rabbitmq:order.create:messageId123
```

#### Job

```
namespace: job
scope: jobName (如: orderSync)
uniqueKey: bizDate+shardId
示例: job:orderSync:20241220:shard0
```

---

## Part 4：平台治理能力 + 依赖方向 + 核心原则

### 七、平台治理能力（idempotent-control）

- **幂等规则注册 / 下发**
- **限流 / 熔断**
- **幂等命中率统计**
- **失败重试策略**
- **黑名单 / 白名单**

### 八、依赖方向（必须遵守）

```
business 
   ↓
idempotent-sdk (注解 + Facade)
   ↓
idempotent-core (engine + repository 抽象 + MQ 抽象)
   ↑
idempotent-repository / idempotent-mq (Redis / DB / Kafka / RabbitMQ 实现)
   ↑
idempotent-starter (Spring Boot 自动装配)
   ↑
idempotent-control / idempotent-console (治理/监控)
```

**任何反向依赖 = 架构错误**

### 九、核心原则总结

1. **core 永远不依赖 infra / Spring / MQ**
2. **SDK + Starter 提供可落地接口**
3. **Repository + MQ Handler 完全可替换、可插拔**
4. **Facade + 注解 = 业务唯一入口**
5. **轻量模式默认安全，全量模式可配置**
6. **Console / Control 提供可观测、可治理能力**

---

## Part 5：工程化示例 + Console 数据模型 + 架构总览

### 1. Maven 依赖示例

#### 轻量模式

```xml
<!-- 只需要这一个依赖 -->
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

**配置（可选）：**

```yaml
tiny:
  idempotent:
    enabled: true
    store: memory  # 轻量模式，使用内存存储
    ttl: 300
    fail-open: true
```

#### 全量模式

```xml
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- 使用 Redis 存储 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- 可选：MQ 幂等支持 -->
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-mq-kafka</artifactId>
</dependency>

<!-- 可选：平台治理和控制台 -->
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-control</artifactId>
</dependency>
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-console</artifactId>
</dependency>
```

**配置：**

```yaml
tiny:
  idempotent:
    enabled: true
    store: redis  # 使用 Redis 存储
    ttl: 300
    fail-open: false  # 生产环境建议 fail-close
```

### 2. Starter 自动配置示例

自动配置会根据 classpath 和配置自动选择：

- **如果存在 Redis** → 使用 `RedisIdempotentRepository`
- **如果存在 JDBC** → 使用 `DatabaseIdempotentRepository`
- **否则** → 使用 `MemoryIdempotentRepository`（轻量模式）

### 3. 业务使用示例

#### 方式 1：使用注解（推荐）

```java
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @PostMapping
    @Idempotent(key = "#userId + ':' + #request.orderNo", timeout = 300)
    public ResponseEntity<Order> createOrder(
            @RequestParam String userId,
            @RequestBody OrderRequest request) {
        // 业务逻辑
        Order order = orderService.create(userId, request);
        return ResponseEntity.ok(order);
    }
}
```

#### 方式 2：使用 Facade

```java
@Service
public class OrderService {
    
    @Autowired
    private IdempotentFacade idempotentFacade;
    
    public Order createOrder(String userId, String orderNo, OrderRequest request) {
        // 构建幂等性 Key
        IdempotentKey key = IdempotentKey.of(
            "http",                                    // namespace
            "OrderService.createOrder",                // scope
            userId + ":" + orderNo                    // uniqueKey
        );
        
        // 构建策略
        IdempotentStrategy strategy = new IdempotentStrategy(300, true);
        
        // 构建上下文
        IdempotentContext context = new IdempotentContext(key, strategy);
        
        // 执行幂等性保护的业务逻辑
        return idempotentFacade.execute(context, () -> {
            // 业务逻辑
            return doCreateOrder(userId, orderNo, request);
        });
    }
}
```

#### 方式 3：MQ 幂等（示例，MQ 模块实现后）

```java
@Component
public class OrderMqConsumer {
    
    @KafkaListener(topics = "order.create")
    @IdempotentMqConsume(topic = "order.create", keyExpression = "#record.key()")
    public void handleOrderCreate(ConsumerRecord<String, String> record) {
        // 业务逻辑
        Order order = parseOrder(record.value());
        orderService.process(order);
    }
}
```

### 4. Console API / 数据模型示意

#### Console 数据模型

```sql
-- 幂等规则表
CREATE TABLE idempotent_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    scene VARCHAR(50) NOT NULL COMMENT '场景：http/mq/job',
    biz_code VARCHAR(100) NOT NULL COMMENT '业务代码',
    key_pattern VARCHAR(500) COMMENT 'Key 模式',
    ttl INT COMMENT 'TTL（秒）',
    enabled BOOLEAN DEFAULT TRUE,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scene_biz_code (scene, biz_code)
) COMMENT='幂等规则表';

-- 幂等记录表
CREATE TABLE idempotent_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    idempotent_key VARCHAR(512) NOT NULL COMMENT '幂等性 Key',
    status VARCHAR(20) NOT NULL COMMENT '状态：PENDING/SUCCESS/FAILED/EXPIRED',
    owner_instance VARCHAR(100) COMMENT '处理实例',
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    error_message TEXT,
    INDEX idx_key (idempotent_key),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) COMMENT='幂等记录表';

-- 幂等指标表
CREATE TABLE idempotent_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL,
    scene VARCHAR(50) NOT NULL,
    hit_count BIGINT DEFAULT 0 COMMENT '命中次数（重复请求）',
    pass_count BIGINT DEFAULT 0 COMMENT '通过次数（首次请求）',
    reject_count BIGINT DEFAULT 0 COMMENT '拒绝次数（失败）',
    UNIQUE KEY uk_date_scene (date, scene)
) COMMENT='幂等指标表';
```

#### Console REST API 示例

```java
@RestController
@RequestMapping("/api/idempotent/console")
public class IdempotentConsoleController {
    
    // 查询幂等记录
    @GetMapping("/records")
    public PageResult<IdempotentRecord> queryRecords(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String status,
            @RequestParam int page,
            @RequestParam int size) {
        // ...
    }
    
    // 查询统计指标
    @GetMapping("/metrics")
    public List<IdempotentMetric> queryMetrics(
            @RequestParam String date,
            @RequestParam(required = false) String scene) {
        // ...
    }
    
    // 更新规则
    @PostMapping("/rules")
    public void updateRule(@RequestBody IdempotentRule rule) {
        // ...
    }
}
```

### 5. 架构总览

```
┌─────────────────────────────────────┐
│        业务系统                      │
│  (HTTP Controller / Service)        │
└───────────────┬─────────────────────┘
                │
┌───────────────┴─────────────────────┐
│    idempotent-sdk                   │
│  (注解 + Facade)                    │
└───────────────┬─────────────────────┘
                │
┌───────────────┴─────────────────────┐
│    idempotent-core                  │
│  (engine + repository抽象 + MQ抽象) │
└───────────────┬─────────────────────┘
                │
    ┌───────────┴───────────┐
    │                       │
┌───┴──────────┐    ┌───────┴────────┐
│ idempotent-  │    │ idempotent-mq  │
│ repository   │    │ (Kafka/Rabbit) │
│ (Redis/DB)   │    │                │
└───┬──────────┘    └───────┬────────┘
    │                       │
    └───────────┬───────────┘
                │
┌───────────────┴─────────────────────┐
│    idempotent-starter               │
│  (Spring Boot 自动装配)             │
└───────────────┬─────────────────────┘
                │
    ┌───────────┴───────────┐
    │                       │
┌───┴──────────┐    ┌───────┴────────┐
│ idempotent-  │    │ idempotent-    │
│ control      │    │ console        │
│ (治理)       │    │ (监控/审计)    │
└──────────────┘    └────────────────┘
```

---

## Part 6：HTTP 接口设计

### 一、业务接口（HTTP 幂等执行相关 - 轻量模式）

业务接口位于 `idempotent-starter` 模块，通过配置 `tiny.idempotent.http-api.enabled=true` 启用。

#### 1. 获取幂等 Token
- **GET/POST** `/idempotent/token`
- **描述**：获取一次性幂等 Token（适用于前端生成请求）
- **参数**：
  - `scope`（可选）：作用域，如业务场景标识
- **响应示例**：
```json
{
  "success": true,
  "token": "http:default:abc123def456",
  "ttl": 300,
  "expireAt": "2024-12-21T18:00:00"
}
```

#### 2. 幂等执行通用接口
- **POST** `/idempotent/execute`
- **描述**：幂等执行通用接口，接收 Key / BizContext / Payload
- **请求体**：
```json
{
  "key": "http:OrderService.createOrder:userId123:orderNo456",
  "ttl": 300,
  "failOpen": true,
  "payload": { ... },
  "bizContext": { ... }
}
```
- **响应示例**：
```json
{
  "success": true,
  "result": { ... },
  "key": "http:OrderService.createOrder:userId123:orderNo456"
}
```

#### 3. 验证 Key 是否可用（预检）
- **POST** `/idempotent/validate`
- **描述**：验证 Key 是否可用（预检）
- **请求体**：
```json
{
  "token": "http:default:abc123def456"
}
```
- **响应示例**：
```json
{
  "success": true,
  "valid": true,
  "token": "http:default:abc123def456"
}
```

#### 4. 主动释放 Key
- **POST** `/idempotent/release`
- **描述**：主动释放 Key（避免长期占用，可选）
- **请求体**：
```json
{
  "token": "http:default:abc123def456"
}
```

### 二、平台治理接口（Control / Console 使用 - 全量模式）

治理接口位于 `idempotent-console` 模块，需要引入 `idempotent-console` 依赖。

#### 1. 规则管理
- **GET** `/console/rules` - 查询幂等规则（支持分页、过滤）
- **POST** `/console/rules` - 新增幂等规则
- **PUT** `/console/rules/{id}` - 修改幂等规则
- **DELETE** `/console/rules/{id}` - 删除幂等规则
- **POST** `/console/rules/enable` - 批量启用规则
- **POST** `/console/rules/disable` - 批量禁用规则

#### 2. 记录查询
- **GET** `/console/records` - 查询幂等执行记录（支持分页、过滤）
- **POST** `/console/records/retry` - 手动触发失败记录的补偿/重试

#### 3. 统计指标
- **GET** `/console/metrics` - 获取命中率、成功率、失败率等统计指标

#### 4. 黑名单管理
- **GET** `/console/blacklist` - 查询黑名单
- **POST** `/console/blacklist` - 添加黑名单
- **DELETE** `/console/blacklist/{id}` - 删除黑名单

### 三、监控/统计接口

#### 1. 幂等执行统计
- **GET** `/metrics/idempotent` - 返回幂等执行统计（命中、成功、失败、冲突率）

#### 2. 热点 Key 统计
- **GET** `/metrics/idempotent/top-keys` - 热点 Key 统计，便于调优缓存和规则

#### 3. MQ 幂等统计
- **GET** `/metrics/idempotent/mq` - MQ 幂等消息处理统计（成功/失败/重复率）

### 四、配置说明

#### 启用 HTTP 业务接口（轻量模式）
```yaml
tiny:
  idempotent:
    enabled: true
    http-api:
      enabled: true  # 启用 HTTP API 接口
    store: database
    ttl: 300
    fail-open: true
```

#### 启用治理接口（全量模式）
```xml
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-console</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.tiny</groupId>
    <artifactId>idempotent-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置（可选）

```yaml
tiny:
  idempotent:
    enabled: true
    store: database  # database / redis / memory
    ttl: 300
    fail-open: true
```

### 3. 使用注解

```java
@PostMapping("/orders")
@Idempotent(key = "#userId + ':' + #orderNo", timeout = 300)
public Order createOrder(@RequestParam String userId, @RequestBody OrderRequest request) {
    // 业务逻辑
}
```

---

## 模块说明

- **idempotent-core**: 核心引擎，纯 Java 实现，无框架依赖
- **idempotent-sdk**: 业务使用层，提供注解和 Facade
- **idempotent-starter**: Spring Boot 自动配置，包含 HTTP 业务接口（可选）
- **idempotent-repository**: 存储实现（Redis/DB/Memory）
- **idempotent-mq**: MQ 幂等实现（Kafka/RabbitMQ/RocketMQ）（待实现）
- **idempotent-control**: 平台治理能力（待实现）
- **idempotent-console**: Web 管控台（已实现基础接口，治理功能待完善）

### 异常处理

所有 HTTP 接口使用 `tiny-common-exception` 模块提供统一的异常响应格式：

- **成功响应**：保持向后兼容的 Map 格式
- **错误响应**：使用 `ErrorResponse` 统一格式（包含 code、message、detail、status、path、timestamp）
- **异常处理**：Controller 继承 `BaseExceptionHandler`，自动处理参数验证、运行时异常等通用异常

---

## 版本

- **当前版本**: 1.0.0-SNAPSHOT
- **Java 版本**: 21
- **Spring Boot 版本**: 3.5.8

---

## 许可证

Copyright © 2024 Tiny Project

