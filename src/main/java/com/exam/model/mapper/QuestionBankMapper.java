package com.exam.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.entity.QuestionBank;
import com.exam.model.vo.bank.BankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 题库 Mapper 接口
 */
@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 分页查询题库列表（关联创建者名称）
     *
     * @param page    分页参数
     * @param keyword 关键词
     * @return 分页题库视图列表
     */
    IPage<BankVO> selectBankListWithCreator(IPage<BankVO> page,
                                              @Param("keyword") String keyword);
}
