package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.course.CourseCreateRequest;
import com.exam.model.dto.course.CourseUpdateRequest;
import com.exam.model.vo.course.CourseVO;
import com.exam.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 创建课程
     */
    @PostMapping
    public Result<Void> createCourse(@Valid @RequestBody CourseCreateRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以创建课程
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        courseService.createCourse(request, userId);
        return Result.success();
    }

    /**
     * 更新课程
     */
    @PutMapping("/{id}")
    public Result<Void> updateCourse(@PathVariable Long id,
                                     @RequestBody CourseUpdateRequest request,
                                     HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有课程，教师只能操作自己的课程
        if (!"ADMIN".equals(roleCode)) {
            CourseVO course = courseService.getCourseById(id);
            if (!"TEACHER".equals(roleCode) || !course.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        courseService.updateCourse(id, request);
        return Result.success();
    }

    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id,
                                     HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有课程，教师只能操作自己的课程
        if (!"ADMIN".equals(roleCode)) {
            CourseVO course = courseService.getCourseById(id);
            if (!"TEACHER".equals(roleCode) || !course.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        courseService.deleteCourse(id);
        return Result.success();
    }

    /**
     * 根据ID获取课程信息
     */
    @GetMapping("/{id}")
    public Result<CourseVO> getCourseById(@PathVariable Long id) {
        CourseVO courseVO = courseService.getCourseById(id);
        return Result.success(courseVO);
    }

    /**
     * 分页查询课程列表
     */
    @GetMapping("/list")
    public Result<IPage<CourseVO>> listCourses(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<CourseVO> result = courseService.listCourses(teacherId, page, size);
        return Result.success(result);
    }
}
