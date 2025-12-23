-- Tiny Platform 数据字典模块数据库表结构
-- 创建时间: 2025-01-XX
-- 说明: 数据字典核心表结构，支持多租户和平台字典

-- ============================================
-- 1. 字典类型表
-- ============================================
CREATE TABLE IF NOT EXISTS dict_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_code VARCHAR(64) NOT NULL UNIQUE COMMENT '字典编码，唯一标识，如 GENDER, ORDER_STATUS',
    dict_name VARCHAR(128) NOT NULL COMMENT '字典名称，如 性别, 订单状态',
    description VARCHAR(255) COMMENT '字典描述',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID，0表示平台字典，>0表示租户自定义字典',
    category_id BIGINT COMMENT '分类ID，用于字典分组',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    ext_attrs JSON COMMENT '扩展属性，JSON格式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_dict_code (dict_code),
    INDEX idx_category_id (category_id),
    INDEX idx_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典类型表';

-- ============================================
-- 2. 字典项表
-- ============================================
CREATE TABLE IF NOT EXISTS dict_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID，关联dict_type.id',
    value VARCHAR(64) NOT NULL COMMENT '字典值，如 MALE, FEMALE, PENDING, PAID',
    label VARCHAR(128) NOT NULL COMMENT '字典标签，如 男, 女, 待支付, 已支付',
    description VARCHAR(255) COMMENT '字典项描述',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID，0表示平台字典项，>0表示租户自定义字典项（只能覆盖label）',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    ext_attrs JSON COMMENT '扩展属性，JSON格式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by VARCHAR(64) COMMENT '创建人',
    updated_by VARCHAR(64) COMMENT '更新人',
    UNIQUE KEY uk_dict_type_value_tenant (dict_type_id, value, tenant_id) COMMENT '同一字典类型下，同一租户内value唯一',
    INDEX idx_dict_type_id (dict_type_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_value (value),
    INDEX idx_enabled (enabled),
    INDEX idx_sort_order (sort_order),
    FOREIGN KEY (dict_type_id) REFERENCES dict_type(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项表';

-- ============================================
-- 3. 租户策略表（可选，用于治理能力）
-- ============================================
CREATE TABLE IF NOT EXISTS tenant_policy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    policy_type VARCHAR(32) NOT NULL COMMENT '策略类型：STRICT_VALIDATION, FORCE_CHANGE, CI_CHECK',
    enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用',
    config JSON COMMENT '策略配置，JSON格式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_policy (tenant_id, policy_type),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_policy_type (policy_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户策略表';

-- ============================================
-- 4. 能力矩阵表（可选，用于治理能力）
-- ============================================
CREATE TABLE IF NOT EXISTS capability_matrix (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    capability_code VARCHAR(64) NOT NULL COMMENT '能力编码，如 DICT_LEVEL1, DICT_LEVEL2',
    enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用',
    config JSON COMMENT '能力配置，JSON格式',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_tenant_capability (tenant_id, capability_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_capability_code (capability_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='能力矩阵表';

-- ============================================
-- 5. 字典版本表（可选，用于版本管理）
-- ============================================
CREATE TABLE IF NOT EXISTS dict_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    version VARCHAR(32) NOT NULL COMMENT '版本号，如 1.0.0',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    description VARCHAR(255) COMMENT '版本描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    created_by VARCHAR(64) COMMENT '创建人',
    UNIQUE KEY uk_dict_version_tenant (dict_code, version, tenant_id),
    INDEX idx_dict_code (dict_code),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典版本表';

-- ============================================
-- 6. 字典项版本快照表（可选，用于版本管理）
-- ============================================
CREATE TABLE IF NOT EXISTS dict_item_version_snapshot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_version_id BIGINT NOT NULL COMMENT '字典版本ID',
    dict_item_id BIGINT COMMENT '字典项ID（可为NULL，表示已删除）',
    value VARCHAR(64) NOT NULL COMMENT '字典值',
    label VARCHAR(128) NOT NULL COMMENT '字典标签',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    ext_attrs JSON COMMENT '扩展属性',
    INDEX idx_dict_version_id (dict_version_id),
    INDEX idx_dict_item_id (dict_item_id),
    FOREIGN KEY (dict_version_id) REFERENCES dict_version(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典项版本快照表';

-- ============================================
-- 7. 字典审计日志表（可选，用于审计）
-- ============================================
CREATE TABLE IF NOT EXISTS dict_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    dict_code VARCHAR(64) NOT NULL COMMENT '字典编码',
    dict_item_id BIGINT COMMENT '字典项ID',
    operation_type VARCHAR(32) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE',
    tenant_id BIGINT DEFAULT 0 COMMENT '租户ID',
    old_value JSON COMMENT '变更前值，JSON格式',
    new_value JSON COMMENT '变更后值，JSON格式',
    operator VARCHAR(64) COMMENT '操作人',
    operator_ip VARCHAR(64) COMMENT '操作IP',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_item_id (dict_item_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='字典审计日志表';

-- ============================================
-- 8. 初始化平台字典数据（示例）
-- ============================================
-- 插入性别字典类型
INSERT INTO dict_type (dict_code, dict_name, description, tenant_id, enabled, sort_order) 
VALUES ('GENDER', '性别', '性别字典', 0, TRUE, 1)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 插入性别字典项
INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'MALE', '男', '男性', 0, TRUE, 1 FROM dict_type WHERE dict_code = 'GENDER'
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'FEMALE', '女', '女性', 0, TRUE, 2 FROM dict_type WHERE dict_code = 'GENDER'
ON DUPLICATE KEY UPDATE label = VALUES(label);

-- 插入订单状态字典类型
INSERT INTO dict_type (dict_code, dict_name, description, tenant_id, enabled, sort_order)
VALUES ('ORDER_STATUS', '订单状态', '订单状态字典', 0, TRUE, 2)
ON DUPLICATE KEY UPDATE dict_name = VALUES(dict_name);

-- 插入订单状态字典项
INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'PENDING', '待支付', '订单待支付', 0, TRUE, 1 FROM dict_type WHERE dict_code = 'ORDER_STATUS'
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'PAID', '已支付', '订单已支付', 0, TRUE, 2 FROM dict_type WHERE dict_code = 'ORDER_STATUS'
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'SHIPPED', '已发货', '订单已发货', 0, TRUE, 3 FROM dict_type WHERE dict_code = 'ORDER_STATUS'
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'COMPLETED', '已完成', '订单已完成', 0, TRUE, 4 FROM dict_type WHERE dict_code = 'ORDER_STATUS'
ON DUPLICATE KEY UPDATE label = VALUES(label);

INSERT INTO dict_item (dict_type_id, value, label, description, tenant_id, enabled, sort_order)
SELECT id, 'CANCELLED', '已取消', '订单已取消', 0, TRUE, 5 FROM dict_type WHERE dict_code = 'ORDER_STATUS'
ON DUPLICATE KEY UPDATE label = VALUES(label);

