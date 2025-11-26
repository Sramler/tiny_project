# Console 日志配置指南

## 概述

项目已集成日志工具 `logger.ts`，支持通过环境变量控制 console 输出。可以根据不同环境（开发/生产）灵活配置日志级别和输出开关。

## 功能特性

1. **环境变量控制**：通过 `.env` 文件配置日志输出
2. **日志级别支持**：支持 debug、info、warn、error、none 等级别
3. **按类型控制**：可以分别控制 log、debug、warn、error 的输出
4. **开发/生产环境默认值**：不同环境有不同的默认配置

## 环境变量配置

### 日志开关配置

在 `.env` 文件中配置以下环境变量：

```bash
# 是否启用 console.log（默认: true）
VITE_ENABLE_CONSOLE_LOG=true

# 是否启用 console.debug（默认: 仅在开发环境）
VITE_ENABLE_CONSOLE_DEBUG=true

# 是否启用 console.warn（默认: true）
VITE_ENABLE_CONSOLE_WARN=true

# 是否启用 console.error（默认: true）
VITE_ENABLE_CONSOLE_ERROR=true
```

### 日志级别配置

通过 `VITE_LOG_LEVEL` 设置日志级别（优先级高于开关配置）：

```bash
# 日志级别：debug | info | warn | error | none
# 默认值：开发环境为 'debug'，生产环境为 'info'
VITE_LOG_LEVEL=debug
```

日志级别说明：
- `debug`：输出所有日志（包括 debug）
- `info`：输出 info、warn、error（不输出 debug）
- `warn`：只输出 warn 和 error
- `error`：只输出 error
- `none`：不输出任何日志

## 使用方式

### 方式 1：使用 logger 工具（推荐）

在代码中使用 logger 工具替代 console：

```typescript
import logger from '@/utils/logger'

// 调试信息（默认仅在开发环境输出）
logger.debug('调试信息', data)

// 普通信息
logger.log('普通信息', data)
logger.info('信息', data)  // 与 log 相同

// 警告信息
logger.warn('警告信息', data)

// 错误信息
logger.error('错误信息', error)

// 分组输出（仅在 debug 级别启用）
logger.group('分组标题')
logger.log('分组内容')
logger.groupEnd()

// 表格输出（仅在 debug 级别启用）
logger.table(data)
```

### 方式 2：直接使用 console（不推荐）

虽然可以直接使用 `console.log()` 等，但不推荐，因为：
1. 无法通过环境变量控制
2. 生产环境无法关闭
3. 不利于统一管理

## 默认配置

### 开发环境（`npm run dev`）

- `VITE_ENABLE_CONSOLE_LOG` = `true`
- `VITE_ENABLE_CONSOLE_DEBUG` = `true`
- `VITE_ENABLE_CONSOLE_WARN` = `true`
- `VITE_ENABLE_CONSOLE_ERROR` = `true`
- `VITE_LOG_LEVEL` = `debug`

**效果**：输出所有日志，包括 debug 信息。

### 生产环境（`npm run build`）

- `VITE_ENABLE_CONSOLE_LOG` = `true`
- `VITE_ENABLE_CONSOLE_DEBUG` = `false`
- `VITE_ENABLE_CONSOLE_WARN` = `true`
- `VITE_ENABLE_CONSOLE_ERROR` = `true`
- `VITE_LOG_LEVEL` = `info`

**效果**：不输出 debug 信息，只输出 info、warn、error。

## 配置示例

### 示例 1：开发环境完整日志

`.env.development` 文件：

```bash
VITE_API_BASE_URL=http://localhost:9000
VITE_ENABLE_CONSOLE_LOG=true
VITE_ENABLE_CONSOLE_DEBUG=true
VITE_LOG_LEVEL=debug
```

### 示例 2：生产环境最小日志

`.env.production` 文件：

```bash
VITE_API_BASE_URL=https://api.example.com
VITE_ENABLE_CONSOLE_LOG=false
VITE_ENABLE_CONSOLE_DEBUG=false
VITE_ENABLE_CONSOLE_WARN=false
VITE_ENABLE_CONSOLE_ERROR=true
VITE_LOG_LEVEL=error
```

**效果**：只输出错误日志。

### 示例 3：完全禁用日志

`.env.production` 文件：

```bash
VITE_LOG_LEVEL=none
```

**效果**：不输出任何日志（即使单个开关设置为 true）。

## 已使用 logger 的文件

以下文件已使用 logger 工具：

- ✅ `src/router/index.ts` - 路由配置
- ✅ `src/utils/request.ts` - HTTP 请求工具（部分使用）
- ✅ 其他工具文件

## 迁移建议

### 将 console 替换为 logger

1. **导入 logger**：
   ```typescript
   import logger from '@/utils/logger'
   ```

2. **替换 console 调用**：
   ```typescript
   // 替换前
   console.log('信息')
   console.debug('调试信息')
   console.warn('警告')
   console.error('错误')
   
   // 替换后
   logger.log('信息')
   logger.debug('调试信息')
   logger.warn('警告')
   logger.error('错误')
   ```

### 注意事项

1. **错误日志**：`logger.error()` 在生产环境通常应该启用，用于错误追踪
2. **调试信息**：`logger.debug()` 默认只在开发环境启用，适合详细的调试信息
3. **信息日志**：`logger.log()` 和 `logger.info()` 功能相同，用于一般信息输出
4. **警告日志**：`logger.warn()` 用于警告信息，通常在生产环境也应该启用

## 最佳实践

1. **开发环境**：使用 `debug` 级别，输出所有日志
2. **测试环境**：使用 `info` 级别，不输出 debug 日志
3. **生产环境**：使用 `error` 或 `warn` 级别，只输出重要信息

## 相关文件

- `src/utils/logger.ts` - 日志工具实现
- `.env` / `.env.development` / `.env.production` - 环境变量配置

## 验证配置

可以在浏览器控制台中测试：

```javascript
import logger from '@/utils/logger'

// 测试不同级别的日志
logger.debug('这是 debug 日志')
logger.log('这是 info 日志')
logger.warn('这是 warn 日志')
logger.error('这是 error 日志')
```

根据环境变量配置，只有符合条件的日志会输出到控制台。

