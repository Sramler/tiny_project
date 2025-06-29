# 菜单和资源管理重构总结

## 重构背景

根据用户反馈，菜单应该来源于资源表，而不是独立的菜单表。本次重构将菜单管理功能整合到资源管理系统中，实现统一的权限资源管理。

## 重构内容

### 1. 数据库设计重构

#### 原设计问题

- 独立的菜单表和资源表，数据冗余
- 菜单和资源分离，管理复杂
- 权限控制分散

#### 新设计方案

- **统一资源表**：所有权限资源（菜单、按钮、API）统一存储在 `resource` 表中
- **类型区分**：通过 `type` 字段区分资源类型
  - `0` - 目录
  - `1` - 菜单
  - `2` - 按钮
  - `3` - 接口
- **树形结构**：支持父子关系，实现菜单层级

#### 数据库表结构

```sql
CREATE TABLE `resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL DEFAULT '' COMMENT '权限资源名（后端内部识别名）',
  `path` varchar(200) NOT NULL DEFAULT '' COMMENT '前端路由路径',
  `uri` varchar(200) NOT NULL DEFAULT '' COMMENT '后端 API 路径',
  `method` varchar(10) NOT NULL DEFAULT '' COMMENT 'HTTP 方法',
  `icon` varchar(200) NOT NULL DEFAULT '' COMMENT '菜单图标',
  `show_icon` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否显示图标',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重，越小越靠前',
  `component` varchar(200) NOT NULL DEFAULT '' COMMENT 'Vue 路由组件路径',
  `redirect` varchar(200) NOT NULL DEFAULT '' COMMENT '重定向地址（父菜单使用）',
  `hidden` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否在侧边栏隐藏',
  `keep_alive` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否缓存页面',
  `title` varchar(100) NOT NULL DEFAULT '' COMMENT '前端菜单显示标题',
  `permission` varchar(100) NOT NULL DEFAULT '' COMMENT '权限标识，用于前端控制',
  `type` tinyint NOT NULL DEFAULT '0' COMMENT '资源类型：0-目录，1-菜单，2-按钮，3-接口',
  `parent_id` bigint DEFAULT NULL COMMENT '父资源ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限资源表';
```

### 2. 后端代码重构

#### 删除的文件

- `Menu.java` - 菜单实体类
- `MenuCreateUpdateDto.java` - 菜单创建更新 DTO
- `MenuRequestDto.java` - 菜单请求 DTO
- `MenuResponseDto.java` - 菜单响应 DTO
- `MenuSortDto.java` - 菜单排序 DTO
- `MenuRepository.java` - 菜单仓库
- `MenuService.java` - 菜单服务接口
- `MenuServiceImpl.java` - 菜单服务实现
- `MenuController.java` - 菜单控制器

#### 更新的文件

##### Resource.java - 资源实体类

- 更新字段定义，匹配新的数据库结构
- 所有字段设为非空，使用正确的默认值
- 添加时间戳字段 `createdAt`、`updatedAt`
- 添加生命周期回调方法

##### ResourceType.java - 资源类型枚举

- 重新定义资源类型：
  - `DIRECTORY(0, "目录")`
  - `MENU(1, "菜单")`
  - `BUTTON(2, "按钮")`
  - `API(3, "接口")`
- 添加描述字段和 toString 方法

##### ResourceController.java - 资源控制器

- 添加菜单管理 API 端点：
  - `GET /sys/resources/menus` - 获取菜单列表
  - `GET /sys/resources/menus/tree` - 获取菜单树
  - `POST /sys/resources/menus` - 创建菜单
  - `PUT /sys/resources/menus/{id}` - 更新菜单
  - `DELETE /sys/resources/menus/{id}` - 删除菜单
  - `POST /sys/resources/menus/batch/delete` - 批量删除菜单
  - `PUT /sys/resources/menus/{id}/sort` - 更新菜单排序

##### ResourceService.java - 资源服务接口

- 添加 `findByTypeIn(List<ResourceType> types)` 方法

##### ResourceServiceImpl.java - 资源服务实现

- 实现 `findByTypeIn` 方法
- 修复方法调用，使用正确的 getter 方法

##### ResourceRepository.java - 资源仓库

- 添加 `findByTypeInOrderBySortAsc(List<ResourceType> types)` 方法

### 3. 前端代码重构

#### 更新的文件

##### menu.ts - 菜单 API

- 更新所有 API 端点，指向资源 API 的菜单相关端点
- 修复 request 调用方式，使用 `request.get`、`request.post` 等方法
- 添加菜单类型选项函数

##### 数据库脚本

- 更新 `schema.sql`：移除独立的菜单表，统一使用资源表
- 更新 `data.sql`：添加基于资源表的测试数据

## 重构后的优势

### 1. 数据统一

- 所有权限资源统一管理
- 减少数据冗余
- 简化数据关系

### 2. 功能完整

- 菜单管理功能完整保留
- 支持树形结构
- 支持批量操作
- 支持排序功能

### 3. 扩展性强

- 支持多种资源类型
- 便于添加新的资源类型
- 统一的权限控制

### 4. 维护简单

- 减少代码重复
- 统一的 API 接口
- 简化的数据库结构

## API 接口设计

### 菜单管理 API

```
GET    /sys/resources/menus              # 获取菜单列表（分页）
GET    /sys/resources/menus/tree         # 获取菜单树
POST   /sys/resources/menus              # 创建菜单
PUT    /sys/resources/menus/{id}         # 更新菜单
DELETE /sys/resources/menus/{id}         # 删除菜单
POST   /sys/resources/menus/batch/delete # 批量删除菜单
PUT    /sys/resources/menus/{id}/sort    # 更新菜单排序
```

### 资源管理 API

```
GET    /sys/resources                    # 获取资源列表（分页）
GET    /sys/resources/tree               # 获取资源树
POST   /sys/resources                    # 创建资源
PUT    /sys/resources/{id}               # 更新资源
DELETE /sys/resources/{id}               # 删除资源
POST   /sys/resources/batch/delete       # 批量删除资源
PUT    /sys/resources/{id}/sort          # 更新资源排序
```

## 测试数据

### 菜单结构

```
系统管理 (目录)
├── 用户管理 (菜单)
├── 角色管理 (菜单)
├── 菜单管理 (菜单)
└── 资源管理 (菜单)
```

### 权限分配

- **ADMIN 角色**：拥有所有菜单权限
- **USER 角色**：只有用户管理权限

## 部署说明

### 1. 数据库更新

```sql
-- 执行新的数据库脚本
source schema.sql;
source data.sql;
```

### 2. 后端部署

```bash
# 重新编译
mvn clean compile

# 启动服务
mvn spring-boot:run
```

### 3. 前端测试

- 访问菜单管理页面：http://localhost:3000/system/menu
- 访问资源管理页面：http://localhost:3000/system/resource

## 注意事项

### 1. 数据迁移

- 如果已有菜单数据，需要迁移到资源表
- 确保菜单类型正确设置（0-目录，1-菜单）

### 2. 权限配置

- 更新角色权限配置
- 确保菜单权限正确分配

### 3. 前端适配

- 菜单组件需要适配新的数据结构
- 确保图标和路径正确显示

## 后续优化

### 1. 功能增强

- 菜单拖拽排序
- 权限预览功能
- 数据导入导出

### 2. 性能优化

- 数据库查询优化
- 缓存策略优化

### 3. 用户体验

- 操作确认提示
- 加载状态优化
- 错误处理完善

## 总结

本次重构成功将菜单管理整合到资源管理系统中，实现了：

1. **数据统一**：所有权限资源统一管理
2. **功能完整**：保留所有原有功能
3. **扩展性强**：支持多种资源类型
4. **维护简单**：减少代码重复

重构后的系统更加简洁、高效，为后续功能扩展奠定了良好基础。
