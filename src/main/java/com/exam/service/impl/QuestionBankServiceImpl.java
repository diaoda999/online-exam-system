package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.bank.BankCreateRequest;
import com.exam.model.dto.bank.BankUpdateRequest;
import com.exam.model.entity.Question;
import com.exam.model.entity.QuestionBank;
import com.exam.model.entity.QuestionBankItem;
import com.exam.model.entity.User;
import com.exam.model.mapper.QuestionBankItemMapper;
import com.exam.model.mapper.QuestionBankMapper;
import com.exam.model.mapper.QuestionMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.bank.BankDetailVO;
import com.exam.model.vo.bank.BankVO;
import com.exam.model.vo.question.QuestionVO;
import com.exam.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 题库服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private final QuestionBankMapper questionBankMapper;
    private final QuestionBankItemMapper questionBankItemMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;

    @Override
    public BankVO createBank(BankCreateRequest request, Long creatorId) {
        QuestionBank bank = QuestionBank.builder()
                .bankName(request.getBankName())
                .description(request.getDescription())
                .creatorId(creatorId)
                .questionCount(0)
                .build();
        questionBankMapper.insert(bank);

        log.info("题库创建成功: bankId={}, creatorId={}", bank.getId(), creatorId);
        return convertToVO(bank);
    }

    @Override
    public void updateBank(Long id, BankUpdateRequest request) {
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在");
        }

        if (request.getBankName() != null) {
            bank.setBankName(request.getBankName());
        }
        if (request.getDescription() != null) {
            bank.setDescription(request.getDescription());
        }

        questionBankMapper.updateById(bank);
        log.info("题库更新成功: bankId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBank(Long id) {
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在");
        }

        // 级联删除 question_bank_item
        questionBankItemMapper.delete(
                new LambdaQueryWrapper<QuestionBankItem>().eq(QuestionBankItem::getBankId, id)
        );

        // 删除题库
        questionBankMapper.deleteById(id);
        log.info("题库删除: bankId={}", id);
    }

    @Override
    public BankDetailVO getBankById(Long id) {
        QuestionBank bank = questionBankMapper.selectById(id);
        if (bank == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在");
        }

        User creator = userMapper.selectById(bank.getCreatorId());

        // 查询题库中的题目列表
        List<QuestionBankItem> items = questionBankItemMapper.selectList(
                new LambdaQueryWrapper<QuestionBankItem>().eq(QuestionBankItem::getBankId, id)
        );
        List<QuestionVO> questionVOs = new ArrayList<>();
        for (QuestionBankItem item : items) {
            Question question = questionMapper.selectById(item.getQuestionId());
            if (question != null) {
                // 简化版：不含 analysis 和 answer
                questionVOs.add(QuestionVO.builder()
                        .id(question.getId())
                        .content(question.getContent())
                        .questionType(question.getQuestionType())
                        .optionA(question.getOptionA())
                        .optionB(question.getOptionB())
                        .optionC(question.getOptionC())
                        .optionD(question.getOptionD())
                        .difficulty(question.getDifficulty())
                        .subject(question.getSubject())
                        .createTime(question.getCreateTime())
                        .updateTime(question.getUpdateTime())
                        .build());
            }
        }

        return BankDetailVO.builder()
                .id(bank.getId())
                .bankName(bank.getBankName())
                .description(bank.getDescription())
                .creatorName(creator != null ? creator.getRealName() : null)
                .questionCount(bank.getQuestionCount())
                .createTime(bank.getCreateTime())
                .updateTime(bank.getUpdateTime())
                .questions(questionVOs)
                .build();
    }

    @Override
    public IPage<BankVO> listBanks(String keyword, int page, int size) {
        Page<BankVO> pageParam = new Page<>(page, size);
        return questionBankMapper.selectBankListWithCreator(pageParam, keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addQuestions(Long bankId, List<Long> questionIds) {
        QuestionBank bank = questionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在");
        }

        int addedCount = 0;
        for (Long questionId : questionIds) {
            // 检查题目是否存在
            Question question = questionMapper.selectById(questionId);
            if (question == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在: " + questionId);
            }

            // 幂等校验：已在题库中则跳过
            Long exists = questionBankItemMapper.selectCount(
                    new LambdaQueryWrapper<QuestionBankItem>()
                            .eq(QuestionBankItem::getBankId, bankId)
                            .eq(QuestionBankItem::getQuestionId, questionId)
            );
            if (exists > 0) {
                log.warn("题目已在题库中: bankId={}, questionId={}", bankId, questionId);
                continue;
            }

            QuestionBankItem item = QuestionBankItem.builder()
                    .bankId(bankId)
                    .questionId(questionId)
                    .build();
            questionBankItemMapper.insert(item);
            addedCount++;
        }

        // 更新题库题目计数
        if (addedCount > 0) {
            bank.setQuestionCount(bank.getQuestionCount() != null ? bank.getQuestionCount() + addedCount : addedCount);
            questionBankMapper.updateById(bank);
        }

        log.info("题目添加到题库: bankId={}, addedCount={}", bankId, addedCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeQuestions(Long bankId, List<Long> questionIds) {
        QuestionBank bank = questionBankMapper.selectById(bankId);
        if (bank == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "题库不存在");
        }

        int removedCount = 0;
        for (Long questionId : questionIds) {
            int deleted = questionBankItemMapper.delete(
                    new LambdaQueryWrapper<QuestionBankItem>()
                            .eq(QuestionBankItem::getBankId, bankId)
                            .eq(QuestionBankItem::getQuestionId, questionId)
            );
            removedCount += deleted;
        }

        // 更新题库题目计数
        if (removedCount > 0) {
            int newCount = (bank.getQuestionCount() != null ? bank.getQuestionCount() : 0) - removedCount;
            bank.setQuestionCount(Math.max(newCount, 0));
            questionBankMapper.updateById(bank);
        }

        log.info("题目从题库移除: bankId={}, removedCount={}", bankId, removedCount);
    }

    /**
     * 将 QuestionBank 实体转换为 BankVO
     */
    private BankVO convertToVO(QuestionBank bank) {
        User creator = userMapper.selectById(bank.getCreatorId());
        return BankVO.builder()
                .id(bank.getId())
                .bankName(bank.getBankName())
                .description(bank.getDescription())
                .creatorName(creator != null ? creator.getRealName() : null)
                .questionCount(bank.getQuestionCount())
                .createTime(bank.getCreateTime())
                .updateTime(bank.getUpdateTime())
                .build();
    }
}
