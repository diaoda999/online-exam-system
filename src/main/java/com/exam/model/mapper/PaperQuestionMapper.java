package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.PaperQuestion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 试卷-题目关联 Mapper 接口
 */
@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {
}
