# 迁移指南

## 从旧代码迁移到 Starter

### 包名变更

| 旧包名 | 新包名 |
|--------|--------|
| `com.tiny.oauthserver.common.annotation.Idempotent` | `com.tiny.idempotent.annotation.Idempotent` |
| `com.tiny.oauthserver.common.exception.IdempotentException` | `com.tiny.idempotent.exception.IdempotentException` |
| `com.tiny.oauthserver.common.idempotent.*` | `com.tiny.idempotent.*` |

### 迁移步骤

1. **更新依赖**：在 `pom.xml` 中添加 starter 依赖
2. **更新导入**：将旧的包名改为新的包名
3. **删除旧代码**：删除 `com.tiny.oauthserver.common.idempotent` 包下的旧代码

### 示例

#### 导入更新

```java
// 旧代码
import com.tiny.oauthserver.common.annotation.Idempotent;
import com.tiny.oauthserver.common.exception.IdempotentException;

// 新代码
import com.tiny.idempotent.annotation.Idempotent;
import com.tiny.idempotent.exception.IdempotentException;
```

#### 使用方式（保持不变）

```java
@PostMapping("/users")
@Idempotent
public ResponseEntity<User> createUser(@RequestBody User user) {
    // 使用方式完全一致
}
```

### 配置变更

配置项保持不变，仍然是 `tiny.idempotent.*`。

