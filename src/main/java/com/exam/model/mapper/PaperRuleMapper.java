package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.model.entity.PaperRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 随机组卷规则 Mapper 接口
 */
@Mapper
public interface PaperRuleMapper extends BaseMapper<PaperRule> {
}
