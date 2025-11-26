# TRACE_ID 集成指南

## 概述

本项目已全面集成 TRACE_ID 功能，用于分布式追踪和请求链路追踪。所有 HTTP 请求都会自动携带 TRACE_ID，帮助后端进行日志关联和问题排查。

## 功能特性

1. **自动生成 TRACE_ID**：基于 UUID 生成 32 位十六进制字符串
2. **会话级复用**：同一浏览器标签页/窗口会话中复用相同的 TRACE_ID
3. **自动添加到请求头**：所有 axios 和 fetch 请求自动携带 TRACE_ID
4. **后端兼容**：支持后端识别的多种 header 名称

## 后端支持的 Header 名称

后端 `HttpRequestLoggingFilter` 支持以下 header 名称（按优先级）：

1. `traceparent` - W3C Trace Context 标准
2. `x-b3-traceid` - Zipkin B3 格式
3. `x-trace-id` - 通用格式（**当前使用**）
4. `trace-id` - 通用格式
5. `x-request-id` - Request ID（**当前使用**）

前端会自动添加：
- `X-Trace-Id`：会话级 TRACE_ID（32 字符）
- `X-Request-Id`：请求级 Request ID（16 字符）

## 实现位置

### 1. TRACE_ID 工具函数

**文件**：`src/utils/traceId.ts`

提供了以下功能：

```typescript
// 生成新的 TRACE_ID
generateTraceId(): string

// 生成请求 ID
generateRequestId(): string

// 获取或创建会话级 TRACE_ID
getOrCreateTraceId(): string

// 创建新的 TRACE_ID（替换旧值）
createNewTraceId(): string

// 清除 TRACE_ID
clearTraceId(): void

// 获取当前 TRACE_ID（不创建）
getCurrentTraceId(): string | null

// 为 fetch 选项添加 TRACE_ID headers
addTraceIdToFetchOptions(options: RequestInit): RequestInit

// 带 TRACE_ID 的 fetch 包装函数
fetchWithTraceId(url: string, options?: RequestInit): Promise<Response>
```

### 2. Axios 请求拦截器

**文件**：`src/utils/request.ts`

所有通过 `request` 工具发送的 axios 请求都会自动添加 TRACE_ID：

```typescript
import request from '@/utils/request'

// 自动携带 TRACE_ID
const data = await request.get('/api/users')
const result = await request.post('/api/users', userData)
```

### 3. Fetch 请求支持

**文件**：`src/auth/auth.ts`

`fetchWithAuth` 函数已集成 TRACE_ID 支持：

```typescript
import { useAuth } from '@/auth/auth'

const { fetchWithAuth } = useAuth()
const response = await fetchWithAuth('/api/protected', {
  method: 'GET'
})
```

**文件**：多个 Vue 组件

使用 `fetchWithTraceId` 工具函数：

```typescript
import { fetchWithTraceId } from '@/utils/traceId'

const response = await fetchWithTraceId('/api/endpoint', {
  method: 'GET',
  credentials: 'include',
  headers: { Accept: 'application/json' }
})
```

## 使用方式

### 方式 1：使用 axios（推荐）

```typescript
import request from '@/utils/request'

// 自动携带 TRACE_ID，无需手动添加
const users = await request.get('/sys/users')
const result = await request.post('/sys/users', userData)
```

### 方式 2：使用 fetchWithTraceId

```typescript
import { fetchWithTraceId } from '@/utils/traceId'

const response = await fetchWithTraceId('/api/endpoint', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(data),
})
const result = await response.json()
```

### 方式 3：使用 fetchWithAuth（带认证）

```typescript
import { useAuth } from '@/auth/auth'

const { fetchWithAuth } = useAuth()
const response = await fetchWithAuth('/api/protected', {
  method: 'GET'
})
```

### 方式 4：手动添加 TRACE_ID

```typescript
import { addTraceIdToFetchOptions } from '@/utils/traceId'

const options = addTraceIdToFetchOptions({
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data),
})

const response = await fetch('/api/endpoint', options)
```

## TRACE_ID 生命周期

### 会话级 TRACE_ID

- **存储位置**：`sessionStorage`（键名：`app_trace_id`）
- **生命周期**：同一浏览器标签页/窗口会话期间有效
- **作用域**：同一会话中的所有请求共享相同的 TRACE_ID
- **过期**：浏览器标签页关闭时自动清除

### 请求级 Request ID

- **生成方式**：每个请求生成新的 16 字符 Request ID
- **用途**：唯一标识单个请求，用于后端日志记录

## 已集成的位置

以下文件已集成 TRACE_ID 支持：

### 核心工具
- ✅ `src/utils/request.ts` - Axios 请求拦截器
- ✅ `src/utils/traceId.ts` - TRACE_ID 工具函数
- ✅ `src/auth/auth.ts` - fetchWithAuth 函数
- ✅ `src/router/index.ts` - 路由守卫中的 fetch

### Vue 组件
- ✅ `src/views/security/TotpBind.vue` - TOTP 绑定页面
- ✅ `src/views/Profile.vue` - 用户资料页面
- ✅ `src/views/Setting.vue` - 设置页面

## 后端集成

后端通过 `HttpRequestLoggingFilter` 自动处理 TRACE_ID：

1. **从请求头获取**：优先从请求头中读取 TRACE_ID
2. **自动生成**：如果请求头中没有，则使用 Request ID 作为 TRACE_ID
3. **日志记录**：所有日志都会记录 TRACE_ID，便于问题追踪

### 后端日志格式

```
2024-11-20 10:00:00.123 [http-nio-9000-exec-1] INFO  com.tiny.oauthserver traceId=550e8400e29b41d4a716446655440000 requestId=550e8400e29b41d4 userId=123 - 请求处理完成
```

## 调试和排查

### 查看当前 TRACE_ID

在浏览器控制台中：

```javascript
import { getCurrentTraceId } from '@/utils/traceId'
console.log('当前 TRACE_ID:', getCurrentTraceId())
```

### 查看请求头

在浏览器开发者工具的 Network 面板中，可以查看每个请求的请求头：

```
X-Trace-Id: 550e8400e29b41d4a716446655440000
X-Request-Id: 550e8400e29b41
```

### 后端日志查询

在后端日志中搜索 TRACE_ID：

```bash
# 查看特定 TRACE_ID 的所有日志
grep "traceId=550e8400e29b41d4a716446655440000" logs/oauth-server.log
```

## 注意事项

1. **不要手动修改 TRACE_ID**：除非有特殊需求，否则应使用工具函数管理
2. **会话级复用**：同一会话中的请求共享 TRACE_ID，便于追踪用户操作流程
3. **新会话新 ID**：打开新的浏览器标签页会生成新的 TRACE_ID
4. **生产环境**：TRACE_ID 功能对生产环境透明，不影响业务逻辑

## 扩展

### 自定义 TRACE_ID 生成策略

如果需要自定义 TRACE_ID 生成方式，可以修改 `src/utils/traceId.ts` 中的 `generateTraceId()` 函数。

### 添加其他追踪 Header

如果需要添加其他追踪相关的 header（如 `X-Span-Id`），可以在 `src/utils/traceId.ts` 中的 `addTraceIdToFetchOptions()` 函数中添加。

## 相关文件

- `src/utils/traceId.ts` - TRACE_ID 工具函数
- `src/utils/request.ts` - Axios 请求配置
- `src/auth/auth.ts` - 认证相关 fetch
- 后端：`src/main/java/com/tiny/oauthserver/sys/filter/HttpRequestLoggingFilter.java`

