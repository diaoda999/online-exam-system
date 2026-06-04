package com.exam.model.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试记录详情视图对象（含答题详情）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamRecordDetailVO {

    /** 记录ID */
    private Long id;

    /** 考试ID */
    private Long examId;

    /** 考试名称 */
    private String examName;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 提交时间 */
    private LocalDateTime submitTime;

    /** 记录状态 */
    private String status;

    /** 总分，-1未批改 */
    private Integer totalScore;

    /** 客观题得分 */
    private Integer objectiveScore;

    /** 主观题得分 */
    private Integer subjectiveScore;

    /** 答题详情列表 */
    private List<ExamAnswerVO> answers;
}
