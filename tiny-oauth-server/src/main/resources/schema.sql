-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `account_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未过期',
    `account_non_locked` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未锁定',
    `credentials_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '密码是否未过期',
    `last_login_at` TIMESTAMP NULL COMMENT '最后登录时间',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建角色表
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

-- 创建用户-角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_role_user_id` (`user_id`),
    KEY `idx_user_role_role_id` (`role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- 创建资源表（统一的权限资源表，包含菜单和API）
CREATE TABLE IF NOT EXISTS `resource` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    `name` VARCHAR(100) NOT NULL COMMENT '权限资源名（后端内部识别名）',
    `url` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '前端路由路径',
    `uri` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '后端 API 路径',
    `method` VARCHAR(10) NOT NULL DEFAULT '' COMMENT 'HTTP 方法',
    `icon` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '菜单图标',
    `show_icon` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否显示图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重，越小越靠前',
    `component` VARCHAR(200) NOT NULL DEFAULT '' COMMENT 'Vue 路由组件路径',
    `redirect` VARCHAR(200) NOT NULL DEFAULT '' COMMENT '重定向地址（父菜单使用）',
    `hidden` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否在侧边栏隐藏',
    `keep_alive` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否缓存页面',
    `title` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '前端菜单显示标题',
    `permission` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '权限标识，用于前端控制',
    `type` TINYINT NOT NULL DEFAULT 0 COMMENT '资源类型：0-目录，1-菜单，2-按钮，3-接口',
    `parent_id` BIGINT DEFAULT NULL COMMENT '父资源ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY `idx_resource_parent_id` (`parent_id`),
    KEY `idx_resource_type` (`type`),
    KEY `idx_resource_sort` (`sort`),
    KEY `idx_resource_hidden` (`hidden`),
    FOREIGN KEY (`parent_id`) REFERENCES `resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限资源表';

-- 创建角色-资源关联表
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

-- 创建用户认证方法表
CREATE TABLE IF NOT EXISTS `user_authentication_method` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `authentication_provider` VARCHAR(50) NOT NULL COMMENT '认证提供者',
    `authentication_type` VARCHAR(50) NOT NULL COMMENT '认证类型',
    `authentication_configuration` JSON NOT NULL COMMENT '认证配置',
    `is_primary_method` BOOLEAN DEFAULT FALSE COMMENT '是否主要认证方法',
    `is_method_enabled` BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    `authentication_priority` INT DEFAULT 0 COMMENT '认证优先级',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `expires_at` TIMESTAMP NULL COMMENT '过期时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_auth_method` (`user_id`, `authentication_provider`, `authentication_type`),
    KEY `idx_user_provider` (`user_id`, `authentication_provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证方法表'; 