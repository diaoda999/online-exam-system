package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("paper")
public class Paper {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
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

    /** 创建者ID */
    private Long creatorId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
