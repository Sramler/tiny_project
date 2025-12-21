# tiny_project 命名合理性分析

## 一、当前命名情况

### 根项目
- **artifactId**: `tiny_project`（使用下划线）
- **groupId**: `com.tiny`
- **名称**: `tiny_project`

### 子模块命名
| 模块名 | 命名方式 | 说明 |
|--------|---------|------|
| `tiny_web` | 下划线 `_` | ⚠️ 不一致 |
| `tiny-oauth-server` | 连字符 `-` | ✅ 标准 |
| `tiny-oauth-client` | 连字符 `-` | ✅ 标准 |
| `tiny-oauth-resource` | 连字符 `-` | ✅ 标准 |
| `tiny-common-exception` | 连字符 `-` | ✅ 标准 |
| `tiny-idempotent-platform` | 连字符 `-` | ✅ 标准 |
| `tiny-idempotent-starter` | 连字符 `-` | ✅ 标准（旧） |

## 二、命名规范分析

### ⚠️ 存在的问题

#### 1. 命名不一致 ⭐⭐⭐
- **根项目**: `tiny_project`（下划线）
- **子模块**: 大部分使用 `tiny-xxx`（连字符）
- **例外**: `tiny_web` 也使用下划线

**影响**：
- 命名规范不统一
- 可能造成混淆
- 不符合 Maven 最佳实践（推荐使用连字符）

#### 2. 名称过于通用 ⭐⭐
- `tiny_project` 这个名字比较通用，不够具体
- 无法从名称直接了解项目的具体用途

#### 3. 下划线 vs 连字符 ⭐⭐⭐
- Maven 推荐使用连字符（`-`）而不是下划线（`_`）
- 连字符在 URL 和文件系统中更友好
- 许多工具和平台对下划线的处理不如连字符

## 三、Maven 命名最佳实践

### 推荐的命名规范

1. **artifactId** 使用连字符（`-`）
   - ✅ 推荐：`tiny-oauth-server`
   - ❌ 不推荐：`tiny_oauth_server`

2. **groupId** 使用点号（`.`），通常表示组织域名的倒序
   - ✅ 示例：`com.tiny`、`org.springframework`

3. **版本号** 使用语义化版本（SemVer）
   - ✅ 示例：`1.0.0`、`1.0.0-SNAPSHOT`

## 四、建议方案

### 方案 A：重命名为 `tiny-platform`（推荐）⭐⭐⭐⭐⭐

**理由**：
- ✅ 使用连字符，符合 Maven 规范
- ✅ 名称更具体（platform 表示这是一个平台项目）
- ✅ 与子模块命名一致
- ✅ 简洁明了

**需要修改**：
1. 目录名：`tiny_project` → `tiny-platform`
2. `pom.xml` 中的 `artifactId` 和 `<name>`
3. 所有子模块的 `parent` 引用

### 方案 B：保持现状（不推荐）⭐⭐

**理由**：
- ✅ 无需修改，工作量小
- ❌ 命名不一致
- ❌ 不符合最佳实践

### 方案 C：重命名为更具体的名称 ⭐⭐⭐

**选项**：
- `tiny-oauth-platform`：如果主要关注 OAuth 相关功能
- `tiny-enterprise-platform`：如果是企业级平台
- `tiny-microservices-platform`：如果是微服务平台

**考虑因素**：
- 项目的主要用途和范围
- 未来扩展的方向
- 组织的命名规范

## 五、实施建议

### 推荐：方案 A（重命名为 `tiny-platform`）

**步骤**：
1. 重命名根目录
2. 更新根 `pom.xml`
3. 更新所有子模块的 `parent` 引用
4. 更新文档和配置文件

**风险评估**：
- ⚠️ **低风险**：主要是配置文件和引用更新
- ⚠️ **Git 历史**：可能需要处理 Git 历史（如果重要）
- ⚠️ **CI/CD**：需要更新 CI/CD 配置中的路径引用

**工作量评估**：
- 约 15-30 分钟
- 影响文件：约 10-15 个 POM 文件

## 六、如果保持现状

### 需要至少统一 `tiny_web` 的命名

如果选择保持 `tiny_project` 名称，建议至少：
1. 将 `tiny_web` 重命名为 `tiny-web`（统一使用连字符）
2. 或者将根项目改为 `tiny-project`（统一使用连字符）

**最小改动方案**：
- 保持 `tiny_project` 根项目名称
- 统一 `tiny_web` → `tiny-web`

## 七、总结

### 当前命名的问题
1. ⚠️ 根项目使用下划线，子模块使用连字符（不一致）
2. ⚠️ `tiny_web` 也使用下划线（不一致）
3. ⚠️ 名称过于通用

### 推荐方案
✅ **重命名为 `tiny-platform`**（方案 A）

**理由**：
- 符合 Maven 最佳实践
- 命名统一（全部使用连字符）
- 名称更具体
- 工作量适中

### 替代方案
如果不想大改，至少统一命名规范：
- 将 `tiny_web` 重命名为 `tiny-web`
- 保持 `tiny_project` 或改为 `tiny-project`

