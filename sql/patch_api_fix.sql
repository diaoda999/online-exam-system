-- ============================================================
-- 补丁脚本 v3：API 全流程验证修复
-- 修复 init.sql 建表时的字段缺失/NOT NULL 约束问题
-- MySQL 8.0 兼容版本
-- ============================================================

USE exam_system;

-- -----------------------------------------------------------
-- 1. role 表：补充 create_time / update_time
--    原因：Role 实体有 @TableField(fill=FieldFill.INSERT)，但建表缺失
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_role_columns;
DELIMITER //
CREATE PROCEDURE fix_role_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='role' AND COLUMN_NAME='create_time') THEN
        ALTER TABLE `role` ADD COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='role' AND COLUMN_NAME='update_time') THEN
        ALTER TABLE `role` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;
CALL fix_role_columns();
DROP PROCEDURE IF EXISTS fix_role_columns;

-- -----------------------------------------------------------
-- 2. class 表：补充 update_time
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_class_columns;
DELIMITER //
CREATE PROCEDURE fix_class_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='class' AND COLUMN_NAME='update_time') THEN
        ALTER TABLE `class` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;
CALL fix_class_columns();
DROP PROCEDURE IF EXISTS fix_class_columns;

-- -----------------------------------------------------------
-- 3. question_category 表：补充 update_time
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_category_columns;
DELIMITER //
CREATE PROCEDURE fix_category_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='question_category' AND COLUMN_NAME='update_time') THEN
        ALTER TABLE `question_category` ADD COLUMN `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;
CALL fix_category_columns();
DROP PROCEDURE IF EXISTS fix_category_columns;

-- -----------------------------------------------------------
-- 4. question 表：NOT NULL 字段改为可空
--    原因：Question 实体没有 categoryId/courseId/createBy 字段，
--    MyBatis-Plus INSERT 时这些列为 NULL，但数据库要求 NOT NULL
-- -----------------------------------------------------------
ALTER TABLE `question` MODIFY COLUMN `category_id` BIGINT NULL DEFAULT NULL;
ALTER TABLE `question` MODIFY COLUMN `course_id`   BIGINT NULL DEFAULT NULL;
ALTER TABLE `question` MODIFY COLUMN `create_by`   BIGINT NULL DEFAULT NULL;

-- -----------------------------------------------------------
-- 5. paper 表：
--    a) 补充 duration 字段（实体用 duration，表原字段名为 duration_minutes）
--    b) NOT NULL 字段改为可空
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_paper_columns;
DELIMITER //
CREATE PROCEDURE fix_paper_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='paper' AND COLUMN_NAME='duration') THEN
        ALTER TABLE `paper` ADD COLUMN `duration` INT NOT NULL DEFAULT 120;
    END IF;
END //
DELIMITER ;
CALL fix_paper_columns();
DROP PROCEDURE IF EXISTS fix_paper_columns;

ALTER TABLE `paper` MODIFY COLUMN `course_id` BIGINT NULL DEFAULT NULL;
ALTER TABLE `paper` MODIFY COLUMN `create_by` BIGINT NULL DEFAULT NULL;

-- -----------------------------------------------------------
-- 6. paper_question 表：补充 create_time
-- -----------------------------------------------------------
DROP PROCEDURE IF EXISTS fix_paper_question_columns;
DELIMITER //
CREATE PROCEDURE fix_paper_question_columns()
BEGIN
    IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='exam_system' AND TABLE_NAME='paper_question' AND COLUMN_NAME='create_time') THEN
        ALTER TABLE `paper_question` ADD COLUMN `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
    END IF;
END //
DELIMITER ;
CALL fix_paper_question_columns();
DROP PROCEDURE IF EXISTS fix_paper_question_columns;

-- -----------------------------------------------------------
-- 7. exam 表：NOT NULL 字段改为可空
--    原因：Exam 实体没有 courseId/createBy/creatorId 字段
-- -----------------------------------------------------------
ALTER TABLE `exam` MODIFY COLUMN `course_id`  BIGINT NULL DEFAULT NULL;
ALTER TABLE `exam` MODIFY COLUMN `create_by`  BIGINT NULL DEFAULT NULL;
ALTER TABLE `exam` MODIFY COLUMN `creator_id` BIGINT NULL DEFAULT NULL;
