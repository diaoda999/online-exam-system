package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.course.CourseCreateRequest;
import com.exam.model.dto.course.CourseUpdateRequest;
import com.exam.model.vo.course.CourseVO;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param request   创建课程请求
     * @param teacherId 教师ID
     */
    void createCourse(CourseCreateRequest request, Long teacherId);

    /**
     * 更新课程
     *
     * @param id      课程ID
     * @param request 更新请求
     */
    void updateCourse(Long id, CourseUpdateRequest request);

    /**
     * 删除课程
     *
     * @param id 课程ID
     */
    void deleteCourse(Long id);

    /**
     * 根据ID获取课程信息
     *
     * @param id 课程ID
     * @return 课程视图对象
     */
    CourseVO getCourseById(Long id);

    /**
     * 分页查询课程列表
     *
     * @param teacherId 教师ID（可选）
     * @param page      页码
     * @param size      每页数量
     * @return 分页结果
     */
    IPage<CourseVO> listCourses(Long teacherId, int page, int size);
}
