# idempotent-platform 命名规范分析

## 一、当前命名情况

### 现有模块命名

| 模块名 | 前缀 | 说明 |
|--------|------|------|
| `tiny_web` | `tiny_` | 使用下划线（历史遗留） |
| `tiny-oauth-server` | `tiny-` | ✅ 有前缀 |
| `tiny-oauth-client` | `tiny-` | ✅ 有前缀 |
| `tiny-oauth-resource` | `tiny-` | ✅ 有前缀 |
| `tiny-idempotent-starter` | `tiny-` | ✅ 有前缀（旧模块，已废弃） |
| `tiny-common-exception` | `tiny-` | ✅ 有前缀 |
| `idempotent-platform` | ❌ 无前缀 | 新增模块 |

### 子模块命名

`idempotent-platform` 的子模块：
- `idempotent-core`
- `idempotent-repository`
- `idempotent-sdk`
- `idempotent-starter`
- `idempotent-console`
- `idempotent-example`

## 二、命名一致性分析

### ✅ 支持统一前缀的理由

1. **命名规范一致性**
   - 项目中绝大多数模块都有 `tiny-` 前缀
   - 统一前缀可以保持代码库的整洁和规范性
   - 符合组织命名空间约定（groupId: `com.tiny`）

2. **明确归属关系**
   - `tiny-` 前缀明确标识这是 `tiny_project` 的一部分
   - 便于识别和管理项目模块

3. **避免命名冲突**
   - 如果有其他项目也使用 `idempotent-platform` 名称，可以避免冲突
   - 明确的作用域和命名空间

### ⚠️ 不支持统一前缀的理由

1. **子模块命名考虑**
   - 如果父模块改为 `tiny-idempotent-platform`，子模块是否也要改为 `tiny-idempotent-core`？
   - 可能导致子模块名称过长
   - 增加重命名的工作量

2. **外部依赖引用**
   - 如果其他项目要引用这些模块，`tiny-idempotent-*` 可能不够简洁
   - 但从 `groupId` 已经可以看出归属（`com.tiny`）

3. **行业惯例**
   - 有些开源项目使用功能命名（如 `spring-boot-starter-web`）
   - 有些项目使用组织前缀（如 `spring-*`）

## 三、推荐方案

### 方案 A：统一使用 `tiny-` 前缀（推荐）⭐⭐⭐⭐⭐

#### 重命名内容

**父模块**：
- `idempotent-platform` → `tiny-idempotent-platform`

**子模块（可选，建议保持原样）**：
- 子模块可以保持 `idempotent-core`、`idempotent-sdk` 等名称
- 理由：`groupId` 已经明确归属（`com.tiny`），子模块名称可以更简洁

#### 优点
- ✅ 与项目其他模块命名一致
- ✅ 明确标识归属
- ✅ 符合组织命名规范
- ✅ 子模块名称简洁

#### 缺点
- ⚠️ 需要重命名父模块目录和配置
- ⚠️ 需要更新所有引用

### 方案 B：保持现状 ⭐⭐⭐

#### 说明
- 保持 `idempotent-platform` 名称不变
- 通过 `groupId: com.tiny` 已经明确归属

#### 优点
- ✅ 无需重命名，工作量小
- ✅ 子模块名称简洁

#### 缺点
- ⚠️ 与项目其他模块命名不一致
- ⚠️ 可能造成命名规范混乱

### 方案 C：子模块也添加前缀 ⭐⭐

#### 重命名内容

**父模块**：
- `idempotent-platform` → `tiny-idempotent-platform`

**所有子模块**：
- `idempotent-core` → `tiny-idempotent-core`
- `idempotent-sdk` → `tiny-idempotent-sdk`
- `idempotent-starter` → `tiny-idempotent-starter`
- `idempotent-repository` → `tiny-idempotent-repository`
- `idempotent-console` → `tiny-idempotent-console`
- `idempotent-example` → `tiny-idempotent-example`

#### 优点
- ✅ 完全统一命名规范

#### 缺点
- ⚠️ 工作量大（需要重命名多个模块）
- ⚠️ 子模块名称过长
- ⚠️ 可能导致依赖引用变更

## 四、实施建议

### 推荐：方案 A（仅重命名父模块）

**理由**：
1. ✅ 平衡了命名一致性和工作量
2. ✅ 父模块名称统一，子模块保持简洁
3. ✅ `groupId` 已经明确归属，子模块无需前缀

**需要修改的地方**：
1. 目录重命名：`idempotent-platform/` → `tiny-idempotent-platform/`
2. 父 POM 的 `<artifactId>` 和 `<name>`
3. 根 `pom.xml` 的 `<module>` 标签
4. 所有引用 `idempotent-platform` 的依赖配置
5. README 文档中的路径引用

**不需要修改的地方**：
1. 子模块的 artifactId（保持 `idempotent-core` 等）
2. 包名（保持 `com.tiny.idempotent.*`）

## 五、实施步骤

如果采用方案 A，需要执行：

1. 重命名目录
2. 更新父 POM
3. 更新根 POM
4. 更新所有依赖引用（如 `tiny-oauth-server/pom.xml`）
5. 更新文档（README.md）
6. 验证编译和测试

## 六、结论

**建议采用方案 A**：将 `idempotent-platform` 重命名为 `tiny-idempotent-platform`，子模块保持原样。

**原因**：
- 与项目其他模块命名一致
- 工作量适中
- 子模块名称简洁
- 通过 `groupId` 已经明确归属

