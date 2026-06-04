package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题库实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_bank")
public class QuestionBank {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题库名称 */
    private String bankName;

    /** 题库描述 */
    private String description;

    /** 创建者ID */
    private Long creatorId;

    /** 题目数量 */
    private Integer questionCount;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
