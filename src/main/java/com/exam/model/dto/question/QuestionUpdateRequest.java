package com.exam.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新题目请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionUpdateRequest {

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
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 难度 */
    private Integer difficulty;

    /** 学科分类 */
    private String subject;
}
