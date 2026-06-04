package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.bank.BankCreateRequest;
import com.exam.model.dto.bank.BankUpdateRequest;
import com.exam.model.vo.bank.BankDetailVO;
import com.exam.model.vo.bank.BankVO;

import java.util.List;

/**
 * 题库服务接口
 */
public interface QuestionBankService {

    /**
     * 创建题库
     *
     * @param request   创建题库请求
     * @param creatorId 创建者ID
     * @return 题库视图对象
     */
    BankVO createBank(BankCreateRequest request, Long creatorId);

    /**
     * 更新题库
     *
     * @param id      题库ID
     * @param request 更新请求
     */
    void updateBank(Long id, BankUpdateRequest request);

    /**
     * 删除题库
     *
     * @param id 题库ID
     */
    void deleteBank(Long id);

    /**
     * 根据ID获取题库详情
     *
     * @param id 题库ID
     * @return 题库详情视图对象
     */
    BankDetailVO getBankById(Long id);

    /**
     * 分页查询题库列表
     *
     * @param keyword 关键词（可选）
     * @param page    页码
     * @param size    每页数量
     * @return 分页结果
     */
    IPage<BankVO> listBanks(String keyword, int page, int size);

    /**
     * 添加题目到题库
     *
     * @param bankId      题库ID
     * @param questionIds 题目ID列表
     */
    void addQuestions(Long bankId, List<Long> questionIds);

    /**
     * 从题库移除题目
     *
     * @param bankId      题库ID
     * @param questionIds 题目ID列表
     */
    void removeQuestions(Long bankId, List<Long> questionIds);
}
