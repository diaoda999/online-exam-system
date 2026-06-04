-- ============================================================
-- 补丁脚本 v2：T03 题库与试卷 + T04 考试核心流程 新增表
-- MySQL 8.0 兼容版本（不使用 ADD COLUMN IF NOT EXISTS）
-- ============================================================

USE exam_system;

-- -----------------------------------------------------------
-- 题库表（T03 新增）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question_bank` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `bank_name`      VARCHAR(100)  NOT NULL,
    `description`    VARCHAR(500)  DEFAULT NULL,
    `creator_id`     BIGINT        NOT NULL,
    `question_count` INT           NOT NULL DEFAULT 0,
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 题库-题目关联表（T03 新增）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question_bank_item` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT,
    `bank_id`     BIGINT   NOT NULL,
    `question_id` BIGINT   NOT NULL,
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_bank_question` (`bank_id`, `question_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 试卷规则表（T03 随机组卷）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paper_rule` (
    `id`                 BIGINT   NOT NULL AUTO_INCREMENT,
    `paper_id`           BIGINT   NOT NULL,
    `question_type`      TINYINT  NOT NULL,
    `difficulty`         TINYINT  NOT NULL,
    `question_count`     INT      NOT NULL,
    `score_per_question` INT      NOT NULL DEFAULT 0,
    `bank_id`            BIGINT   DEFAULT NULL,
    `create_time`        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 考试答案表（T04 新增）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `exam_answer` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `record_id`   BIGINT        NOT NULL,
    `question_id` BIGINT        NOT NULL,
    `answer`      VARCHAR(2000) DEFAULT NULL,
    `score`       INT           DEFAULT -1,
    `is_correct`  TINYINT       DEFAULT NULL,
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_record_id` (`record_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------
-- 向 question 表补充缺少的列（使用存储过程条件判断）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_question_columns;
DELIMITER //
CREATE PROCEDURE add_question_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_a') THEN
        ALTER TABLE `question` ADD COLUMN `option_a` VARCHAR(500) DEFAULT NULL AFTER `content`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_b') THEN
        ALTER TABLE `question` ADD COLUMN `option_b` VARCHAR(500) DEFAULT NULL AFTER `option_a`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_c') THEN
        ALTER TABLE `question` ADD COLUMN `option_c` VARCHAR(500) DEFAULT NULL AFTER `option_b`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_d') THEN
        ALTER TABLE `question` ADD COLUMN `option_d` VARCHAR(500) DEFAULT NULL AFTER `option_c`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='analysis') THEN
        ALTER TABLE `question` ADD COLUMN `analysis` TEXT DEFAULT NULL AFTER `answer`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='subject') THEN
        ALTER TABLE `question` ADD COLUMN `subject` VARCHAR(100) DEFAULT NULL AFTER `difficulty`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='creator_id') THEN
        ALTER TABLE `question` ADD COLUMN `creator_id` BIGINT DEFAULT NULL AFTER `subject`;
    END IF;
END //
DELIMITER ;
CALL add_question_columns();
DROP PROCEDURE IF EXISTS add_question_columns;

-- -----------------------------------------------------------
-- 向 paper 表补充缺少的列
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_paper_columns;
DELIMITER //
CREATE PROCEDURE add_paper_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='paper' AND COLUMN_NAME='paper_type') THEN
        ALTER TABLE `paper` ADD COLUMN `paper_type` TINYINT NOT NULL DEFAULT 1 AFTER `paper_name`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='paper' AND COLUMN_NAME='creator_id') THEN
        ALTER TABLE `paper` ADD COLUMN `creator_id` BIGINT DEFAULT NULL;
    END IF;
END //
DELIMITER ;
CALL add_paper_columns();
DROP PROCEDURE IF EXISTS add_paper_columns;

-- -----------------------------------------------------------
-- 向 exam 表补充缺少的列
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_exam_columns;
DELIMITER //
CREATE PROCEDURE add_exam_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam' AND COLUMN_NAME='duration') THEN
        ALTER TABLE `exam` ADD COLUMN `duration` INT NOT NULL DEFAULT 120 AFTER `end_time`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam' AND COLUMN_NAME='creator_id') THEN
        ALTER TABLE `exam` ADD COLUMN `creator_id` BIGINT NOT NULL DEFAULT 0 AFTER `duration`;
    END IF;
END //
DELIMITER ;
CALL add_exam_columns();
DROP PROCEDURE IF EXISTS add_exam_columns;

-- -----------------------------------------------------------
-- 向 exam_record 表补充缺少的列
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS add_record_columns;
DELIMITER //
CREATE PROCEDURE add_record_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_record' AND COLUMN_NAME='objective_score') THEN
        ALTER TABLE `exam_record` ADD COLUMN `objective_score` INT DEFAULT -1 AFTER `total_score`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_record' AND COLUMN_NAME='subjective_score') THEN
        ALTER TABLE `exam_record` ADD COLUMN `subjective_score` INT DEFAULT -1 AFTER `objective_score`;
    END IF;
END //
DELIMITER ;
CALL add_record_columns();
DROP PROCEDURE IF EXISTS add_record_columns;

-- -----------------------------------------------------------
-- 插入角色数据（幂等）
-- -----------------------------------------------------------
INSERT IGNORE INTO `role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有最高权限'),
(2, '教师',   'TEACHER', '教师，可管理课程、题目、试卷和考试'),
(3, '学生',   'STUDENT', '学生，可参加考试和查看成绩');
