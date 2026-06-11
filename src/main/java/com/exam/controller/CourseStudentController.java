package com.exam.controller;

import com.exam.common.result.Result;
import com.exam.model.vo.course.CourseStudentVO;
import com.exam.model.vo.user.UserVO;
import com.exam.service.CourseStudentService;
import com.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程学生管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/course-student")
@RequiredArgsConstructor
public class CourseStudentController {

    private final CourseStudentService courseStudentService;
    private final UserService userService;

    /**
     * 邀请学生加入课程（教师/管理员）
     */
    @PostMapping("/invite")
    public Result<Void> inviteStudents(@RequestBody InviteRequest request,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        Long userId = (Long) httpRequest.getAttribute("userId");
        courseStudentService.inviteStudents(request.getCourseId(), request.getStudentIds(), userId);
        return Result.success();
    }

    /**
     * 学生同意加入课程
     */
    @PostMapping("/{id}/accept")
    public Result<Void> acceptInvitation(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        courseStudentService.acceptInvitation(id, userId);
        return Result.success();
    }

    /**
     * 学生拒绝加入课程
     */
    @PostMapping("/{id}/reject")
    public Result<Void> rejectInvitation(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        courseStudentService.rejectInvitation(id, userId);
        return Result.success();
    }

    /**
     * 移除课程中的学生（教师/管理员）
     */
    @DeleteMapping("/remove")
    public Result<Void> removeStudent(@RequestBody RemoveRequest request,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        courseStudentService.removeStudent(request.getCourseId(), request.getStudentId());
        return Result.success();
    }

    /**
     * 获取课程的学生列表（教师/管理员）
     */
    @GetMapping("/course/{courseId}")
    public Result<List<CourseStudentVO>> getCourseStudents(@PathVariable Long courseId) {
        List<CourseStudentVO> list = courseStudentService.getCourseStudents(courseId);
        return Result.success(list);
    }

    /**
     * 获取当前学生的邀请列表
     */
    @GetMapping("/my-invitations")
    public Result<List<CourseStudentVO>> getMyInvitations(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<CourseStudentVO> list = courseStudentService.getStudentInvitations(userId);
        return Result.success(list);
    }

    /**
     * 获取当前学生的已加入课程
     */
    @GetMapping("/my-courses")
    public Result<List<CourseStudentVO>> getMyCourses(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<CourseStudentVO> list = courseStudentService.getStudentCourses(userId);
        return Result.success(list);
    }

    /**
     * 获取可选的学生列表（用于邀请选择器）
     */
    @GetMapping("/available-students")
    public Result<List<UserVO>> getAvailableStudents() {
        List<UserVO> students = userService.listUsers("STUDENT", 1, 1, 1000).getRecords();
        return Result.success(students);
    }

    /** 邀请请求 */
    @lombok.Data
    public static class InviteRequest {
        private Long courseId;
        private List<Long> studentIds;
    }

    /** 移除请求 */
    @lombok.Data
    public static class RemoveRequest {
        private Long courseId;
        private Long studentId;
    }
}
