package com.exam.model.dto.paper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建试卷请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperCreateRequest {

    /** 试卷名称 */
    @NotBlank(message = "试卷名称不能为空")
    private String paperName;

    /** 试卷类型：1-手工组卷 2-随机组卷 */
    @NotNull(message = "试卷类型不能为空")
    private Integer paperType;

    /** 总分 */
    @NotNull(message = "总分不能为空")
    private Integer totalScore;

    /** 及格分，默认60 */
    @NotNull(message = "及格分不能为空")
    @Builder.Default
    private Integer passScore = 60;

    /** 考试时长（分钟），默认120 */
    @NotNull(message = "考试时长不能为空")
    @Builder.Default
    private Integer duration = 120;

    /** 手工组卷题目列表（paperType=1时使用） */
    private List<PaperQuestionItem> questions;

    /** 随机组卷规则列表（paperType=2时使用） */
    private List<PaperRuleItem> rules;
}
