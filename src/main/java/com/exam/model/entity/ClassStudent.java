package com.exam.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 班级-学生关联实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("class_student")
public class ClassStudent {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 班级ID */
    private Long classId;

    /** 学生ID */
    private Long studentId;
}
