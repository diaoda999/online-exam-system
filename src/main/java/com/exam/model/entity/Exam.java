package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考试实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exam")
public class Exam {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试名称 */
    private String examName;

    /** 试卷ID */
    private Long paperId;

    /** 班级ID */
    private Long classId;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 考试状态 */
    private String status;

    /** 创建者ID */
    private Long creatorId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
