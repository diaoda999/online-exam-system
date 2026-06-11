package com.exam.service;

import com.exam.model.vo.exam.ExamStatisticsVO;

/**
 * 成绩统计服务接口
 */
public interface ExamStatisticsService {

    /**
     * 获取考试统计信息（平均分/最高分/最低分/及格率/分数分布）
     * 结果缓存到 Redis，TTL 10分钟
     */
    ExamStatisticsVO getExamStatistics(Long examId);

    /**
     * 获取班级考试统计汇总
     */
    ExamStatisticsVO getClassStatistics(Long classId, Long examId);

    /**
     * 清除缓存（批改完成后调用）
     */
    void clearCache(Long examId);
}
