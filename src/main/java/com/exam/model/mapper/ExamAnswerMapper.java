package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.ExamAnswer;
import com.exam.model.vo.exam.ExamAnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考试答案 Mapper 接口
 */
@Mapper
public interface ExamAnswerMapper extends BaseMapper<ExamAnswer> {

    /**
     * 根据记录ID查询答案列表（关联题目信息）
     *
     * @param recordId 记录ID
     * @return 答案视图列表
     */
    List<ExamAnswerVO> selectAnswerListByRecordId(@Param("recordId") Long recordId);
}
