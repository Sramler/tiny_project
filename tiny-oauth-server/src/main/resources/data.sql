-- 插入用户数据
INSERT INTO `user` (`username`, `password`, `nickname`, `enabled`, `account_non_expired`, `account_non_locked`, `credentials_non_expired`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', true, true, true, true),
('user', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '普通用户', true, true, true, true);

-- 插入角色数据
INSERT INTO `role` (`name`, `description`) VALUES
('ADMIN', '系统管理员'),
('USER', '普通用户');

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