package com.exam.model.vo.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVO {

    /** 班级ID */
    private Long id;

    /** 班级名称 */
    private String className;

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 教师ID */
    private Long teacherId;

    /** 教师姓名 */
    private String teacherName;

    /** 学生人数 */
    private Integer studentCount;

    /** 创建时间 */
    private LocalDateTime createTime;
}
