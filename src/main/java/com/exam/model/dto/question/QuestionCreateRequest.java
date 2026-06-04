package com.exam.model.dto.question;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建题目请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionCreateRequest {

    /** 题干内容 */
    @NotBlank(message = "题干内容不能为空")
    private String content;

    /** 题目类型：1单选 2多选 3判断 4填空 5简答 */
    @NotNull(message = "题目类型不能为空")
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
    @NotBlank(message = "正确答案不能为空")
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 难度：1-5，默认2 */
    @Builder.Default
    private Integer difficulty = 2;

    /** 学科分类 */
    private String subject;

    /** 所属题库ID列表（可选，创建时加入题库） */
    private List<Long> bankIds;
}
