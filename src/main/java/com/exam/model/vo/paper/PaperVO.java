package com.exam.model.vo.paper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 试卷视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperVO {

    /** 试卷ID */
    private Long id;

    /** 试卷名称 */
    private String paperName;

    /** 试卷类型：1-手工组卷 2-随机组卷 */
    private Integer paperType;

    /** 总分 */
    private Integer totalScore;

    /** 及格分 */
    private Integer passScore;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 创建者名称 */
    private String creatorName;

    /** 题目数量 */
    private Integer questionCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
