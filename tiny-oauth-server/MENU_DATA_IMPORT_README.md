# 菜单数据导入功能说明

## 功能概述

菜单数据导入功能允许您将 `menu.json` 文件中的菜单结构数据导入到数据库中，实现菜单的批量创建和管理。

## 文件结构

```
tiny-oauth-server/
├── src/main/resources/
│   ├── menu.json                    # 菜单数据源文件
│   ├── menu_data_insert.sql         # 手动SQL插入脚本
│   └── migration_path_to_url.sql    # 数据库字段迁移脚本
├── src/main/java/com/tiny/oauthserver/sys/
│   ├── util/
│   │   └── MenuDataImporter.java    # 菜单数据导入工具类
│   └── controller/
│       └── MenuDataController.java  # 菜单数据导入控制器
└── src/main/webapp/src/views/menu/
    └── MenuDataImport.vue           # 前端导入页面
```

## 使用方法

### 方法一：通过前端页面导入（推荐）

1. 启动后端服务：

   ```bash
   cd tiny-oauth-server
   mvn spring-boot:run
   ```

2. 启动前端开发服务器：

   ```bash
   cd src/main/webapp
   npm run dev
   ```

3. 访问菜单数据导入页面：

   ```
   http://localhost:5174/menu-data-import
   ```

4. 点击"开始导入菜单数据"按钮

### 方法二：通过 API 接口导入

1. 导入默认菜单数据：

   ```bash
   curl -X POST http://localhost:8080/sys/menu-data/import
   ```

2. 检查导入服务状态：
   ```bash
   curl -X GET http://localhost:8080/sys/menu-data/status
   ```

### 方法三：手动执行 SQL 脚本

1. 执行数据库迁移脚本（如果需要）：

   ```sql
   -- 将 path 字段重命名为 url
   ALTER TABLE resource CHANGE COLUMN path url VARCHAR(200) NOT NULL DEFAULT '' COMMENT '前端路由路径';
   ```

2. 执行菜单数据插入脚本：
   ```sql
   -- 执行 src/main/resources/menu_data_insert.sql
   ```

## 菜单数据结构

### menu.json 文件格式

```json
[
  {
    "title": "工作台",
    "url": "/",
    "icon": "HomeOutlined",
    "showIcon": true,
    "component": "/views/Dashboard.vue"
  },
  {
    "title": "系统管理",
    "url": "/system",
    "icon": "SettingOutlined",
    "showIcon": true,
    "redirect": "/system/role",
    "children": [
      {
        "title": "角色管理",
        "url": "/system/role",
        "showIcon": false,
        "component": "/views/role/role.vue"
      }
    ]
  }
]
```

### 字段说明

| 字段名    | 类型    | 必填 | 说明         |
| --------- | ------- | ---- | ------------ |
| title     | String  | 是   | 菜单显示标题 |
| url       | String  | 是   | 前端路由路径 |
| icon      | String  | 否   | 菜单图标名称 |
| showIcon  | Boolean | 否   | 是否显示图标 |
| component | String  | 否   | Vue 组件路径 |
| redirect  | String  | 否   | 重定向地址   |
| children  | Array   | 否   | 子菜单列表   |

## 数据库映射

### 资源类型

- `type = 0`: 目录（有子菜单的菜单项）
- `type = 1`: 菜单（没有子菜单的菜单项）
- `type = 2`: 按钮
- `type = 3`: 接口

### 字段映射

| JSON 字段 | 数据库字段 | 说明                          |
| --------- | ---------- | ----------------------------- |
| title     | title      | 菜单标题                      |
| url       | url        | 前端路由路径                  |
| icon      | icon       | 图标名称                      |
| showIcon  | show_icon  | 是否显示图标                  |
| component | component  | 组件路径                      |
| redirect  | redirect   | 重定向地址                    |
| -         | uri        | 后端 API 路径（默认空字符串） |
| -         | method     | HTTP 方法（默认空字符串）     |
| -         | permission | 权限标识（自动生成）          |
| -         | sort       | 排序权重（自动生成）          |

## 注意事项

### 1. 数据重复处理

- 导入前建议清空现有菜单数据
- 如果菜单已存在，可能会产生重复数据
- 建议在开发环境或测试环境中使用

### 2. 权限标识生成

系统会根据菜单标题自动生成权限标识：

- 工作台 → `工作台:view`
- 角色管理 → `角色管理:view`

### 3. 资源名称生成

系统会根据菜单标题自动生成资源名称：

- 工作台 → `工作台`
- 角色管理 → `sub_角色管理`

### 4. 数据库要求

- 确保 `resource` 表已正确创建
- 确保字段类型和约束正确
- 建议先执行数据库迁移脚本

## 错误处理

### 常见错误

1. **文件不存在错误**

   ```
   错误：menu.json 文件不存在
   解决：确保 src/main/resources/menu.json 文件存在
   ```

2. **JSON 格式错误**

   ```
   错误：JSON 格式不正确
   解决：检查 menu.json 文件的 JSON 格式
   ```

3. **数据库连接错误**

   ```
   错误：数据库连接失败
   解决：检查数据库配置和连接
   ```

4. **字段约束错误**
   ```
   错误：字段不能为空
   解决：检查数据库表结构和约束
   ```

## 扩展功能

### 自定义菜单数据

1. 修改 `menu.json` 文件
2. 重新导入数据
3. 或使用自定义 JSON 文件导入

### 批量导入

可以通过修改 `MenuDataImporter` 类来支持：

- 多个 JSON 文件导入
- 增量导入
- 数据验证和清理

## 相关 API

### 菜单数据导入 API

- `POST /sys/menu-data/import` - 导入默认菜单数据
- `POST /sys/menu-data/import/{jsonFilePath}` - 导入指定文件
- `GET /sys/menu-data/status` - 检查服务状态

### 菜单管理 API

- `GET /sys/resources/menus` - 获取菜单列表
- `GET /sys/resources/menus/tree` - 获取菜单树
- `POST /sys/resources/menus` - 创建菜单
- `PUT /sys/resources/menus/{id}` - 更新菜单
- `DELETE /sys/resources/menus/{id}` - 删除菜单

## 更新日志

### v1.0.0 (2024-06-27)

- 初始版本发布
- 支持从 JSON 文件导入菜单数据
- 提供前端导入页面和 API 接口
- 支持菜单树形结构导入
- 自动生成权限标识和资源名称
