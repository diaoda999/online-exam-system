package com.exam.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionQueryRequest {

    /** 题目类型 */
    private Integer questionType;

    /** 难度 */
    private Integer difficulty;

    /** 学科分类 */
    private String subject;

    /** 关键词搜索（搜索题干内容） */
    private String keyword;

    /** 题库ID */
    private Long bankId;

    /** 页码，默认1 */
    @Builder.Default
    private int page = 1;

    /** 每页数量，默认10 */
    @Builder.Default
    private int size = 10;
}
