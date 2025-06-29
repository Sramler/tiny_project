# API 接口测试文档

## 测试环境

- 后端服务：http://localhost:8080
- 前端服务：http://localhost:3000

## 菜单管理接口测试

### 1. 获取菜单列表

```bash
curl -X GET "http://localhost:8080/sys/menus?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. 获取菜单树

```bash
curl -X GET "http://localhost:8080/sys/menus/tree" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 创建菜单

```bash
curl -X POST "http://localhost:8080/sys/menus" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
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
  }'
```

### 4. 更新菜单

```bash
curl -X PUT "http://localhost:8080/sys/menus/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "test-menu-updated",
    "title": "测试菜单-更新",
    "path": "/test-updated",
    "icon": "TestOutlined",
    "showIcon": true,
    "sort": 200,
    "hidden": false,
    "keepAlive": false,
    "permission": "test:menu:updated"
  }'
```

### 5. 删除菜单

```bash
curl -X DELETE "http://localhost:8080/sys/menus/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 资源管理接口测试

### 1. 获取资源列表

```bash
curl -X GET "http://localhost:8080/sys/resources?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 2. 获取资源树

```bash
curl -X GET "http://localhost:8080/sys/resources/tree" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. 创建资源

```bash
curl -X POST "http://localhost:8080/sys/resources" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "test-resource",
    "title": "测试资源",
    "path": "/test-resource",
    "uri": "/api/test",
    "method": "GET",
    "type": 2,
    "sort": 100,
    "permission": "test:resource"
  }'
```

### 4. 更新资源

```bash
curl -X PUT "http://localhost:8080/sys/resources/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "test-resource-updated",
    "title": "测试资源-更新",
    "path": "/test-resource-updated",
    "uri": "/api/test-updated",
    "method": "POST",
    "type": 2,
    "sort": 200,
    "permission": "test:resource:updated"
  }'
```

### 5. 删除资源

```bash
curl -X DELETE "http://localhost:8080/sys/resources/1" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 前端页面测试

### 1. 菜单管理页面

访问：http://localhost:3000/system/menu

测试功能：

- [ ] 页面正常加载
- [ ] 查询功能正常
- [ ] 分页功能正常
- [ ] 新建菜单功能
- [ ] 编辑菜单功能
- [ ] 删除菜单功能
- [ ] 批量删除功能
- [ ] 列设置功能
- [ ] 添加子菜单功能

### 2. 资源管理页面

访问：http://localhost:3000/system/resource

测试功能：

- [ ] 页面正常加载
- [ ] 查询功能正常
- [ ] 分页功能正常
- [ ] 新建资源功能
- [ ] 编辑资源功能
- [ ] 删除资源功能
- [ ] 批量删除功能
- [ ] 列设置功能
- [ ] 资源类型筛选功能

## 常见问题排查

### 1. 前端页面无法访问

- 检查前端服务是否正常启动
- 检查端口是否被占用
- 检查控制台是否有错误信息

### 2. API 接口返回 404

- 检查后端服务是否正常启动
- 检查接口路径是否正确
- 检查是否需要认证 token

### 3. 数据库连接问题

- 检查数据库服务是否正常
- 检查数据库配置是否正确
- 检查数据库表是否已创建

### 4. 权限问题

- 检查用户是否已登录
- 检查用户角色权限配置
- 检查 OAuth2 配置是否正确

## 测试数据

### 菜单测试数据

```sql
INSERT INTO menu (name, title, path, icon, show_icon, sort, component, hidden, keep_alive, permission)
VALUES
('system', '系统管理', '/system', 'SettingOutlined', true, 1, null, false, false, 'system'),
('user', '用户管理', '/system/user', 'UserOutlined', true, 1, '/views/user/User.vue', false, false, 'user:list'),
('role', '角色管理', '/system/role', 'TeamOutlined', true, 2, '/views/role/role.vue', false, false, 'role:list'),
('menu', '菜单管理', '/system/menu', 'MenuOutlined', true, 3, '/views/menu/Menu.vue', false, false, 'menu:list'),
('resource', '资源管理', '/system/resource', 'ApiOutlined', true, 4, '/views/resource/resource.vue', false, false, 'resource:list');
```

### 资源测试数据

```sql
INSERT INTO resource (name, title, path, uri, method, type, sort, permission)
VALUES
('system', '系统管理', '/system', null, null, 0, 1, 'system'),
('user', '用户管理', '/system/user', '/api/users', 'GET', 2, 1, 'user:list'),
('role', '角色管理', '/system/role', '/api/roles', 'GET', 2, 2, 'role:list'),
('menu', '菜单管理', '/system/menu', '/api/menus', 'GET', 2, 3, 'menu:list'),
('resource', '资源管理', '/system/resource', '/api/resources', 'GET', 2, 4, 'resource:list');
```
