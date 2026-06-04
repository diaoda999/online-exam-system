package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.ClassStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级-学生关联 Mapper 接口
 */
@Mapper
public interface ClassStudentMapper extends BaseMapper<ClassStudent> {
}
