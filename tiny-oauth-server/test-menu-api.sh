#!/bin/bash

# 菜单管理API测试脚本
# 使用方法：./test-menu-api.sh

BASE_URL="http://localhost:8080"
TOKEN="your_token_here"  # 需要替换为实际的token

echo "=== 菜单管理API测试 ==="

# 1. 获取菜单列表
echo "1. 测试获取菜单列表..."
curl -X GET "${BASE_URL}/sys/menus?page=0&size=10" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -w "\nHTTP状态码: %{http_code}\n\n"

# 2. 获取菜单树
echo "2. 测试获取菜单树..."
curl -X GET "${BASE_URL}/sys/menus/tree" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -w "\nHTTP状态码: %{http_code}\n\n"

# 3. 创建菜单
echo "3. 测试创建菜单..."
curl -X POST "${BASE_URL}/sys/menus" \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-menu",
    "title": "测试菜单",
    "path": "/test",
    "icon": "TestOutlined",
    "showIcon": true,
    "sort": 100,
    "hidden": false,
    "keepAlive": false,
    "permission": "test:menu"
  }' \
  -w "\nHTTP状态码: %{http_code}\n\n"

echo "=== 测试完成 ===" 