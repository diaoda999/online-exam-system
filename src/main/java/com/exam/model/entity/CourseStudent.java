package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 课程-学生关联实体（邀请制）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("course_student")
public class CourseStudent {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 课程ID */
    private Long courseId;

    /** 学生ID */
    private Long studentId;

    /** 状态: PENDING-待确认, ACCEPTED-已加入, REJECTED-已拒绝 */
    private String status;

    /** 邀请人ID */
    private Long inviterId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
