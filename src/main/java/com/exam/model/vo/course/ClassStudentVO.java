package com.exam.model.vo.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级学生邀请VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassStudentVO {

    /** 记录ID */
    private Long id;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String studentName;

    /** 学生用户名 */
    private String studentUsername;

    /** 状态: PENDING/ACCEPTED/REJECTED */
    private String status;

    /** 邀请人ID */
    private Long inviterId;

    /** 邀请人姓名 */
    private String inviterName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
