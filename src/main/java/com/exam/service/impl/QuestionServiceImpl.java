package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.question.QuestionCreateRequest;
import com.exam.model.dto.question.QuestionQueryRequest;
import com.exam.model.dto.question.QuestionUpdateRequest;
import com.exam.model.entity.Question;
import com.exam.model.entity.QuestionBank;
import com.exam.model.entity.QuestionBankItem;
import com.exam.model.entity.User;
import com.exam.model.mapper.QuestionBankItemMapper;
import com.exam.model.mapper.QuestionBankMapper;
import com.exam.model.mapper.QuestionMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.question.QuestionVO;
import com.exam.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 题目服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionBankMapper questionBankMapper;
    private final QuestionBankItemMapper questionBankItemMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionVO createQuestion(QuestionCreateRequest request, Long creatorId) {
        // 构建题目实体
        Question question = Question.builder()
                .content(request.getContent())
                .questionType(request.getQuestionType())
                .optionA(request.getOptionA())
                .optionB(request.getOptionB())
                .optionC(request.getOptionC())
                .optionD(request.getOptionD())
                .answer(request.getAnswer())
                .analysis(request.getAnalysis())
                .difficulty(request.getDifficulty() != null ? request.getDifficulty() : 2)
                .subject(request.getSubject())
                .creatorId(creatorId)
                .build();
        questionMapper.insert(question);

        // 如果传了 bankIds，自动添加到题库
        if (request.getBankIds() != null && !request.getBankIds().isEmpty()) {
            for (Long bankId : request.getBankIds()) {
                QuestionBank bank = questionBankMapper.selectById(bankId);
                if (bank == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在: " + bankId);
                }
                // 添加关联
                QuestionBankItem item = QuestionBankItem.builder()
                        .bankId(bankId)
                        .questionId(question.getId())
                        .build();
                questionBankItemMapper.insert(item);
                // 更新题库题目计数 +1
                bank.setQuestionCount(bank.getQuestionCount() != null ? bank.getQuestionCount() + 1 : 1);
                questionBankMapper.updateById(bank);
            }
        }

        log.info("题目创建成功: questionId={}, creatorId={}", question.getId(), creatorId);
        return getQuestionById(question.getId());
    }

    @Override
    public void updateQuestion(Long id, QuestionUpdateRequest request) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }

        if (request.getContent() != null) {
            question.setContent(request.getContent());
        }
        if (request.getQuestionType() != null) {
            question.setQuestionType(request.getQuestionType());
        }
        if (request.getOptionA() != null) {
            question.setOptionA(request.getOptionA());
        }
        if (request.getOptionB() != null) {
            question.setOptionB(request.getOptionB());
        }
        if (request.getOptionC() != null) {
            question.setOptionC(request.getOptionC());
        }
        if (request.getOptionD() != null) {
            question.setOptionD(request.getOptionD());
        }
        if (request.getAnswer() != null) {
            question.setAnswer(request.getAnswer());
        }
        if (request.getAnalysis() != null) {
            question.setAnalysis(request.getAnalysis());
        }
        if (request.getDifficulty() != null) {
            question.setDifficulty(request.getDifficulty());
        }
        if (request.getSubject() != null) {
            question.setSubject(request.getSubject());
        }

        questionMapper.updateById(question);
        log.info("题目更新成功: questionId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }

        // 删除题库关联，同时更新题库计数
        List<QuestionBankItem> items = questionBankItemMapper.selectList(
                new LambdaQueryWrapper<QuestionBankItem>().eq(QuestionBankItem::getQuestionId, id)
        );
        for (QuestionBankItem item : items) {
            QuestionBank bank = questionBankMapper.selectById(item.getBankId());
            if (bank != null && bank.getQuestionCount() != null && bank.getQuestionCount() > 0) {
                bank.setQuestionCount(bank.getQuestionCount() - 1);
                questionBankMapper.updateById(bank);
            }
            questionBankItemMapper.deleteById(item.getId());
        }

        // 删除试卷关联（paper_question）
        // 注意：这里不直接删除，试卷中的题目引用通过试卷管理处理
        // 但为了数据一致性，这里也清理
        // paper_question 的清理在 PaperService 中处理

        questionMapper.deleteById(id);
        log.info("题目删除: questionId={}", id);
    }

    @Override
    public QuestionVO getQuestionById(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在");
        }
        return convertToVO(question);
    }

    @Override
    public IPage<QuestionVO> listQuestions(QuestionQueryRequest query) {
        Page<QuestionVO> pageParam = new Page<>(query.getPage(), query.getSize());
        IPage<QuestionVO> result = questionMapper.selectQuestionList(pageParam, query);

        // 为每个题目填充 bankNames
        for (QuestionVO vo : result.getRecords()) {
            vo.setBankNames(getBankNamesByQuestionId(vo.getId()));
        }
        return result;
    }

    /**
     * 将 Question 实体转换为 QuestionVO
     */
    private QuestionVO convertToVO(Question question) {
        User creator = userMapper.selectById(question.getCreatorId());
        return QuestionVO.builder()
                .id(question.getId())
                .content(question.getContent())
                .questionType(question.getQuestionType())
                .optionA(question.getOptionA())
                .optionB(question.getOptionB())
                .optionC(question.getOptionC())
                .optionD(question.getOptionD())
                .answer(question.getAnswer())
                .analysis(question.getAnalysis())
                .difficulty(question.getDifficulty())
                .subject(question.getSubject())
                .creatorName(creator != null ? creator.getRealName() : null)
                .createTime(question.getCreateTime())
                .updateTime(question.getUpdateTime())
                .bankNames(getBankNamesByQuestionId(question.getId()))
                .build();
    }

    /**
     * 根据题目ID获取所属题库名称列表
     */
    private List<String> getBankNamesByQuestionId(Long questionId) {
        List<QuestionBankItem> items = questionBankItemMapper.selectList(
                new LambdaQueryWrapper<QuestionBankItem>().eq(QuestionBankItem::getQuestionId, questionId)
        );
        List<String> bankNames = new ArrayList<>();
        for (QuestionBankItem item : items) {
            QuestionBank bank = questionBankMapper.selectById(item.getBankId());
            if (bank != null) {
                bankNames.add(bank.getBankName());
            }
        }
        return bankNames;
    }
}
