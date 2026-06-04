package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.model.dto.question.QuestionCreateRequest;
import com.exam.model.dto.question.QuestionQueryRequest;
import com.exam.model.dto.question.QuestionUpdateRequest;
import com.exam.model.vo.question.QuestionVO;

/**
 * 题目服务接口
 */
public interface QuestionService {

    /**
     * 创建题目
     *
     * @param request   创建题目请求
     * @param creatorId 创建者ID
     * @return 题目视图对象
     */
    QuestionVO createQuestion(QuestionCreateRequest request, Long creatorId);

    /**
     * 更新题目
     *
     * @param id      题目ID
     * @param request 更新请求
     */
    void updateQuestion(Long id, QuestionUpdateRequest request);

    /**
     * 删除题目
     *
     * @param id 题目ID
     */
    void deleteQuestion(Long id);

    /**
     * 根据ID获取题目信息
     *
     * @param id 题目ID
     * @return 题目视图对象
     */
    QuestionVO getQuestionById(Long id);

    /**
     * 分页查询题目列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<QuestionVO> listQuestions(QuestionQueryRequest query);
}
