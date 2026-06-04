package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考试答案实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("exam_answer")
public class ExamAnswer {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 考试记录ID */
    private Long recordId;

    /** 题目ID */
    private Long questionId;

    /** 学生答案 */
    private String answer;

    /** 得分，-1未批改 */
    private Integer score;

    /** 是否正确：0错 1对，仅客观题 */
    private Integer isCorrect;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
