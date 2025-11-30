# Spring Security OAuth2 Long 类型反序列化问题修复

## 问题描述

在反序列化 OAuth2 授权数据时，遇到以下错误：

```
java.lang.IllegalArgumentException: The class with java.lang.Long and name of java.lang.Long is not in the allowlist.
```

**错误原因**：
- Spring Security 使用 allowlist 机制限制可反序列化的类型，以防止反序列化攻击
- 默认情况下，`java.lang.Long` 不在 allowlist 中
- 当 OAuth2 授权数据中包含 Long 类型值时，反序列化会失败

**相关 Issue**：
- [Spring Security Issue #4370](https://github.com/spring-projects/spring-security/issues/4370)
- [Spring Security Issue #12294](https://github.com/spring-projects/spring-security/issues/12294)

## 解决流程

### 1. 问题定位
- 错误发生在 `JdbcOAuth2AuthorizationService$OAuth2AuthorizationRowMapper.parseMap`
- 根本原因：`SecurityJackson2Modules$AllowlistTypeIdResolver.typeFromId` 检查 allowlist 时发现 Long 不在其中

### 2. 解决方案探索
尝试了多种方法：
1. **反射修改 allowlist Set**：直接访问并修改 Spring Security 内部的静态 allowlist
2. **自定义 TypeIdResolver**：包装 Spring Security 的解析器（过于复杂）
3. **Mixin 方式**：需要修改序列化格式（不适用）

### 3. 最终方案
使用反射 + Unsafe 直接修改 Spring Security 的静态 final 字段 `ALLOWLIST_CLASS_NAMES`

**关键发现**：
- allowlist 存储在 `SecurityJackson2Modules$AllowlistTypeIdResolver.ALLOWLIST_CLASS_NAMES`
- 字段类型：`static final Set<String>`（存储类名字符串，不是 Class 对象）
- 字段是不可变的 `Collections.unmodifiableSet`
- 需要使用 `Unsafe` 来修改 `final` 字段

### 4. 实现步骤
1. 创建 `LongAllowlistModule` 继承 `SimpleModule`
2. 在 `setupModule` 方法中执行修改（确保在 ObjectMapper 初始化时执行）
3. 使用双重检查锁定确保只执行一次
4. 通过反射找到 `ALLOWLIST_CLASS_NAMES` 字段
5. 检测 Set 元素类型（String 或 Class）
6. 创建新的 Set，添加 `"java.lang.Long"` 或 `Long.class`
7. 使用 `Unsafe` 替换 final 字段的值
8. 验证修改是否成功

## 技术要点

### 为什么使用 Unsafe？
- `ALLOWLIST_CLASS_NAMES` 是 `static final` 字段
- 普通反射无法修改 `final` 字段（Java 12+ 已限制）
- `Unsafe` 可以绕过 JVM 的限制，直接修改内存

### 为什么在 setupModule 中执行？
- 确保在 ObjectMapper 使用之前修改 allowlist
- `setupModule` 在模块注册时自动调用，时机正确

### 为什么使用双重检查锁定？
- 防止多线程环境下重复修改
- 确保只执行一次，提高性能

## 文件清单

- `LongAllowlistModule.java`：核心实现，负责修改 allowlist
- `JacksonConfig.java`：注册模块到 authorizationMapper

## 官方推荐方式 vs 当前方案

### 官方推荐方式（已实现）
Spring Security 官方推荐通过以下方式处理反序列化问题：
1. **使用 Jackson 注解**：在自定义类上使用 `@JsonSerialize` 和 `@JsonDeserialize` 注解
2. **自定义序列化器/反序列化器**：为特定字段创建自定义序列化器/反序列化器
3. **使用 Mixin**：为需要反序列化的类创建 Mixin（可选）

### 当前实现方案（符合官方指南）
我们采用了**官方推荐的扩展方式**：

1. **在 SecurityUser 类上添加注解**：
   ```java
   @JsonSerialize(using = SecurityUserLongSerializer.class)
   @JsonDeserialize(using = SecurityUserLongDeserializer.class)
   public Long getUserId() {
       return userId;
   }
   ```

2. **自定义序列化器**（`SecurityUserLongSerializer`）：
   - 将 `Long` 类型的 `userId` 序列化为 `String`
   - 避免 Spring Security allowlist 检查失败
   - 避免 JavaScript 精度丢失

3. **自定义反序列化器**（`SecurityUserLongDeserializer`）：
   - 支持从 `String` 或 `Number` 反序列化为 `Long`
   - 兼容新旧数据格式

4. **备用方案**（`LongAllowlistModule`）：
   - 虽然通过自定义序列化器已经解决了问题，但保留此模块作为备用方案
   - 以防其他 Long 类型字段需要支持

### 方案优势
1. **符合官方指南**：使用 Jackson 注解和自定义序列化器，不修改框架内部实现
2. **类型安全**：通过注解明确指定序列化/反序列化行为
3. **兼容性好**：支持新旧数据格式（String 和 Number）
4. **易于维护**：代码清晰，易于理解和维护
5. **性能优化**：避免 JavaScript 精度丢失问题

### 为什么还需要 LongAllowlistModule？
虽然通过自定义序列化器已经解决了 `SecurityUser.userId` 的问题，但：
1. **其他 Long 字段**：如果 OAuth2 授权数据中包含其他 Long 类型字段，仍需要 allowlist 支持
2. **备用方案**：如果自定义序列化器失效，此模块仍可工作
3. **向后兼容**：对于已经序列化为 Long 类型的旧数据，allowlist 模块可以处理

## 注意事项

1. **兼容性**：使用 `Unsafe` 可能在未来的 Java 版本中失效，需要关注 Spring Security 的更新
2. **安全性**：修改 allowlist 会降低反序列化安全性，但 Long 是安全的 Java 基础类型
3. **维护性**：如果 Spring Security 内部实现改变，可能需要调整代码
4. **官方支持**：此方案不是官方推荐，但是在现有约束下的实用解决方案

## 测试验证

修改后应验证：
1. OAuth2 授权流程正常工作
2. 包含 Long 类型的授权数据可以正常反序列化
3. 应用启动时看到成功日志

## 参考链接

- [Spring Security Jackson2Modules 源码](https://github.com/spring-projects/spring-security/blob/main/core/src/main/java/org/springframework/security/jackson2/SecurityJackson2Modules.java)
- [Jackson Module 文档](https://github.com/FasterXML/jackson-docs/wiki/JacksonModules)

