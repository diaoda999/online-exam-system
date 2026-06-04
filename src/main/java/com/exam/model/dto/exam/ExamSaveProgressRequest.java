package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存考试进度请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSaveProgressRequest {

    /** 考试Token */
    @NotBlank(message = "考试Token不能为空")
    private String examToken;

    /** 题目ID */
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 学生答案 */
    private String answer;
}
