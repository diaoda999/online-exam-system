package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.result.Result;
import com.exam.model.dto.exam.GradeRequest;
import com.exam.model.vo.exam.ExamRecordDetailVO;
import com.exam.model.vo.exam.ExamRecordVO;
import com.exam.service.ExamRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 考试记录控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/exam-record")
@RequiredArgsConstructor
public class ExamRecordController {

    private final ExamRecordService examRecordService;

    /**
     * 分页查询考试记录列表
     * 教师和管理员可查看
     */
    @GetMapping("/list")
    public Result<IPage<ExamRecordVO>> listRecords(
            @RequestParam Long examId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        IPage<ExamRecordVO> result = examRecordService.listRecords(examId, status, page, size);
        return Result.success(result);
    }

    /**
     * 获取记录详情（含答案列表）
     * 教师和管理员可查看
     */
    @GetMapping("/{id}")
    public Result<ExamRecordDetailVO> getRecordDetail(@PathVariable Long id,
                                                        HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        ExamRecordDetailVO detail = examRecordService.getRecordDetail(id);
        return Result.success(detail);
    }

    /**
     * 自动批改客观题
     * 对指定考试所有已提交的记录，自动批改单选/多选/判断题
     */
    @PostMapping("/{examId}/grade-objective")
    public Result<Void> gradeObjective(@PathVariable Long examId,
                                        HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examRecordService.gradeObjective(examId);
        return Result.success();
    }

    /**
     * 手动批改主观题
     * 教师逐题给分，系统自动重新计算总分并判断是否全部批改完成
     */
    @PostMapping("/grade-subjective")
    public Result<Void> gradeSubjective(@Valid @RequestBody GradeRequest request,
                                         HttpServletRequest httpRequest) {
        String roleCode = (String) httpRequest.getAttribute("roleCode");

        if (!"ADMIN".equals(roleCode) && !"TEACHER".equals(roleCode)) {
            return Result.error(403, "无权操作");
        }

        examRecordService.gradeSubjective(request);
        return Result.success();
    }
}
