package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.CourseStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程-学生关联 Mapper 接口
 */
@Mapper
public interface CourseStudentMapper extends BaseMapper<CourseStudent> {
}
