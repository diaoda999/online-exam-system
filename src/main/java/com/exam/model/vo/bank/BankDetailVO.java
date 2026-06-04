package com.exam.model.vo.bank;

import com.exam.model.vo.question.QuestionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题库详情视图对象（含题目列表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailVO {

    /** 题库ID */
    private Long id;

    /** 题库名称 */
    private String bankName;

    /** 题库描述 */
    private String description;

    /** 创建者名称 */
    private String creatorName;

    /** 题目数量 */
    private Integer questionCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 题库中的题目列表（简化版，不含analysis和answer） */
    private List<QuestionVO> questions;
}
