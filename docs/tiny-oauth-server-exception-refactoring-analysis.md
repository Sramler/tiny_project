# tiny-oauth-server 异常响应格式重构分析

## 当前状态

### ✅ 已使用统一格式
- `GlobalExceptionHandler` 已继承 `BaseExceptionHandler`
- 异常抛出会返回 `ErrorResponse` 格式：
```json
{
  "code": 1002,
  "message": "缺少参数",
  "detail": "token 不能为空",
  "status": 400,
  "path": "/idempotent/validate",
  "timestamp": "2024-12-21T18:40:00"
}
```

### ❌ 未统一格式的地方

#### 1. Controller 中手动返回的错误（约 25+ 处）
**位置**：
- `UserController`：多处使用 `Map.of("success", false, "error", "...")`
- `SecurityController`：多处使用 `Map.of("success", false, "error", "未登录")`
- `MenuController`、`ProcessController` 等

**示例**：
```java
// 当前格式
return ResponseEntity.status(401).body(Map.of(
    "success", false,
    "error", "用户未认证"
));

// 应该改为
return ResponseEntity.status(401).body(ErrorResponse.builder()
    .code(ErrorCode.UNAUTHORIZED.getCode())
    .message(ErrorCode.UNAUTHORIZED.getMessage())
    .detail("用户未认证")
    .status(401)
    .build());
```

#### 2. Service 层返回的错误（约 15+ 处）
**位置**：
- `SecurityServiceImpl`：多处返回 `Map.of("success", false, "error", "...")`

**示例**：
```java
// 当前格式
return Map.of("success", false, "error", "验证码错误");

// 应该改为
// Service 层抛出异常，由 GlobalExceptionHandler 统一处理
throw new BusinessException(ErrorCode.VALIDATION_ERROR, "验证码错误");
```

#### 3. OAuth2ExceptionHandler（1 处）
**位置**：
- `OAuth2ExceptionHandler.handle()` 返回 `ResponseEntity<String>`

**示例**：
```java
// 当前格式
return ResponseEntity.badRequest().body("OAuth2 Error [xxx]: ...");

// 应该改为
return ResponseEntity.badRequest().body(ErrorResponse.builder()...);
```

## 重构必要性分析

### ⚠️ 问题

#### 1. 响应格式不统一
- **异常抛出**：返回 `ErrorResponse` 格式（统一）
- **手动返回**：返回 `Map` 格式（不统一）
- **前端处理**：需要兼容两种格式，增加复杂度

#### 2. 缺少错误码
- 手动返回的错误没有错误码
- 不利于错误统计和监控
- 前端无法根据错误码做统一的错误处理

#### 3. 维护成本高
- 错误消息散落在各处
- 修改响应格式需要修改多处
- 容易出现不一致

### ✅ 重构收益

#### 1. 统一响应格式
- 所有错误响应都是 `ErrorResponse` 格式
- 前端只需要处理一种格式
- API 文档更规范

#### 2. 统一错误码
- 所有错误都有明确的错误码
- 便于错误统计和监控
- 便于前端统一错误处理

#### 3. 降低维护成本
- 错误响应逻辑统一
- 修改响应格式只需修改一处
- 减少重复代码

## 重构建议

### 方案 1：渐进式重构（推荐 ⭐⭐⭐⭐）

#### 步骤
1. **先重构 Service 层**：将返回 Map 改为抛出异常
2. **重构 Controller 手动返回**：使用 `ErrorResponse` 构建器
3. **重构 OAuth2ExceptionHandler**：使用 `ErrorResponse`

#### 优点
- 风险可控，可以逐步迁移
- 不影响现有功能
- 可以逐步测试验证

### 方案 2：创建响应工具类（推荐 ⭐⭐⭐⭐⭐）

#### 实现方式
创建 `ResponseUtils` 工具类，简化错误响应构建：

```java
public class ResponseUtils {
    public static ResponseEntity<ErrorResponse> error(ErrorCode errorCode, String detail) {
        ErrorResponse response = ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .detail(detail)
            .status(errorCode.getStatusValue())
            .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
    
    public static ResponseEntity<ErrorResponse> unauthorized(String detail) {
        return error(ErrorCode.UNAUTHORIZED, detail);
    }
    
    public static ResponseEntity<ErrorResponse> badRequest(String detail) {
        return error(ErrorCode.VALIDATION_ERROR, detail);
    }
    
    // ... 其他便捷方法
}
```

**使用示例**：
```java
// 重构前
return ResponseEntity.status(401).body(Map.of("success", false, "error", "未登录"));

// 重构后
return ResponseUtils.unauthorized("未登录");
```

#### 优点
- 代码更简洁
- 统一构建逻辑
- 易于维护

### 方案 3：Service 层抛出异常（最佳实践 ⭐⭐⭐⭐⭐）

#### 实现方式
Service 层抛出异常，Controller 捕获或让全局处理器处理：

```java
// Service 层
public void bindTotp(User user, String password, String totpCode) {
    if (totpCode == null || totpCode.isEmpty()) {
        throw new BusinessException(ErrorCode.VALIDATION_ERROR, "缺少TOTP验证码");
    }
    // ... 业务逻辑
}

// Controller 层（简化）
@PostMapping("/totp/bind")
public ResponseEntity<?> bindTotp(@RequestBody Map<String, String> req) {
    User user = getCurrentUser();
    if (user == null) {
        throw new UnauthorizedException("未登录");
    }
    securityService.bindTotp(user, null, req.get("totpCode"));
    return ResponseEntity.ok(Map.of("success", true, "message", "绑定成功"));
}
```

#### 优点
- 符合异常处理最佳实践
- Controller 代码更简洁
- 异常统一由 `GlobalExceptionHandler` 处理
- 错误响应格式完全统一

## 重构优先级

### 高优先级
1. ✅ **OAuth2ExceptionHandler**：影响 OAuth2 认证流程
2. ✅ **Controller 中的手动错误返回**：直接影响 API 响应格式

### 中优先级
3. ⚠️ **Service 层的 Map 返回**：逐步改为抛出异常

### 低优先级
4. 📝 **成功响应格式**：保持现状或逐步统一（可选）

## 总结

**建议重构** ⭐⭐⭐⭐

**理由**：
1. ✅ 统一响应格式，提升 API 规范性
2. ✅ 统一错误码，便于监控和前端处理
3. ✅ 降低维护成本
4. ✅ 符合 REST API 最佳实践

**推荐方案**：**方案 2 + 方案 3**
- 创建 `ResponseUtils` 工具类简化构建
- Service 层改为抛出异常
- Controller 中手动返回使用工具类

**实施步骤**：
1. 创建 `ResponseUtils` 工具类
2. 创建 `BusinessException` 等业务异常类
3. 重构 Controller 手动返回的错误
4. 重构 Service 层的错误返回
5. 重构 `OAuth2ExceptionHandler`

