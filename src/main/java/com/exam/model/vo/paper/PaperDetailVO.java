package com.exam.model.vo.paper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 试卷详情视图对象（含题目和规则）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaperDetailVO {

    /** 试卷ID */
    private Long id;

    /** 试卷名称 */
    private String paperName;

    /** 试卷类型 */
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

    /** 试卷题目列表（含分值和排序） */
    private List<PaperQuestionVO> questions;

    /** 随机组卷规则（仅paperType=2时有） */
    private List<PaperRuleVO> rules;
}
