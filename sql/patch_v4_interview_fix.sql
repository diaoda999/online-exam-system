-- ============================================================
-- 补丁脚本 v4：面试审计后的全面修复
-- 修复 init.sql 与 Java 实体的严重不一致
-- MySQL 8.0 兼容版本，幂等可重复执行
-- ============================================================

USE exam_system;

-- -----------------------------------------------------------
-- 1. 重命名 answer 表为 exam_answer（与 @TableName("exam_answer") 一致）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_answer_table_name;
DELIMITER //
CREATE PROCEDURE fix_answer_table_name()
BEGIN
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='answer') THEN
        RENAME TABLE `answer` TO `exam_answer`;
    END IF;
END //
DELIMITER ;
CALL fix_answer_table_name();
DROP PROCEDURE IF EXISTS fix_answer_table_name;

-- -----------------------------------------------------------
-- 2. exam_answer 表：修改列名与实体字段对齐
--    exam_record_id → record_id, user_answer → answer
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_answer_columns;
DELIMITER //
CREATE PROCEDURE fix_answer_columns()
BEGIN
    -- exam_record_id → record_id
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND COLUMN_NAME='exam_record_id') THEN
        ALTER TABLE `exam_answer` CHANGE COLUMN `exam_record_id` `record_id` BIGINT NOT NULL COMMENT '考试记录ID';
    END IF;
    -- user_answer → answer
    IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND COLUMN_NAME='user_answer') THEN
        ALTER TABLE `exam_answer` CHANGE COLUMN `user_answer` `answer` TEXT DEFAULT NULL COMMENT '学生答案';
    END IF;
    -- 补充 update_time
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND COLUMN_NAME='update_time') THEN
        ALTER TABLE `exam_answer` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';
    END IF;
    -- 补充 is_correct
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND COLUMN_NAME='is_correct') THEN
        ALTER TABLE `exam_answer` ADD COLUMN `is_correct` TINYINT DEFAULT NULL COMMENT '是否正确 0错 1对';
    END IF;
END //
DELIMITER ;
CALL fix_answer_columns();
DROP PROCEDURE IF EXISTS fix_answer_columns;

-- -----------------------------------------------------------
-- 3. exam_answer 表：更新索引名（如果列名变了）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_answer_indexes;
DELIMITER //
CREATE PROCEDURE fix_answer_indexes()
BEGIN
    -- 如果 record_id 索引已存在但名称不同，重建
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND INDEX_NAME='idx_record_id') THEN
        -- 删除旧索引名（如果存在）
        IF EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_answer' AND INDEX_NAME='idx_exam_record_id') THEN
            ALTER TABLE `exam_answer` DROP INDEX `idx_exam_record_id`;
        END IF;
        ALTER TABLE `exam_answer` ADD INDEX `idx_record_id` (`record_id`);
    END IF;
END //
DELIMITER ;
CALL fix_answer_indexes();
DROP PROCEDURE IF EXISTS fix_answer_indexes;

-- -----------------------------------------------------------
-- 4. exam_record 表：补充 objective_score / subjective_score 列
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_record_columns;
DELIMITER //
CREATE PROCEDURE fix_record_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_record' AND COLUMN_NAME='objective_score') THEN
        ALTER TABLE `exam_record` ADD COLUMN `objective_score` INT DEFAULT -1 COMMENT '客观题得分' AFTER `total_score`;
    ELSE
        -- 确保默认值为 -1
        ALTER TABLE `exam_record` MODIFY COLUMN `objective_score` INT DEFAULT -1 COMMENT '客观题得分';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_record' AND COLUMN_NAME='subjective_score') THEN
        ALTER TABLE `exam_record` ADD COLUMN `subjective_score` INT DEFAULT -1 COMMENT '主观题得分' AFTER `objective_score`;
    ELSE
        ALTER TABLE `exam_record` MODIFY COLUMN `subjective_score` INT DEFAULT -1 COMMENT '主观题得分';
    END IF;

    -- total_score 默认值也设为 -1
    ALTER TABLE `exam_record` MODIFY COLUMN `total_score` INT DEFAULT -1 COMMENT '总得分';
END //
DELIMITER ;
CALL fix_record_columns();
DROP PROCEDURE IF EXISTS fix_record_columns;

-- -----------------------------------------------------------
-- 5. exam_record 表：添加 exam_id + user_id 联合索引（防重复进入）
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_record_indexes;
DELIMITER //
CREATE PROCEDURE fix_record_indexes()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='exam_record' AND INDEX_NAME='idx_exam_user') THEN
        ALTER TABLE `exam_record` ADD INDEX `idx_exam_user` (`exam_id`, `user_id`);
    END IF;
END //
DELIMITER ;
CALL fix_record_indexes();
DROP PROCEDURE IF EXISTS fix_record_indexes;

-- -----------------------------------------------------------
-- 6. question 表：补充 analysis / subject / optionA-H 列
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_question_columns;
DELIMITER //
CREATE PROCEDURE fix_question_columns()
BEGIN
    -- analysis 答案解析
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='analysis') THEN
        ALTER TABLE `question` ADD COLUMN `analysis` TEXT DEFAULT NULL COMMENT '答案解析' AFTER `answer`;
    END IF;
    -- subject 学科/知识点
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='subject') THEN
        ALTER TABLE `question` ADD COLUMN `subject` VARCHAR(100) DEFAULT NULL COMMENT '学科/知识点' AFTER `difficulty`;
    END IF;
    -- 选项 A-H 独立列
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_a') THEN
        ALTER TABLE `question` ADD COLUMN `option_a` VARCHAR(1000) DEFAULT NULL COMMENT '选项A' AFTER `question_type`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_b') THEN
        ALTER TABLE `question` ADD COLUMN `option_b` VARCHAR(1000) DEFAULT NULL COMMENT '选项B' AFTER `option_a`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_c') THEN
        ALTER TABLE `question` ADD COLUMN `option_c` VARCHAR(1000) DEFAULT NULL COMMENT '选项C' AFTER `option_b`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_d') THEN
        ALTER TABLE `question` ADD COLUMN `option_d` VARCHAR(1000) DEFAULT NULL COMMENT '选项D' AFTER `option_c`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_e') THEN
        ALTER TABLE `question` ADD COLUMN `option_e` VARCHAR(1000) DEFAULT NULL COMMENT '选项E' AFTER `option_d`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_f') THEN
        ALTER TABLE `question` ADD COLUMN `option_f` VARCHAR(1000) DEFAULT NULL COMMENT '选项F' AFTER `option_e`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_g') THEN
        ALTER TABLE `question` ADD COLUMN `option_g` VARCHAR(1000) DEFAULT NULL COMMENT '选项G' AFTER `option_f`;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='option_h') THEN
        ALTER TABLE `question` ADD COLUMN `option_h` VARCHAR(1000) DEFAULT NULL COMMENT '选项H' AFTER `option_g`;
    END IF;
END //
DELIMITER ;
CALL fix_question_columns();
DROP PROCEDURE IF EXISTS fix_question_columns;

-- -----------------------------------------------------------
-- 7. question 表：creator_id 列补充
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_question_creator;
DELIMITER //
CREATE PROCEDURE fix_question_creator()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question' AND COLUMN_NAME='creator_id') THEN
        ALTER TABLE `question` ADD COLUMN `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID' AFTER `create_by`;
    END IF;
END //
DELIMITER ;
CALL fix_question_creator();
DROP PROCEDURE IF EXISTS fix_question_creator;

SELECT 'Patch v4 applied successfully: database schema aligned with Java entities' AS result;
