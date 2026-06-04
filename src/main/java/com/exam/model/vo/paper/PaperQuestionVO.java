package com.exam.model.vo.paper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试卷题目视图对象（含题目详情和分值）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperQuestionVO {

    /** 试卷题目关联ID */
    private Long id;

    /** 题目ID */
    private Long questionId;

    /** 题干内容 */
    private String content;

    /** 题目类型 */
    private Integer questionType;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /** 该题分值 */
    private Integer score;

    /** 排序号 */
    private Integer sortOrder;
}
