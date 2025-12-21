# problem-spring-web 集成方案分析

## 当前状态

### 1. tiny_web 项目
- ✅ 已引入 `problem-spring-web-starter` (v0.29.1)
- ❌ `GlobalExceptionHandling` 被注释（未启用）
- ✅ 有 `ProblemFormatAdvice` 用于格式化 Problem 响应

### 2. tiny-oauth-server 项目
- ❌ 未引入 `problem-spring-web`
- ✅ 使用自定义 `GlobalExceptionHandler`
- ✅ 处理了 `IdempotentException`、`MethodArgumentNotValidException` 等

## 方案对比

### 方案 1：父 POM 统一管理版本（推荐 ⭐⭐⭐）

#### 实现方式
在 `tiny_project/pom.xml` 的 `dependencyManagement` 中统一管理版本：

```xml
<dependencyManagement>
    <dependencies>
        <!-- problem-spring-web 版本管理 -->
        <dependency>
            <groupId>org.zalando</groupId>
            <artifactId>problem-spring-web-starter</artifactId>
            <version>0.29.1</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

各子项目按需引入：
```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>problem-spring-web-starter</artifactId>
</dependency>
```

#### 优点
- ✅ **版本统一管理**：所有项目使用同一版本，避免冲突
- ✅ **灵活性强**：各项目可按需引入，不强制依赖
- ✅ **实现简单**：只需在父 POM 中添加版本管理
- ✅ **符合 Maven 最佳实践**：版本统一管理，依赖按需引入

#### 缺点
- ❌ 每个项目需要单独引入依赖
- ❌ 异常处理逻辑需要在各项目中实现

#### 适用场景
- 多个项目需要统一版本
- 不同项目可能有不同的异常处理需求
- 希望保持项目间解耦

---

### 方案 2：封装为 Starter（不推荐 ⭐）

#### 实现方式
创建一个 `tiny-exception-starter` 模块：
```
tiny-exception-starter/
├── pom.xml
└── src/main/java/
    └── com/tiny/exception/
        ├── autoconfigure/
        │   └── ExceptionAutoConfiguration.java
        └── handler/
            └── GlobalExceptionHandler.java
```

#### 优点
- ✅ 异常处理逻辑统一封装
- ✅ 自动配置，使用方便
- ✅ 可以统一异常响应格式

#### 缺点
- ❌ **过度设计**：异常处理通常是业务相关的，不适合抽象到基础设施层
- ❌ **灵活性差**：不同项目的异常处理需求差异较大（如 `tiny-oauth-server` 需要处理 `IdempotentException`）
- ❌ **维护成本高**：需要维护单独的 starter 模块
- ❌ **扩展困难**：业务自定义异常处理会被限制

#### 适用场景
- 异常处理逻辑完全相同
- 所有项目都需要相同的异常处理策略
- 团队希望完全统一异常响应格式

---

### 方案 3：父 POM 管理 + 共享基类（推荐 ⭐⭐⭐⭐）

#### 实现方式
1. 在父 POM 中统一管理版本（同方案 1）
2. 创建一个共享的 `common-exception` 模块，提供基础异常处理类
3. 各项目继承并扩展

```
tiny_project/
├── common-exception/          # 新增共享模块
│   └── src/main/java/
│       └── com/tiny/exception/
│           └── BaseExceptionHandler.java
├── tiny-oauth-server/
│   └── GlobalExceptionHandler extends BaseExceptionHandler
└── tiny_web/
    └── GlobalExceptionHandler extends BaseExceptionHandler
```

#### 优点
- ✅ **版本统一管理**
- ✅ **基础逻辑复用**：通用异常处理逻辑统一
- ✅ **灵活扩展**：各项目可以覆盖或扩展特定异常处理
- ✅ **代码复用**：减少重复代码

#### 缺点
- ❌ 需要额外创建一个模块
- ❌ 项目间存在依赖关系（但可接受）

#### 适用场景
- 多个项目有部分相同的异常处理逻辑
- 希望统一基础异常响应格式
- 同时保持各项目的自定义能力

---

## 推荐方案：方案 1 + 方案 3 混合

### 实施步骤

#### Step 1: 在父 POM 中统一管理版本

```xml
<!-- tiny_project/pom.xml -->
<dependencyManagement>
    <dependencies>
        <!-- problem-spring-web 版本管理 -->
        <dependency>
            <groupId>org.zalando</groupId>
            <artifactId>problem-spring-web-starter</artifactId>
            <version>0.29.1</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Step 2: 创建 common-exception 模块（可选）

如果多个项目有相同的异常处理逻辑，可以创建共享模块。

#### Step 3: 各项目按需引入

```xml
<!-- tiny-oauth-server/pom.xml -->
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>problem-spring-web-starter</artifactId>
</dependency>
```

#### Step 4: 实现项目级别的 GlobalExceptionHandler

各项目可以根据需求实现自己的异常处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler implements ProblemHandling {
    
    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<Problem> handleIdempotentException(
            IdempotentException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withType(URI.create("https://example.org/problems/idempotent"))
            .withTitle("幂等性检查失败")
            .withStatus(Status.CONFLICT)
            .withDetail(ex.getMessage())
            .build();
        return create(ex, problem, request);
    }
    
    // 其他异常处理...
}
```

---

## 为什么不推荐 Starter 方式？

### 1. 异常处理是业务相关的
- 不同项目的异常类型不同（如 `IdempotentException` 只在特定项目使用）
- 异常响应格式可能因项目而异
- 异常处理策略可能不同（如有些项目需要国际化，有些不需要）

### 2. 灵活性要求高
- 业务可能需要在异常处理中添加业务逻辑
- 可能需要集成业务监控、日志等
- 异常处理可能需要访问项目的特定 Bean

### 3. Starter 的适用场景
Starter 更适合：
- ✅ **配置类功能**：如数据库连接、Redis 配置
- ✅ **基础设施**：如幂等性、限流、链路追踪
- ✅ **通用工具**：如日志、监控

异常处理属于**业务代码**，不适合封装为 Starter。

---

## 实施建议

### 立即行动
1. ✅ 在父 POM 中添加 `problem-spring-web-starter` 版本管理
2. ✅ 在 `tiny-oauth-server` 中引入依赖
3. ✅ 重构 `GlobalExceptionHandler`，使用 `ProblemHandling` 接口

### 后续优化
1. 评估是否需要 `common-exception` 模块
2. 统一异常响应格式（如统一使用 Problem 格式）
3. 考虑添加异常监控和告警

---

## 总结

| 方案 | 推荐度 | 适用场景 |
|------|--------|----------|
| 父 POM 统一管理版本 | ⭐⭐⭐⭐⭐ | **推荐**：多项目统一版本，各项目按需引入 |
| 父 POM + 共享基类 | ⭐⭐⭐⭐ | 多项目有相同基础逻辑时 |
| 封装为 Starter | ⭐ | **不推荐**：异常处理是业务相关，不适合抽象 |

**最终建议**：使用**方案 1（父 POM 统一管理版本）**，各项目按需引入并实现自己的异常处理逻辑。

