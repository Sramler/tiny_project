# 菜单和资源管理功能说明

## 概述

本项目新增了完整的菜单管理和资源管理功能，包括后端 CRUD 操作、数据库表结构、API 接口等。

## 功能特性

### 菜单管理

- ✅ 菜单的增删改查
- ✅ 树形菜单结构
- ✅ 菜单排序和拖拽
- ✅ 菜单权限控制
- ✅ 菜单显示/隐藏控制
- ✅ 菜单缓存控制
- ✅ 批量操作

### 资源管理

- ✅ 资源的增删改查
- ✅ 多种资源类型（菜单、按钮、API）
- ✅ 树形资源结构
- ✅ 资源排序
- ✅ 权限标识管理
- ✅ 批量操作

## 数据库表结构

### 1. menu 表（菜单表）

```sql
CREATE TABLE `menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称（后端内部识别名）',
    `title` VARCHAR(100) NOT NULL COMMENT '菜单标题（前端显示名）',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标名称',
    `show_icon` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否显示图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `component` VARCHAR(200) DEFAULT NULL COMMENT 'Vue组件路径',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '重定向地址',
    `hidden` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否在侧边栏隐藏',
    `keep_alive` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要缓存页面',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级菜单ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2. resource 表（资源表）

```sql
CREATE TABLE `resource` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '后端内部识别名',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '前端展示名',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
    `uri` VARCHAR(200) DEFAULT NULL COMMENT '后端API路径',
    `method` VARCHAR(10) DEFAULT 'GET' COMMENT 'HTTP方法',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标',
    `show_icon` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否显示图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `component` VARCHAR(200) DEFAULT NULL COMMENT 'Vue组件路径',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '重定向地址',
    `hidden` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否隐藏',
    `keep_alive` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要缓存',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '资源类型：0-菜单，1-按钮，2-API',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级资源ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3. role_menu 表（角色-菜单关联表）

```sql
CREATE TABLE `role_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4. role_resource 表（角色-资源关联表）

```sql
CREATE TABLE `role_resource` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## API 接口

### 菜单管理 API

#### 1. 分页查询菜单

```
GET /sys/menus?page=0&size=10&name=xxx&title=xxx&path=xxx&parentId=xxx&hidden=xxx
```

#### 2. 获取菜单详情

```
GET /sys/menus/{id}
```

#### 3. 创建菜单

```
POST /sys/menus
Content-Type: application/json

{
    "name": "menu-name",
    "title": "菜单标题",
    "path": "/menu/path",
    "icon": "MenuOutlined",
    "showIcon": true,
    "sort": 1,
    "component": "views/menu/Menu.vue",
    "redirect": null,
    "hidden": false,
    "keepAlive": true,
    "permission": "menu:view",
    "parentId": null
}
```

#### 4. 更新菜单

```
PUT /sys/menus/{id}
Content-Type: application/json

{
    "name": "menu-name",
    "title": "菜单标题",
    "path": "/menu/path",
    "icon": "MenuOutlined",
    "showIcon": true,
    "sort": 1,
    "component": "views/menu/Menu.vue",
    "redirect": null,
    "hidden": false,
    "keepAlive": true,
    "permission": "menu:view",
    "parentId": null
}
```

#### 5. 删除菜单

```
DELETE /sys/menus/{id}
```

#### 6. 批量删除菜单

```
POST /sys/menus/batch/delete
Content-Type: application/json

[1, 2, 3]
```

#### 7. 获取菜单树

```
GET /sys/menus/tree
```

#### 8. 更新菜单排序

```
PUT /sys/menus/{id}/sort?sort=1
```

#### 9. 批量更新菜单排序

```
PUT /sys/menus/batch/sort
Content-Type: application/json

[
    {"id": 1, "sort": 1, "parentId": null},
    {"id": 2, "sort": 2, "parentId": null}
]
```

### 资源管理 API

#### 1. 分页查询资源

```
GET /sys/resources?page=0&size=10&name=xxx&title=xxx&path=xxx&uri=xxx&type=xxx&parentId=xxx&hidden=xxx
```

#### 2. 获取资源详情

```
GET /sys/resources/{id}
```

#### 3. 创建资源

```
POST /sys/resources
Content-Type: application/json

{
    "name": "resource-name",
    "title": "资源标题",
    "path": "/resource/path",
    "uri": "/api/resource",
    "method": "GET",
    "icon": "ApiOutlined",
    "showIcon": true,
    "sort": 1,
    "component": "views/resource/Resource.vue",
    "redirect": null,
    "hidden": false,
    "keepAlive": true,
    "permission": "resource:view",
    "type": 0,
    "parentId": null
}
```

#### 4. 更新资源

```
PUT /sys/resources/{id}
Content-Type: application/json

{
    "name": "resource-name",
    "title": "资源标题",
    "path": "/resource/path",
    "uri": "/api/resource",
    "method": "GET",
    "icon": "ApiOutlined",
    "showIcon": true,
    "sort": 1,
    "component": "views/resource/Resource.vue",
    "redirect": null,
    "hidden": false,
    "keepAlive": true,
    "permission": "resource:view",
    "type": 0,
    "parentId": null
}
```

#### 5. 删除资源

```
DELETE /sys/resources/{id}
```

#### 6. 批量删除资源

```
POST /sys/resources/batch/delete
Content-Type: application/json

[1, 2, 3]
```

#### 7. 获取资源树

```
GET /sys/resources/tree
```

#### 8. 更新资源排序

```
PUT /sys/resources/{id}/sort?sort=1
```

#### 9. 获取资源类型列表

```
GET /sys/resources/types
```

## 部署步骤

### 1. 执行数据库脚本

```bash
# 执行表结构脚本
mysql -u username -p database_name < schema.sql

# 执行初始数据脚本
mysql -u username -p database_name < menu_resource_data.sql
```

### 2. 启动后端服务

```bash
cd tiny-oauth-server
mvn spring-boot:run
```

### 3. 测试 API 接口

```bash
# 测试菜单API
curl -X GET "http://localhost:8080/sys/menus" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试资源API
curl -X GET "http://localhost:8080/sys/resources" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 权限控制

### 菜单权限

- 通过`role_menu`表控制用户对菜单的访问权限
- 前端根据用户角色动态生成菜单树
- 支持菜单的显示/隐藏控制

### 资源权限

- 通过`role_resource`表控制用户对资源的访问权限
- 支持细粒度的权限控制（菜单、按钮、API）
- 前端可通过权限标识控制按钮显示

### 权限验证

```java
// 在Controller方法上添加权限注解
@PreAuthorize("hasAuthority('menu:view')")
@GetMapping("/menus")
public ResponseEntity<PageResponse<MenuResponseDto>> getMenus(...) {
    // 方法实现
}
```

## 前端集成

### 1. 菜单组件

```vue
<template>
  <a-menu>
    <template v-for="menu in menuTree" :key="menu.id">
      <a-sub-menu v-if="menu.children && menu.children.length > 0">
        <template #title>
          <span>{{ menu.title }}</span>
        </template>
        <a-menu-item v-for="child in menu.children" :key="child.id">
          {{ child.title }}
        </a-menu-item>
      </a-sub-menu>
      <a-menu-item v-else :key="menu.id">
        {{ menu.title }}
      </a-menu-item>
    </template>
  </a-menu>
</template>
```

### 2. 权限指令

```vue
<template>
  <div>
    <a-button v-permission="'user:add'">新增用户</a-button>
    <a-button v-permission="'user:edit'">编辑用户</a-button>
    <a-button v-permission="'user:delete'">删除用户</a-button>
  </div>
</template>
```

## 注意事项

1. **数据完整性**：删除菜单或资源时，会检查是否有子项，有子项时不允许删除
2. **唯一性约束**：菜单名称、路径，资源名称、路径、URI 都要求唯一
3. **权限继承**：子菜单/资源会继承父级的权限控制
4. **缓存控制**：支持页面缓存控制，提高用户体验
5. **排序权重**：通过 sort 字段控制显示顺序，数值越小越靠前

## 扩展功能

### 1. 动态权限验证

```java
@Component
public class DynamicPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // 实现动态权限验证逻辑
        return true;
    }
}
```

### 2. 权限缓存

```java
@Cacheable(value = "userPermissions", key = "#userId")
public List<String> getUserPermissions(Long userId) {
    // 获取用户权限列表
    return permissionService.getUserPermissions(userId);
}
```

### 3. 权限同步

```java
@EventListener
public void handleRoleChangeEvent(RoleChangeEvent event) {
    // 角色变更时，清除相关用户的权限缓存
    cacheManager.getCache("userPermissions").evict(event.getUserId());
}
```

## 故障排除

### 1. 菜单不显示

- 检查用户是否有对应菜单的权限
- 检查菜单的 hidden 字段是否为 true
- 检查菜单的 parentId 是否正确

### 2. 权限验证失败

- 检查用户角色是否正确分配
- 检查角色-资源关联是否正确
- 检查权限标识是否正确

### 3. 数据库连接问题

- 检查数据库连接配置
- 检查表结构是否正确创建
- 检查外键约束是否正确

## 联系支持

如有问题，请联系开发团队或查看项目文档。
