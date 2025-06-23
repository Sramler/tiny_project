-- 创建数据库
CREATE DATABASE IF NOT EXISTS tiny_web
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

-- 使用该数据库
USE tiny_web;

-- 用户表
CREATE TABLE user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '加密密码',
  nickname VARCHAR(50) COMMENT '昵称',
  enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
  account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未过期',
  account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '账号是否未锁定',
  credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '密码是否未过期',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT = '用户表';

-- 角色表
CREATE TABLE role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识（如ROLE_ADMIN）',
  description VARCHAR(100) COMMENT '角色描述'
) COMMENT = '角色表';

-- 用户-角色关联表
CREATE TABLE user_role (
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
) COMMENT = '用户与角色关联表';

-- 资源表
CREATE TABLE resource (
  id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '资源ID',
  name VARCHAR(100) NOT NULL COMMENT '资源名称',
  path VARCHAR(200) COMMENT '资源路径（如 /api/user/add）',
  method VARCHAR(10) COMMENT 'HTTP方法（如GET、POST）',
  type VARCHAR(20) COMMENT '资源类型（如MENU, BUTTON, API）',
  parent_id BIGINT DEFAULT NULL COMMENT '父资源ID，用于菜单树',
  sort INT DEFAULT 0 COMMENT '排序号'
) COMMENT = '资源表';

-- 角色-资源关联表
CREATE TABLE role_resource (
  role_id BIGINT NOT NULL COMMENT '角色ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  PRIMARY KEY (role_id, resource_id),
  FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
  FOREIGN KEY (resource_id) REFERENCES resource(id) ON DELETE CASCADE
) COMMENT = '角色与资源关联表';