package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.constant.RedisKeyConstant;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.entity.Exam;
import com.exam.model.entity.ExamRecord;
import com.exam.model.entity.User;
import com.exam.model.mapper.ExamMapper;
import com.exam.model.mapper.ExamRecordMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.exam.ExamStatisticsVO;
import com.exam.service.ExamStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 成绩统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamStatisticsServiceImpl implements ExamStatisticsService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long STATS_CACHE_TTL_MINUTES = 10;

    @Override
    public ExamStatisticsVO getExamStatistics(Long examId) {
        // 尝试从 Redis 读取缓存
        String cacheKey = String.format(RedisKeyConstant.EXAM_STATS, examId);
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof ExamStatisticsVO) {
                log.debug("从Redis缓存获取考试统计: examId={}", examId);
                return (ExamStatisticsVO) cached;
            }
        } catch (Exception e) {
            log.warn("Redis读取统计缓存失败: examId={}", examId, e);
        }

        ExamStatisticsVO vo = computeStatistics(examId);

        // 写入 Redis 缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, vo,
                    STATS_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Redis写入统计缓存失败: examId={}", examId, e);
        }

        return vo;
    }

    @Override
    public ExamStatisticsVO getClassStatistics(Long classId, Long examId) {
        // 班级统计目前直接实时计算（可后续加缓存）
        return computeStatistics(examId);
    }

    @Override
    public void clearCache(Long examId) {
        String cacheKey = String.format(RedisKeyConstant.EXAM_STATS, examId);
        try {
            redisTemplate.delete(cacheKey);
            log.debug("清除考试统计缓存: examId={}", examId);
        } catch (Exception e) {
            log.warn("Redis清除统计缓存失败: examId={}", examId, e);
        }
    }

    /**
     * 核心统计计算逻辑
     */
    private ExamStatisticsVO computeStatistics(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 查询所有记录
        List<ExamRecord> allRecords = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
        );

        int totalStudents = allRecords.size();
        int submittedCount = 0;
        int gradedCount = 0;
        int notSubmittedCount = 0;

        List<Integer> validScores = new ArrayList<>();
        int maxScore = 0;
        int minScore = Integer.MAX_VALUE;
        double sumScore = 0;
        int passCount = 0;
        int passScore = 60; // 默认及格线

        // 分数段分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("90-100", 0);
        distribution.put("80-89", 0);
        distribution.put("70-79", 0);
        distribution.put("60-69", 0);
        distribution.put("0-59", 0);

        List<ExamStatisticsVO.StudentScoreVO> studentScores = new ArrayList<>();

        for (ExamRecord record : allRecords) {
            String status = record.getStatus();
            if (ExamStatusConstant.RECORD_SUBMITTED.equals(status)) {
                submittedCount++;
            } else if (ExamStatusConstant.RECORD_GRADED.equals(status)) {
                submittedCount++;
                gradedCount++;
            } else {
                notSubmittedCount++;
            }

            // 只统计已批改的成绩
            if (ExamStatusConstant.RECORD_GRADED.equals(status) && record.getTotalScore() != null) {
                int score = record.getTotalScore();
                validScores.add(score);
                sumScore += score;
                passCount += (score >= passScore ? 1 : 0);

                if (score > maxScore) maxScore = score;
                if (score < minScore) minScore = score;

                // 分数段统计
                if (score >= 90) distribution.merge("90-100", 1, Integer::sum);
                else if (score >= 80) distribution.merge("80-89", 1, Integer::sum);
                else if (score >= 70) distribution.merge("70-79", 1, Integer::sum);
                else if (score >= 60) distribution.merge("60-69", 1, Integer::sum);
                else distribution.merge("0-59", 1, Integer::sum);

                // 构建学生成绩明细
                User user = userMapper.selectById(record.getUserId());
                studentScores.add(ExamStatisticsVO.StudentScoreVO.builder()
                        .userId(record.getUserId())
                        .username(user != null ? user.getUsername() : "未知")
                        .realName(user != null ? user.getRealName() : "未知")
                        .totalScore(score)
                        .objectiveScore(record.getObjectiveScore())
                        .subjectiveScore(record.getSubjectiveScore())
                        .status(status)
                        .build());
            }
        }

        if (minScore == Integer.MAX_VALUE) minScore = 0;

        double averageScore = validScores.isEmpty() ? 0 : sumScore / validScores.size();
        // 保留一位小数
        averageScore = Math.round(averageScore * 10.0) / 10.0;

        double passRate = validScores.isEmpty() ? 0
                : Math.round((double) passCount / validScores.size() * 1000.0) / 10.0;

        return ExamStatisticsVO.builder()
                .examId(examId)
                .examName(exam.getExamName())
                .totalStudents(totalStudents)
                .submittedCount(submittedCount)
                .gradedCount(gradedCount)
                .notSubmittedCount(notSubmittedCount)
                .averageScore(averageScore)
                .maxScore(validScores.isEmpty() ? null : maxScore)
                .minScore(validScores.isEmpty() ? null : minScore)
                .passRate(passRate)
                .scoreDistribution(distribution)
                .studentScores(studentScores)
                .build();
    }
}
