package com.exam.model.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建班级请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCreateRequest {

    /** 班级名称 */
    @NotBlank(message = "班级名称不能为空")
    private String className;

    /** 课程ID */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /** 学生ID列表 */
    private List<Long> studentIds;
}
