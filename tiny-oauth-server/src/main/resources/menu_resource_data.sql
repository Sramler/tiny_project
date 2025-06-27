-- 插入菜单数据
INSERT INTO `menu` (`name`, `title`, `path`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `permission`, `parent_id`) VALUES
-- 顶级菜单
('dashboard', '仪表盘', '/dashboard', 'DashboardOutlined', true, 1, 'views/Dashboard.vue', NULL, false, true, 'dashboard:view', NULL),
('system', '系统管理', '/system', 'SettingOutlined', true, 2, NULL, NULL, false, false, 'system:view', NULL),
('user', '用户管理', '/user', 'UserOutlined', true, 3, NULL, NULL, false, false, 'user:view', NULL),

-- 系统管理子菜单
('system-user', '用户管理', '/system/user', 'UserOutlined', true, 1, 'views/user/User.vue', NULL, false, true, 'system:user:view', 2),
('system-role', '角色管理', '/system/role', 'TeamOutlined', true, 2, 'views/role/role.vue', NULL, false, true, 'system:role:view', 2),
('system-menu', '菜单管理', '/system/menu', 'MenuOutlined', true, 3, 'views/menu/Menu.vue', NULL, false, true, 'system:menu:view', 2),
('system-resource', '资源管理', '/system/resource', 'ApiOutlined', true, 4, 'views/resource/Resource.vue', NULL, false, true, 'system:resource:view', 2),

-- 用户管理子菜单
('user-list', '用户列表', '/user/list', 'UserOutlined', true, 1, 'views/user/UserList.vue', NULL, false, true, 'user:list:view', 3),
('user-profile', '个人资料', '/user/profile', 'ProfileOutlined', true, 2, 'views/user/Profile.vue', NULL, false, true, 'user:profile:view', 3);

-- 插入资源数据
INSERT INTO `resource` (`name`, `title`, `path`, `uri`, `method`, `icon`, `show_icon`, `sort`, `component`, `redirect`, `hidden`, `keep_alive`, `permission`, `type`, `parent_id`) VALUES
-- 菜单类型资源
('dashboard', '仪表盘', '/dashboard', NULL, 'GET', 'DashboardOutlined', true, 1, 'views/Dashboard.vue', NULL, false, true, 'dashboard:view', 0, NULL),
('system', '系统管理', '/system', NULL, 'GET', 'SettingOutlined', true, 2, NULL, NULL, false, false, 'system:view', 0, NULL),
('system-user', '用户管理', '/system/user', NULL, 'GET', 'UserOutlined', true, 1, 'views/user/User.vue', NULL, false, true, 'system:user:view', 0, 2),
('system-role', '角色管理', '/system/role', NULL, 'GET', 'TeamOutlined', true, 2, 'views/role/role.vue', NULL, false, true, 'system:role:view', 0, 2),
('system-menu', '菜单管理', '/system/menu', NULL, 'GET', 'MenuOutlined', true, 3, 'views/menu/Menu.vue', NULL, false, true, 'system:menu:view', 0, 2),
('system-resource', '资源管理', '/system/resource', NULL, 'GET', 'ApiOutlined', true, 4, 'views/resource/Resource.vue', NULL, false, true, 'system:resource:view', 0, 2),

-- API类型资源
('user:list', '用户列表查询', NULL, '/sys/users', 'GET', NULL, false, 1, NULL, NULL, false, false, 'user:list:query', 2, NULL),
('user:create', '用户创建', NULL, '/sys/users', 'POST', NULL, false, 2, NULL, NULL, false, false, 'user:create', 2, NULL),
('user:update', '用户更新', NULL, '/sys/users/{id}', 'PUT', NULL, false, 3, NULL, NULL, false, false, 'user:update', 2, NULL),
('user:delete', '用户删除', NULL, '/sys/users/{id}', 'DELETE', NULL, false, 4, NULL, NULL, false, false, 'user:delete', 2, NULL),
('user:batch-delete', '用户批量删除', NULL, '/sys/users/batch/delete', 'POST', NULL, false, 5, NULL, NULL, false, false, 'user:batch:delete', 2, NULL),
('user:batch-enable', '用户批量启用', NULL, '/sys/users/batch/enable', 'POST', NULL, false, 6, NULL, NULL, false, false, 'user:batch:enable', 2, NULL),
('user:batch-disable', '用户批量禁用', NULL, '/sys/users/batch/disable', 'POST', NULL, false, 7, NULL, NULL, false, false, 'user:batch:disable', 2, NULL),

('role:list', '角色列表查询', NULL, '/sys/roles', 'GET', NULL, false, 1, NULL, NULL, false, false, 'role:list:query', 2, NULL),
('role:create', '角色创建', NULL, '/sys/roles', 'POST', NULL, false, 2, NULL, NULL, false, false, 'role:create', 2, NULL),
('role:update', '角色更新', NULL, '/sys/roles/{id}', 'PUT', NULL, false, 3, NULL, NULL, false, false, 'role:update', 2, NULL),
('role:delete', '角色删除', NULL, '/sys/roles/{id}', 'DELETE', NULL, false, 4, NULL, NULL, false, false, 'role:delete', 2, NULL),
('role:batch-delete', '角色批量删除', NULL, '/sys/roles/batch/delete', 'POST', NULL, false, 5, NULL, NULL, false, false, 'role:batch:delete', 2, NULL),

('menu:list', '菜单列表查询', NULL, '/sys/menus', 'GET', NULL, false, 1, NULL, NULL, false, false, 'menu:list:query', 2, NULL),
('menu:create', '菜单创建', NULL, '/sys/menus', 'POST', NULL, false, 2, NULL, NULL, false, false, 'menu:create', 2, NULL),
('menu:update', '菜单更新', NULL, '/sys/menus/{id}', 'PUT', NULL, false, 3, NULL, NULL, false, false, 'menu:update', 2, NULL),
('menu:delete', '菜单删除', NULL, '/sys/menus/{id}', 'DELETE', NULL, false, 4, NULL, NULL, false, false, 'menu:delete', 2, NULL),
('menu:batch-delete', '菜单批量删除', NULL, '/sys/menus/batch/delete', 'POST', NULL, false, 5, NULL, NULL, false, false, 'menu:batch:delete', 2, NULL),
('menu:tree', '菜单树查询', NULL, '/sys/menus/tree', 'GET', NULL, false, 6, NULL, NULL, false, false, 'menu:tree:query', 2, NULL),
('menu:sort', '菜单排序更新', NULL, '/sys/menus/{id}/sort', 'PUT', NULL, false, 7, NULL, NULL, false, false, 'menu:sort:update', 2, NULL),
('menu:batch-sort', '菜单批量排序', NULL, '/sys/menus/batch/sort', 'PUT', NULL, false, 8, NULL, NULL, false, false, 'menu:batch:sort', 2, NULL),

('resource:list', '资源列表查询', NULL, '/sys/resources', 'GET', NULL, false, 1, NULL, NULL, false, false, 'resource:list:query', 2, NULL),
('resource:create', '资源创建', NULL, '/sys/resources', 'POST', NULL, false, 2, NULL, NULL, false, false, 'resource:create', 2, NULL),
('resource:update', '资源更新', NULL, '/sys/resources/{id}', 'PUT', NULL, false, 3, NULL, NULL, false, false, 'resource:update', 2, NULL),
('resource:delete', '资源删除', NULL, '/sys/resources/{id}', 'DELETE', NULL, false, 4, NULL, NULL, false, false, 'resource:delete', 2, NULL),
('resource:batch-delete', '资源批量删除', NULL, '/sys/resources/batch/delete', 'POST', NULL, false, 5, NULL, NULL, false, false, 'resource:batch:delete', 2, NULL),
('resource:tree', '资源树查询', NULL, '/sys/resources/tree', 'GET', NULL, false, 6, NULL, NULL, false, false, 'resource:tree:query', 2, NULL),
('resource:sort', '资源排序更新', NULL, '/sys/resources/{id}/sort', 'PUT', NULL, false, 7, NULL, NULL, false, false, 'resource:sort:update', 2, NULL),

-- 按钮类型资源
('user:add-btn', '用户新增按钮', NULL, NULL, NULL, 'PlusOutlined', true, 1, NULL, NULL, false, false, 'user:add', 1, NULL),
('user:edit-btn', '用户编辑按钮', NULL, NULL, NULL, 'EditOutlined', true, 2, NULL, NULL, false, false, 'user:edit', 1, NULL),
('user:delete-btn', '用户删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 3, NULL, NULL, false, false, 'user:delete', 1, NULL),
('user:batch-delete-btn', '用户批量删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 4, NULL, NULL, false, false, 'user:batch:delete', 1, NULL),
('user:enable-btn', '用户启用按钮', NULL, NULL, NULL, 'CheckCircleOutlined', true, 5, NULL, NULL, false, false, 'user:enable', 1, NULL),
('user:disable-btn', '用户禁用按钮', NULL, NULL, NULL, 'StopOutlined', true, 6, NULL, NULL, false, false, 'user:disable', 1, NULL),

('role:add-btn', '角色新增按钮', NULL, NULL, NULL, 'PlusOutlined', true, 1, NULL, NULL, false, false, 'role:add', 1, NULL),
('role:edit-btn', '角色编辑按钮', NULL, NULL, NULL, 'EditOutlined', true, 2, NULL, NULL, false, false, 'role:edit', 1, NULL),
('role:delete-btn', '角色删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 3, NULL, NULL, false, false, 'role:delete', 1, NULL),
('role:batch-delete-btn', '角色批量删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 4, NULL, NULL, false, false, 'role:batch:delete', 1, NULL),

('menu:add-btn', '菜单新增按钮', NULL, NULL, NULL, 'PlusOutlined', true, 1, NULL, NULL, false, false, 'menu:add', 1, NULL),
('menu:edit-btn', '菜单编辑按钮', NULL, NULL, NULL, 'EditOutlined', true, 2, NULL, NULL, false, false, 'menu:edit', 1, NULL),
('menu:delete-btn', '菜单删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 3, NULL, NULL, false, false, 'menu:delete', 1, NULL),
('menu:batch-delete-btn', '菜单批量删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 4, NULL, NULL, false, false, 'menu:batch:delete', 1, NULL),

('resource:add-btn', '资源新增按钮', NULL, NULL, NULL, 'PlusOutlined', true, 1, NULL, NULL, false, false, 'resource:add', 1, NULL),
('resource:edit-btn', '资源编辑按钮', NULL, NULL, NULL, 'EditOutlined', true, 2, NULL, NULL, false, false, 'resource:edit', 1, NULL),
('resource:delete-btn', '资源删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 3, NULL, NULL, false, false, 'resource:delete', 1, NULL),
('resource:batch-delete-btn', '资源批量删除按钮', NULL, NULL, NULL, 'DeleteOutlined', true, 4, NULL, NULL, false, false, 'resource:batch:delete', 1, NULL);

-- 为管理员角色分配所有菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) 
SELECT 1, id FROM `menu` WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

-- 为管理员角色分配所有资源权限
INSERT INTO `role_resource` (`role_id`, `resource_id`) 
SELECT 1, id FROM `resource`;

-- 为普通用户角色分配部分菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) 
SELECT 2, id FROM `menu` WHERE id IN (1, 3, 8, 9);

-- 为普通用户角色分配部分资源权限
INSERT INTO `role_resource` (`role_id`, `resource_id`) 
SELECT 2, id FROM `resource` WHERE name IN ('dashboard', 'user:list', 'user:profile', 'user:profile:view');

-- 为访客角色分配只读菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) 
SELECT 3, id FROM `menu` WHERE id IN (1);

-- 为访客角色分配只读资源权限
INSERT INTO `role_resource` (`role_id`, `resource_id`) 
SELECT 3, id FROM `resource` WHERE name IN ('dashboard'); 