package com.exam.model.vo.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionVO {

    /** 题目ID */
    private Long id;

    /** 题干内容 */
    private String content;

    /** 题目类型 */
    private Integer questionType;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /** 正确答案 */
    private String answer;

    /** 答案解析 */
    private String analysis;

    /** 难度 */
    private Integer difficulty;

    /** 学科分类 */
    private String subject;

    /** 创建者名称 */
    private String creatorName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 所属题库名称列表 */
    private List<String> bankNames;
}
