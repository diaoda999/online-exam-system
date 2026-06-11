package com.exam.model.vo.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程学生邀请VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStudentVO {

    /** 记录ID */
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

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
