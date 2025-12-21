# .DS_Store 清理与防护

## 现象

- 远程与本地均出现 `.DS_Store`（根目录和 `tiny_web/.DS_Store`），属于 macOS Finder 生成的环境文件，不应入库。

## 已采取的修复

1. 从 Git 追踪中移除：`git rm ./.DS_Store` 与 `git rm tiny_web/.DS_Store`
2. 确认忽略规则：`.gitignore` 已包含 `**/.DS_Store`

## 诊断与验证

- 检查远程是否仍含 `.DS_Store`：
  ```bash
  git ls-tree -r origin/main | grep DS_Store || true
  ```
- 检查本地已跟踪的 `.DS_Store`：
  ```bash
  git ls-files '**/.DS_Store'
  ```
- 命令输出为空即已清除。

## 预防操作

- 清理所有工作副本中的 `.DS_Store`：
  ```bash
  find . -name '.DS_Store' -print -delete
  git status
  ```
- 保持 `.gitignore` 中的 `**/.DS_Store` 规则。
- 习惯性在提交前清理（如 `rm -f **/.DS_Store`），避免再入库。
