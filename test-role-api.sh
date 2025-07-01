#!/bin/bash

# 测试角色管理API
echo "=== 测试角色管理API ==="

# 等待应用启动
echo "等待应用启动..."
sleep 10

# 测试获取角色列表
echo "1. 测试获取角色列表..."
curl -X GET "http://localhost:8080/sys/roles?page=0&size=10" \
  -H "Content-Type: application/json" \
  | jq '.'

# 测试创建角色
echo -e "\n2. 测试创建角色..."
curl -X POST "http://localhost:8080/sys/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试角色",
    "code": "ROLE_TEST",
    "description": "这是一个测试角色",
    "builtin": false,
    "enabled": true
  }' \
  | jq '.'

# 测试获取单个角色
echo -e "\n3. 测试获取单个角色..."
curl -X GET "http://localhost:8080/sys/roles/1" \
  -H "Content-Type: application/json" \
  | jq '.'

# 测试更新角色
echo -e "\n4. 测试更新角色..."
curl -X PUT "http://localhost:8080/sys/roles/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "更新后的角色",
    "code": "ROLE_UPDATED",
    "description": "这是更新后的角色描述",
    "builtin": false,
    "enabled": true
  }' \
  | jq '.'

echo -e "\n=== 测试完成 ===" 