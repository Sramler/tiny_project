# common-exception 方案收益分析

## 当前状态对比

### tiny-oauth-server
- ✅ 使用简单的 `GlobalExceptionHandler`
- ✅ 返回 `ResponseEntity<String>`（纯文本响应）
- ✅ 处理：`IdempotentException`、`MethodArgumentValidException`、`RuntimeException`、`Exception`
- ❌ 无统一错误码规范
- ❌ 无统一响应格式

### tiny_web
- ✅ 定义了 `ResponseCode` 枚举（统一错误码）
- ✅ 有 `BusinessException` 和 `GlobalResponse`（统一响应格式）
- ✅ 使用 `ProblemFormatAdvice` 格式化 Problem 响应
- ❌ `GlobalExceptionHandling` 被注释（未启用）
- ✅ 使用 `problem-spring-web-starter`

## common-exception 收益分析

### ✅ 显著收益（⭐⭐⭐⭐⭐）

#### 1. 统一异常响应格式
**当前问题**：
- `tiny-oauth-server` 返回纯文本：`"操作失败: xxx"`
- `tiny_web` 返回 Problem 格式（但未启用）

**收益**：
- ✅ 前端可以统一处理异常响应
- ✅ API 文档更规范
- ✅ 便于监控和日志分析
- ✅ 符合 REST API 最佳实践（RFC 7807）

#### 2. 减少重复代码
**当前重复代码**：
```java
// tiny-oauth-server
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<String> handleValidationException(...) {
    BindingResult bindingResult = ex.getBindingResult();
    List<String> errors = bindingResult.getFieldErrors().stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.toList());
    String errorMessage = "参数验证失败: " + String.join(", ", errors);
    return ResponseEntity.badRequest().body(errorMessage);
}

// tiny_web 也需要类似的代码
```

**收益**：
- ✅ 一次实现，多处复用
- ✅ 维护成本降低（修改一处，所有项目生效）
- ✅ 减少 bug 风险

#### 3. 统一错误码规范
**当前问题**：
- `tiny-oauth-server` 无错误码
- `tiny_web` 有 `ResponseCode` 枚举

**收益**：
- ✅ 统一的错误码体系
- ✅ 便于错误定位和统计
- ✅ 便于前端错误处理
- ✅ 便于监控和告警

#### 4. 统一日志格式
**当前问题**：
- 各项目日志格式不统一
- 日志级别使用不一致

**收益**：
- ✅ 统一的日志格式（便于日志分析）
- ✅ 统一的日志级别策略
- ✅ 便于集成日志中心

### ⚠️ 中等收益（⭐⭐⭐）

#### 5. 统一异常处理策略
- 统一的异常分类和处理规则
- 统一的异常转换逻辑
- 便于扩展（新项目直接继承）

#### 6. 便于国际化支持
- 统一的消息国际化机制
- 多语言错误消息支持

### ❌ 潜在问题（需要注意）

#### 1. 灵活性可能受限
**问题**：某些项目可能需要特殊的异常处理逻辑

**解决方案**：
- ✅ 使用**模板方法模式**，允许子类覆盖特定方法
- ✅ 提供**钩子方法**（Hook），允许扩展

#### 2. 模块间依赖
**问题**：增加模块依赖关系

**影响**：
- ✅ 依赖很轻量（只是异常处理基类）
- ✅ 不涉及业务逻辑，依赖关系清晰

#### 3. 初始投入
**问题**：需要创建模块和迁移代码

**影响**：
- ✅ 一次投入，长期收益
- ✅ 可以逐步迁移（不强制所有项目立即使用）

## 实施建议

### 方案设计

#### 1. 模块结构
```
common-exception/
├── pom.xml
└── src/main/java/com/tiny/common/exception/
    ├── base/
    │   └── BaseExceptionHandler.java        # 基础异常处理器
    ├── response/
    │   ├── ErrorResponse.java               # 统一错误响应 DTO
    │   └── ErrorResponseBuilder.java        # 响应构建器
    ├── code/
    │   └── ErrorCode.java                   # 错误码枚举（通用）
    └── util/
        └── ExceptionUtils.java              # 异常工具类
```

#### 2. 基础异常处理器设计

```java
@RestControllerAdvice
public abstract class BaseExceptionHandler {
    
    // 通用异常处理（final，不允许覆盖）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentValidException ex) {
        // 统一的参数验证异常处理
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, ex);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex) {
        // 统一的运行时异常处理
        return buildErrorResponse(ErrorCode.INTERNAL_ERROR, ex);
    }
    
    // 业务异常处理（抽象方法，子类实现）
    @ExceptionHandler(Exception.class)
    public abstract ResponseEntity<ErrorResponse> handleBusinessException(
            Exception ex);
    
    // 钩子方法（子类可以覆盖）
    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode, Exception ex) {
        // 默认实现，子类可以覆盖
        return ErrorResponseBuilder.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .detail(getExceptionDetail(ex))
            .build();
    }
    
    // 工具方法（子类可以使用）
    protected String getExceptionDetail(Exception ex) {
        // 统一的异常详情提取逻辑
    }
}
```

#### 3. 项目使用示例

**tiny-oauth-server**：
```java
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
    
    // 只需要处理项目特定的异常
    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<ErrorResponse> handleIdempotentException(
            IdempotentException ex) {
        return buildErrorResponse(ErrorCode.IDEMPOTENT_CONFLICT, ex);
    }
    
    // 覆盖默认的业务异常处理（如果需要）
    @Override
    public ResponseEntity<ErrorResponse> handleBusinessException(
            Exception ex) {
        // 项目特定的业务异常处理逻辑
        return super.handleBusinessException(ex);
    }
}
```

**tiny_web**：
```java
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex) {
        ResponseCode rc = ex.getResponseCode();
        return ErrorResponseBuilder.builder()
            .code(rc.getCode())
            .message(rc.getMessage())
            .status(rc.getStatus().value())
            .build();
    }
    
    // 可以覆盖默认实现，使用 Problem 格式
    @Override
    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode, Exception ex) {
        // 使用 Problem 格式
        return ProblemResponseBuilder.builder()...
    }
}
```

## 收益量化

### 代码减少
- **参数验证异常处理**：每个项目 ~15 行 → 0 行（继承基类）
- **通用异常处理**：每个项目 ~10 行 → 0 行
- **响应格式构建**：每个项目 ~20 行 → 0 行
- **总计**：每个项目减少 ~45 行代码

### 维护成本
- **修改响应格式**：1 处修改 vs N 处修改（N = 项目数量）
- **添加通用异常处理**：1 处添加 vs N 处添加
- **统一错误码**：1 套规范 vs N 套规范

### 开发效率
- **新项目启动**：减少异常处理开发时间 ~1 小时
- **统一规范**：减少团队讨论和沟通成本
- **错误定位**：统一的错误码，便于问题排查

## 实施步骤

### Phase 1: 创建模块（1-2 小时）
1. 创建 `common-exception` 模块
2. 定义基础异常处理器
3. 定义统一错误响应格式

### Phase 2: 迁移 tiny-oauth-server（1 小时）
1. 引入 `common-exception` 依赖
2. `GlobalExceptionHandler` 继承 `BaseExceptionHandler`
3. 测试验证

### Phase 3: 迁移 tiny_web（1-2 小时）
1. 引入 `common-exception` 依赖
2. 整合 `ResponseCode` 和 `common-exception` 的错误码
3. 迁移异常处理器
4. 测试验证

### Phase 4: 优化和扩展（持续）
1. 根据使用反馈优化
2. 添加新的通用异常处理
3. 完善文档

## 总结

### 收益评分

| 维度 | 评分 | 说明 |
|------|------|------|
| **代码复用** | ⭐⭐⭐⭐⭐ | 显著减少重复代码 |
| **统一规范** | ⭐⭐⭐⭐⭐ | 统一响应格式和错误码 |
| **开发效率** | ⭐⭐⭐⭐ | 新项目快速启动 |
| **维护成本** | ⭐⭐⭐⭐⭐ | 维护成本显著降低 |
| **灵活性** | ⭐⭐⭐⭐ | 通过继承和钩子保持灵活 |

### 最终建议

**强烈推荐实施 common-exception 方案** ⭐⭐⭐⭐⭐

**理由**：
1. ✅ **收益明显**：代码减少、规范统一、维护成本降低
2. ✅ **风险可控**：可以逐步迁移，不影响现有功能
3. ✅ **扩展性好**：通过继承和钩子方法保持灵活性
4. ✅ **长期价值**：随着项目增多，收益会放大

**实施优先级**：
1. 🔥 **高优先级**：创建模块，定义基础结构
2. 🔥 **高优先级**：迁移 `tiny-oauth-server`（代码简单，风险低）
3. ⚠️ **中优先级**：迁移 `tiny_web`（需要整合现有 ResponseCode）
4. 📝 **低优先级**：持续优化和扩展

