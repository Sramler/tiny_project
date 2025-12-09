# .DS_Store 清理方案

## 问题
- 远程仓库存在 macOS 系统文件 `.DS_Store`（路径：`tiny_web/.DS_Store`），属环境产物，不应入库。

## 处理动作（已执行）
1. 删除并从 Git 追踪中移除：`git rm tiny_web/.DS_Store`
2. 确认忽略规则已存在：`.gitignore` 中包含 `**/.DS_Store`

## 预防与建议
- 如再次出现 `.DS_Store`，可在仓库根目录执行：
  ```bash
  find . -name '.DS_Store' -print -delete
  git status
  ```
- 保持 `.gitignore` 中的 `**/.DS_Store` 规则。
- 尽量在源码目录执行操作前清理工作区（例如使用 `rm -f **/.DS_Store` 或在 macOS Finder 设置隐藏文件不生成）。

## 验证
- `git ls-files '**/.DS_Store'` 输出为空即表示仓库不再追踪该文件。
