package com.exam.controller;

import com.exam.common.result.Result;
import com.exam.model.vo.course.ClassCourseVO;
import com.exam.model.vo.course.ClassStudentVO;
import com.exam.model.vo.user.UserVO;
import com.exam.service.ClassStudentService;
import com.exam.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级学生管理 + 班级课程管理 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/class-student")
@RequiredArgsConstructor
public class ClassStudentController {

    private final ClassStudentService classStudentService;
    private final UserService userService;

    // ===== 班级学生邀请 =====

    /** 邀请学生加入班级（教师/管理员） */
    @PostMapping("/invite")
    public Result<Void> inviteStudents(@RequestBody InviteRequest request,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        Long userId = (Long) httpRequest.getAttribute("userId");
        classStudentService.inviteStudents(request.getClassId(), request.getStudentIds(), userId);
        return Result.success();
    }

    /** 学生同意加入班级 */
    @PostMapping("/{id}/accept")
    public Result<Void> acceptInvitation(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        classStudentService.acceptInvitation(id, userId);
        return Result.success();
    }

    /** 学生拒绝加入班级 */
    @PostMapping("/{id}/reject")
    public Result<Void> rejectInvitation(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        classStudentService.rejectInvitation(id, userId);
        return Result.success();
    }

    /** 移除班级中的学生（教师/管理员） */
    @DeleteMapping("/remove")
    public Result<Void> removeStudent(@RequestBody RemoveRequest request,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        classStudentService.removeStudent(request.getClassId(), request.getStudentId());
        return Result.success();
    }

    /** 获取班级的学生列表（教师/管理员） */
    @GetMapping("/class/{classId}")
    public Result<List<ClassStudentVO>> getClassStudents(@PathVariable Long classId) {
        List<ClassStudentVO> list = classStudentService.getClassStudents(classId);
        return Result.success(list);
    }

    /** 获取当前学生的班级邀请列表 */
    @GetMapping("/my-invitations")
    public Result<List<ClassStudentVO>> getMyInvitations(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ClassStudentVO> list = classStudentService.getStudentInvitations(userId);
        return Result.success(list);
    }

    /** 获取当前学生的已加入班级 */
    @GetMapping("/my-classes")
    public Result<List<ClassStudentVO>> getMyClasses(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ClassStudentVO> list = classStudentService.getStudentClasses(userId);
        return Result.success(list);
    }

    /** 获取可选的学生列表（用于邀请选择器） */
    @GetMapping("/available-students")
    public Result<List<UserVO>> getAvailableStudents() {
        List<UserVO> students = userService.listUsers("STUDENT", 1, 1, 1000).getRecords();
        return Result.success(students);
    }

    // ===== 班级需修读课程 =====

    /** 添加课程到班级需修读（教师/管理员） */
    @PostMapping("/class-course/add")
    public Result<Void> addCourseToClass(@RequestBody ClassCourseRequest request,
                                          HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        Long userId = (Long) httpRequest.getAttribute("userId");
        classStudentService.addCourseToClass(request.getClassId(), request.getCourseId(), userId);
        return Result.success();
    }

    /** 从班级需修读移除课程（教师/管理员） */
    @DeleteMapping("/class-course/remove")
    public Result<Void> removeCourseFromClass(@RequestBody ClassCourseRequest request,
                                               HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        classStudentService.removeCourseFromClass(request.getClassId(), request.getCourseId());
        return Result.success();
    }

    /** 获取班级的需修读课程列表 */
    @GetMapping("/class-course/class/{classId}")
    public Result<List<ClassCourseVO>> getClassCourses(@PathVariable Long classId) {
        List<ClassCourseVO> list = classStudentService.getClassCourses(classId);
        return Result.success(list);
    }

    /** 获取所有班级的需修读课程（所有人可见） */
    @GetMapping("/class-course/all")
    public Result<List<ClassCourseVO>> getAllClassCourses() {
        List<ClassCourseVO> list = classStudentService.getAllClassCourses();
        return Result.success(list);
    }

    // ===== 内部请求类 =====

    @lombok.Data
    public static class InviteRequest {
        private Long classId;
        private List<Long> studentIds;
    }

    @lombok.Data
    public static class RemoveRequest {
        private Long classId;
        private Long studentId;
    }

    @lombok.Data
    public static class ClassCourseRequest {
        private Long classId;
        private Long courseId;
    }
}
