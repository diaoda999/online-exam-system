-- ============================================================
-- 在线考试系统 数据库初始化脚本
-- ============================================================

CREATE DATABASE IF NOT EXISTS exam_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE exam_system;

-- -----------------------------------------------------------
-- 角色表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name`   VARCHAR(50)  NOT NULL COMMENT '角色名称',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- -----------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)   NOT NULL COMMENT '用户名',
    `password`    VARCHAR(100)  NOT NULL COMMENT '密码',
    `real_name`   VARCHAR(50)   DEFAULT NULL COMMENT '真实姓名',
    `role_id`     BIGINT        NOT NULL COMMENT '角色ID',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -----------------------------------------------------------
-- 课程表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `course` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_name` VARCHAR(100)  NOT NULL COMMENT '课程名称',
    `course_code` VARCHAR(50)   NOT NULL COMMENT '课程编码',
    `teacher_id`  BIGINT        NOT NULL COMMENT '教师ID',
    `description` VARCHAR(500)  DEFAULT NULL COMMENT '课程描述',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_course_code` (`course_code`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

-- -----------------------------------------------------------
-- 班级表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `class` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_name`  VARCHAR(100)  NOT NULL COMMENT '班级名称',
    `course_id`   BIGINT        NOT NULL COMMENT '课程ID',
    `teacher_id`  BIGINT        NOT NULL COMMENT '教师ID',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- -----------------------------------------------------------
-- 班级-学生关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `class_student` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_id`    BIGINT NOT NULL COMMENT '班级ID',
    `student_id`  BIGINT NOT NULL COMMENT '学生ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_class_student` (`class_id`, `student_id`),
    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级-学生关联表';

-- -----------------------------------------------------------
-- 题目分类表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question_category` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_name` VARCHAR(100)  NOT NULL COMMENT '分类名称',
    `course_id`     BIGINT        NOT NULL COMMENT '课程ID',
    `description`   VARCHAR(500)  DEFAULT NULL COMMENT '分类描述',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目分类表';

-- -----------------------------------------------------------
-- 题目表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `question` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id`    BIGINT        NOT NULL COMMENT '分类ID',
    `course_id`      BIGINT        NOT NULL COMMENT '课程ID',
    `question_type`  VARCHAR(20)   NOT NULL COMMENT '题目类型 SINGLE_CHOICE/MULTI_CHOICE/JUDGE/FILL/SHORT_ANSWER',
    `content`        TEXT          NOT NULL COMMENT '题目内容',
    `options`        TEXT          DEFAULT NULL COMMENT '选项(JSON数组)',
    `answer`         VARCHAR(1000) NOT NULL COMMENT '正确答案',
    `score`          INT           NOT NULL DEFAULT 0 COMMENT '分值',
    `difficulty`     TINYINT       NOT NULL DEFAULT 1 COMMENT '难度 1-5',
    `create_by`      BIGINT        NOT NULL COMMENT '创建人ID',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_question_type` (`question_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- -----------------------------------------------------------
-- 试卷表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paper` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_name`       VARCHAR(200)  NOT NULL COMMENT '试卷名称',
    `course_id`        BIGINT        NOT NULL COMMENT '课程ID',
    `total_score`      INT           NOT NULL DEFAULT 0 COMMENT '总分',
    `pass_score`       INT           NOT NULL DEFAULT 0 COMMENT '及格分',
    `duration_minutes` INT           NOT NULL DEFAULT 60 COMMENT '考试时长(分钟)',
    `create_by`        BIGINT        NOT NULL COMMENT '创建人ID',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

-- -----------------------------------------------------------
-- 试卷-题目关联表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `paper_question` (
    `id`         BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_id`   BIGINT NOT NULL COMMENT '试卷ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `sort_order` INT    NOT NULL DEFAULT 0 COMMENT '排序',
    `score`      INT    NOT NULL DEFAULT 0 COMMENT '分值',
    PRIMARY KEY (`id`),
    KEY `idx_paper_id` (`paper_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷-题目关联表';

-- -----------------------------------------------------------
-- 考试表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `exam` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_name`   VARCHAR(200)  NOT NULL COMMENT '考试名称',
    `paper_id`    BIGINT        NOT NULL COMMENT '试卷ID',
    `course_id`   BIGINT        NOT NULL COMMENT '课程ID',
    `class_id`    BIGINT        NOT NULL COMMENT '班级ID',
    `start_time`  DATETIME      NOT NULL COMMENT '开始时间',
    `end_time`    DATETIME      NOT NULL COMMENT '结束时间',
    `status`      VARCHAR(20)   NOT NULL DEFAULT 'NOT_STARTED' COMMENT '考试状态',
    `create_by`   BIGINT        NOT NULL COMMENT '创建人ID',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_paper_id` (`paper_id`),
    KEY `idx_course_id` (`course_id`),
    KEY `idx_class_id` (`class_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- -----------------------------------------------------------
-- 考试记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `exam_record` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_id`     BIGINT       NOT NULL COMMENT '考试ID',
    `user_id`     BIGINT       NOT NULL COMMENT '用户ID',
    `start_time`  DATETIME     DEFAULT NULL COMMENT '开始时间',
    `submit_time` DATETIME     DEFAULT NULL COMMENT '提交时间',
    `status`      VARCHAR(20)  NOT NULL DEFAULT 'STARTED' COMMENT '记录状态 STARTED/SUBMITTED/GRADED',
    `token`       VARCHAR(200) DEFAULT NULL COMMENT '考试Token',
    `total_score` INT          DEFAULT NULL COMMENT '总得分',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_exam_id` (`exam_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- -----------------------------------------------------------
-- 答题记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `answer` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_record_id`  BIGINT       NOT NULL COMMENT '考试记录ID',
    `question_id`     BIGINT       NOT NULL COMMENT '题目ID',
    `user_answer`     TEXT         DEFAULT NULL COMMENT '用户答案',
    `is_correct`      TINYINT      DEFAULT NULL COMMENT '是否正确 1-正确 0-错误',
    `score`           INT          DEFAULT NULL COMMENT '得分',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_exam_record_id` (`exam_record_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答题记录表';

-- -----------------------------------------------------------
-- 初始角色数据
-- -----------------------------------------------------------
INSERT INTO `role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '管理员', 'ADMIN', '系统管理员，拥有最高权限'),
(2, '教师',   'TEACHER', '教师，可管理课程、题目、试卷和考试'),
(3, '学生',   'STUDENT', '学生，可参加考试和查看成绩');
