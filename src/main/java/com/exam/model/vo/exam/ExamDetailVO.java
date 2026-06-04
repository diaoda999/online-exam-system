package com.exam.model.vo.exam;

import com.exam.model.vo.paper.PaperDetailVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 考试详情视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamDetailVO {

    /** 考试ID */
    private Long id;

    /** 考试名称 */
    private String examName;

    /** 试卷ID */
    private Long paperId;

    /** 试卷名称 */
    private String paperName;

    /** 班级ID */
    private Long classId;

    /** 班级名称 */
    private String className;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 考试状态 */
    private String status;

    /** 创建者名称 */
    private String creatorName;

    /** 班级学生数 */
    private Integer studentCount;

    /** 已提交人数 */
    private Integer submittedCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 试卷完整信息 */
    private PaperDetailVO paper;
}
