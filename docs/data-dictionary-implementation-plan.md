# Tiny Platform 数据字典模块分阶段实施计划

## 实施总览

本文档基于《数据字典设计规范》，提供分阶段实施计划，确保有序推进字典模块的重构和完善。

---

## 阶段划分

| 阶段 | 名称 | 优先级 | 预计工期 | 目标 |
|------|------|--------|---------|------|
| **Phase 0** | 准备阶段 | 高 | 1-2 天 | 创建模块结构、基础配置 |
| **Phase 1** | Core 核心能力 | 高 | 3-5 天 | 实现 Level0 基础能力 |
| **Phase 2** | Starter 自动配置 | 高 | 2-3 天 | Spring Boot 自动装配 |
| **Phase 3** | Repository 实现 | 中 | 2-3 天 | JPA/JDBC 数据访问 |
| **Phase 4** | 缓存实现 | 中 | 2-3 天 | 内存/Redis 缓存 |
| **Phase 5** | REST API 模块 | 中 | 3-4 天 | 前端调用接口 |
| **Phase 6** | 管理界面 | 低 | 5-7 天 | 字典管理 UI |
| **Phase 7** | 治理能力 | 低 | 3-5 天 | Level1/Level2 治理 |
| **Phase 8** | 扩展功能 | 低 | 5-7 天 | 版本管理、审计日志等 |

---

## Phase 0: 准备阶段（1-2 天）

### 0.1 创建模块结构

```bash
# 在 tiny-platform 根目录下创建模块
mkdir -p tiny-core/src/main/java/com/tiny/core/dict
mkdir -p tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter
mkdir -p tiny-core-dict-repository-jpa/src/main/java/com/tiny/core/dict/repository/jpa
mkdir -p tiny-core-dict-cache-memory/src/main/java/com/tiny/core/dict/cache/memory
mkdir -p tiny-core-dict-cache-redis/src/main/java/com/tiny/core/dict/cache/redis
```

### 0.2 更新父 POM

在 `pom.xml` 中添加模块：

```xml
<modules>
    <!-- 现有模块 -->
    <module>tiny-web</module>
    <module>tiny-oauth-server</module>
    <!-- ... -->
    
    <!-- 新增字典模块 -->
    <module>tiny-core</module>
    <module>tiny-core-dict-starter</module>
    <module>tiny-core-dict-repository-jpa</module>
    <module>tiny-core-dict-cache-memory</module>
    <module>tiny-core-dict-cache-redis</module>
</modules>
```

### 0.3 创建数据库表

执行 SQL 脚本创建表结构（参考设计规范第 6 节）。

### 0.4 任务清单

- [ ] 创建 `tiny-core` 模块目录结构
- [ ] 创建 `tiny-core-dict-starter` 模块目录结构
- [ ] 创建 `tiny-core-dict-repository-jpa` 模块目录结构
- [ ] 创建 `tiny-core-dict-cache-memory` 模块目录结构
- [ ] 创建 `tiny-core-dict-cache-redis` 模块目录结构
- [ ] 更新父 POM，添加新模块
- [ ] 执行数据库表创建脚本
- [ ] 配置数据库连接

---

## Phase 1: Core 核心能力（3-5 天）

### 1.1 实现实体类

**文件**: `tiny-core/src/main/java/com/tiny/core/dict/model/`

- [ ] `DictType.java` - 字典类型实体
- [ ] `DictItem.java` - 字典项实体

**要求**：
- 纯 POJO，无框架依赖
- 使用 JPA 注解（但 Core 模块不依赖 JPA，注解在 Starter 模块添加）

### 1.2 实现 Repository 接口

**文件**: `tiny-core/src/main/java/com/tiny/core/dict/repository/`

- [ ] `DictTypeRepository.java` - 字典类型 Repository 接口
- [ ] `DictItemRepository.java` - 字典项 Repository 接口

**要求**：
- 纯接口定义，无实现
- 不依赖 Spring Data JPA

### 1.3 实现缓存抽象

**文件**: `tiny-core/src/main/java/com/tiny/core/dict/cache/`

- [ ] `DictCache.java` - 缓存数据模型
- [ ] `DictCacheManager.java` - 缓存管理接口

**要求**：
- 纯接口和 POJO
- 不依赖 Redis、Spring Cache

### 1.4 实现 DictRuntime API

**文件**: `tiny-core/src/main/java/com/tiny/core/dict/runtime/`

- [ ] `DictRuntime.java` - 核心 API 接口

**要求**：
- 定义核心方法签名
- 不依赖 Spring

### 1.5 实现异常类

**文件**: `tiny-core/src/main/java/com/tiny/core/dict/exception/`

- [ ] `DictException.java` - 字典异常基类
- [ ] `DictNotFoundException.java` - 字典不存在异常
- [ ] `DictValidationException.java` - 字典校验异常

### 1.6 任务清单

- [ ] 实现 DictType 实体类
- [ ] 实现 DictItem 实体类
- [ ] 实现 DictTypeRepository 接口
- [ ] 实现 DictItemRepository 接口
- [ ] 实现 DictCache 模型
- [ ] 实现 DictCacheManager 接口
- [ ] 实现 DictRuntime 接口
- [ ] 实现异常类
- [ ] 编写单元测试

---

## Phase 2: Starter 自动配置（2-3 天）

### 2.1 实现自动配置类

**文件**: `tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter/autoconfigure/`

- [ ] `DictAutoConfiguration.java` - 自动配置类

**要求**：
- 使用 `@AutoConfiguration`
- 条件装配：`@ConditionalOnClass`、`@ConditionalOnProperty`
- 自动装配 DictRuntime、DictCacheManager

### 2.2 实现配置属性

**文件**: `tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter/properties/`

- [ ] `DictProperties.java` - 配置属性类

**要求**：
- 使用 `@ConfigurationProperties`
- 支持 `application.yml` 配置

### 2.3 创建自动配置文件

**文件**: `tiny-core-dict-starter/src/main/resources/META-INF/spring/`

- [ ] `org.springframework.boot.autoconfigure.AutoConfiguration.imports`

**内容**：
```
com.tiny.core.dict.starter.autoconfigure.DictAutoConfiguration
```

### 2.4 实现内存缓存（默认）

**文件**: `tiny-core-dict-cache-memory/src/main/java/com/tiny/core/dict/cache/memory/`

- [ ] `MemoryDictCacheManager.java` - 内存缓存实现

**要求**：
- 实现 DictCacheManager 接口
- 使用 ConcurrentHashMap
- 支持 TTL（可选）

### 2.5 任务清单

- [ ] 实现 DictAutoConfiguration
- [ ] 实现 DictProperties
- [ ] 创建自动配置文件
- [ ] 实现 MemoryDictCacheManager
- [ ] 编写自动配置测试
- [ ] 验证自动装配功能

---

## Phase 3: Repository 实现（2-3 天）

### 3.1 实现 JPA Repository

**文件**: `tiny-core-dict-repository-jpa/src/main/java/com/tiny/core/dict/repository/jpa/`

- [ ] `JpaDictTypeRepository.java` - JPA 实现
- [ ] `JpaDictItemRepository.java` - JPA 实现

**要求**：
- 实现 Core 中的 Repository 接口
- 使用 Spring Data JPA
- 支持多租户查询

### 3.2 实现 Service 层

**文件**: `tiny-core-dict-repository-jpa/src/main/java/com/tiny/core/dict/service/`

- [ ] `DictTypeService.java` - 字典类型服务
- [ ] `DictItemService.java` - 字典项服务

**要求**：
- 封装 Repository 操作
- 实现业务逻辑
- 支持多租户隔离

### 3.3 实现 DictRuntime 实现类

**文件**: `tiny-core-dict-repository-jpa/src/main/java/com/tiny/core/dict/runtime/`

- [ ] `DictRuntimeImpl.java` - DictRuntime 实现

**要求**：
- 实现 Core 中的 DictRuntime 接口
- 集成 DictCacheManager
- 实现多租户查询逻辑

### 3.4 任务清单

- [ ] 实现 JpaDictTypeRepository
- [ ] 实现 JpaDictItemRepository
- [ ] 实现 DictTypeService
- [ ] 实现 DictItemService
- [ ] 实现 DictRuntimeImpl
- [ ] 编写集成测试
- [ ] 验证多租户隔离

---

## Phase 4: 缓存实现（2-3 天）

### 4.1 实现 Redis 缓存

**文件**: `tiny-core-dict-cache-redis/src/main/java/com/tiny/core/dict/cache/redis/`

- [ ] `RedisDictCacheManager.java` - Redis 缓存实现

**要求**：
- 实现 DictCacheManager 接口
- 使用 RedisTemplate
- 支持 TTL、异步刷新

### 4.2 更新自动配置

**文件**: `tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter/autoconfigure/`

- [ ] 更新 `DictAutoConfiguration.java`，支持 Redis 条件装配

**要求**：
- `@ConditionalOnClass(RedisTemplate.class)` 检测 Redis
- 自动切换内存/Redis 缓存

### 4.3 任务清单

- [ ] 实现 RedisDictCacheManager
- [ ] 更新 DictAutoConfiguration，支持 Redis
- [ ] 编写缓存测试
- [ ] 验证缓存性能
- [ ] 验证缓存刷新机制

---

## Phase 5: REST API 模块（3-4 天）

### 5.1 创建 Web 模块

**文件**: `tiny-core-dict-web/pom.xml`

- [ ] 创建 `tiny-core-dict-web` 模块

### 5.2 实现 Controller

**文件**: `tiny-core-dict-web/src/main/java/com/tiny/core/dict/web/controller/`

- [ ] `DictController.java` - REST API 控制器

**要求**：
- 实现设计规范中的 API 接口
- 使用统一响应格式
- 支持分页查询

### 5.3 实现 DTO

**文件**: `tiny-core-dict-web/src/main/java/com/tiny/core/dict/web/dto/`

- [ ] `DictTypeQueryDTO.java`
- [ ] `DictTypeCreateDTO.java`
- [ ] `DictTypeUpdateDTO.java`
- [ ] `DictTypeDTO.java`
- [ ] `DictItemQueryDTO.java`
- [ ] `DictItemCreateDTO.java`
- [ ] `DictItemUpdateDTO.java`
- [ ] `DictItemDTO.java`

### 5.4 实现 Converter

**文件**: `tiny-core-dict-web/src/main/java/com/tiny/core/dict/web/converter/`

- [ ] `DictTypeConverter.java` - Entity ↔ DTO 转换
- [ ] `DictItemConverter.java` - Entity ↔ DTO 转换

### 5.5 任务清单

- [ ] 创建 tiny-core-dict-web 模块
- [ ] 实现 DictController
- [ ] 实现所有 DTO 类
- [ ] 实现 Converter
- [ ] 编写 API 测试
- [ ] 验证 API 功能

---

## Phase 6: 管理界面（5-7 天）

### 6.1 创建前端组件

**文件**: `tiny-oauth-server/src/main/webapp/src/views/dict/`

- [ ] `DictTypeList.vue` - 字典类型列表
- [ ] `DictTypeForm.vue` - 字典类型表单
- [ ] `DictItemList.vue` - 字典项列表
- [ ] `DictItemForm.vue` - 字典项表单
- [ ] `DictManagement.vue` - 字典管理主页面

### 6.2 创建 API 调用

**文件**: `tiny-oauth-server/src/main/webapp/src/api/dict.ts`

- [ ] 实现字典 API 调用函数

### 6.3 创建 Composable

**文件**: `tiny-oauth-server/src/main/webapp/src/composables/useDict.ts`

- [ ] 实现 `useDict` composable

### 6.4 任务清单

- [ ] 创建字典类型列表组件
- [ ] 创建字典类型表单组件
- [ ] 创建字典项列表组件
- [ ] 创建字典项表单组件
- [ ] 创建字典管理主页面
- [ ] 实现 API 调用
- [ ] 实现 useDict composable
- [ ] 编写前端测试

---

## Phase 7: 治理能力（3-5 天）

### 7.1 创建治理模块

**文件**: `tiny-core-governance/pom.xml`

- [ ] 创建 `tiny-core-governance` 模块

### 7.2 实现 Level1 严格校验

**文件**: `tiny-core-governance/src/main/java/com/tiny/core/governance/dict/validation/`

- [ ] `DictValidationService.java` - 严格校验服务

### 7.3 实现 Level2 高级治理

**文件**: `tiny-core-governance/src/main/java/com/tiny/core/governance/dict/`

- [ ] `DictForceService.java` - FORCE 变更服务
- [ ] `DictApprovalService.java` - 审批流程服务

### 7.4 实现租户策略管理

**文件**: `tiny-core-governance/src/main/java/com/tiny/core/governance/tenant/`

- [ ] `TenantPolicyService.java` - 租户策略服务

### 7.5 任务清单

- [ ] 创建 tiny-core-governance 模块
- [ ] 实现 DictValidationService
- [ ] 实现 DictForceService
- [ ] 实现 DictApprovalService
- [ ] 实现 TenantPolicyService
- [ ] 编写治理能力测试

---

## Phase 8: 扩展功能（5-7 天）

### 8.1 字典初始化

**文件**: `tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter/initializer/`

- [ ] `DictInitializer.java` - 字典初始化器

### 8.2 字典变更通知

**文件**: `tiny-core-dict-starter/src/main/java/com/tiny/core/dict/starter/event/`

- [ ] `DictChangeEvent.java` - 字典变更事件
- [ ] `DictChangeNotifier.java` - 变更通知器
- [ ] `DictCacheRefreshListener.java` - 缓存刷新监听器

### 8.3 字典国际化

**文件**: `tiny-core-dict-i18n/`

- [ ] 创建国际化模块
- [ ] 实现国际化表结构
- [ ] 实现国际化服务

### 8.4 字典版本管理

**文件**: `tiny-core-dict-version/`

- [ ] 创建版本管理模块
- [ ] 实现版本表结构
- [ ] 实现版本管理服务

### 8.5 审计日志

**文件**: `tiny-core-dict-audit/`

- [ ] 创建审计日志模块
- [ ] 实现审计日志表结构
- [ ] 实现审计日志服务

### 8.6 CI 校验工具

**文件**: `tiny-core-dict-checker/`

- [ ] 创建 dict-checker 工具模块
- [ ] 实现代码扫描
- [ ] 实现校验规则
- [ ] 集成到 CI/CD

### 8.7 任务清单

- [ ] 实现字典初始化机制
- [ ] 实现字典变更通知
- [ ] 实现字典国际化
- [ ] 实现字典版本管理
- [ ] 实现审计日志
- [ ] 实现 CI 校验工具

---

## 实施优先级建议

### 第一阶段（必须，2 周内完成）

1. **Phase 0**: 准备阶段
2. **Phase 1**: Core 核心能力
3. **Phase 2**: Starter 自动配置
4. **Phase 3**: Repository 实现

**目标**：实现最小可用版本（MVP），支持基本的字典查询功能。

### 第二阶段（重要，1 个月内完成）

5. **Phase 4**: 缓存实现
6. **Phase 5**: REST API 模块

**目标**：完善核心功能，支持前端调用。

### 第三阶段（可选，按需实施）

7. **Phase 6**: 管理界面
8. **Phase 7**: 治理能力
9. **Phase 8**: 扩展功能

**目标**：完善扩展功能，提升用户体验和治理能力。

---

## 验收标准

### Phase 1-3 验收标准（MVP）

- [ ] 可以创建字典类型和字典项
- [ ] 可以通过 DictRuntime 查询字典标签
- [ ] 支持多租户隔离
- [ ] 支持内存缓存
- [ ] 自动配置正常工作

### Phase 4-5 验收标准

- [ ] Redis 缓存正常工作
- [ ] REST API 接口可用
- [ ] 前端可以调用 API
- [ ] 分页查询正常

### Phase 6-8 验收标准

- [ ] 管理界面可用
- [ ] 治理能力正常工作
- [ ] 扩展功能按需实现

---

## 风险控制

### 技术风险

1. **数据库迁移风险**
   - 方案：先创建新表，数据迁移后再切换
   - 回滚：保留旧表，支持快速回滚

2. **性能风险**
   - 方案：缓存机制，性能测试
   - 监控：缓存命中率、查询延迟

3. **多租户隔离风险**
   - 方案：使用 ThreadLocal，拦截器自动设置
   - 测试：多租户隔离测试

### 进度风险

1. **延期风险**
   - 方案：分阶段实施，每阶段独立验收
   - 调整：根据实际情况调整优先级

2. **依赖风险**
   - 方案：模块独立，减少依赖
   - 隔离：Core 模块无框架依赖

---

## 后续维护

### 文档更新

- [ ] 更新 README.md
- [ ] 更新 API 文档
- [ ] 更新使用示例

### 代码质量

- [ ] 代码审查
- [ ] 单元测试覆盖率 > 80%
- [ ] 集成测试覆盖主要场景

### 性能优化

- [ ] 缓存优化
- [ ] 查询优化
- [ ] 监控告警

