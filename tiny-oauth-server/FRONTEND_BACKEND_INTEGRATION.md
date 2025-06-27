# 前后端接口对接文档

## 概述

本文档描述了菜单管理和资源管理模块的前后端接口对接情况，包括 API 接口定义、前端页面实现和数据库设计。

## 后端接口

### 菜单管理接口

#### 基础 CRUD 接口

- `GET /sys/menus` - 获取菜单列表（分页）
- `POST /sys/menus` - 创建菜单
- `PUT /sys/menus/{id}` - 更新菜单
- `DELETE /sys/menus/{id}` - 删除菜单
- `GET /sys/menus/{id}` - 获取菜单详情

#### 特殊接口

- `GET /sys/menus/tree` - 获取菜单树
- `GET /sys/menus/user/{userId}/tree` - 获取用户菜单树
- `POST /sys/menus/batch/delete` - 批量删除菜单
- `PUT /sys/menus/{id}/sort` - 更新菜单排序
- `PUT /sys/menus/batch/sort` - 批量更新菜单排序
- `GET /sys/menus/parent/{parentId}` - 根据父级 ID 获取子菜单
- `GET /sys/menus/top-level` - 获取顶级菜单
- `GET /sys/menus/permission/{permission}` - 根据权限标识获取菜单
- `GET /sys/menus/hidden/{hidden}` - 根据是否隐藏获取菜单
- `GET /sys/menus/check-name` - 检查菜单名称是否存在
- `GET /sys/menus/check-path` - 检查菜单路径是否存在
- `GET /sys/menus/options` - 获取菜单选项列表

### 资源管理接口

#### 基础 CRUD 接口

- `GET /sys/resources` - 获取资源列表（分页）
- `POST /sys/resources` - 创建资源
- `PUT /sys/resources/{id}` - 更新资源
- `DELETE /sys/resources/{id}` - 删除资源
- `GET /sys/resources/{id}` - 获取资源详情

#### 特殊接口

- `GET /sys/resources/tree` - 获取资源树
- `GET /sys/resources/type/{type}` - 根据资源类型获取资源列表
- `POST /sys/resources/batch/delete` - 批量删除资源
- `PUT /sys/resources/{id}/sort` - 更新资源排序
- `GET /sys/resources/parent/{parentId}` - 根据父级 ID 获取子资源列表
- `GET /sys/resources/top-level` - 获取顶级资源列表
- `GET /sys/resources/permission/{permission}` - 根据权限标识获取资源列表
- `GET /sys/resources/types` - 获取资源类型列表
- `GET /sys/resources/check-name` - 检查资源名称是否存在
- `GET /sys/resources/check-path` - 检查资源路径是否存在
- `GET /sys/resources/check-uri` - 检查资源 URI 是否存在

## 前端实现

### API 接口定义

#### 菜单 API (`src/api/menu.ts`)

```typescript
// 主要接口
export function menuList(params?: MenuQuery): Promise<PageResponse<MenuItem>>;
export function getMenuTree(): Promise<MenuItem[]>;
export function createMenu(data: MenuCreateUpdateDto): Promise<MenuItem>;
export function updateMenu(
  id: number | string,
  data: MenuCreateUpdateDto
): Promise<MenuItem>;
export function deleteMenu(id: number | string): Promise<void>;
export function batchDeleteMenus(
  ids: (number | string)[]
): Promise<{ success: boolean; message: string }>;
```

#### 资源 API (`src/api/resource.ts`)

```typescript
// 主要接口
export function resourceList(
  params?: ResourceQuery
): Promise<PageResponse<ResourceItem>>;
export function getResourceTree(): Promise<ResourceItem[]>;
export function createResource(
  data: ResourceCreateUpdateDto
): Promise<ResourceItem>;
export function updateResource(
  id: number | string,
  data: ResourceCreateUpdateDto
): Promise<ResourceItem>;
export function deleteResource(id: number | string): Promise<void>;
export function batchDeleteResources(
  ids: (number | string)[]
): Promise<{ success: boolean; message: string }>;
```

### 页面组件

#### 菜单管理页面 (`src/views/menu/Menu.vue`)

- 支持分页查询
- 支持批量操作
- 支持动态列设置
- 支持树形结构展示
- 支持添加子菜单

#### 资源管理页面 (`src/views/resource/resource.vue`)

- 支持分页查询
- 支持批量操作
- 支持动态列设置
- 支持资源类型筛选
- 支持树形结构展示

#### 表单组件

- `MenuForm.vue` - 菜单创建/编辑表单
- `ResourceForm.vue` - 资源创建/编辑表单

## 数据库设计

### 菜单表 (menu)

```sql
CREATE TABLE menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    title VARCHAR(100) NOT NULL COMMENT '菜单标题',
    path VARCHAR(200) COMMENT '前端路径',
    icon VARCHAR(200) COMMENT '图标',
    show_icon BOOLEAN DEFAULT TRUE COMMENT '是否显示图标',
    sort INT DEFAULT 0 COMMENT '排序权重',
    component VARCHAR(200) COMMENT '组件路径',
    redirect VARCHAR(200) COMMENT '重定向地址',
    hidden BOOLEAN DEFAULT FALSE COMMENT '是否隐藏',
    keep_alive BOOLEAN DEFAULT FALSE COMMENT '是否缓存',
    permission VARCHAR(100) COMMENT '权限标识',
    parent_id BIGINT COMMENT '父级菜单ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort (sort),
    INDEX idx_permission (permission)
);
```

### 资源表 (resource)

```sql
CREATE TABLE resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '资源名称',
    title VARCHAR(100) NOT NULL COMMENT '资源标题',
    path VARCHAR(200) COMMENT '前端路径',
    uri VARCHAR(200) COMMENT 'API路径',
    method VARCHAR(10) COMMENT 'HTTP方法',
    icon VARCHAR(200) COMMENT '图标',
    show_icon BOOLEAN DEFAULT TRUE COMMENT '是否显示图标',
    sort INT DEFAULT 0 COMMENT '排序权重',
    component VARCHAR(200) COMMENT '组件路径',
    redirect VARCHAR(200) COMMENT '重定向地址',
    hidden BOOLEAN DEFAULT FALSE COMMENT '是否隐藏',
    keep_alive BOOLEAN DEFAULT FALSE COMMENT '是否缓存',
    permission VARCHAR(100) COMMENT '权限标识',
    type INT NOT NULL DEFAULT 2 COMMENT '资源类型：0-菜单，1-按钮，2-API',
    parent_id BIGINT COMMENT '父级资源ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type),
    INDEX idx_sort (sort),
    INDEX idx_permission (permission)
);
```

## 功能特性

### 菜单管理

1. **树形结构** - 支持多级菜单管理
2. **图标选择** - 内置图标选择器
3. **权限控制** - 支持权限标识配置
4. **显示控制** - 支持隐藏和缓存配置
5. **排序管理** - 支持拖拽排序

### 资源管理

1. **类型分类** - 支持菜单、按钮、API 三种类型
2. **API 配置** - 支持 HTTP 方法和 URI 配置
3. **权限标识** - 支持细粒度权限控制
4. **树形结构** - 支持多级资源管理
5. **批量操作** - 支持批量删除和排序

## 使用说明

### 启动服务

1. 启动后端服务：`mvn spring-boot:run`
2. 启动前端服务：`npm run dev`

### 访问页面

- 菜单管理：`http://localhost:3000/system/menu`
- 资源管理：`http://localhost:3000/system/resource`

### 测试数据

系统已预置测试数据，包括：

- 系统管理菜单
- 用户管理、角色管理、菜单管理、资源管理子菜单
- 对应的资源配置

## 注意事项

1. **权限验证** - 确保用户具有相应的操作权限
2. **数据完整性** - 删除父级菜单/资源时注意子级数据
3. **路径唯一性** - 菜单路径和资源 URI 需要保持唯一
4. **图标配置** - 图标名称需要与 Ant Design Vue 图标组件对应
5. **排序权重** - 数字越小排序越靠前

## 扩展功能

### 待实现功能

1. **菜单拖拽排序** - 支持可视化拖拽排序
2. **权限预览** - 实时预览权限配置效果
3. **导入导出** - 支持菜单和资源配置的导入导出
4. **版本管理** - 支持配置版本回滚
5. **审计日志** - 记录操作日志

### 性能优化

1. **缓存机制** - 菜单树和资源树缓存
2. **懒加载** - 大数据的懒加载处理
3. **虚拟滚动** - 大量数据的虚拟滚动展示
4. **批量操作** - 优化批量操作的性能

## 故障排除

### 常见问题

1. **接口 404** - 检查后端服务是否正常启动
2. **权限不足** - 检查用户角色和权限配置
3. **数据不显示** - 检查数据库连接和数据完整性
4. **图标不显示** - 检查图标名称是否正确

### 调试方法

1. **浏览器控制台** - 查看前端错误信息
2. **后端日志** - 查看 Spring Boot 应用日志
3. **数据库查询** - 直接查询数据库验证数据
4. **网络请求** - 使用浏览器开发者工具查看 API 请求
