
CREATE database ecc ;

use ecc;

drop table  `user`;
CREATE TABLE `user` (
                        `id` BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
                        `password` VARCHAR(255) NOT NULL COMMENT '用户密码（加密存储）',
                        `email` VARCHAR(100) NOT NULL COMMENT '用户邮箱地址',
                        `appid` VARCHAR(50) DEFAULT NULL COMMENT '应用标识符',
                        `user_type` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '用户类型（NORMAL-普通用户，ADMIN-管理员）',
                        `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `last_login` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                        `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '用户状态（ACTIVE-激活，INACTIVE-未激活，DISABLED-禁用）',
                        `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL地址',

                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_email` (`email`) COMMENT '邮箱唯一索引',
                        KEY `idx_appid` (`appid`) COMMENT '应用ID索引',
                        KEY `idx_user_type` (`user_type`) COMMENT '用户类型索引',
                        KEY `idx_status` (`status`) COMMENT '用户状态索引',
                        KEY `idx_created_at` (`created_at`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 插入示例数据（可选）
-- INSERT INTO `user` (`password`, `email`, `appid`, `user_type`, `status`) VALUES
-- ('$2a$10$encrypted_password_hash', 'admin@example.com', 'ecc-demo', 'ADMIN', 'ACTIVE'),
-- ('$2a$10$encrypted_password_hash', 'user@example.com', 'ecc-demo', 'NORMAL', 'ACTIVE');

CREATE TABLE `user_key` (
    `id` BIGINT PRIMARY KEY NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名-》映射email',
    `publicX` VARCHAR(64) NOT NULL COMMENT '用户公钥X坐标',
    `publicY` VARCHAR(64) NOT NULL COMMENT '用户公钥Y坐标',
    `status` VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE-激活，INACTIVE-未激活，DISABLED-禁用）'
)