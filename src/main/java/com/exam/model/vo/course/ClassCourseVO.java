package com.exam.model.vo.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级需修读课程VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCourseVO {

    /** 记录ID */
    private Long id;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 课程编码 */
    private String courseCode;

    /** 添加人ID */
    private Long adderId;

    /** 添加人姓名 */
    private String adderName;

    /** 创建时间 */
    private LocalDateTime createTime;
}
