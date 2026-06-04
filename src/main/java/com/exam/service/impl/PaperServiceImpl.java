package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.paper.PaperCreateRequest;
import com.exam.model.dto.paper.PaperQuestionItem;
import com.exam.model.dto.paper.PaperRuleItem;
import com.exam.model.entity.Paper;
import com.exam.model.entity.PaperQuestion;
import com.exam.model.entity.PaperRule;
import com.exam.model.entity.Question;
import com.exam.model.entity.QuestionBank;
import com.exam.model.entity.User;
import com.exam.model.mapper.PaperMapper;
import com.exam.model.mapper.PaperQuestionMapper;
import com.exam.model.mapper.PaperRuleMapper;
import com.exam.model.mapper.QuestionBankMapper;
import com.exam.model.mapper.QuestionMapper;
import com.exam.model.mapper.UserMapper;
import com.exam.model.vo.paper.PaperDetailVO;
import com.exam.model.vo.paper.PaperQuestionVO;
import com.exam.model.vo.paper.PaperRuleVO;
import com.exam.model.vo.paper.PaperVO;
import com.exam.service.PaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 试卷服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final PaperRuleMapper paperRuleMapper;
    private final QuestionMapper questionMapper;
    private final QuestionBankMapper questionBankMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaperDetailVO createPaper(PaperCreateRequest request, Long creatorId) {
        // 创建试卷
        Paper paper = Paper.builder()
                .paperName(request.getPaperName())
                .paperType(request.getPaperType())
                .totalScore(request.getTotalScore())
                .passScore(request.getPassScore() != null ? request.getPassScore() : 60)
                .duration(request.getDuration() != null ? request.getDuration() : 120)
                .creatorId(creatorId)
                .build();

        if (request.getPaperType() == 1) {
            // 手工组卷：校验题目存在性 → 批量插入 → 计算totalScore
            if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "手工组卷必须指定题目列表");
            }

            int totalScore = 0;
            int sortOrder = 0;
            paperMapper.insert(paper);

            for (PaperQuestionItem item : request.getQuestions()) {
                Question question = questionMapper.selectById(item.getQuestionId());
                if (question == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在: " + item.getQuestionId());
                }

                PaperQuestion pq = PaperQuestion.builder()
                        .paperId(paper.getId())
                        .questionId(item.getQuestionId())
                        .score(item.getScore())
                        .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : sortOrder++)
                        .build();
                paperQuestionMapper.insert(pq);
                totalScore += item.getScore();
            }

            // 自动计算总分
            paper.setTotalScore(totalScore);
            paperMapper.updateById(paper);

        } else if (request.getPaperType() == 2) {
            // 随机组卷：保存规则，不立即抽题
            if (request.getRules() == null || request.getRules().isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "随机组卷必须指定组卷规则");
            }

            int totalScore = 0;
            paperMapper.insert(paper);

            for (PaperRuleItem ruleItem : request.getRules()) {
                PaperRule rule = PaperRule.builder()
                        .paperId(paper.getId())
                        .questionType(ruleItem.getQuestionType())
                        .difficulty(ruleItem.getDifficulty())
                        .questionCount(ruleItem.getQuestionCount())
                        .scorePerQuestion(ruleItem.getScorePerQuestion())
                        .bankId(ruleItem.getBankId())
                        .build();
                paperRuleMapper.insert(rule);
                totalScore += ruleItem.getQuestionCount() * ruleItem.getScorePerQuestion();
            }

            // 根据规则计算总分
            paper.setTotalScore(totalScore);
            paperMapper.updateById(paper);

        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的试卷类型");
        }

        log.info("试卷创建成功: paperId={}, paperType={}, creatorId={}", paper.getId(), request.getPaperType(), creatorId);
        return getPaperById(paper.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePaper(Long id, PaperCreateRequest request) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "试卷不存在");
        }

        // 更新基本信息
        if (request.getPaperName() != null) {
            paper.setPaperName(request.getPaperName());
        }
        if (request.getPassScore() != null) {
            paper.setPassScore(request.getPassScore());
        }
        if (request.getDuration() != null) {
            paper.setDuration(request.getDuration());
        }

        // 删除旧的关联数据
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );
        paperRuleMapper.delete(
                new LambdaQueryWrapper<PaperRule>().eq(PaperRule::getPaperId, id)
        );

        // 重新插入关联数据
        if (request.getPaperType() != null) {
            paper.setPaperType(request.getPaperType());
        }

        int totalScore = 0;

        if (paper.getPaperType() == 1 && request.getQuestions() != null) {
            int sortOrder = 0;
            for (PaperQuestionItem item : request.getQuestions()) {
                Question question = questionMapper.selectById(item.getQuestionId());
                if (question == null) {
                    throw new BusinessException(ResultCode.NOT_FOUND, "题目不存在: " + item.getQuestionId());
                }

                PaperQuestion pq = PaperQuestion.builder()
                        .paperId(id)
                        .questionId(item.getQuestionId())
                        .score(item.getScore())
                        .sortOrder(item.getSortOrder() != null ? item.getSortOrder() : sortOrder++)
                        .build();
                paperQuestionMapper.insert(pq);
                totalScore += item.getScore();
            }
        } else if (paper.getPaperType() == 2 && request.getRules() != null) {
            for (PaperRuleItem ruleItem : request.getRules()) {
                PaperRule rule = PaperRule.builder()
                        .paperId(id)
                        .questionType(ruleItem.getQuestionType())
                        .difficulty(ruleItem.getDifficulty())
                        .questionCount(ruleItem.getQuestionCount())
                        .scorePerQuestion(ruleItem.getScorePerQuestion())
                        .bankId(ruleItem.getBankId())
                        .build();
                paperRuleMapper.insert(rule);
                totalScore += ruleItem.getQuestionCount() * ruleItem.getScorePerQuestion();
            }
        }

        paper.setTotalScore(totalScore);
        paperMapper.updateById(paper);
        log.info("试卷更新成功: paperId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaper(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "试卷不存在");
        }

        // 级联删除 paper_question
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id)
        );
        // 级联删除 paper_rule
        paperRuleMapper.delete(
                new LambdaQueryWrapper<PaperRule>().eq(PaperRule::getPaperId, id)
        );

        paperMapper.deleteById(id);
        log.info("试卷删除: paperId={}", id);
    }

    @Override
    public PaperDetailVO getPaperById(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "试卷不存在");
        }

        User creator = userMapper.selectById(paper.getCreatorId());

        // 查询试卷题目列表
        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, id)
                        .orderByAsc(PaperQuestion::getSortOrder)
        );
        List<PaperQuestionVO> questionVOs = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question question = questionMapper.selectById(pq.getQuestionId());
            questionVOs.add(PaperQuestionVO.builder()
                    .id(pq.getId())
                    .questionId(pq.getQuestionId())
                    .content(question != null ? question.getContent() : null)
                    .questionType(question != null ? question.getQuestionType() : null)
                    .optionA(question != null ? question.getOptionA() : null)
                    .optionB(question != null ? question.getOptionB() : null)
                    .optionC(question != null ? question.getOptionC() : null)
                    .optionD(question != null ? question.getOptionD() : null)
                    .score(pq.getScore())
                    .sortOrder(pq.getSortOrder())
                    .build());
        }

        // 查询随机组卷规则
        List<PaperRuleVO> ruleVOs = new ArrayList<>();
        if (paper.getPaperType() == 2) {
            List<PaperRule> rules = paperRuleMapper.selectList(
                    new LambdaQueryWrapper<PaperRule>().eq(PaperRule::getPaperId, id)
            );
            for (PaperRule rule : rules) {
                String bankName = null;
                if (rule.getBankId() != null) {
                    QuestionBank bank = questionBankMapper.selectById(rule.getBankId());
                    bankName = bank != null ? bank.getBankName() : null;
                }
                ruleVOs.add(PaperRuleVO.builder()
                        .id(rule.getId())
                        .questionType(rule.getQuestionType())
                        .difficulty(rule.getDifficulty())
                        .questionCount(rule.getQuestionCount())
                        .scorePerQuestion(rule.getScorePerQuestion())
                        .bankId(rule.getBankId())
                        .bankName(bankName)
                        .build());
            }
        }

        return PaperDetailVO.builder()
                .id(paper.getId())
                .paperName(paper.getPaperName())
                .paperType(paper.getPaperType())
                .totalScore(paper.getTotalScore())
                .passScore(paper.getPassScore())
                .duration(paper.getDuration())
                .creatorName(creator != null ? creator.getRealName() : null)
                .questionCount(paperQuestions.size())
                .createTime(paper.getCreateTime())
                .updateTime(paper.getUpdateTime())
                .questions(questionVOs)
                .rules(ruleVOs)
                .build();
    }

    @Override
    public IPage<PaperVO> listPapers(Long creatorId, int page, int size) {
        Page<PaperVO> pageParam = new Page<>(page, size);
        return paperMapper.selectPaperListWithCreator(pageParam, creatorId);
    }
}
