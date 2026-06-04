package com.exam.model.dto.paper;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 试卷题目项DTO（手工组卷用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperQuestionItem {

    /** 题目ID */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 该题分值 */
    @NotNull(message = "题目分值不能为空")
    private Integer score;

    /** 排序号 */
    private Integer sortOrder;
}
