package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.Exam;
import com.exam.model.vo.exam.ExamVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 考试 Mapper 接口
 */
@Mapper
public interface ExamMapper extends BaseMapper<Exam> {

    /**
     * 分页查询考试列表（关联试卷名/班级名/创建者名/提交人数）
     *
     * @param page      分页参数
     * @param creatorId 创建者ID（可选）
     * @param status    考试状态（可选）
     * @return 分页考试视图列表
     */
    IPage<ExamVO> selectExamList(IPage<ExamVO> page,
                                   @Param("creatorId") Long creatorId,
                                   @Param("status") String status);
}
