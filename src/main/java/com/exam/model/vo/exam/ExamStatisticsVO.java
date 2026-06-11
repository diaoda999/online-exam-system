package com.exam.model.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 考试统计视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamStatisticsVO {

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 应考人数 */
    private int totalStudents;

    /** 已提交人数 */
    private int submittedCount;

    /** 已批改人数 */
    private int gradedCount;

    /** 未提交人数 */
    private int notSubmittedCount;

    /** 平均分 */
    private Double averageScore;

    /** 最高分 */
    private Integer maxScore;

    /** 最低分 */
    private Integer minScore;

    /** 及格率(百分比，如 85.5) */
    private Double passRate;

    /** 分数段分布 (如 "90-100": 5, "80-89": 12, ...) */
    private Map<String, Integer> scoreDistribution;

    /** 考生成绩明细 (仅包含已批改的) */
    private List<StudentScoreVO> studentScores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentScoreVO {
        private Long userId;
        private String username;
        private String realName;
        private Integer totalScore;
        private Integer objectiveScore;
        private Integer subjectiveScore;
        private String status;
    }
}
