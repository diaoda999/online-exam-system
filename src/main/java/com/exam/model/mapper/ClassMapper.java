package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.ClassEntity;
import com.exam.model.vo.course.ClassVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 班级 Mapper 接口
 */
@Mapper
public interface ClassMapper extends BaseMapper<ClassEntity> {

    /**
     * 查询班级列表（含课程名+教师名+学生数）
     *
     * @param page     分页参数
     * @param courseId 课程ID
     * @return 分页班级视图列表
     */
    IPage<ClassVO> selectClassListWithDetails(IPage<ClassVO> page,
                                                @Param("courseId") Long courseId);
}
