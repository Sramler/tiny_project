# OIDC 登录问题排查指南

## 问题描述

访问 `http://localhost:5173/login` 时出现重复刷新问题，可能是本地缓存问题导致。

## 问题原因分析

1. **路由配置重复**：`/login` 路由在路由配置中出现了两次
2. **路由守卫逻辑问题**：没有正确处理已认证用户访问登录页的情况
3. **Login 组件直接重定向**：组件挂载时直接调用 `signinRedirect()`，没有检查用户状态
4. **缺少防重复机制**：没有防止重复重定向的机制
5. **本地缓存问题**：OIDC 相关的缓存数据可能导致状态混乱

## 解决方案

### 1. 路由配置优化

- 移除重复的 `/login` 路由配置
- 优化路由守卫逻辑，正确处理已认证用户
- 添加调试页面路由 `/debug`

### 2. 登录组件优化

- 添加认证状态检查，避免已认证用户重复重定向
- 添加防重复重定向机制
- 检查 URL 参数，避免在 OIDC 回调时重复重定向

### 3. 认证状态管理优化

- 添加全局防重复重定向标志
- 优化用户状态恢复逻辑
- 在关键事件中重置登录状态

### 4. 缓存清理工具

创建了 `clearOidcCache()` 函数，可以清理：

- localStorage 中的 OIDC 相关数据
- sessionStorage 中的认证数据
- userManager 中的用户数据

## 使用方法

### 1. 访问调试页面

访问 `http://localhost:5173/debug` 查看：

- 当前认证状态
- URL 参数
- 本地存储信息
- OIDC 相关缓存项

### 2. 清理缓存

在调试页面点击"清理缓存"按钮，或手动执行：

```javascript
// 在浏览器控制台执行
import { clearOidcCache } from '@/utils/auth-utils'
await clearOidcCache()
```

### 3. 手动清理浏览器缓存

1. 打开浏览器开发者工具
2. 进入 Application/Storage 标签
3. 清理 Local Storage 和 Session Storage
4. 删除包含 `oidc`、`user`、`auth` 的项

### 4. 强制登出

在调试页面点击"强制登出"按钮，或手动执行：

```javascript
// 在浏览器控制台执行
import { userManager } from '@/auth/oidc'
await userManager.removeUser()
```

## 调试步骤

### 1. 检查认证状态

访问调试页面，查看：

- 用户认证状态
- 当前用户信息
- Token 过期时间

### 2. 检查 URL 参数

确认当前页面是否包含 OIDC 回调参数：

- `code`：授权码
- `state`：状态参数
- `error`：错误信息

### 3. 检查本地存储

查看 localStorage 和 sessionStorage 中的 OIDC 相关项：

- `oidc-client:vue-client:http://localhost:9000`
- `oidc-client:vue-client:http://localhost:9000:user`
- 其他包含 `oidc`、`user`、`auth` 的项

### 4. 查看浏览器控制台

检查是否有以下错误：

- OIDC 相关错误
- 网络请求失败
- JavaScript 异常

## 常见问题及解决方案

### 1. 重复重定向循环

**症状**：页面不断刷新，无法正常登录

**解决方案**：

1. 清理浏览器缓存
2. 访问调试页面清理 OIDC 缓存
3. 强制登出后重新登录

### 2. 认证状态不一致

**症状**：显示已登录但无法访问受保护页面

**解决方案**：

1. 刷新认证状态
2. 清理缓存
3. 重新登录

### 3. Token 过期问题

**症状**：登录后很快提示需要重新登录

**解决方案**：

1. 检查 Token 过期时间
2. 确认静默续期配置
3. 检查网络连接

### 4. 回调处理失败

**症状**：登录成功后无法跳转到目标页面

**解决方案**：

1. 检查回调 URL 配置
2. 确认路由配置正确
3. 查看回调处理逻辑

## 预防措施

### 1. 开发环境

- 定期清理浏览器缓存
- 使用调试页面监控认证状态
- 保持代码版本一致性

### 2. 生产环境

- 配置正确的域名和端口
- 使用 HTTPS 协议
- 定期检查 Token 配置

### 3. 代码维护

- 定期更新 OIDC 客户端库
- 监控认证相关错误日志
- 保持配置文件的同步

## 相关文件

- `src/auth/auth.ts` - 认证状态管理
- `src/auth/oidc.ts` - OIDC 配置
- `src/views/Login.vue` - 登录页面
- `src/views/OidcCallback.vue` - 回调处理
- `src/views/Debug.vue` - 调试工具
- `src/utils/auth-utils.ts` - 认证工具函数
- `src/router/index.ts` - 路由配置

## 联系支持

如果问题仍然存在，请：

1. 收集调试页面的完整信息
2. 提供浏览器控制台的错误日志
3. 描述具体的操作步骤和现象
