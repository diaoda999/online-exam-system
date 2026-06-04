package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.course.CourseCreateRequest;
import com.exam.model.dto.course.CourseUpdateRequest;
import com.exam.model.entity.Course;
import com.exam.model.entity.User;
import com.exam.model.mapper.CourseMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.course.CourseVO;
import com.exam.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Override
    public void createCourse(CourseCreateRequest request, Long teacherId) {
        // 验证教师存在
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "教师不存在");
        }

        // 检查课程编码唯一
        Long count = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getCourseCode, request.getCourseCode())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "课程编码已存在");
        }

        Course course = Course.builder()
                .courseName(request.getCourseName())
                .courseCode(request.getCourseCode())
                .teacherId(teacherId)
                .description(request.getDescription())
                .build();
        courseMapper.insert(course);

        log.info("课程创建成功: courseName={}, teacherId={}", request.getCourseName(), teacherId);
    }

    @Override
    public void updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }

        // 检查课程编码唯一性（排除自身）
        if (request.getCourseCode() != null && !request.getCourseCode().equals(course.getCourseCode())) {
            Long count = courseMapper.selectCount(
                    new LambdaQueryWrapper<Course>()
                            .eq(Course::getCourseCode, request.getCourseCode())
                            .ne(Course::getId, id)
            );
            if (count > 0) {
                throw new BusinessException(ResultCode.CONFLICT, "课程编码已存在");
            }
        }

        if (request.getCourseName() != null) {
            course.setCourseName(request.getCourseName());
        }
        if (request.getCourseCode() != null) {
            course.setCourseCode(request.getCourseCode());
        }
        if (request.getDescription() != null) {
            course.setDescription(request.getDescription());
        }

        courseMapper.updateById(course);
        log.info("课程更新成功: courseId={}", id);
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        courseMapper.deleteById(id);
        log.info("课程删除: courseId={}", id);
    }

    @Override
    public CourseVO getCourseById(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "课程不存在");
        }
        return convertToVO(course);
    }

    @Override
    public IPage<CourseVO> listCourses(Long teacherId, int page, int size) {
        Page<CourseVO> pageParam = new Page<>(page, size);
        return courseMapper.selectCourseListWithTeacher(pageParam, teacherId);
    }

    /**
     * 将 Course 实体转换为 CourseVO
     */
    private CourseVO convertToVO(Course course) {
        User teacher = userMapper.selectById(course.getTeacherId());
        return CourseVO.builder()
                .id(course.getId())
                .courseName(course.getCourseName())
                .courseCode(course.getCourseCode())
                .teacherId(course.getTeacherId())
                .teacherName(teacher != null ? teacher.getRealName() : null)
                .description(course.getDescription())
                .createTime(course.getCreateTime())
                .build();
    }
}
