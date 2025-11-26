# 调试 302 重定向问题指南

## 问题描述

当后端返回 302 重定向到登录页时，浏览器会清空控制台日志，导致无法看到调试信息。这对于排查 `/self/security/status` 等接口的认证问题非常困难。

## 为什么会出现 302 而不是 401？

Spring Security 默认行为：
- **未认证的请求**：如果请求的是需要认证的接口，Spring Security 会重定向到登录页（302）
- **已认证但无权限**：返回 403 Forbidden
- **API 接口**：如果配置了 `@ResponseBody` 或 `@RestController`，应该返回 401，但 Spring Security 的过滤器可能在 Controller 之前就拦截了

## 调试方法

### 方法 1：使用浏览器 Network 面板（推荐）

这是最直接和可靠的方法：

1. **打开开发者工具**：按 `F12` 或 `Cmd+Option+I` (Mac) / `Ctrl+Shift+I` (Windows)
2. **切换到 Network 标签**
3. **启用 "Preserve log"**：勾选 Network 面板顶部的 "Preserve log" 选项
   - 这样即使页面跳转，请求记录也不会被清空
4. **查看请求详情**：
   - 找到 `/self/security/status` 请求
   - 点击查看详情：
     - **Headers**：查看请求头和响应头
     - **Response**：查看响应内容（302 重定向时可能为空）
     - **Preview**：查看响应预览
   - 特别关注：
     - `Location` 响应头（重定向目标）
     - `Set-Cookie` 响应头（Session 状态）
     - 请求的 `Cookie` 头（是否包含 Session）

### 方法 2：使用持久化日志（已实现）

我们已经实现了持久化日志功能，日志会保存到 `localStorage`，即使页面跳转也不会丢失：

1. **触发 401/302 错误**
2. **跳转到 401 页面** (`/exception/401`)
3. **查看调试日志**：
   - 在 401 页面点击 "显示调试日志"
   - 查看最近的日志记录，包括：
     - 请求 URL
     - 请求方法
     - 请求头
     - 响应状态码
     - 时间戳

**使用代码**：
```typescript
import { persistentLogger } from '@/utils/logger'

// 记录日志
persistentLogger.warn('检测到 401 错误', {
  url: '/self/security/status',
  method: 'GET',
  headers: {...},
}, '/self/security/status', 401)
```

**查看日志**：
```typescript
import { getPersistentLogs, clearPersistentLogs, exportPersistentLogs } from '@/utils/logger'

// 获取所有日志
const logs = getPersistentLogs()

// 清空日志
clearPersistentLogs()

// 导出日志
const logsJson = exportPersistentLogs()
```

### 方法 3：在控制台中使用 `console.log` 并延迟跳转

在开发环境中，我们已经在跳转前添加了 100ms 延迟，给你时间查看日志：

```typescript
// 开发环境延迟跳转
if (import.meta.env.DEV) {
  await new Promise(resolve => setTimeout(resolve, 100))
}
```

### 方法 4：使用 `debugger` 断点

在关键位置添加 `debugger` 语句：

```typescript
// 在 traceId.ts 或 request.ts 中
if (response.status === 401) {
  debugger // 浏览器会在这里暂停
  // ... 处理逻辑
}
```

### 方法 5：检查后端日志

查看后端 Spring Boot 日志，了解：
- 请求是否到达 Controller
- Session 是否有效
- 认证过滤器的工作情况

**后端日志位置**：
- 控制台输出
- 日志文件（如果配置了）

## 常见问题排查

### 问题 1：为什么返回 302 而不是 401？

**原因**：
- Spring Security 的 `formLogin()` 配置会拦截未认证请求并重定向到登录页
- 即使接口使用 `@ResponseBody`，过滤器层也会先执行

**解决方案**：
1. 检查 Spring Security 配置中的 `formLogin()` 设置
2. 考虑使用 `httpBasic()` 或自定义 `AuthenticationEntryPoint` 返回 401
3. 对于 API 接口，可以使用 `@RestController` + 异常处理器返回 401

### 问题 2：Session 丢失的原因

**可能原因**：
1. **后端重启**：Servlet Session 存储在内存中，重启后丢失
2. **Session 过期**：超过配置的过期时间
3. **Cookie 被清除**：浏览器清除了 Session Cookie
4. **跨域问题**：Cookie 的 `SameSite` 属性导致跨域请求不携带 Cookie

**排查方法**：
```javascript
// 在浏览器控制台检查 Cookie
document.cookie

// 检查 Session Storage
sessionStorage

// 检查 Local Storage
localStorage
```

### 问题 3：如何区分 302 和 401？

**302 重定向**：
- HTTP 状态码：302
- 响应头：`Location: http://localhost:9000/login`
- 浏览器会自动跳转

**401 未授权**：
- HTTP 状态码：401
- 响应头：`WWW-Authenticate: Bearer`（可选）
- 不会自动跳转，需要前端处理

## 最佳实践

1. **开发环境**：
   - 使用 Network 面板 + Preserve log
   - 启用持久化日志
   - 在关键位置添加 `console.log`

2. **生产环境**：
   - 使用持久化日志记录错误
   - 将日志导出并发送到日志系统
   - 监控 401/302 错误率

3. **代码层面**：
   - 统一错误处理（已实现）
   - 记录详细的请求/响应信息
   - 使用 Trace ID 追踪请求链路

## 相关文件

- `src/utils/logger.ts` - 日志工具（包含持久化日志）
- `src/utils/traceId.ts` - Trace ID 和 fetch 包装
- `src/utils/request.ts` - Axios 拦截器
- `src/views/exception/401.vue` - 401 错误页面（包含日志查看器）

## 示例：查看 Network 面板中的 302 请求

1. 打开 Network 面板
2. 启用 "Preserve log"
3. 触发请求（如访问 `/self/security/status`）
4. 找到请求，查看：
   - **Status**: 302
   - **Type**: document 或 xhr
   - **Headers** → **Response Headers**:
     ```
     Location: http://localhost:9000/login
     Set-Cookie: JSESSIONID=...; Path=/; HttpOnly
     ```
   - **Headers** → **Request Headers**:
     ```
     Cookie: JSESSIONID=... (检查是否存在)
     ```

## 总结

**推荐调试流程**：
1. 打开 Network 面板，启用 "Preserve log"
2. 触发问题请求
3. 查看 Network 面板中的请求详情
4. 如果跳转到 401 页面，查看持久化日志
5. 检查后端日志
6. 根据收集的信息定位问题

