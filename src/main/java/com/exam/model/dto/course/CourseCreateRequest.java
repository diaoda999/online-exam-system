package com.exam.model.dto.course;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建课程请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateRequest {

    /** 课程名称 */
    @NotBlank(message = "课程名称不能为空")
    private String courseName;

    /** 课程编码 */
    @NotBlank(message = "课程编码不能为空")
    private String courseCode;

    /** 课程描述 */
    private String description;
}
