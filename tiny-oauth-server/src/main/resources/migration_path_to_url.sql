-- 数据库迁移脚本：将 resource 表的 path 字段重命名为 url
-- 执行时间：2024-06-27

-- 1. 重命名字段
ALTER TABLE resource CHANGE COLUMN path url VARCHAR(200) NOT NULL DEFAULT '' COMMENT '前端路由路径';

-- 2. 更新相关索引（如果需要）
-- 注意：如果之前有基于 path 的索引，需要重新创建基于 url 的索引
-- 这里假设之前没有专门的 path 索引，只有唯一约束

-- 3. 验证迁移结果
-- SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource' AND COLUMN_NAME = 'url'; 