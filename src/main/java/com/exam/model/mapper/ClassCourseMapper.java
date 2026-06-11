package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.ClassCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级-课程关联 Mapper
 */
@Mapper
public interface ClassCourseMapper extends BaseMapper<ClassCourse> {
}
