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
import org.springframework.web.bind.annotation.*;

/**
 * 班级控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    public Result<Void> createClass(@Valid @RequestBody ClassCreateRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }
        classService.createClass(request, userId);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> updateClass(@PathVariable Long id,
                                    @RequestBody ClassUpdateRequest request,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }
        classService.updateClass(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteClass(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (!"ADMIN".equals(roleCode)) {
            ClassDetailVO classDetail = classService.getClassById(id);
            if (!"TEACHER".equals(roleCode) || !classDetail.getTeacherId().equals(userId)) {
                return Result.error(403, "无权操作");
            }
        }
        classService.deleteClass(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ClassDetailVO> getClassById(@PathVariable Long id) {
        return Result.success(classService.getClassById(id));
    }

    @GetMapping("/list")
    public Result<IPage<ClassVO>> listClasses(
            @RequestParam(required = false) Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(classService.listClasses(courseId, page, size));
    }
}
