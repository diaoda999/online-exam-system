package com.exam.model.vo.bank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 题库视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankVO {

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
}
