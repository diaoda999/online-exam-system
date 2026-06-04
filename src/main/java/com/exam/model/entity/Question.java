package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("question")
public class Question {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题干内容 */
    private String content;

    /** 题目类型：1单选 2多选 3判断 4填空 5简答 */
    private Integer questionType;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /** 正确答案 */
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 难度：1-5 */
    private Integer difficulty;

    /** 学科分类 */
    private String subject;

    /** 创建者ID */
    private Long creatorId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
