package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 提交考试请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmitRequest {

    /** 考试Token */
    @NotBlank(message = "考试Token不能为空")
    private String examToken;

    /** 题目答案列表 */
    private List<AnswerItem> answers;
}
