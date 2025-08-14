-- 参赛用户表（参赛团队表）
# CREATE TABLE `users` (
#                          `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
#                          `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（唯一）',
#                          `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
#                          `email` VARCHAR(255) NOT NULL UNIQUE COMMENT '用户邮箱',
#                          `role` ENUM('user', 'admin') NOT NULL DEFAULT 'user' COMMENT '用户角色',
#                          `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否激活（0-未激活，1-已激活）',
#                          `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
#                          `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
#                          PRIMARY KEY (`id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `users` (
                         `id` BIGINT UNSIGNED NOT NULL COMMENT '主键',
                         `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名（唯一）',
                         `password` VARCHAR(255) NOT NULL COMMENT '密码哈希',
                         `email` VARCHAR(255) NOT NULL UNIQUE COMMENT '用户邮箱',
                         `role` ENUM('user', 'admin') NOT NULL DEFAULT 'user' COMMENT '用户角色',
                         `is_active` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否激活（0-未激活，1-已激活）',
                         `gender` ENUM('male', 'female', 'other') NULL COMMENT '性别（男/女/其他，可为空）',
                         `avatar_url` VARCHAR(255) NULL COMMENT '头像URL，可为空',
                         `department` VARCHAR(100) NULL COMMENT '部门，可为空',
                         `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                         `last_login_at` DATETIME DEFAULT NULL COMMENT '最后登录时间',
                         PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 比赛表
CREATE TABLE competitions
(
    id                     BIGINT UNSIGNED AUTO_INCREMENT COMMENT '比赛ID'
        PRIMARY KEY,
    name                   VARCHAR(100)                           NOT NULL COMMENT '比赛名称',
    description            TEXT                                   NULL COMMENT '比赛介绍',
    start_time             DATETIME                               NOT NULL COMMENT '开始时间',
    end_time               DATETIME                               NOT NULL COMMENT '结束时间',
    path                   VARCHAR(255)                           NULL COMMENT '测试集和真实值csv路径',
    is_active              TINYINT(1)   DEFAULT 1                 NOT NULL COMMENT '是否激活',
    participant_count      INT UNSIGNED DEFAULT '0'               NOT NULL COMMENT '报名人数',
    daily_submission_limit INT          DEFAULT 5                 NOT NULL COMMENT '每日提交次数限制',
    created_at             DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    register_start_time    DATETIME                               NOT NULL COMMENT '允许报名的开始时间',
    register_end_time      DATETIME                               NOT NULL COMMENT '允许报名的最晚时间',

    organizer              VARCHAR(100)  NULL COMMENT '比赛主办方',
    prize                  VARCHAR(50)   NULL COMMENT '比赛奖金',
    competition_type       VARCHAR(50)   NULL COMMENT '比赛类型',

    CONSTRAINT unique_competition_name
        UNIQUE (name)
)
    COMMENT '比赛信息表';




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
    `model_path` VARCHAR(255) NOT NULL COMMENT '提交文件路径',
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
    `result_json` TEXT COMMENT '评估指标（JSON格式）',
    `score` FLOAT NOT NULL COMMENT '总分或主要指标',
    PRIMARY KEY (`id`),
    INDEX `idx_comp_user_score` (competition_id, user_id, score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估结果表';

