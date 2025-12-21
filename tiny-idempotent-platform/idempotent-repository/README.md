# Idempotent Repository 模块

## 模块结构

```
idempotent-repository/
├── src/main/java/com/tiny/idempotent/repository/
│   ├── IdempotentRepository.java          # 接口定义
│   ├── redis/
│   │   └── RedisIdempotentRepository.java
│   ├── database/
│   │   └── DatabaseIdempotentRepository.java
│   └── memory/
│       └── MemoryIdempotentRepository.java
```

## 自定义实现

用户可以直接实现 `IdempotentRepository` 接口来提供自定义存储：

```java
@Component
public class CustomIdempotentRepository implements IdempotentRepository {
    // 实现接口方法
}
```

## 使用说明

所有实现都在同一个模块中，通过 `provided` scope 控制依赖：

- Redis 客户端依赖标记为 `provided`
- JDBC 依赖标记为 `provided`

使用方按需引入相应的依赖即可。

