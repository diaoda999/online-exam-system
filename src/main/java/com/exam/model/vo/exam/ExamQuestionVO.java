package com.exam.model.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考试中的题目视图对象（不含正确答案和解析）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamQuestionVO {

    /** 题目ID */
    private Long questionId;

    /** 题干内容 */
    private String content;

    /** 题目类型 */
    private Integer questionType;

    /** 难度 */
    private Integer difficulty;

    /** 学科分类 */
    private String subject;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /** 选项E */
    private String optionE;

    /** 选项F */
    private String optionF;

    /** 选项G */
    private String optionG;

    /** 选项H */
    private String optionH;

    /** 该题分值 */
    private Integer score;

    /** 排序号 */
    private Integer sortOrder;
}
