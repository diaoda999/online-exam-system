package com.exam.model.vo.course;

import com.exam.model.vo.user.UserVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级详情视图对象（含学生列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDetailVO {

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

    /** 学生列表 */
    private List<UserVO> students;
}
