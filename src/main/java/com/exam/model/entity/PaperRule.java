package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 随机组卷规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("paper_rule")
public class PaperRule {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 试卷ID */
    private Long paperId;

    /** 题目类型 */
    private Integer questionType;

    /** 难度 */
    private Integer difficulty;

    /** 题目数量 */
    private Integer questionCount;

    /** 每题分值 */
    private Integer scorePerQuestion;

    /** 限定题库ID（NULL表示全部） */
    private Long bankId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
