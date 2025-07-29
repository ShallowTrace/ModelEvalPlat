-- 参赛用户表（参赛团队表）
CREATE TABLE `users` (
     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
     `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（唯一）',
     `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
     `role` ENUM('user', 'admin') NOT NULL DEFAULT 'user' COMMENT '用户角色',
     `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
     `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
     PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- 比赛表
create table competitions
(
    id                     bigint unsigned auto_increment comment '比赛ID'
        primary key,
    name                   varchar(100)                           not null comment '比赛名称',
    description            text                                   null comment '比赛介绍',
    start_time             datetime                               not null comment '开始时间',
    end_time               datetime                               not null comment '结束时间',
    path                   varchar(255)                           null comment '测试数据和结果路径（文件/URL）',
    is_active              tinyint(1)   default 1                 not null comment '是否激活',
    participant_count      int unsigned default '0'               not null comment '报名人数',
    daily_submission_limit int          default 5                 not null comment '每日提交次数限制',
    created_at             datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint unique_competition_name
        unique (name)
)
    comment '比赛信息表';


-- 报名表
CREATE TABLE `user_competitions` (
     `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
     `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
     `competition_id` BIGINT UNSIGNED NOT NULL COMMENT '比赛ID',
     `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `uniq_user_competition` (`user_id`, `competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户报名表（用户-比赛多对多关系）';


-- 模型提交记录表
CREATE TABLE `submissions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    `competition_id` BIGINT UNSIGNED NOT NULL COMMENT '比赛ID',
    `model_path` VARCHAR(255) NOT NULL COMMENT '模型文件路径',
    `status` ENUM('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED') NOT NULL COMMENT '评测状态',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_competition` (`user_id`, `competition_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型提交记录表';


-- 评估结果表
CREATE TABLE `evaluation_results` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
    `competition_id` BIGINT UNSIGNED NOT NULL COMMENT '比赛ID',
    `submit_time` DATETIME NOT NULL COMMENT '提交时间',
    `result_json` TEXT COMMENT '评估结果（JSON格式）',
    `score` FLOAT NOT NULL COMMENT '总分或主要指标',
    PRIMARY KEY (`id`),
    INDEX `idx_user_competition_time` (`user_id`, `competition_id`, `submit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果表';


