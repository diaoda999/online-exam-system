package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.exam.ExamCreateRequest;
import com.exam.model.dto.exam.ExamSaveProgressRequest;
import com.exam.model.dto.exam.ExamSubmitRequest;
import com.exam.model.dto.exam.ExamUpdateRequest;
import com.exam.model.vo.exam.ExamDetailVO;
import com.exam.model.vo.exam.ExamEnterVO;
import com.exam.model.vo.exam.ExamRecordVO;
import com.exam.model.vo.exam.ExamVO;
import com.exam.service.ExamRecordService;
import com.exam.service.ExamService;
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
 * 考试控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final ExamRecordService examRecordService;

    /**
     * 创建考试
     * 仅教师和管理员可操作
     */
    @PostMapping
    public Result<ExamVO> createExam(@Valid @RequestBody ExamCreateRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        ExamVO examVO = examService.createExam(request, userId);
        return Result.success(examVO);
    }

    /**
     * 更新考试
     * 只有未开始的考试才能修改
     */
    @PutMapping("/{id}")
    public Result<Void> updateExam(@PathVariable Long id,
                                    @RequestBody ExamUpdateRequest request,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examService.updateExam(id, request);
        return Result.success();
    }

    /**
     * 删除考试
     * 只有未开始的考试才能删除
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteExam(@PathVariable Long id,
                                    HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examService.deleteExam(id);
        return Result.success();
    }

    /**
     * 根据ID获取考试详情
     */
    @GetMapping("/{id}")
    public Result<ExamDetailVO> getExamById(@PathVariable Long id) {
        ExamDetailVO examDetail = examService.getExamById(id);
        return Result.success(examDetail);
    }

    /**
     * 分页查询考试列表
     * 管理员可查看全部，教师只看自己创建的，学生查看已参加的
     */
    @GetMapping("/list")
    public Result<IPage<ExamVO>> listExams(
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        // 教师只能查看自己创建的考试
        if ("TEACHER".equals(roleCode)) {
            creatorId = userId;
        }

        IPage<ExamVO> result = examService.listExams(creatorId, status, page, size);
        return Result.success(result);
    }

    /**
     * 发布考试
     * 发布后创建学生考试记录，随机组卷会抽题
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publishExam(@PathVariable Long id,
                                     HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examService.publishExam(id);
        return Result.success();
    }

    /**
     * 学生进入考试
     * 返回考试Token、剩余时间、题目列表（不含答案）
     */
    @PostMapping("/{id}/enter")
    public Result<ExamEnterVO> enterExam(@PathVariable Long id,
                                           HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ExamEnterVO enterVO = examService.enterExam(id, userId);
        return Result.success(enterVO);
    }

    /**
     * 保存单题答题进度到Redis
     * 需要考试Token验证
     */
    @PostMapping("/progress")
    public Result<Void> saveProgress(@Valid @RequestBody ExamSaveProgressRequest request,
                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        examService.saveProgress(request, userId);
        return Result.success();
    }

    /**
     * 提交考试
     * 需要考试Token验证，合并Redis缓存答案与请求答案
     */
    @PostMapping("/submit")
    public Result<Void> submitExam(@Valid @RequestBody ExamSubmitRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        examService.submitExam(request, userId);
        return Result.success();
    }

    /**
     * 获取考试剩余时间（秒）
     */
    @GetMapping("/{id}/remaining")
    public Result<Long> getRemainingTime(@PathVariable Long id,
                                          HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Long remaining = examService.getRemainingTime(id, userId);
        return Result.success(remaining);
    }

    /**
     * 结束考试
     * 教师或管理员操作，对未提交学生触发MQ自动提交
     */
    @PostMapping("/{id}/end")
    public Result<Void> endExam(@PathVariable Long id,
                                 HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examService.endExam(id);
        return Result.success();
    }

    /**
     * 查询当前学生在指定考试的记录
     */
    @GetMapping("/{id}/record")
    public Result<ExamRecordVO> getMyRecord(@PathVariable Long id,
                                             HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ExamRecordVO record = examRecordService.getRecordByExamAndUser(id, userId);
        return Result.success(record);
    }
}
