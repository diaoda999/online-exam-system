package com.exam.model.dto.exam;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 创建考试请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateRequest {

    /** 考试名称 */
    @NotBlank(message = "考试名称不能为空")
    private String examName;

    /** 试卷ID */
    @NotNull(message = "试卷ID不能为空")
    private Long paperId;

    /** 班级ID */
    @NotNull(message = "班级ID不能为空")
    private Long classId;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 结束时间 */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /** 考试时长（分钟） */
    @NotNull(message = "考试时长不能为空")
    private Integer duration;
}
