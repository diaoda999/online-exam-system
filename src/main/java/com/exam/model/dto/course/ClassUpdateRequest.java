package com.exam.model.dto.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 更新班级请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassUpdateRequest {

    /** 班级名称 */
    private String className;

    /** 学生ID列表 */
    private List<Long> studentIds;
}
