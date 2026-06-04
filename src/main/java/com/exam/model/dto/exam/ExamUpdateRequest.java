package com.exam.model.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新考试请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamUpdateRequest {

    /** 考试名称 */
    private String examName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 考试时长（分钟） */
    private Integer duration;
}
