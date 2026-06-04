package com.exam.model.vo.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 进入考试返回视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamEnterVO {

    /** 考试Token */
    private String examToken;

    /** 考试名称 */
    private String examName;

    /** 考试时长（分钟） */
    private Integer duration;

    /** 剩余秒数 */
    private Long remainingSeconds;

    /** 考试题目列表（不含正确答案和解析） */
    private List<ExamQuestionVO> questions;
}
