-- 插入初始角色
INSERT INTO role (id, name, description) VALUES
    (1, 'ROLE_ADMIN', '系统管理员');

-- 插入初始资源（3个示例 API 资源）
INSERT INTO resource (id, name, path, method, type, parent_id, sort) VALUES
                                                                         (1, '用户列表', '/sys/user/list', 'GET', 'API', NULL, 1),
                                                                         (2, '添加用户', '/sys/user/add', 'POST', 'API', NULL, 2),
                                                                         (3, '删除用户', '/sys/user/delete', 'DELETE', 'API', NULL, 3);

-- 插入初始用户（密码应与 Spring Security 配置一致）
-- 示例密码为 `admin`，使用 BCrypt 加密后的值
INSERT INTO user (id, username, password, nickname, enabled, account_non_expired, account_non_locked, credentials_non_expired, create_time, update_time)
VALUES (
           1,
           'admin',
           '{bcrypt}$2a$10$WzQPMx3cPM4dfx.Z3lysyuWe2uxDrPYxvmh9ExhFwERzTrgGU5R8u', -- admin
           '超级管理员',
           true, true, true, true,
           NOW(), NOW()
       );

-- 用户与角色关联（admin -> ROLE_ADMIN）
INSERT INTO user_role (user_id, role_id) VALUES
    (1, 1);

-- 角色与资源关联（ROLE_ADMIN -> 所有资源）
INSERT INTO role_resource (role_id, resource_id) VALUES
                                                     (1, 1),
                                                     (1, 2),
                                                     (1, 3);