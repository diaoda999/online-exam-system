package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批改项DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeItem {

    /** 答案记录ID */
    @NotNull(message = "答案记录ID不能为空")
    private Long answerId;

    /** 得分 */
    @NotNull(message = "得分不能为空")
    private Integer score;

    /** 是否正确：0错 1对 */
    private Integer isCorrect;
}
