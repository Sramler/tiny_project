## tiny-oauth-server fixes

### OIDC 登出 400：post_logout_redirect_uri 不匹配

- **现象**：调用 `/connect/logout` 报 400，日志提示 `OpenID Connect 1.0 Logout Request Parameter: post_logout_redirect_uri`。
- **原因**：Spring Authorization Server 对 `post_logout_redirect_uri` 进行全字符串精确匹配。前端在注销时将 `trace_id` 追加到 `post_logout_redirect_uri` 的查询参数，导致与后端已注册的 `http://localhost:5173/` 不一致，校验失败。
- **思路**：
  - 保持 `post_logout_redirect_uri` 与注册值完全一致，不附加动态查询参数。
  - 需要追踪 ID 时，改为通过请求头或额外查询参数传递，不影响重定向 URI 本身。
- **本项目处理方案**：
  - 前端退出调用改为：
    - `post_logout_redirect_uri` 使用固定的 `settings.post_logout_redirect_uri`，不再追加 `trace_id`。
    - `trace_id` 通过 `extraQueryParams` 传给 `/connect/logout`，后端的 `HttpRequestLoggingFilter` 会从请求头/参数提取并写入 MDC，链路日志保持可追踪。
    - 本地回退跳转同样使用固定的 `post_logout_redirect_uri`，避免 OIDC 校验再次失败。
