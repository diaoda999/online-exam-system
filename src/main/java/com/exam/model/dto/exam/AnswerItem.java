package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 答案项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerItem {

    /** 题目ID */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 学生答案 */
    private String answer;
}
