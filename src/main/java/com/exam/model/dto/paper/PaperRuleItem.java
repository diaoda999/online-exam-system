package com.exam.model.dto.paper;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 随机组卷规则项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperRuleItem {

    /** 题目类型 */
    @NotNull(message = "题目类型不能为空")
    private Integer questionType;

    /** 难度 */
    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    /** 题目数量 */
    @NotNull(message = "题目数量不能为空")
    private Integer questionCount;

    /** 每题分值 */
    @NotNull(message = "每题分值不能为空")
    private Integer scorePerQuestion;

    /** 限定题库ID（NULL表示全部） */
    private Long bankId;
}
