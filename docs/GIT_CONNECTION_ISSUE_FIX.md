# Git 连接问题解决方案

## 问题描述

在执行 `git pull --tags origin main` 时遇到以下错误：

```
fatal: unable to access 'https://github.com/Sramler/tiny_project/': Failed to connect to github.com port 443 after 75004 ms: Couldn't connect to server
```

## 问题分析

### 症状

- ICMP 连接正常（ping github.com 成功）
- HTTPS 端口 443 连接超时
- Git 配置使用 HTTP/1.1 协议

### 可能原因

1. Git 配置使用 HTTP/1.1，可能导致连接超时
2. 网络环境对 HTTPS 443 端口有限制
3. 防火墙或代理设置问题

## 解决方案

### 方案一：更新 Git HTTP 协议版本（推荐）

将 Git 的 HTTP 版本从 HTTP/1.1 更新为 HTTP/2，并优化相关配置：

```bash
# 更新 HTTP 版本为 HTTP/2
git config --global http.version HTTP/2

# 优化 HTTP 缓冲区设置
git config --global http.postBuffer 524288000

# 禁用低速连接限制
git config --global http.lowSpeedLimit 0
git config --global http.lowSpeedTime 0

# 确保 SSL 验证已启用
git config --global http.sslVerify true
```

### 方案二：使用 SSH 替代 HTTPS

如果 HTTPS 连接持续不稳定，可以切换到 SSH 方式：

#### 1. 生成 SSH 密钥（如果还没有）

```bash
ssh-keygen -t ed25519 -C "your_email@example.com"
```

#### 2. 将公钥添加到 GitHub

```bash
cat ~/.ssh/id_ed25519.pub
```

复制输出的公钥内容，在 GitHub 设置中添加 SSH 密钥。

#### 3. 修改远程仓库地址

```bash
# 查看当前远程地址
git remote -v

# 将 HTTPS 地址改为 SSH
git remote set-url origin git@github.com:Sramler/tiny_project.git

# 验证修改
git remote -v
```

#### 4. 测试 SSH 连接

```bash
ssh -T git@github.com
```

### 方案三：配置代理（如果需要）

如果网络环境需要通过代理访问：

```bash
# 设置 HTTP 代理
git config --global http.proxy http://proxy.example.com:8080
git config --global https.proxy https://proxy.example.com:8080

# 如果需要认证
git config --global http.proxy http://username:password@proxy.example.com:8080

# 取消代理设置
git config --global --unset http.proxy
git config --global --unset https.proxy
```

### 方案四：增加超时时间

如果连接速度较慢，可以增加超时时间：

```bash
# 设置连接超时时间（秒）
git config --global http.timeout 300
```

## 验证修复

执行以下命令验证修复是否成功：

```bash
# 测试连接
git pull --tags origin main

# 或者测试 fetch
git fetch origin
```

## 已应用的配置

当前项目已应用以下配置：

- `http.version=HTTP/2`
- `http.postBuffer=524288000`
- `http.lowSpeedLimit=0`
- `http.lowSpeedTime=0`
- `http.sslVerify=true`

## 其他建议

1. **检查网络环境**：确认防火墙或网络策略是否限制了 HTTPS 连接
2. **使用 VPN**：如果在受限网络环境中，考虑使用 VPN
3. **更换网络**：尝试使用不同的网络环境（如移动热点）
4. **联系网络管理员**：在企业网络环境中，可能需要联系 IT 部门开放相关端口

## 相关资源

- [Git 官方文档 - 配置](https://git-scm.com/book/zh/v2/自定义-Git-配置-Git)
- [GitHub 帮助 - 使用 SSH](https://docs.github.com/zh/authentication/connecting-to-github-with-ssh)
- [GitHub 帮助 - 故障排除](https://docs.github.com/zh/get-started/getting-started-with-git/troubleshooting-connectivity-problems)

## 更新记录

- **2024-XX-XX**：初始文档创建，记录 Git HTTPS 连接超时问题及解决方案
