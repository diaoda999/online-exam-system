package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.question.QuestionQueryRequest;
import com.exam.model.entity.Question;
import com.exam.model.vo.question.QuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题目 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 分页查询题目列表（关联创建者名称和题库信息）
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页题目视图列表
     */
    IPage<QuestionVO> selectQuestionList(IPage<QuestionVO> page,
                                          @Param("query") QuestionQueryRequest query);
}
