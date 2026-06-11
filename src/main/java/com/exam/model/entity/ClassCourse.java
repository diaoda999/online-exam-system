package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 班级-课程关联实体（需修读课程）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("class_course")
public class ClassCourse {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级ID */
    private Long classId;

    /** 课程ID */
    private Long courseId;

    /** 添加人ID */
    private Long adderId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
