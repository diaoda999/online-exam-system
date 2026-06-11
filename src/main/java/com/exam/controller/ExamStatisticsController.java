package com.exam.controller;

import com.exam.common.result.Result;
import com.exam.model.vo.exam.ExamStatisticsVO;
import com.exam.service.ExamStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩统计控制器
 */
@RestController
@RequestMapping("/api/exam-statistics")
@RequiredArgsConstructor
public class ExamStatisticsController {

    private final ExamStatisticsService statisticsService;

    /**
     * 获取考试统计信息
     * 包含：平均分、最高分、最低分、及格率、分数段分布、学生成绩明细
     * 结果缓存到 Redis，TTL 10分钟
     */
    @GetMapping("/exam/{examId}")
    public Result<ExamStatisticsVO> getExamStatistics(@PathVariable Long examId) {
        return Result.success(statisticsService.getExamStatistics(examId));
    }

    /**
     * 获取班级某次考试的统计
     */
    @GetMapping("/class/{classId}/exam/{examId}")
    public Result<ExamStatisticsVO> getClassStatistics(
            @PathVariable Long classId,
            @PathVariable Long examId) {
        return Result.success(statisticsService.getClassStatistics(classId, examId));
    }

    /**
     * 清除统计缓存（批改完成或教师手动刷新时调用）
     */
    @DeleteMapping("/cache/{examId}")
    public Result<Void> clearCache(@PathVariable Long examId) {
        statisticsService.clearCache(examId);
        return Result.success();
    }
}
