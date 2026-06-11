package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.enums.QuestionType;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.exam.GradeItem;
import com.exam.model.dto.exam.GradeRequest;
import com.exam.model.entity.Exam;
import com.exam.model.entity.ExamAnswer;
import com.exam.model.entity.ExamRecord;
import com.exam.model.entity.PaperQuestion;
import com.exam.model.entity.Question;
import com.exam.model.mapper.ExamAnswerMapper;
import com.exam.model.mapper.ExamMapper;
import com.exam.model.mapper.ExamRecordMapper;
import com.exam.model.mapper.PaperQuestionMapper;
import com.exam.model.mapper.QuestionMapper;
import com.exam.model.vo.exam.ExamAnswerVO;
import com.exam.model.vo.exam.ExamRecordDetailVO;
import com.exam.model.vo.exam.ExamRecordVO;
import com.exam.service.ExamRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考试记录服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamRecordServiceImpl implements ExamRecordService {

    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final ExamMapper examMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public IPage<ExamRecordVO> listRecords(Long examId, String status, int page, int size) {
        Page<ExamRecordVO> pageParam = new Page<>(page, size);
        return examRecordMapper.selectRecordList(pageParam, examId, status);
    }

    @Override
    public ExamRecordDetailVO getRecordDetail(Long recordId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试记录不存在");
        }

        Exam exam = examMapper.selectById(record.getExamId());
        String examName = exam != null ? exam.getExamName() : null;

        // 查询答案详情
        List<ExamAnswerVO> answerVOs = examAnswerMapper.selectAnswerListByRecordId(recordId);

        return ExamRecordDetailVO.builder()
                .id(record.getId())
                .examId(record.getExamId())
                .examName(examName)
                .userId(record.getUserId())
                .startTime(record.getStartTime())
                .submitTime(record.getSubmitTime())
                .status(record.getStatus())
                .totalScore(record.getTotalScore())
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(record.getSubjectiveScore())
                .answers(answerVOs)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeObjective(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 获取所有已提交的记录
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, ExamStatusConstant.RECORD_SUBMITTED)
        );

        for (ExamRecord record : records) {
            gradeSingleRecord(record.getId(), exam.getPaperId());
        }

        log.info("客观题自动批改完成: examId={}, recordCount={}", examId, records.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeSingleRecord(Long recordId, Long paperId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            log.warn("gradeSingleRecord: record not found, recordId={}", recordId);
            return;
        }

        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId));

        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));

        int objectiveScore = 0;
        boolean allGraded = true;

        for (ExamAnswer answer : answers) {
            PaperQuestion pq = paperQuestions.stream()
                    .filter(p -> p.getQuestionId().equals(answer.getQuestionId()))
                    .findFirst().orElse(null);
            if (pq == null) continue;

            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;

            QuestionType questionType = QuestionType.of(question.getQuestionType());
            if (questionType == QuestionType.SINGLE_CHOICE
                    || questionType == QuestionType.MULTI_CHOICE
                    || questionType == QuestionType.TRUE_FALSE) {
                int isCorrect = 0;
                int score = 0;
                if (answer.getAnswer() != null && answer.getAnswer().trim().equals(question.getAnswer().trim())) {
                    isCorrect = 1;
                    score = pq.getScore();
                }
                answer.setScore(score);
                answer.setIsCorrect(isCorrect);
                examAnswerMapper.updateById(answer);
                objectiveScore += score;
            } else {
                if (answer.getScore() == null || answer.getScore() == -1) allGraded = false;
            }
        }

        record.setObjectiveScore(objectiveScore);
        int subjectiveScore = 0;
        for (ExamAnswer answer : answers) {
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            QuestionType qt = QuestionType.of(question.getQuestionType());
            if ((qt == QuestionType.FILL_BLANK || qt == QuestionType.SHORT_ANSWER)
                    && answer.getScore() != null && answer.getScore() >= 0) {
                subjectiveScore += answer.getScore();
            }
        }
        record.setSubjectiveScore(subjectiveScore);
        record.setTotalScore(objectiveScore + subjectiveScore);
        if (allGraded) record.setStatus(ExamStatusConstant.RECORD_GRADED);
        examRecordMapper.updateById(record);

        log.info("gradeSingleRecord: recordId={}, objective={}, subjective={}, total={}",
                recordId, objectiveScore, subjectiveScore, record.getTotalScore());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void gradeSubjective(GradeRequest request) {
        int totalSubjectiveScore = 0;
        Long recordId = null;
        ExamRecord record = null;

        for (GradeItem item : request.getAnswers()) {
            ExamAnswer answer = examAnswerMapper.selectById(item.getAnswerId());
            if (answer == null) {
                throw new BusinessException(ResultCode.NOT_FOUND, "答案记录不存在: " + item.getAnswerId());
            }

            answer.setScore(item.getScore());
            answer.setIsCorrect(item.getIsCorrect());
            examAnswerMapper.updateById(answer);

            if (recordId == null) {
                recordId = answer.getRecordId();
            }
        }

        if (recordId != null) {
            record = examRecordMapper.selectById(recordId);
            if (record != null) {
                // 重新计算主观题得分
                List<ExamAnswer> allAnswers = examAnswerMapper.selectList(
                        new LambdaQueryWrapper<ExamAnswer>()
                                .eq(ExamAnswer::getRecordId, recordId)
                );

                int subjectiveScore = 0;
                int objectiveScore = record.getObjectiveScore() != null && record.getObjectiveScore() >= 0
                        ? record.getObjectiveScore() : 0;
                boolean allGraded = true;

                for (ExamAnswer ans : allAnswers) {
                    Question question = questionMapper.selectById(ans.getQuestionId());
                    if (question == null) {
                        continue;
                    }
                    QuestionType questionType = QuestionType.of(question.getQuestionType());
                    if (questionType == QuestionType.FILL_BLANK || questionType == QuestionType.SHORT_ANSWER) {
                        if (ans.getScore() != null && ans.getScore() >= 0) {
                            subjectiveScore += ans.getScore();
                        } else {
                            allGraded = false;
                        }
                    }
                }

                record.setSubjectiveScore(subjectiveScore);
                record.setTotalScore(objectiveScore + subjectiveScore);

                // 检查所有题目是否都已批改
                if (allGraded) {
                    // 还需检查客观题是否已批改
                    boolean objectiveAllGraded = true;
                    for (ExamAnswer ans : allAnswers) {
                        Question question = questionMapper.selectById(ans.getQuestionId());
                        if (question == null) {
                            continue;
                        }
                        QuestionType questionType = QuestionType.of(question.getQuestionType());
                        if ((questionType == QuestionType.SINGLE_CHOICE
                                || questionType == QuestionType.MULTI_CHOICE
                                || questionType == QuestionType.TRUE_FALSE)
                                && (ans.getScore() == null || ans.getScore() == -1)) {
                            objectiveAllGraded = false;
                            break;
                        }
                    }
                    if (objectiveAllGraded) {
                        record.setStatus(ExamStatusConstant.RECORD_GRADED);
                    }
                }

                examRecordMapper.updateById(record);
            }
        }

        log.info("主观题手动批改完成: recordId={}", recordId);
    }

    @Override
    public ExamRecordVO getRecordByExamAndUser(Long examId, Long userId) {
        ExamRecord record = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId)
        );
        if (record == null) {
            return null;
        }

        Exam exam = examMapper.selectById(examId);
        return ExamRecordVO.builder()
                .id(record.getId())
                .examId(record.getExamId())
                .examName(exam != null ? exam.getExamName() : null)
                .userId(record.getUserId())
                .startTime(record.getStartTime())
                .submitTime(record.getSubmitTime())
                .status(record.getStatus())
                .totalScore(record.getTotalScore())
                .objectiveScore(record.getObjectiveScore())
                .subjectiveScore(record.getSubjectiveScore())
                .build();
    }
}
