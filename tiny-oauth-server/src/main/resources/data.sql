-- 插入用户数据
INSERT INTO `user` (`username`, `nickname`, `enabled`, `account_non_expired`, `account_non_locked`, `credentials_non_expired`) VALUES
('admin', '管理员', true, true, true, true),
('user', '普通用户', true, true, true, true);

-- 插入角色数据
INSERT INTO `role` (`code`, `name`, `description`, `builtin`, `enabled`) VALUES
('ROLE_ADMIN', '系统管理员', '拥有系统所有权限的管理员角色', true, true),
('ROLE_USER', '普通用户', '普通用户角色，拥有基本权限', true, true);

-- 插入用户角色关联数据
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin -> ADMIN
(2, 2); -- user -> USER

-- 插入资源数据（包含菜单和API）
-- 系统管理目录
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`) VALUES
('system', '/system', '', '', 'SettingOutlined', 1, 1, '', '', 0, 0, '系统管理', 'system', 0, NULL);

-- 用户管理菜单
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`) VALUES
('user', '/system/user', '/api/users', 'GET', 'UserOutlined', 1, 1, '/views/user/User.vue', '', 0, 0, '用户管理', 'user:list', 1, 1);

-- 角色管理菜单
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`) VALUES
('role', '/system/role', '/api/roles', 'GET', 'TeamOutlined', 1, 2, '/views/role/role.vue', '', 0, 0, '角色管理', 'role:list', 1, 1);

-- 菜单管理菜单
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`) VALUES
('menu', '/system/menu', '/api/resources/menus', 'GET', 'MenuOutlined', 1, 3, '/views/menu/Menu.vue', '', 0, 0, '菜单管理', 'menu:list', 1, 1);

-- 资源管理菜单
INSERT INTO `resource` (`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`) VALUES
('resource', '/system/resource', '/api/resources', 'GET', 'ApiOutlined', 1, 4, '/views/resource/resource.vue', '', 0, 0, '资源管理', 'resource:list', 1, 1);

-- 调度中心目录
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('scheduling', '/scheduling', '', '', 'ClusterOutlined', 1, 10, '', '/scheduling/dag', 0, 0, '调度中心', 'scheduling', 0, NULL);
SET @scheduling_dir_id = LAST_INSERT_ID();

-- 调度中心 - DAG 管理
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('schedulingDag', '/scheduling/dag', '/scheduling/dag/list', 'GET', 'BranchesOutlined', 1, 11, '/views/scheduling/Dag.vue', '', 0, 0, 'DAG 管理', 'scheduling:dag:list', 1, @scheduling_dir_id);

-- 调度中心 - 任务管理
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('schedulingTask', '/scheduling/task', '/scheduling/task/list', 'GET', 'ProfileOutlined', 1, 12, '/views/scheduling/Task.vue', '', 0, 0, '任务管理', 'scheduling:task:list', 1, @scheduling_dir_id);

-- 调度中心 - 任务类型
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('schedulingTaskType', '/scheduling/task-type', '/scheduling/task-type/list', 'GET', 'DatabaseOutlined', 1, 13, '/views/scheduling/TaskType.vue', '', 0, 0, '任务类型', 'scheduling:task-type:list', 1, @scheduling_dir_id);

-- 调度中心 - 运行历史
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('schedulingDagHistory', '/scheduling/dag-history', '/scheduling/dag/run/list', 'GET', 'HistoryOutlined', 1, 14, '/views/scheduling/DagHistory.vue', '', 0, 0, '运行历史', 'scheduling:dag-run:list', 1, @scheduling_dir_id);

-- 调度中心 - 审计日志
INSERT INTO `resource`
(`name`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `title`, `permission`, `type`, `parent_id`)
VALUES
('schedulingAudit', '/scheduling/audit', '/scheduling/audit/list', 'GET', 'SecurityScanOutlined', 1, 15, '/views/scheduling/Audit.vue', '', 0, 0, '审计日志', 'scheduling:audit:list', 1, @scheduling_dir_id);

-- 插入角色资源关联数据
INSERT INTO `role_resource` (`role_id`, `resource_id`) VALUES
-- ADMIN角色拥有所有资源
(1, 1), -- ADMIN -> system
(1, 2), -- ADMIN -> user
(1, 3), -- ADMIN -> role
(1, 4), -- ADMIN -> menu
(1, 5), -- ADMIN -> resource
-- USER角色只有用户管理
(2, 2); -- USER -> user 

-- 插入用户认证方法数据
-- 为每个用户添加 LOCAL + PASSWORD 认证方法
INSERT INTO `user_authentication_method` 
    (`user_id`, `authentication_provider`, `authentication_type`, `authentication_configuration`, `is_primary_method`, `is_method_enabled`, `authentication_priority`, `created_at`, `updated_at`)
SELECT
    u.id,
    'LOCAL',
    'PASSWORD',
    JSON_OBJECT(
        'password', CASE WHEN u.username = 'admin'
                        THEN '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'
                        ELSE '{bcrypt}$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa'
                   END,
        'password_changed_at', DATE_FORMAT(NOW(), '%Y-%m-%dT%H:%i:%sZ'),
        'hash_algorithm', 'bcrypt',
        'password_version', 1,
        'created_by', 'data.sql'
    ),
    true,
    true,
    0,
    NOW(),
    NOW()
FROM user u
WHERE u.username IN ('admin', 'user');