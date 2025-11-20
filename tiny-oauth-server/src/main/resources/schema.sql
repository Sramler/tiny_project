-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `account_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未过期',
    `account_non_locked` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未锁定',
    `credentials_non_expired` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '密码是否未过期',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `last_login_at` TIMESTAMP NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP地址',
    `last_login_device` VARCHAR(200) DEFAULT NULL COMMENT '最后登录设备',
    `failed_login_count` INT NOT NULL DEFAULT 0 COMMENT '失败登录次数',
    `last_failed_login_at` TIMESTAMP NULL COMMENT '最后失败登录时间',
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
    `last_verified_at` TIMESTAMP NULL COMMENT '最后验证时间',
    `last_verified_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后验证IP地址',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `expires_at` TIMESTAMP NULL COMMENT '过期时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_auth_method` (`user_id`, `authentication_provider`, `authentication_type`),
    KEY `idx_user_provider` (`user_id`, `authentication_provider`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证方法表';

-- 创建用户认证审计表
CREATE TABLE IF NOT EXISTS `user_authentication_audit` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `user_id` BIGINT NULL COMMENT '对应 user.id，可为空（匿名或外部登录）',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名冗余字段，便于查询',
    `event_type` VARCHAR(50) NOT NULL COMMENT '事件类型，如 LOGIN / LOGOUT / MFA_BIND / TOKEN_ISSUE',
    `success` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '事件是否成功',
    `authentication_provider` VARCHAR(50) NULL COMMENT '认证提供者，如 LOCAL / GITHUB / GOOGLE / LDAP',
    `authentication_factor` VARCHAR(50) NULL COMMENT '认证因子类型，如 PASSWORD / TOTP / OAUTH2 / EMAIL / MFA',
    `ip_address` VARCHAR(45) NULL COMMENT '登录 IP 地址',
    `user_agent` VARCHAR(255) NULL COMMENT '客户端 User-Agent 信息',
    `session_id` VARCHAR(128) NULL COMMENT '会话 ID，可选',
    `token_id` VARCHAR(128) NULL COMMENT 'OAuth2 / OIDC token ID，可选',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户认证审计表';

-- 创建用户头像表
CREATE TABLE IF NOT EXISTS `user_avatar` (
    `user_id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID，与user.id一对一关系',
    `content_type` VARCHAR(128) NOT NULL COMMENT 'MIME类型，如image/png, image/jpeg, image/webp',
    `filename` VARCHAR(255) DEFAULT NULL COMMENT '原始文件名',
    `file_size` INT UNSIGNED NOT NULL COMMENT '文件大小（字节），限制在1MB以内',
    `data` LONGBLOB NOT NULL COMMENT '头像二进制数据，使用LONGBLOB支持最大4GB',
    `content_hash` CHAR(64) DEFAULT NULL COMMENT 'SHA-256哈希值（64字符），用于去重、校验和缓存',
    `uploaded_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    KEY `idx_content_hash` (`content_hash`) COMMENT '索引：用于去重查询',
    CONSTRAINT `chk_file_size` CHECK (`file_size` <= 1048576) COMMENT '文件大小限制：最大1MB'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户头像表'; 

-- 创建 HTTP 请求日志表
CREATE TABLE IF NOT EXISTS `http_request_log` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
    `trace_id` CHAR(32) NOT NULL COMMENT '全链路 trace id（十六进制）',
    `span_id` CHAR(32) DEFAULT NULL COMMENT '当前 span id（可选）',
    `request_id` CHAR(32) NOT NULL COMMENT '本服务内请求ID（唯一）',
    `service_name` VARCHAR(64) NOT NULL COMMENT '服务名，如 user-service',
    `env` VARCHAR(32) NOT NULL COMMENT '环境：dev/test/pre/prod',
    `module` VARCHAR(64) DEFAULT NULL COMMENT '业务模块/分组，如 order/payment',
    `user_id` VARCHAR(128) DEFAULT NULL COMMENT '用户ID（若未登录则空）',
    `client_ip` VARCHAR(45) DEFAULT NULL COMMENT '客户端IP（支持 IPv6）',
    `host` VARCHAR(128) DEFAULT NULL COMMENT '请求的 Host/域名',
    `user_agent` VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent（建议截断）',
    `http_version` VARCHAR(16) DEFAULT NULL COMMENT 'HTTP/1.1, HTTP/2 等',
    `method` VARCHAR(10) NOT NULL COMMENT 'GET/POST/PUT/DELETE 等',
    `path_template` VARCHAR(256) NOT NULL COMMENT '路径模板，如 /orders/{id}',
    `raw_path` VARCHAR(1024) DEFAULT NULL COMMENT '原始请求路径，如 /orders/123?x=1',
    `query_string` VARCHAR(1024) DEFAULT NULL COMMENT '原始 query string（脱敏/限长）',
    `request_size` BIGINT DEFAULT NULL COMMENT '请求体大小（字节）',
    `response_size` BIGINT DEFAULT NULL COMMENT '响应体大小（字节）',
    `status` SMALLINT DEFAULT NULL COMMENT 'HTTP 状态码',
    `success` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否成功：1=成功，0=失败',
    `duration_ms` INT DEFAULT NULL COMMENT '耗时（毫秒）',
    `error` VARCHAR(512) DEFAULT NULL COMMENT '错误摘要（message），不要存堆栈',
    `request_body` MEDIUMTEXT DEFAULT NULL COMMENT '请求体（按需开启；脱敏+限长）',
    `response_body` MEDIUMTEXT DEFAULT NULL COMMENT '响应体（按需开启；脱敏+限长）',
    `request_at` TIMESTAMP NOT NULL COMMENT '请求发生时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志写入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_request_id` (`request_id`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_service_env_created` (`service_name`, `env`, `created_at`),
    KEY `idx_path_template_created` (`path_template`, `created_at`),
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_status_created` (`status`, `created_at`),
    KEY `idx_trace` (`trace_id`),
    KEY `idx_request_at` (`request_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='HTTP 请求/响应日志（面向数据分析，body 可选，注意脱敏与限长）';

-- 创建导出任务表
CREATE TABLE IF NOT EXISTS `export_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '任务唯一ID（UUID）',
    `user_id` VARCHAR(64) NOT NULL COMMENT '任务发起用户ID',
    `username` VARCHAR(128) DEFAULT NULL COMMENT '任务发起用户名（可选）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/RUNNING/SUCCESS/FAILED/CANCELED',
    `progress` INT DEFAULT 0 COMMENT '导出进度 0~100（动态更新）',
    `total_rows` BIGINT DEFAULT NULL COMMENT '总行数（可为 NULL 表示未知或估算值）',
    `processed_rows` BIGINT DEFAULT 0 COMMENT '已处理行数（Slice/Iterator 用这个）',
    `sheet_count` INT DEFAULT 1 COMMENT '总 Sheet 数（用于多Sheet导出）',
    `file_path` VARCHAR(512) DEFAULT NULL COMMENT '导出文件本地路径（或 OSS 对象key）',
    `download_url` VARCHAR(1024) DEFAULT NULL COMMENT '可选：文件下载URL（通常是 OSS 预签名URL）',
    `error_msg` TEXT COMMENT '错误信息（失败时记录）',
    `error_code` VARCHAR(64) DEFAULT NULL COMMENT '错误编码（可选，便于分类统计）',
    `query_params` JSON DEFAULT NULL COMMENT '导出查询参数（JSON 持久化，用于审计和重跑）',
    `worker_id` VARCHAR(64) DEFAULT NULL COMMENT '执行任务的 worker 实例ID',
    `attempt` INT NOT NULL DEFAULT 0 COMMENT '任务尝试次数',
    `last_heartbeat` DATETIME DEFAULT NULL COMMENT '最近心跳时间（检测僵尸任务）',
    `expire_at` DATETIME DEFAULT NULL COMMENT '文件失效时间（定时清理任务用）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '任务更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_expire_at` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出任务表';