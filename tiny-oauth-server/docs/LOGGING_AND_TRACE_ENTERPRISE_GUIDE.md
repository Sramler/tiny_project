## 日志与 TraceId 企业接入规范

本指南汇总并统一了项目中与日志、TraceId 相关的设计和操作规范，作为新系统接入与排查问题的标准文档。

---

## 1. 总体设计目标

- **统一追踪标识**：所有 HTTP 请求从前端到后端、再到数据库/调度日志，都包含：
  - `traceId`：会话级/链路级追踪 ID（32 位十六进制）
  - `requestId`：单次请求 ID（16 位十六进制）
  - `userId`：当前认证用户 ID（登录后）
  - `ip`：客户端 IP
- **完整闭环**：从前端发起（包括 OIDC 协议跳转）、后端 Filter / Spring Security / 业务层 / DB 操作 / 审计日志，形成一条完整可追踪链路。
- **运维友好**：通过一两个命令即可快速定位某个用户/请求的全链路日志。

---

## 2. 前端规范（webapp）

### 2.1 TraceId 与 RequestId 生成与存储

文件：`src/utils/traceId.ts`

- **TraceId 生成**：
  - `generateTraceId()`：使用 `crypto.randomUUID().replace(/-/g, '')` 生成 32 位十六进制字符串。
- **RequestId 生成**：
  - `generateRequestId()`：从 TraceId 截取前 16 位。
- **会话存储与兜底**：
  - 使用 `TRACE_STORAGE_KEY = 'app_trace_id'` 存在 `sessionStorage` 中。
  - 封装 `safeSessionStorage`，在 `sessionStorage` 不可用时：
    - 使用内存变量 `inMemoryTraceId` 兜底。
    - 打印 `console.warn('[TRACE_ID] ...')`，但不影响业务流程。
  - 核心接口：
    - `getOrCreateTraceId()`：获取当前会话 TraceId，不存在则生成并存储。
    - `createNewTraceId()`：强制生成新的 TraceId。
    - `clearTraceId()`：清除 TraceId。
    - `getCurrentTraceId()`：只读当前 TraceId，不创建。

### 2.2 统一为 HTTP 请求注入 TraceId

#### 2.2.1 axios 封装（业务 API）

文件：`src/utils/request.ts`

- 在 axios 请求拦截器中：
  - `const traceId = getOrCreateTraceId()`
  - `const requestId = generateRequestId()`
  - 统一设置：
    - `config.headers['X-Trace-Id'] = traceId`
    - `config.headers['X-Request-Id'] = requestId`
  - 同时注入 Bearer Token 等认证信息。
- 在响应拦截器中，可打印/记录后端返回的 `x-trace-id` / `x-request-id`（仅开发环境 DEBUG 时输出）。

> **规范要求**：前端所有业务 API 调用必须通过 `request.ts` 导出的封装方法（`get/post/put/delete/patch`），禁止直接 `axios(...)` 以避免漏加 TraceId。

#### 2.2.2 fetchWithTraceId（通用 fetch 包装）

文件：`src/utils/traceId.ts`

- 方法：`fetchWithTraceId(url, options)`
  - 通过 `addTraceIdToFetchOptions(options)` 注入：
    - `X-Trace-Id`
    - `X-Request-Id`
  - 处理：
    - 超时控制（默认 5 秒，可配）
    - 401 / 302 未授权统一跳转 `/exception/401`
    - 网络错误错误码封装（`ECONNABORTED`、`ERR_NETWORK` 等）

> **规范要求**：凡是直接使用 `fetch` 的代码，应改为使用 `fetchWithTraceId`，或显式调用 `addTraceIdToFetchOptions`。

#### 2.2.3 fetchWithAuth（带认证的 fetch）

文件：`src/auth/auth.ts`

- 方法：`fetchWithAuth(url, options)`
  - 先通过 OIDC 的 `getAccessToken()` 获取有效 token（必要时 silent renew）。
  - 构造 headers：
    - `Authorization: Bearer <token>`
  - 再调用 `addTraceIdToFetchOptions` 统一注入 TraceId。

> **规范要求**：访问受保护资源（需要登录才能访问的接口），前端应优先使用 `fetchWithAuth`。

### 2.3 OIDC 客户端与不可控重定向

#### 2.3.1 全局 fetch 猴子补丁（仅限授权服务器 authority）

文件：`src/auth/oidc.ts`

- 函数：`installOidcFetchWithTraceId(authority: string)`
  - 在创建 `UserManager` 之前调用。
  - 保存原始 `window.fetch` 为 `originalFetch`。
  - 重写 `window.fetch`：
    - 解析 `urlString`。
    - 若 `urlString.startsWith(authority)`：
      - 使用 `addTraceIdToFetchOptions(init ?? {})` 注入 TraceId / RequestId。
      - 调用 `originalFetch(input, optionsWithTrace)`。
    - 否则回退 `originalFetch(input, init)`。
  - 避免重复安装（`window.__oidcTraceFetchInstalled` 标记）。

> **效果**：包括 `/.well-known/openid-configuration`、`/oauth2/token`、`/userinfo` 等，由 `oidc-client-ts` 内部发起的 HTTP 请求也会自动带上 TraceId。

#### 2.3.2 登录重定向：在 authorize URL 上追加 trace_id

文件：`src/auth/auth.ts` 中 `login()`：

- 逻辑：
  - 生成 TraceId：`const traceId = getOrCreateTraceId()`
  - 调用：
    ```ts
    await userManager.signinRedirect({
      state: {
        returnUrl: window.location.pathname + window.location.search,
        trace_id: traceId,
      },
      extraQueryParams: {
        trace_id: traceId,
      },
    });
    ```

> **效果**：授权请求 `/oauth2/authorize?...&trace_id=<traceId>`，后端可从 query 中解析 TraceId，用于不可控重定向场景的追踪。

#### 2.3.3 注销重定向：在 post_logout_redirect_uri 上追加 trace_id

文件：`src/auth/auth.ts` 中 `logout()`：

- 正常 `signoutRedirect`：

  ```ts
  const traceId = getOrCreateTraceId();
  const postLogoutUrl = new URL(settings.post_logout_redirect_uri);
  postLogoutUrl.searchParams.set("trace_id", traceId);

  await userManager.signoutRedirect({
    id_token_hint: currentUser.id_token,
    post_logout_redirect_uri: postLogoutUrl.toString(),
  });
  ```

- 本地 fallback：
  ```ts
  const traceId = getOrCreateTraceId();
  const fallbackUrl = new URL(settings.post_logout_redirect_uri);
  fallbackUrl.searchParams.set("trace_id", traceId);
  window.location.href = fallbackUrl.toString();
  ```

> **效果**：注销流程及注销后的回跳 URL 也带 traceId，整条链路可追踪。

#### 2.3.4 silent renew 特例：独立 HTML 中的 TraceId 支持

文件：`public/silent-renew.html`

- 使用 CDN 的 `oidc-client-ts`，在该 HTML 内：
  - 实现简化版 `getOrCreateTraceId()`（sessionStorage + 内存兜底）。
  - 按 `userManager.settings.authority` 安装 fetch 包装（与主应用逻辑一致）。
  - 确保 silent renew 相关请求也自动注入 `X-Trace-Id` / `X-Request-Id`。

> **规范建议**：如未来不再使用 CDN 版，而是打包本地模块，可复用 `traceId.ts` 与 `installOidcFetchWithTraceId`，避免重复实现。

---

## 3. 后端规范（oauth-server）

### 3.1 日志字段与 MDC 约定

所有日志 pattern 中统一使用以下 MDC key：

- `traceId`：链路 ID（来自 header 或 query 或 fallback）
- `requestId`：请求 ID（来自 header 或 fallback）
- `userId`：当前请求用户 ID（由 `HttpRequestLoggingInterceptor` 解析并设置）
- `clientIp`：客户端 IP（由 `IpUtils.getClientIp()` 解析并设置到 MDC）

### 3.2 HttpRequestLoggingFilter：TraceId / RequestId / IP 的统一入口

文件：`src/main/java/com/tiny/oauthserver/sys/filter/HttpRequestLoggingFilter.java`

#### 3.2.1 注册方式与顺序

- 通过 `HttpRequestLoggingFilterConfig` 使用 `FilterRegistrationBean` 注册：
  - `order = Ordered.HIGHEST_PRECEDENCE`，确保在 Spring Security 之前执行。
  - 使用 application name 和环境信息初始化。

#### 3.2.2 TraceId / RequestId / SpanId / clientIp 填充流程

在 `doFilterInternal` 中：

1. 包装 request/response 为 `ContentCaching*Wrapper`。
2. 记录起始时间、服务名、环境等属性到 request attribute。
3. 生成 / 提取：
   - `requestId = getOrCreateRequestId()`：
     - 优先读取 header `X-Request-Id`；
     - 没有则 `randomHex()` 生成；
     - 回写 `X-Request-Id` 到响应头。
   - `traceId = resolveTraceId(request, requestId)`：
     - 详见下节。
   - `spanId = resolveSpanId(request)`：
     - 支持 B3 等分布式追踪 header。
4. 写入 request attribute：
   - `ATTR_REQUEST_ID` / `ATTR_TRACE_ID` / `ATTR_SPAN_ID`。
5. 写入 MDC：
   - `MDC.put("traceId", traceId)`
   - `MDC.put("requestId", requestId)`
   - `MDC.put("spanId", spanId)`（可为空）
   - `MDC.put("clientIp", IpUtils.getClientIp(request))`
6. 执行 filterChain 并在 finally 中 `MDC.clear()`，防止线程复用污染。

#### 3.2.3 TraceId 解析优先级（header → query → fallback）

函数：`resolveTraceId(HttpServletRequest request, String fallback)`：

1. **Header 优先**：
   - 遍历 `TRACE_ID_HEADERS`：
     - `traceparent`
     - `x-b3-traceid`
     - `x-trace-id`
     - `trace-id`
     - `X-Trace-Id`
     - `X-Request-Id` / `x-request-id`（作为兜底）
   - 拿到第一个非空值后：
     - 调用 `sanitizeId(...)` 归一化（去 `-`、小写、截断 64 长度）。
     - 打 DEBUG 日志：`[TRACE_ID] 从请求头 'X-Trace-Id' 获取到 traceId: 原始 -> 归一化后`。
2. **Query 参数 `trace_id`**：
   - `String traceParam = request.getParameter("trace_id")`；
   - 非空则同样 sanitize 并打 DEBUG：`[TRACE_ID] 从 query 参数 trace_id 获取到 traceId: ...`。
   - 用于不可控重定向场景（authorize、logout、post_logout_redirect）。
3. **Fallback：使用 requestId**：
   - 将 `fallback`（`requestId`）经 sanitize 后作为 TraceId。
   - DEBUG：`[TRACE_ID] 未找到 traceId 请求头或 trace_id 参数，使用 fallback (requestId): ...`。
   - INFO 运维日志（标记缺失来源）：
     ```text
     [TRACE_ID][FALLBACK] 上游未传递任何 trace 相关请求头或 trace_id 参数，使用 fallback(requestId) 作为 traceId。method=... path=... host=... remoteIp=... userAgent=...
     ```

> **规范要求**：任何外部接入系统，如无法按约定发送 `X-Trace-Id`，至少应能在问题排查时根据 fallback 生成的 TraceId / RequestId 定位。

### 3.3 HttpRequestLoggingInterceptor：UserId / Client 信息补充

文件：`src/main/java/com/tiny/oauthserver/sys/interceptor/HttpRequestLoggingInterceptor.java`

- 在 `preHandle` 中：
  - 将 `HttpRequestLoggingFilter.ATTR_USER_ID` 填充为当前认证用户 ID（按 `SecurityUser` → `UserDetails` → JWT claims → principal 顺序解析）。
  - 可选：将 `userId` 写入 MDC（当前已通过 MDC 注入）。
- 在 `afterCompletion` 中：
  - 构造 `HttpRequestLog` 实体，补充：
    - `serviceName` / `env` / `traceId` / `requestId` / `spanId`
    - `clientIp = IpUtils.getClientIp(request)`
    - `host` / `userAgent` / `httpVersion` / `method`
    - `pathTemplate` / `rawPath` / `queryString`
    - 请求/响应大小与内容（按配置决定是否记录 body）
    - `status` / `success` / `durationMs` / `error`
  - 调用 `HttpRequestLogService` 异步保存。

---

## 4. 日志输出规范（logback）

文件：`src/main/resources/logback-spring.xml`

### 4.1 控制台输出（开发友好）

```xml
<appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
  <encoder>
    <!-- 短时间 + 截断 logger，便于开发查看 -->
    <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36}
      traceId=%X{traceId} requestId=%X{requestId} userId=%X{userId} ip=%X{clientIp} - %msg%n
    </pattern>
  </encoder>
</appender>
```

### 4.2 全量业务日志文件（oauth-server.log）

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>${LOG_PATH}/${APP_NAME}.log</file>
  <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/${APP_NAME}.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <maxHistory>30</maxHistory>
    <maxFileSize>200MB</maxFileSize>
    <totalSizeCap>10GB</totalSizeCap>
  </rollingPolicy>
  <encoder>
    <!-- 完整 logger 名称，便于精确检索 -->
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger
      traceId=%X{traceId} requestId=%X{requestId} userId=%X{userId} ip=%X{clientIp} - %msg%n
    </pattern>
  </encoder>
</appender>
```

### 4.3 错误日志文件（oauth-server-error.log）

```xml
<appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
  <file>${LOG_PATH}/${APP_NAME}-error.log</file>
  <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
    <level>WARN</level>
  </filter>
  <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/${APP_NAME}-error.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <maxHistory>90</maxHistory>
    <maxFileSize>200MB</maxFileSize>
    <totalSizeCap>20GB</totalSizeCap>
  </rollingPolicy>
  <encoder>
    <!-- 同样使用完整 logger 名称，附带异常堆栈 -->
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger
      traceId=%X{traceId} requestId=%X{requestId} userId=%X{userId} ip=%X{clientIp} - %msg%n%ex
    </pattern>
  </encoder>
</appender>
```

### 4.4 审计日志（oauth-server-audit.log）

保持原有规范，重点输出安全相关字段：

```xml
<appender name="AUDIT_FILE" ...>
  <encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50}
      traceId=%X{traceId} requestId=%X{requestId} userId=%X{userId} ip=%X{clientIp} - %msg%n
    </pattern>
  </encoder>
</appender>
```

### 4.5 日志级别建议

在 `application-*.yaml` 中统一配置：

```yaml
logging:
  level:
    # 业务代码
    com.tiny.oauthserver: INFO

    # TraceId 相关过滤器与拦截器：排查问题时可临时调到 DEBUG
    com.tiny.oauthserver.sys.filter.HttpRequestLoggingFilter: INFO
    com.tiny.oauthserver.sys.interceptor.HttpRequestLoggingInterceptor: INFO

    # 框架降噪
    org.springframework: WARN
    org.springframework.security: WARN
    org.hibernate.SQL: INFO
    org.hibernate.type: WARN
    org.hibernate.validator: WARN
    sun.rmi: WARN
    javax.management.remote.rmi: WARN
```

---

## 5. 运维排查与验证流程

### 5.1 快速定位某次请求全链路日志

1. 在浏览器控制台（或前端页面）获取当前 TraceId：
   - `getCurrentTraceId()` 或在调试工具中输出。
2. 在后端日志中 grep：

```bash
TRACE_ID="你的TraceId值"

# 查看完整链路
grep "$TRACE_ID" logs/oauth-server.log | head -50
```

### 5.2 验证 TraceId 是否正确获取

关键检查点：

1. **Filter 阶段**：有 `[TRACE_ID] 请求头检查` 与 `[TRACE_ID] 请求路径` 日志，且能看到 `X-Trace-Id` / `x-trace-id`。
2. **解析阶段**：存在 `[TRACE_ID] 从请求头 'X-Trace-Id' 获取到 traceId: 原始值 -> 归一化值` 或 `[TRACE_ID] 未找到 traceId 请求头，使用 fallback (requestId)` 等日志。
3. **MDC 阶段**：`[TRACE_ID] 请求路径 ... MDC中的traceId/MDC中的requestId` 与解析结果一致。
4. **业务日志阶段**：`SecurityServiceImpl / Controller / Repository` 等日志中的 `traceId=` 与前端/解析结果一致。
5. **数据库阶段**：`org.hibernate.SQL` 日志中也能看到相同的 `traceId=`。

### 5.3 处理 TraceId 为空的场景

主要为以下情况（正常/可接受）：

- 应用启动期：`[main]`、`[background-preinit]` 等线程的框架初始化日志。
- 后台线程：调度任务（Quartz）、连接池（HikariCP）、JMX/RMI。
- 异步线程：`SimpleAsyncTaskExecutor-*` 保存请求日志时，其日志前缀中的 `traceId=` 可能为空，但消息体中含有 TraceId。

> 如需在异步线程日志前缀中也带 TraceId，可使用 `MDC.getCopyOfContextMap()` 或 Spring `TaskDecorator` 将 MDC 透传到异步线程。

---

## 6. 接入新系统时的要求总结

1. **前端/调用方**：

   - 业务 API：
     - 使用统一封装（axios 实例、`fetchWithTraceId`、`fetchWithAuth`）。
   - OIDC / 协议类：
     - 通过 header 或 query `trace_id` 传递 TraceId（推荐 header，query 用于重定向场景）。

2. **服务端接入方**：

   - 在入口 Filter 中统一解析 TraceId / RequestId / clientIp，并写入 MDC。
   - 日志 Pattern 中统一输出：`traceId`、`requestId`、`userId`、`ip`。

3. **日志与运维**：
   - 保持全量日志、错误日志、审计日志分离存储。
   - 使用合适的日志级别，减少框架噪音，保证业务与追踪信息清晰可见。
   - 遇到跨系统问题时，以 TraceId 为主键在各系统日志中进行全链路检索。

此文作为日志与 TraceId 的企业级接入规范，新系统接入时应遵循本规范进行实现与自检。
