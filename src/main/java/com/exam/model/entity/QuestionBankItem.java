package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题库-题目关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question_bank_item")
public class QuestionBankItem {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题库ID */
    private Long bankId;

    /** 题目ID */
    private Long questionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
