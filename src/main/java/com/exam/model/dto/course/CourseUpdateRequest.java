package com.exam.model.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新课程请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseUpdateRequest {

    /** 课程名称 */
    private String courseName;

    /** 课程编码 */
    private String courseCode;

    /** 课程描述 */
    private String description;
}
