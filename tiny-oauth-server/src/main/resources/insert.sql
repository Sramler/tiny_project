-- 为user表添加最后登录时间字段
ALTER TABLE user ADD COLUMN last_login_at DATETIME DEFAULT NULL COMMENT '最后登录时间';

-- 插入角色数据
INSERT INTO role (name, description) VALUES
('ROLE_ADMIN', '管理员'),
('ROLE_USER', '普通用户'),
('ROLE_GUEST', '访客');

-- 插入用户数据
INSERT INTO user (username, password, nickname, enabled, account_non_expired, account_non_locked, credentials_non_expired, last_login_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', true, true, true, true, '2024-06-25 10:00:00'),
('user001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户1', true, true, true, true, '2024-06-24 15:30:00'),
('user002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户2', true, true, true, true, '2024-06-23 14:20:00'),
('user003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户3', false, true, true, true, '2024-06-22 09:15:00'),
('user004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户4', true, false, true, true, '2024-06-21 16:45:00'),
('user005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户5', true, true, false, true, '2024-06-20 11:30:00'),
('user006', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户6', true, true, true, false, '2024-06-19 13:20:00'),
('user007', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户7', true, true, true, true, '2024-06-18 08:45:00'),
('user008', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户8', false, false, true, true, '2024-06-17 17:10:00'),
('user009', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户9', true, true, false, false, '2024-06-16 12:30:00'),
('user010', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '用户10', false, true, true, false, '2024-06-15 10:20:00');

-- 插入用户-角色关联数据
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- user001 -> ROLE_USER
(3, 2), -- user002 -> ROLE_USER
(4, 2), -- user003 -> ROLE_USER
(5, 2), -- user004 -> ROLE_USER
(6, 2), -- user005 -> ROLE_USER
(7, 2), -- user006 -> ROLE_USER
(8, 2), -- user007 -> ROLE_USER
(9, 2), -- user008 -> ROLE_USER
(10, 2), -- user009 -> ROLE_USER
(11, 3); -- user010 -> ROLE_GUEST

INSERT INTO `tiny-web`.
    `oauth2_registered_client`
    (`id`,
     `client_id`,
     `client_id_issued_at`,
     `client_secret`,
     `client_secret_expires_at`,
     `client_name`,
     `client_authentication_methods`,
     `authorization_grant_types`,
     `redirect_uris`,
     `post_logout_redirect_uris`,
     `scopes`,
     `client_settings`,
     `token_settings`)
VALUES (
        '3eacac0e-0de9-4727-9a64-6bdd4be2ee1f',
        'oidc-client',
        '2023-07-12 07:33:42',
        '{noop}secret',
        NULL,
        '3eacac0e-0de9-4727-9a64-6bdd4be2ee1f',
        'client_secret_basic',
        'refresh_token,authorization_code',
        'http://www.baidu.com',
        'http://127.0.0.1:8080/',
        'openid,profile',
        '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.client.require-proof-key\":false,\"settings.client.require-authorization-consent\":true}',
        '{\"@class\":\"java.util.Collections$UnmodifiableMap\",\"settings.token.reuse-refresh-tokens\":true,\"-token-signature-algorithm\":[\"org.springframework.security.oauth2.jose.jws.SignatureAlgorithm\",\"RS256\"],\"settings.token.access-token-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.access-token-format\":{\"@class\":\"org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat\",\"value\":\"self-contained\"},\"settings.token.refresh-token-time-to-live\":[\"java.time.Duration\",3600.000000000],\"settings.token.authorization-code-time-to-live\":[\"java.time.Duration\",300.000000000],\"settings.token.device-code-time-to-live\":[\"java.time.Duration\",300.000000000]}');