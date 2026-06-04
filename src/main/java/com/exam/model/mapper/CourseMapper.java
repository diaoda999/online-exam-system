package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.Course;
import com.exam.model.vo.course.CourseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程 Mapper 接口
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 查询课程列表（含教师名称）
     *
     * @param page      分页参数
     * @param teacherId 教师ID
     * @return 分页课程视图列表
     */
    IPage<CourseVO> selectCourseListWithTeacher(IPage<CourseVO> page,
                                                  @Param("teacherId") Long teacherId);
}
