package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.course.ClassCreateRequest;
import com.exam.model.dto.course.ClassUpdateRequest;
import com.exam.model.vo.course.ClassDetailVO;
import com.exam.model.vo.course.ClassVO;
import com.exam.service.ClassService;
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

import java.util.List;

/**
 * 班级控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    /**
     * 创建班级
     */
    @PostMapping
    public Result<Void> createClass(@Valid @RequestBody ClassCreateRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 只有教师和管理员可以创建班级
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        classService.createClass(request, userId);
        return Result.success();
    }

    /**
     * 更新班级
     */
    @PutMapping("/{id}")
    public Result<Void> updateClass(@PathVariable Long id,
                                    @RequestBody ClassUpdateRequest request,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有班级，教师只能操作自己的班级
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        classService.updateClass(id, request);
        return Result.success();
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteClass(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有班级，教师只能操作自己的班级
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        classService.deleteClass(id);
        return Result.success();
    }

    /**
     * 根据ID获取班级详情
     */
    @GetMapping("/{id}")
    public Result<ClassDetailVO> getClassById(@PathVariable Long id) {
        ClassDetailVO classDetailVO = classService.getClassById(id);
        return Result.success(classDetailVO);
    }

    /**
     * 分页查询班级列表
     */
    @GetMapping("/list")
    public Result<IPage<ClassVO>> listClasses(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<ClassVO> result = classService.listClasses(courseId, page, size);
        return Result.success(result);
    }

    /**
     * 添加学生到班级
     */
    @PostMapping("/{id}/students")
    public Result<Void> addStudents(@PathVariable Long id,
                                    @RequestBody List<Long> studentIds,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有班级，教师只能操作自己的班级
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        classService.addStudents(id, studentIds);
        return Result.success();
    }

    /**
     * 从班级移除学生
     */
    @DeleteMapping("/{id}/students")
    public Result<Void> removeStudents(@PathVariable Long id,
                                       @RequestBody List<Long> studentIds,
                                       HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");

        // 管理员可操作所有班级，教师只能操作自己的班级
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }

        classService.removeStudents(id, studentIds);
        return Result.success();
    }
}
