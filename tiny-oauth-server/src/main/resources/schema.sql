-- 创建菜单表
CREATE TABLE IF NOT EXISTS `menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    `name` VARCHAR(100) NOT NULL COMMENT '菜单名称（后端内部识别名）',
    `title` VARCHAR(100) NOT NULL COMMENT '菜单标题（前端显示名）',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标名称',
    `show_icon` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否显示图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重，越小越靠前',
    `component` VARCHAR(200) DEFAULT NULL COMMENT 'Vue组件路径',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '重定向地址',
    `hidden` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否在侧边栏隐藏',
    `keep_alive` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要缓存页面',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父级菜单ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_menu_name` (`name`),
    UNIQUE KEY `uk_menu_path` (`path`),
    KEY `idx_menu_parent_id` (`parent_id`),
    KEY `idx_menu_sort` (`sort`),
    KEY `idx_menu_hidden` (`hidden`),
    FOREIGN KEY (`parent_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- 创建资源表（如果不存在）
CREATE TABLE IF NOT EXISTS `resource` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
    `name` VARCHAR(100) NOT NULL COMMENT '后端内部识别名（权限资源名）',
    `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径，如 /user/list',
    `uri` VARCHAR(200) DEFAULT NULL COMMENT '后端 API 路径，如 /api/user/add',
    `method` VARCHAR(10) DEFAULT 'GET' COMMENT 'HTTP方法',
    `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标，如 UserOutlined',
    `show_icon` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否显示图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '菜单排序权重，越小越靠前',
    `component` VARCHAR(200) DEFAULT NULL COMMENT '对应 Vue 路由的组件路径，如 "views/user/UserList.vue"',
    `redirect` VARCHAR(200) DEFAULT NULL COMMENT '需要重定向的路由地址（用于父菜单）',
    `hidden` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否在侧边栏隐藏（如详情页不显示）',
    `keep_alive` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否需要缓存页面',
    `title` VARCHAR(100) DEFAULT NULL COMMENT '前端展示名（菜单显示标题）',
    `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识，用于前端 v-permission 控制',
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '资源类型：0-菜单，1-按钮，2-API',
    `parent_id` BIGINT DEFAULT NULL COMMENT '菜单结构层级',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_resource_name` (`name`),
    UNIQUE KEY `uk_resource_path` (`path`),
    UNIQUE KEY `uk_resource_uri` (`uri`),
    KEY `idx_resource_parent_id` (`parent_id`),
    KEY `idx_resource_sort` (`sort`),
    KEY `idx_resource_type` (`type`),
    KEY `idx_resource_hidden` (`hidden`),
    FOREIGN KEY (`parent_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资源表';

-- 创建角色-资源关联表（如果不存在）
CREATE TABLE IF NOT EXISTS `role_resource` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_resource` (`role_id`, `resource_id`),
    KEY `idx_role_resource_role_id` (`role_id`),
    KEY `idx_role_resource_resource_id` (`resource_id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-资源关联表';

-- 创建角色-菜单关联表
CREATE TABLE IF NOT EXISTS `role_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_menu_role_id` (`role_id`),
    KEY `idx_role_menu_menu_id` (`menu_id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`menu_id`) REFERENCES `menu` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-菜单关联表'; 