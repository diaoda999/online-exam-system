package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考试记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exam_record")
public class ExamRecord {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 用户ID */
    private Long userId;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 记录状态 */
    private String status;

    /** 总分，-1未批改 */
    private Integer totalScore;

    /** 客观题得分，-1未批改 */
    private Integer objectiveScore;

    /** 主观题得分，-1未批改 */
    private Integer subjectiveScore;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
