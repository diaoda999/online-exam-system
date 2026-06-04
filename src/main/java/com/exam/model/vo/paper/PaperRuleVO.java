package com.exam.model.vo.paper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随机组卷规则视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperRuleVO {

    /** 规则ID */
    private Long id;

    /** 题目类型 */
    private Integer questionType;

    /** 难度 */
    private Integer difficulty;

    /** 题目数量 */
    private Integer questionCount;

    /** 每题分值 */
    private Integer scorePerQuestion;

    /** 限定题库ID */
    private Long bankId;

    /** 题库名称 */
    private String bankName;
}
