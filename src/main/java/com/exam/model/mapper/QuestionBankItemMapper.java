package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.QuestionBankItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 题库-题目关联 Mapper 接口
 */
@Mapper
public interface QuestionBankItemMapper extends BaseMapper<QuestionBankItem> {
}
