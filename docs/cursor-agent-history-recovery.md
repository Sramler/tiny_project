# Cursor Agent 历史记录恢复指南

## 问题描述

当项目从 `tiny_project` 重命名为 `tiny-platform` 后，Cursor IDE 的 Agent 对话历史记录可能无法显示，这是因为：

1. **工作区路径改变**：Cursor 使用工作区路径的哈希值来标识不同的项目
2. **新的工作区标识**：路径改变后，Cursor 会为新的路径生成新的工作区标识符
3. **历史记录关联**：旧的历史记录仍然关联到旧的工作区标识符

## Cursor 历史记录存储位置

### 1. 项目级别（`.cursor` 目录）
- 位置：`/Users/bliu/code/tiny-platform/.cursor/`
- 内容：项目特定的配置和调试日志
- 状态：✅ 已存在

### 2. 工作区存储（workspaceStorage）
- 位置：`~/Library/Application Support/Cursor/User/workspaceStorage/`
- 内容：每个工作区的独立存储，包括：
  - `chatEditingSessions/` - 对话编辑会话
  - `redhat.java/` - Java 语言服务器数据
  - 其他扩展数据
- 特点：每个工作区路径对应一个唯一的哈希值目录

### 3. 全局存储（globalStorage）
- 位置：`~/Library/Application Support/Cursor/User/globalStorage/`
- 内容：
  - `state.vscdb` - SQLite 数据库，存储全局状态
  - `storage.json` - JSON 格式的存储
  - 扩展的全局数据

## 解决方案

### 方案 1：查找旧工作区并手动迁移（推荐）⭐⭐⭐⭐⭐

#### 步骤 1：查找旧工作区的哈希值

```bash
# 方法 1：检查 workspaceStorage 目录的修改时间
ls -lt "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage" | head -20

# 方法 2：查找包含旧项目名称的目录
find "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage" -type f -name "*.json" -exec grep -l "tiny_project" {} \; 2>/dev/null

# 方法 3：检查 chatEditingSessions 目录
find "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage" -type d -name "chatEditingSessions" -exec ls -lt {} \; 2>/dev/null
```

#### 步骤 2：备份旧工作区数据

```bash
# 假设找到旧工作区哈希值为：OLD_WORKSPACE_HASH
OLD_WORKSPACE_HASH="旧工作区的哈希值"
NEW_WORKSPACE_HASH="新工作区的哈希值"

# 备份旧工作区的 chatEditingSessions
cp -r \
  "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage/${OLD_WORKSPACE_HASH}/chatEditingSessions" \
  "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage/${OLD_WORKSPACE_HASH}/chatEditingSessions.backup"

# 备份到新工作区
mkdir -p "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage/${NEW_WORKSPACE_HASH}/chatEditingSessions"
cp -r \
  "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage/${OLD_WORKSPACE_HASH}/chatEditingSessions"/* \
  "/Users/bliu/Library/Application Support/Cursor/User/workspaceStorage/${NEW_WORKSPACE_HASH}/chatEditingSessions/"
```

#### 步骤 3：重启 Cursor

关闭并重新打开 Cursor，历史记录应该会显示。

### 方案 2：使用符号链接（临时方案）⭐⭐⭐

如果无法找到旧工作区，可以尝试创建符号链接：

```bash
# 1. 找到当前工作区的哈希值
# 可以通过检查最近修改的 workspaceStorage 目录来确定

# 2. 创建符号链接（如果旧路径还存在）
# 注意：这需要旧路径仍然存在
ln -s \
  "/Users/bliu/code/tiny-platform" \
  "/Users/bliu/code/tiny_project"
```

### 方案 3：从全局数据库恢复（高级）⭐⭐

#### 检查全局数据库

```bash
# 安装 sqlite3（如果未安装）
# macOS: brew install sqlite3

# 查看数据库表结构
sqlite3 "/Users/bliu/Library/Application Support/Cursor/User/globalStorage/state.vscdb" \
  ".tables"

# 查找包含旧路径的记录
sqlite3 "/Users/bliu/Library/Application Support/Cursor/User/globalStorage/state.vscdb" \
  "SELECT * FROM sqlite_master WHERE sql LIKE '%tiny_project%';"
```

⚠️ **警告**：直接修改数据库可能导致数据损坏，请先备份！

### 方案 4：接受历史记录丢失（最简单）⭐

如果历史记录不重要，可以：

1. 继续使用新的工作区
2. 新的对话会正常保存
3. 旧的历史记录会保留在旧工作区中，但不会显示

## 已修复的问题

### ✅ 代码中的硬编码路径

已修复以下文件中的硬编码路径：

1. `tiny-oauth-server/src/main/java/com/tiny/export/demo/DemoExportUsageController.java`
   - `/Users/bliu/code/tiny_project/.cursor/debug.log` → `/Users/bliu/code/tiny-platform/.cursor/debug.log`

2. `tiny-oauth-server/src/main/java/com/tiny/export/demo/DemoExportUsageService.java`
   - `/Users/bliu/code/tiny_project/.cursor/debug.log` → `/Users/bliu/code/tiny-platform/.cursor/debug.log`

## 预防措施

### 1. 使用相对路径或环境变量

避免在代码中硬编码绝对路径：

```java
// ❌ 不推荐
FileWriter fw = new FileWriter("/Users/bliu/code/tiny-platform/.cursor/debug.log", true);

// ✅ 推荐：使用系统属性或环境变量
String projectRoot = System.getProperty("user.dir");
String debugLogPath = projectRoot + "/.cursor/debug.log";
FileWriter fw = new FileWriter(debugLogPath, true);
```

### 2. 定期备份工作区数据

```bash
# 创建备份脚本
cat > ~/backup-cursor-workspace.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="$HOME/backups/cursor-workspace-$(date +%Y%m%d)"
mkdir -p "$BACKUP_DIR"
cp -r "$HOME/Library/Application Support/Cursor/User/workspaceStorage" "$BACKUP_DIR/"
cp -r "$HOME/Library/Application Support/Cursor/User/globalStorage" "$BACKUP_DIR/"
echo "备份完成: $BACKUP_DIR"
EOF

chmod +x ~/backup-cursor-workspace.sh
```

### 3. 使用 Git 管理项目配置

将 `.cursor` 目录中的重要配置提交到 Git（注意排除敏感信息）：

```bash
# .gitignore
.cursor/debug.log
.cursor/*.log
.cursor/cache/
```

## 验证步骤

### 1. 检查代码路径修复

```bash
# 确认没有旧的硬编码路径
grep -r "tiny_project" tiny-oauth-server/src/main/java/com/tiny/export/demo/ || echo "✅ 没有找到旧路径"
```

### 2. 检查 Cursor 工作区

1. 打开 Cursor
2. 打开项目：`/Users/bliu/code/tiny-platform`
3. 检查 Agent 对话历史是否显示
4. 尝试开始新的对话，确认可以正常保存

### 3. 检查调试日志

```bash
# 确认调试日志路径正确
ls -la /Users/bliu/code/tiny-platform/.cursor/debug.log
```

## 迁移执行记录

### ✅ 已完成迁移（2024-12-21）

1. ✅ **找到工作区标识**
   - 旧工作区（tiny_project）：`d91e516989e544b7bdfbe3db1fa30d60`
   - 新工作区（tiny-platform）：`361bd1cfbec8978ac8d0901b7c1a54f7`

2. ✅ **备份数据库**
   - 已备份旧工作区数据库：`state.vscdb.backup.20241221_*`
   - 已备份新工作区数据库：`state.vscdb.backup.20241221_*`

3. ✅ **导出历史记录**
   - 从旧工作区导出了 231 条聊天相关记录
   - 包括：`workbench.panel.aichat.*`、`workbench.panel.composerChatViewPane.*`、`memento.webviewView.*`、`cursor/agentLayout.*` 等

4. ✅ **导入到新工作区**
   - 成功导入 230 条记录到新工作区数据库
   - 数据库大小从 48KB 增加到 120KB
   - 包含 111 条 `composerChatViewPane` 记录

5. ✅ **修复代码中的硬编码路径**
   - `DemoExportUsageController.java`：2 处路径更新
   - `DemoExportUsageService.java`：4 处路径更新

### 📋 后续迁移操作（2024-12-21 更新）

1. ✅ **导入会话记录**
   - 导入了 `interactive.sessions` 和 `history.entries`
   - 更新了 `history.entries` 中的路径引用（`tiny_project` → `tiny-platform`）

2. ⚠️ **问题：重启后历史记录仍未显示**

   可能的原因：
   - **对话历史存储在 Cursor 服务器端**：Agent 对话可能同步到 Cursor 的云端服务器
   - **工作区标识符关联**：服务器端可能使用工作区路径哈希来关联对话
   - **账户同步问题**：需要重新登录或同步账户

3. 🔍 **进一步排查建议**

   - **检查 Cursor 账户同步**：
     - 打开 Cursor 设置
     - 检查账户登录状态
     - 尝试登出并重新登录
   
   - **检查网络连接**：
     - 确保可以访问 Cursor 服务器
     - 检查是否有防火墙阻止
   
   - **联系 Cursor 支持**：
     - 如果历史记录很重要，可以联系 Cursor 官方支持
     - 提供旧工作区路径和新工作区路径
     - 询问是否可以迁移服务器端的对话历史

4. ✅ **已验证的迁移内容**
   - ✅ 工作区数据库配置（230 条记录）
   - ✅ 编辑器历史记录（history.entries）
   - ✅ 会话配置（interactive.sessions）
   - ⚠️ Agent 对话历史（可能存储在服务器端）

## 总结

### ✅ 已完成

1. ✅ 修复代码中的硬编码路径（`tiny_project` → `tiny-platform`）
2. ✅ 创建恢复指南文档
3. ✅ 找到并识别旧工作区和新工作区
4. ✅ 备份数据库文件
5. ✅ 导出并迁移历史记录（230 条）

### 💡 建议

- **优先尝试方案 1**：查找并迁移旧工作区的数据
- **如果历史记录不重要**：可以直接使用方案 4，继续使用新的工作区
- **未来预防**：避免硬编码路径，使用相对路径或配置项

## 相关文档

- [Cursor IDE 重新导入项目指南](./cursor-ide-reimport-guide.md)
- [项目重命名影响分析](./tiny-platform-vs-tiny-project-naming-comparison.md)

