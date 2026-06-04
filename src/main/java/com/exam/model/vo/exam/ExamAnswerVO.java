package com.exam.model.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 考试答案视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamAnswerVO {

    /** 答案记录ID */
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

    /** 正确答案 */
    private String correctAnswer;

    /** 学生答案 */
    private String studentAnswer;

    /** 得分 */
    private Integer score;

    /** 是否正确：0错 1对 */
    private Integer isCorrect;
}
