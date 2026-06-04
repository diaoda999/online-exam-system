package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级实体（class 是 Java 关键字，使用 ClassEntity）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("class")
public class ClassEntity {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级名称 */
    private String className;

    /** 课程ID */
    private Long courseId;

    /** 教师ID */
    private Long teacherId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
