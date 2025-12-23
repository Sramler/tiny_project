# GitHub 仓库重命名指南

## 当前状态

- **旧仓库名**：`tiny_project`
- **新仓库名**：`tiny-platform`
- **GitHub 用户名**：`Sramler`

## 步骤 1：在 GitHub 上重命名仓库

### 方法 1：通过 GitHub 网页界面（推荐）

1. **访问仓库设置**
   - 打开浏览器，访问：`https://github.com/Sramler/tiny_project`
   - 点击仓库页面右上角的 **Settings**（设置）按钮

2. **重命名仓库**
   - 在 Settings 页面中，向下滚动到 **Repository name**（仓库名称）部分
   - 将 `tiny_project` 修改为 `tiny-platform`
   - 点击 **Rename**（重命名）按钮
   - 确认重命名操作

3. **验证**
   - 重命名后，GitHub 会自动将旧 URL 重定向到新 URL
   - 新仓库地址：`https://github.com/Sramler/tiny-platform`

### 方法 2：使用 GitHub CLI（如果已安装）

```bash
gh repo rename tiny-platform --repo Sramler/tiny_project
```

## 步骤 2：更新本地 Git 配置（已完成）

本地远程 URL 已更新为：
```bash
git remote set-url origin https://github.com/Sramler/tiny-platform
```

## 步骤 3：验证连接

执行以下命令验证远程连接：

```bash
# 检查远程配置
git remote -v

# 测试连接
git fetch origin

# 查看远程分支
git branch -r
```

## 注意事项

### ✅ 重命名后的影响

1. **URL 变更**
   - 旧 URL：`https://github.com/Sramler/tiny_project`
   - 新 URL：`https://github.com/Sramler/tiny-platform`
   - GitHub 会自动设置重定向，但建议更新所有引用

2. **需要更新的地方**
   - ✅ 本地 Git 远程 URL（已完成）
   - ⚠️ CI/CD 配置（如 GitHub Actions、Jenkins 等）
   - ⚠️ 文档中的仓库链接
   - ⚠️ 其他项目的依赖引用
   - ⚠️ 书签和收藏

3. **不会影响**
   - ✅ Git 历史记录
   - ✅ Issues 和 Pull Requests
   - ✅ Stars 和 Forks
   - ✅ 已克隆的本地仓库（只需更新远程 URL）

### ⚠️ 重要提示

- **重命名后，旧的 URL 会重定向到新 URL**，但建议尽快更新所有引用
- **如果仓库是公开的**，重命名可能会影响其他用户的克隆和引用
- **如果仓库有 Webhooks**，需要更新 Webhook URL
- **如果仓库有部署配置**，需要更新部署脚本中的仓库 URL

## 验证步骤

完成重命名后，执行以下命令验证：

```bash
# 1. 检查远程 URL
git remote -v
# 应该显示：origin  https://github.com/Sramler/tiny-platform

# 2. 测试连接
git fetch origin
# 应该成功获取远程更新

# 3. 推送测试（可选）
git push origin main
# 应该成功推送到新仓库
```

## 如果遇到问题

### 问题 1：无法连接到远程仓库

**症状**：`git fetch` 或 `git push` 失败

**解决**：
```bash
# 检查远程 URL
git remote -v

# 如果 URL 不正确，重新设置
git remote set-url origin https://github.com/Sramler/tiny-platform

# 测试连接
git fetch origin
```

### 问题 2：GitHub 上找不到仓库

**症状**：访问新 URL 时显示 404

**解决**：
1. 确认已在 GitHub 上完成重命名
2. 检查仓库名称是否正确（区分大小写）
3. 确认有访问权限

### 问题 3：推送被拒绝

**症状**：`git push` 时提示权限错误

**解决**：
```bash
# 检查认证方式
# 如果使用 HTTPS，可能需要更新凭据
git config --global credential.helper store

# 或者使用 SSH（推荐）
git remote set-url origin git@github.com:Sramler/tiny-platform.git
```

## 使用 SSH 方式（推荐）

如果希望使用 SSH 方式连接，可以：

```bash
# 更新为 SSH URL
git remote set-url origin git@github.com:Sramler/tiny-platform.git

# 验证
git remote -v
```

## 总结

1. ✅ **本地远程 URL 已更新**：`https://github.com/Sramler/tiny-platform`
2. ⏳ **需要在 GitHub 上重命名仓库**：`tiny_project` → `tiny-platform`
3. ⏳ **验证连接**：执行 `git fetch origin` 确认连接正常

完成 GitHub 上的重命名后，所有操作都应该正常工作。

