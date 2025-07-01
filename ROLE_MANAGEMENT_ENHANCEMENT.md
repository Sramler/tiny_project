# 角色管理功能增强

## 概述

本次更新为角色管理功能增加了以下新字段的展示和管理：

- **code** - 角色标识（如：ROLE_ADMIN）
- **builtin** - 是否内置角色
- **enabled** - 是否启用
- **created_at** - 创建时间
- **updated_at** - 更新时间

## 后端更新

### 1. 数据库表结构更新

**文件：** `tiny-oauth-server/src/main/resources/schema.sql`

```sql
-- 角色表新增字段
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    `code` VARCHAR(50) NOT NULL COMMENT '角色标识',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `builtin` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否内置',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_role_code` (`code`),
    UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';
```

### 2. 实体类更新

**文件：** `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/model/Role.java`

- 已包含所有新字段的定义
- 包含 `@PrePersist` 和 `@PreUpdate` 注解自动管理时间字段

### 3. DTO 类更新

**文件：** `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/model/RoleResponseDto.java`

```java
public class RoleResponseDto {
    private Long id;
    private String name;
    private String code; // 角色标识
    private String description; // 描述
    private boolean builtin; // 是否内置
    private boolean enabled; // 是否启用
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    // 构造函数和getter/setter方法
}
```

**文件：** `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/model/RoleCreateUpdateDto.java`

```java
public class RoleCreateUpdateDto {
    private Long id;
    private String name;
    private String code; // 角色标识
    private String description; // 描述
    private boolean builtin; // 是否内置
    private boolean enabled; // 是否启用

    // getter/setter方法
}
```

### 4. 查询功能增强

**文件：** `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/model/RoleRequestDto.java`

- 新增 `code` 字段用于按角色标识搜索

**文件：** `tiny-oauth-server/src/main/java/com/tiny/oauthserver/sys/service/impl/RoleServiceImpl.java`

- 更新查询逻辑，支持按角色名和角色标识进行模糊搜索

### 5. 示例数据更新

**文件：** `tiny-oauth-server/src/main/resources/data.sql`

```sql
-- 插入角色数据
INSERT INTO `role` (`code`, `name`, `description`, `builtin`, `enabled`) VALUES
('ROLE_ADMIN', '系统管理员', '拥有系统所有权限的管理员角色', true, true),
('ROLE_USER', '普通用户', '普通用户角色，拥有基本权限', true, true);
```

## 前端更新

### 1. 角色列表页面

**文件：** `tiny-oauth-server/src/main/webapp/src/views/role/role.vue`

#### 新增表格列：

- 角色标识（code）
- 是否内置（builtin）- 使用标签显示
- 是否启用（enabled）- 使用标签显示
- 创建时间（createdAt）- 格式化显示
- 更新时间（updatedAt）- 格式化显示

#### 查询功能增强：

- 新增角色标识搜索框
- 支持按角色名和角色标识进行搜索

#### 自定义渲染：

```vue
<template v-if="column.dataIndex === 'builtin'">
  <a-tag :color="record.builtin ? 'blue' : 'default'">
    {{ record.builtin ? "是" : "否" }}
  </a-tag>
</template>
<template v-else-if="column.dataIndex === 'enabled'">
  <a-tag :color="record.enabled ? 'green' : 'red'">
    {{ record.enabled ? "启用" : "禁用" }}
  </a-tag>
</template>
<template v-else-if="column.dataIndex === 'createdAt'">
  {{ formatDateTime(record.createdAt) }}
</template>
```

### 2. 角色表单组件

**文件：** `tiny-oauth-server/src/main/webapp/src/views/role/RoleForm.vue`

#### 新增表单项：

- 角色标识输入框（必填，支持大写字母和下划线）
- 是否内置开关
- 是否启用开关

#### 表单验证规则：

```javascript
code: [
  { required: true, message: "角色标识不能为空", trigger: "blur" },
  { min: 2, max: 50, message: "长度2-50字符", trigger: "blur" },
  {
    pattern: /^[A-Z_]+$/,
    message: "角色标识只能包含大写字母和下划线",
    trigger: "blur",
  },
];
```

## 功能特性

### 1. 角色标识管理

- 角色标识唯一性约束
- 支持大写字母和下划线格式
- 前端实时验证

### 2. 内置角色标识

- 通过 `builtin` 字段标识系统内置角色
- 内置角色通常不可删除
- 前端使用蓝色标签显示

### 3. 角色状态管理

- 通过 `enabled` 字段控制角色是否可用
- 前端使用绿色/红色标签显示状态
- 禁用角色不影响已分配用户

### 4. 时间戳管理

- 自动记录创建和更新时间
- 前端格式化显示为中文日期时间格式
- 支持时区转换

### 5. 搜索功能

- 支持按角色名模糊搜索
- 支持按角色标识模糊搜索
- 支持组合搜索条件

## 使用说明

### 1. 创建角色

1. 点击"新建"按钮
2. 填写角色名和角色标识（必填）
3. 填写描述（可选）
4. 设置是否内置和是否启用
5. 点击"保存"

### 2. 编辑角色

1. 在角色列表中点击"编辑"按钮
2. 修改角色信息
3. 点击"保存"

### 3. 搜索角色

1. 在搜索框中输入角色名或角色标识
2. 点击"搜索"按钮
3. 点击"重置"按钮清空搜索条件

### 4. 批量操作

1. 选择要操作的角色（支持多选）
2. 点击"批量删除"按钮
3. 确认删除操作

## 注意事项

1. **角色标识唯一性**：每个角色标识必须唯一，建议使用 `ROLE_` 前缀
2. **内置角色保护**：内置角色通常不应被删除，建议在删除前检查 `builtin` 字段
3. **权限控制**：角色启用状态影响权限验证，禁用角色后用户将失去相应权限
4. **数据一致性**：删除角色前应确保没有用户正在使用该角色

## 测试验证

可以使用提供的测试脚本 `test-role-api.sh` 验证 API 功能：

```bash
./test-role-api.sh
```

测试内容包括：

- 获取角色列表
- 创建新角色
- 获取单个角色详情
- 更新角色信息
