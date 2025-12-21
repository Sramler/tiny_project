# Cursor IDE 重新导入项目指南

## 一、为什么可能需要重新导入？

由于我们进行了以下重命名操作：

- 根目录：`tiny_project` → `tiny-platform`
- 模块：`tiny_web` → `tiny-web`
- 根 POM：`artifactId` 更新为 `tiny-platform`

IDE 的配置文件（`.cursor`、`.idea`、`.vscode`）中可能包含旧的项目路径和引用。

## 二、是否需要重新导入？

### ✅ 建议：刷新项目即可（通常不需要完全重新导入）

**Cursor 通常会自动检测项目变化**，但建议执行以下操作：

### 方法 1：刷新项目（推荐）⭐⭐⭐⭐⭐

1. **重新打开项目**

   - `File` → `Open Folder` → 选择 `/Users/bliu/code/tiny-platform`
   - 或者直接关闭并重新打开 Cursor

2. **刷新 Maven 项目**（如果使用 Maven 插件）

   - 右键点击 `pom.xml` → `Reload Maven Project`
   - 或者使用命令面板：`Ctrl/Cmd + Shift + P` → `Maven: Reload Project`

3. **重新索引项目**
   - Cursor 会自动重新索引，但可以手动触发：
   - `Ctrl/Cmd + Shift + P` → `Java: Clean Java Language Server Workspace`

### 方法 2：清理并重新打开（如果方法 1 有问题）

如果刷新后仍有问题，可以清理 IDE 配置：

1. **关闭 Cursor**

2. **备份并清理 IDE 配置**（可选）

   ```bash
   # 备份
   cp -r .idea .idea.backup
   cp -r .cursor .cursor.backup

   # 清理（可选，如果需要完全重置）
   # rm -rf .idea
   # rm -rf .cursor
   ```

3. **重新打开项目**
   - `File` → `Open Folder` → `/Users/bliu/code/tiny-platform`

## 三、验证项目是否正确识别

### 检查清单

1. ✅ **Maven 模块识别**

   - 检查 `pom.xml` 是否被正确识别为 Maven 项目
   - 检查所有子模块是否都显示在项目树中

2. ✅ **代码提示和补全**

   - 检查 Java 代码提示是否正常
   - 检查包导入是否正常

3. ✅ **编译和运行**

   - 尝试编译项目（应该成功）
   - 检查是否有路径相关的错误

4. ✅ **依赖解析**
   - 检查 Maven 依赖是否正确解析
   - 检查是否有红色波浪线（依赖错误）

## 四、常见问题处理

### 问题 1：找不到模块

**症状**：IDE 提示找不到某个模块

**解决**：

- 刷新 Maven 项目
- 检查 `.idea/modules.xml` 是否正确
- 重新加载项目

### 问题 2：路径错误

**症状**：IDE 显示旧的路径（`tiny_project`）

**解决**：

- 清理 IDE 缓存
- 重新打开项目
- 如果问题持续，可以删除 `.idea` 和 `.cursor` 目录（会丢失 IDE 配置，但会重新生成）

### 问题 3：Maven 依赖解析失败

**症状**：Maven 依赖无法解析

**解决**：

- 检查 Maven 设置
- 重新下载依赖：`mvn clean install -U`
- 刷新 Maven 项目

## 五、推荐操作流程

### 标准流程（推荐）

1. ✅ **保持项目打开状态**
2. ✅ **刷新 Maven 项目**（右键 `pom.xml` → `Reload Maven Project`）
3. ✅ **等待 IDE 重新索引**
4. ✅ **验证编译**（应该已经通过）

### 如果遇到问题

1. ✅ **关闭 Cursor**
2. ✅ **删除 IDE 缓存**（可选）
   ```bash
   rm -rf .idea
   rm -rf .cursor
   ```
3. ✅ **重新打开项目**
4. ✅ **等待 IDE 重新生成配置**

## 六、总结

### ✅ 通常不需要完全重新导入

**原因**：

- Cursor 会自动检测项目变化
- Maven 项目结构没有改变（只是名称改变）
- 只需要刷新 Maven 项目即可

### ⚠️ 如果遇到问题

- 先尝试刷新 Maven 项目
- 如果问题持续，再考虑清理 IDE 配置
- 最后的选择是完全重新导入

### 📝 验证要点

- Maven 模块识别正确
- 代码提示和补全正常
- 编译成功
- 依赖解析正确
