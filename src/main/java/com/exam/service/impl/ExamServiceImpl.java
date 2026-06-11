package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.constant.RabbitMQConstant;
import com.exam.common.constant.RedisKeyConstant;
import com.exam.common.enums.QuestionType;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.exam.ExamCreateRequest;
import com.exam.model.dto.exam.ExamSaveProgressRequest;
import com.exam.model.dto.exam.ExamSubmitRequest;
import com.exam.model.dto.exam.ExamUpdateRequest;
import com.exam.model.entity.ClassEntity;
import com.exam.model.entity.ClassStudent;
import com.exam.model.entity.Exam;
import com.exam.model.entity.ExamAnswer;
import com.exam.model.entity.ExamRecord;
import com.exam.model.entity.Paper;
import com.exam.model.entity.PaperQuestion;
import com.exam.model.entity.PaperRule;
import com.exam.model.entity.Question;
import com.exam.model.entity.QuestionBankItem;
import com.exam.model.mapper.ClassMapper;
import com.exam.model.mapper.ClassStudentMapper;
import com.exam.model.mapper.ExamMapper;
import com.exam.model.mapper.ExamAnswerMapper;
import com.exam.model.mapper.ExamRecordMapper;
import com.exam.model.mapper.PaperMapper;
import com.exam.model.mapper.PaperQuestionMapper;
import com.exam.model.mapper.PaperRuleMapper;
import com.exam.model.mapper.QuestionBankItemMapper;
import com.exam.model.mapper.QuestionMapper;
import com.exam.model.vo.exam.ExamDetailVO;
import com.exam.model.vo.exam.ExamEnterVO;
import com.exam.model.vo.exam.ExamQuestionVO;
import com.exam.model.vo.exam.ExamVO;
import com.exam.model.vo.paper.PaperDetailVO;
import com.exam.service.ExamService;
import com.exam.service.ExamRecordService;
import com.exam.service.PaperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 考试服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamAnswerMapper examAnswerMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final PaperRuleMapper paperRuleMapper;
    private final QuestionMapper questionMapper;
    private final QuestionBankItemMapper questionBankItemMapper;
    private final ClassMapper classMapper;
    private final ClassStudentMapper classStudentMapper;
    private final PaperService paperService;
    private final ExamRecordService examRecordService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamVO createExam(ExamCreateRequest request, Long creatorId) {
        // 校验试卷存在
        Paper paper = paperMapper.selectById(request.getPaperId());
        if (paper == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "试卷不存在");
        }

        // 校验班级存在
        ClassEntity classEntity = classMapper.selectById(request.getClassId());
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        Exam exam = Exam.builder()
                .examName(request.getExamName())
                .paperId(request.getPaperId())
                .classId(request.getClassId())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .duration(request.getDuration())
                .status(ExamStatusConstant.EXAM_NOT_STARTED)
                .creatorId(creatorId)
                .build();
        examMapper.insert(exam);

        log.info("考试创建成功: examId={}, creatorId={}", exam.getId(), creatorId);
        return getExamVOById(exam.getId());
    }

    @Override
    public void updateExam(Long id, ExamUpdateRequest request) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 只有未开始的考试才能修改
        if (!ExamStatusConstant.EXAM_NOT_STARTED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "只有未开始的考试才能修改");
        }

        if (request.getExamName() != null) {
            exam.setExamName(request.getExamName());
        }
        if (request.getStartTime() != null) {
            exam.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            exam.setEndTime(request.getEndTime());
        }
        if (request.getDuration() != null) {
            exam.setDuration(request.getDuration());
        }

        examMapper.updateById(exam);
        log.info("考试更新成功: examId={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 只有未开始的考试才能删除
        if (!ExamStatusConstant.EXAM_NOT_STARTED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "只有未开始的考试才能删除");
        }

        // 级联删除考试记录和答案
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id)
        );
        for (ExamRecord record : records) {
            examAnswerMapper.delete(
                    new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, record.getId())
            );
        }
        examRecordMapper.delete(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getExamId, id)
        );

        examMapper.deleteById(id);
        log.info("考试删除: examId={}", id);
    }

    @Override
    public ExamDetailVO getExamById(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        ExamVO examVO = getExamVOById(id);
        PaperDetailVO paperDetail = paperService.getPaperById(exam.getPaperId());

        return ExamDetailVO.builder()
                .id(examVO.getId())
                .examName(examVO.getExamName())
                .paperId(examVO.getPaperId())
                .paperName(examVO.getPaperName())
                .classId(examVO.getClassId())
                .className(examVO.getClassName())
                .startTime(examVO.getStartTime())
                .endTime(examVO.getEndTime())
                .duration(examVO.getDuration())
                .status(examVO.getStatus())
                .creatorName(examVO.getCreatorName())
                .studentCount(examVO.getStudentCount())
                .submittedCount(examVO.getSubmittedCount())
                .createTime(examVO.getCreateTime())
                .paper(paperDetail)
                .build();
    }

    @Override
    public IPage<ExamVO> listExams(Long creatorId, String status, int page, int size, Long studentId) {
        Page<ExamVO> pageParam = new Page<>(page, size);
        return examMapper.selectExamList(pageParam, creatorId, status, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 校验状态
        if (!ExamStatusConstant.EXAM_NOT_STARTED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "只有未开始的考试才能发布");
        }

        // 校验试卷存在
        Paper paper = paperMapper.selectById(exam.getPaperId());
        if (paper == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "试卷不存在");
        }

        // 校验班级存在
        ClassEntity classEntity = classMapper.selectById(exam.getClassId());
        if (classEntity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "班级不存在");
        }

        // 随机组卷：根据规则抽题生成 paper_question
        if (paper.getPaperType() != null && paper.getPaperType() == 2) {
            generateRandomPaperQuestions(paper.getId());
        }

        // 根据开始时间判断状态
        LocalDateTime now = LocalDateTime.now();
        if (!exam.getStartTime().isAfter(now)) {
            exam.setStatus(ExamStatusConstant.EXAM_IN_PROGRESS);
        }
        examMapper.updateById(exam);

        // Redis 存考试状态
        String statusKey = String.format(RedisKeyConstant.EXAM_STATUS, exam.getId(), 0);
        try {
            redisTemplate.opsForValue().set(statusKey, exam.getStatus(),
                    Duration.ofHours(RedisKeyConstant.EXAM_STATE_TTL_HOURS));
        } catch (Exception e) {
            log.error("Redis写入考试状态失败: examId={}", exam.getId(), e);
        }

        // 为班级每个学生创建 ExamRecord
        List<ClassStudent> classStudents = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getClassId, exam.getClassId())
        );
        for (ClassStudent cs : classStudents) {
            ExamRecord record = ExamRecord.builder()
                    .examId(exam.getId())
                    .userId(cs.getStudentId())
                    .status(ExamStatusConstant.EXAM_NOT_STARTED)
                    .totalScore(-1)
                    .objectiveScore(-1)
                    .subjectiveScore(-1)
                    .build();
            examRecordMapper.insert(record);
        }

        log.info("考试发布成功: examId={}, studentCount={}", exam.getId(), classStudents.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExamEnterVO enterExam(Long examId, Long userId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 校验考试状态
        if (ExamStatusConstant.EXAM_NOT_STARTED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "考试尚未开始");
        }
        if (ExamStatusConstant.EXAM_ENDED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "考试已结束");
        }

        // 校验学生在班级中
        Long studentCount = classStudentMapper.selectCount(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getClassId, exam.getClassId())
                        .eq(ClassStudent::getStudentId, userId)
        );
        if (studentCount == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您不在此考试的班级中");
        }

        // 校验是否已提交
        ExamRecord existingRecord = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId)
        );
        if (existingRecord != null && ExamStatusConstant.RECORD_SUBMITTED.equals(existingRecord.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "您已提交过此考试");
        }

        // 生成 examToken（UUID），存入 Redis
        String examToken = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = String.format(RedisKeyConstant.EXAM_TOKEN, examId, userId);
        String tokenValue = examId + ":" + userId;
        long tokenTtlSeconds = exam.getDuration() * 60L + RedisKeyConstant.EXAM_TOKEN_EXTRA_MINUTES * 60L;
        try {
            // 存储 exam:token:{examId}:{userId} -> tokenValue
            redisTemplate.opsForValue().set(tokenKey, tokenValue,
                    Duration.ofSeconds(tokenTtlSeconds));
            // 存储 exam:token:value:{token} -> tokenValue（用于通过 token 反查 examId:userId）
            String tokenValueKey = "exam:token:value:" + examToken;
            redisTemplate.opsForValue().set(tokenValueKey, tokenValue,
                    Duration.ofSeconds(tokenTtlSeconds));
        } catch (Exception e) {
            log.error("Redis写入考试Token失败: examId={}, userId={}", examId, userId, e);
        }

        // 创建/更新 ExamRecord
        if (existingRecord == null) {
            // 首次进入：直接创建已开始的记录（适用于未经过 publishExam 流程的情况）
            ExamRecord newRecord = ExamRecord.builder()
                    .examId(examId)
                    .userId(userId)
                    .status(ExamStatusConstant.RECORD_STARTED)
                    .startTime(LocalDateTime.now())
                    .totalScore(-1)
                    .objectiveScore(-1)
                    .subjectiveScore(-1)
                    .build();
            examRecordMapper.insert(newRecord);
            existingRecord = newRecord;
        } else if (ExamStatusConstant.EXAM_NOT_STARTED.equals(existingRecord.getStatus())) {
            existingRecord.setStatus(ExamStatusConstant.RECORD_STARTED);
            existingRecord.setStartTime(LocalDateTime.now());
            examRecordMapper.updateById(existingRecord);
        }

        // 获取试卷题目列表（不含正确答案和解析）
        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, exam.getPaperId())
                        .orderByAsc(PaperQuestion::getSortOrder)
        );
        List<ExamQuestionVO> questionVOs = new ArrayList<>();
        for (PaperQuestion pq : paperQuestions) {
            Question question = questionMapper.selectById(pq.getQuestionId());
            if (question != null) {
                questionVOs.add(ExamQuestionVO.builder()
                        .questionId(question.getId())
                        .content(question.getContent())
                        .questionType(question.getQuestionType())
                        .difficulty(question.getDifficulty())
                        .subject(question.getSubject())
                        .optionA(question.getOptionA())
                        .optionB(question.getOptionB())
                        .optionC(question.getOptionC())
                        .optionD(question.getOptionD())
                        .optionE(question.getOptionE())
                        .optionF(question.getOptionF())
                        .optionG(question.getOptionG())
                        .optionH(question.getOptionH())
                        .score(pq.getScore())
                        .sortOrder(pq.getSortOrder())
                        .build());
            }
        }

        // 计算剩余时间
        LocalDateTime now = LocalDateTime.now();
        long remainingSeconds = 0;
        if (exam.getEndTime() != null && exam.getEndTime().isAfter(now)) {
            remainingSeconds = Duration.between(now, exam.getEndTime()).getSeconds();
        }

        // Redis 存倒计时
        String timerKey = String.format(RedisKeyConstant.EXAM_TIMER, examId, userId);
        try {
            redisTemplate.opsForValue().set(timerKey, String.valueOf(remainingSeconds),
                    Duration.ofSeconds(remainingSeconds > 0 ? remainingSeconds : 1));
        } catch (Exception e) {
            log.error("Redis写入考试计时器失败: examId={}, userId={}", examId, userId, e);
        }

        // 获取已保存的答题进度
        Map<Long, String> savedAnswers = getProgress(examId, userId);

        log.info("学生进入考试: examId={}, userId={}, remainingSeconds={}", examId, userId, remainingSeconds);

        return ExamEnterVO.builder()
                .examToken(examToken)
                .examName(exam.getExamName())
                .duration(exam.getDuration())
                .remainingSeconds(remainingSeconds)
                .questions(questionVOs)
                .savedAnswers(savedAnswers)
                .build();
    }

    @Override
    public void saveProgress(ExamSaveProgressRequest request, Long userId) {
        // 验证 examToken，获取 examId
        String tokenValue = verifyExamToken(request.getExamToken(), userId);
        Long examId = Long.parseLong(tokenValue.split(":")[0]);

        // 验证考试未结束
        Exam exam = examMapper.selectById(examId);
        if (exam == null || ExamStatusConstant.EXAM_ENDED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "考试已结束");
        }

        // Redis 存答题进度到 Hash
        String progressKey = String.format(RedisKeyConstant.EXAM_PROGRESS, examId, userId);
        try {
            redisTemplate.opsForHash().put(progressKey,
                    String.valueOf(request.getQuestionId()),
                    request.getAnswer() != null ? request.getAnswer() : "");
            redisTemplate.expire(progressKey, RedisKeyConstant.EXAM_STATE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Redis保存答题进度失败: examId={}, userId={}, questionId={}",
                    examId, userId, request.getQuestionId(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitExam(ExamSubmitRequest request, Long userId) {
        // 验证 examToken
        String tokenValue = verifyExamToken(request.getExamToken(), userId);
        Long examId = Long.parseLong(tokenValue.split(":")[0]);

        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 获取考试记录
        ExamRecord record = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId)
        );
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试记录不存在");
        }
        if (ExamStatusConstant.RECORD_SUBMITTED.equals(record.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "已提交过此考试");
        }

        // 从 Redis 获取缓存的答题进度
        String progressKey = String.format(RedisKeyConstant.EXAM_PROGRESS, examId, userId);
        Map<Object, Object> cachedAnswers = new HashMap<>();
        try {
            cachedAnswers = redisTemplate.opsForHash().entries(progressKey);
        } catch (Exception e) {
            log.error("Redis读取答题进度失败: examId={}, userId={}", examId, userId, e);
        }

        // 合并答案：请求中的答案优先于Redis缓存
        Map<Long, String> finalAnswers = new HashMap<>();
        for (Map.Entry<Object, Object> entry : cachedAnswers.entrySet()) {
            finalAnswers.put(Long.parseLong(entry.getKey().toString()), entry.getValue().toString());
        }
        if (request.getAnswers() != null) {
            for (var item : request.getAnswers()) {
                finalAnswers.put(item.getQuestionId(), item.getAnswer());
            }
        }

        // 批量保存所有 ExamAnswer 记录
        for (Map.Entry<Long, String> entry : finalAnswers.entrySet()) {
            examAnswerMapper.insert(ExamAnswer.builder()
                    .recordId(record.getId())
                    .questionId(entry.getKey())
                    .answer(entry.getValue())
                    .score(-1)
                    .build());
        }

        // 更新 ExamRecord
        record.setStatus(ExamStatusConstant.RECORD_SUBMITTED);
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.updateById(record);

        // 【新增】交卷后立即自动批改客观题
        try {
            examRecordService.gradeSingleRecord(record.getId(), exam.getPaperId());
        } catch (Exception e) {
            log.error("自动批改客观题失败: examId={}, userId={}", examId, userId, e);
            // 批改失败不阻断提交流程，教师可后续手动批改
        }

        // 清理 Redis 数据（含examToken反查key）
        cleanRedisData(examId, userId, request.getExamToken());

        log.info("学生提交考试: examId={}, userId={}, answerCount={}", examId, userId, finalAnswers.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoSubmitExam(Long examId, Long userId) {
        ExamRecord record = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId)
        );

        if (record == null) {
            log.warn("自动提交：考试记录不存在, examId={}, userId={}", examId, userId);
            return;
        }

        // 如果已提交，跳过
        if (ExamStatusConstant.RECORD_SUBMITTED.equals(record.getStatus())
                || ExamStatusConstant.RECORD_GRADED.equals(record.getStatus())) {
            log.info("自动提交：学生已提交, examId={}, userId={}", examId, userId);
            return;
        }

        // 从 Redis 获取缓存答案
        String progressKey = String.format(RedisKeyConstant.EXAM_PROGRESS, examId, userId);
        Map<Object, Object> cachedAnswers = new HashMap<>();
        try {
            cachedAnswers = redisTemplate.opsForHash().entries(progressKey);
        } catch (Exception e) {
            log.error("自动提交：Redis读取答题进度失败: examId={}, userId={}", examId, userId, e);
        }

        // 保存答案
        for (Map.Entry<Object, Object> entry : cachedAnswers.entrySet()) {
            examAnswerMapper.insert(ExamAnswer.builder()
                    .recordId(record.getId())
                    .questionId(Long.parseLong(entry.getKey().toString()))
                    .answer(entry.getValue().toString())
                    .score(-1)
                    .build());
        }

        // 更新记录状态
        record.setStatus(ExamStatusConstant.RECORD_SUBMITTED);
        record.setSubmitTime(LocalDateTime.now());
        examRecordMapper.updateById(record);

        // 【新增】自动提交后也批改客观题
        try {
            Exam exam = examMapper.selectById(examId);
            if (exam != null) {
                examRecordService.gradeSingleRecord(record.getId(), exam.getPaperId());
            }
        } catch (Exception e) {
            log.error("自动提交批改客观题失败: examId={}, userId={}", examId, userId, e);
        }

        // 清理 Redis（自动提交时无examToken，反查key自然过期）
        cleanRedisData(examId, userId, null);

        log.info("自动提交考试完成: examId={}, userId={}, answerCount={}", examId, userId, cachedAnswers.size());
    }

    @Override
    public Map<Long, String> getProgress(Long examId, Long userId) {
        String progressKey = String.format(RedisKeyConstant.EXAM_PROGRESS, examId, userId);
        Map<Long, String> result = new HashMap<>();
        try {
            Map<Object, Object> cachedAnswers = redisTemplate.opsForHash().entries(progressKey);
            for (Map.Entry<Object, Object> entry : cachedAnswers.entrySet()) {
                result.put(Long.parseLong(entry.getKey().toString()), entry.getValue().toString());
            }
        } catch (Exception e) {
            log.error("Redis读取答题进度失败: examId={}, userId={}", examId, userId, e);
        }
        return result;
    }

    @Override
    public Long getRemainingTime(Long examId, Long userId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        // 先从 Redis 获取
        String timerKey = String.format(RedisKeyConstant.EXAM_TIMER, examId, userId);
        try {
            Object remaining = redisTemplate.opsForValue().get(timerKey);
            if (remaining != null) {
                return Long.parseLong(remaining.toString());
            }
        } catch (Exception e) {
            log.error("Redis读取剩余时间失败: examId={}, userId={}", examId, userId, e);
        }

        // Redis 无数据，从数据库计算
        LocalDateTime now = LocalDateTime.now();
        if (exam.getEndTime() != null && exam.getEndTime().isAfter(now)) {
            return Duration.between(now, exam.getEndTime()).getSeconds();
        }
        return 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endExam(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "考试不存在");
        }

        if (ExamStatusConstant.EXAM_ENDED.equals(exam.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, "考试已结束");
        }

        // 更新考试状态
        exam.setStatus(ExamStatusConstant.EXAM_ENDED);
        examMapper.updateById(exam);

        // Redis 更新考试状态
        String statusKey = String.format(RedisKeyConstant.EXAM_STATUS, exam.getId(), 0);
        try {
            redisTemplate.opsForValue().set(statusKey, ExamStatusConstant.EXAM_ENDED,
                    Duration.ofHours(RedisKeyConstant.EXAM_STATE_TTL_HOURS));
        } catch (Exception e) {
            log.error("Redis更新考试状态失败: examId={}", exam.getId(), e);
        }

        // 对未提交的学生发送 MQ 消息触发自动提交
        List<ExamRecord> unsubmittedRecords = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, id)
                        .ne(ExamRecord::getStatus, ExamStatusConstant.RECORD_SUBMITTED)
                        .ne(ExamRecord::getStatus, ExamStatusConstant.RECORD_GRADED)
        );

        for (ExamRecord record : unsubmittedRecords) {
            try {
                Map<String, Object> message = new HashMap<>();
                message.put("examId", id);
                message.put("userId", record.getUserId());
                rabbitTemplate.convertAndSend(
                        RabbitMQConstant.EXAM_EXCHANGE,
                        RabbitMQConstant.GRADING_ROUTING_KEY,
                        message
                );
            } catch (Exception e) {
                log.error("发送自动提交MQ消息失败: examId={}, userId={}", id, record.getUserId(), e);
            }
        }

        log.info("考试结束: examId={}, autoSubmitCount={}", id, unsubmittedRecords.size());
    }

    // ==================== 私有方法 ====================

    /**
     * 随机组卷：根据规则抽题生成 paper_question
     */
    private void generateRandomPaperQuestions(Long paperId) {
        List<PaperRule> rules = paperRuleMapper.selectList(
                new LambdaQueryWrapper<PaperRule>().eq(PaperRule::getPaperId, paperId)
        );

        // 先删除旧的 paper_question（如果存在）
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, paperId)
        );

        Set<Long> usedQuestionIds = new HashSet<>();
        int totalScore = 0;
        int sortOrder = 0;

        for (PaperRule rule : rules) {
            // 按条件查询题目
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<Question>()
                    .eq(Question::getQuestionType, rule.getQuestionType())
                    .eq(Question::getDifficulty, rule.getDifficulty());

            // 如果限定了题库，通过子查询筛选
            if (rule.getBankId() != null) {
                List<QuestionBankItem> bankItems = questionBankItemMapper.selectList(
                        new LambdaQueryWrapper<QuestionBankItem>()
                                .eq(QuestionBankItem::getBankId, rule.getBankId())
                );
                List<Long> bankQuestionIds = bankItems.stream()
                        .map(QuestionBankItem::getQuestionId)
                        .filter(id -> !usedQuestionIds.contains(id))
                        .toList();
                if (bankQuestionIds.isEmpty()) {
                    throw new BusinessException(ResultCode.CONFLICT,
                            "题库题目不足，无法生成试卷（题型=" + QuestionType.of(rule.getQuestionType()).getDesc()
                                    + ", 难度=" + rule.getDifficulty() + "）");
                }
                queryWrapper.in(Question::getId, bankQuestionIds);
            } else {
                // 排除已使用的题目
                if (!usedQuestionIds.isEmpty()) {
                    queryWrapper.notIn(Question::getId, usedQuestionIds);
                }
            }

            List<Question> candidates = questionMapper.selectList(queryWrapper);
            if (candidates.size() < rule.getQuestionCount()) {
                throw new BusinessException(ResultCode.CONFLICT,
                        "题库题目不足，无法生成试卷（需要" + rule.getQuestionCount()
                                + "题，仅有" + candidates.size() + "题）");
            }

            // 随机抽取
            List<Question> selected = new ArrayList<>(candidates);
            java.util.Collections.shuffle(selected);
            selected = selected.subList(0, rule.getQuestionCount());

            for (Question question : selected) {
                PaperQuestion pq = PaperQuestion.builder()
                        .paperId(paperId)
                        .questionId(question.getId())
                        .score(rule.getScorePerQuestion())
                        .sortOrder(sortOrder++)
                        .build();
                paperQuestionMapper.insert(pq);
                usedQuestionIds.add(question.getId());
                totalScore += rule.getScorePerQuestion();
            }
        }

        // 更新试卷总分
        Paper paper = paperMapper.selectById(paperId);
        paper.setTotalScore(totalScore);
        paperMapper.updateById(paper);
    }

    /**
     * 验证 examToken，返回存储的值
     */
    private String verifyExamToken(String examToken, Long userId) {
        // 由于 token key 包含 examId 和 userId，但提交时只有 token 不知道 examId
        // 所以采用另一种方式：遍历可能的 key 不现实
        // 更好的方案：token key 使用 token 字符串本身
        // 修正：使用 exam:token:{tokenValue} 格式存储
        String tokenKey = "exam:token:value:" + examToken;
        try {
            Object value = redisTemplate.opsForValue().get(tokenKey);
            if (value == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "考试Token无效或已过期");
            }
            return value.toString();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Redis验证考试Token失败: token={}", examToken, e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "验证考试Token失败");
        }
    }

    /**
     * 清理 Redis 考试数据
     *
     * @param examId    考试ID
     * @param userId    用户ID
     * @param examToken 考试Token（手动提交时传入，自动提交时为null由TTL自然过期）
     */
    private void cleanRedisData(Long examId, Long userId, String examToken) {
        try {
            String progressKey = String.format(RedisKeyConstant.EXAM_PROGRESS, examId, userId);
            String timerKey = String.format(RedisKeyConstant.EXAM_TIMER, examId, userId);
            String tokenKey = String.format(RedisKeyConstant.EXAM_TOKEN, examId, userId);
            redisTemplate.delete(progressKey);
            redisTemplate.delete(timerKey);
            redisTemplate.delete(tokenKey);

            // 清理 token 反查 key: exam:token:value:{token}
            if (examToken != null && !examToken.isBlank()) {
                String tokenValueKey = "exam:token:value:" + examToken;
                redisTemplate.delete(tokenValueKey);
            }
        } catch (Exception e) {
            log.error("清理Redis考试数据失败: examId={}, userId={}", examId, userId, e);
        }
    }

    /**
     * 获取 ExamVO
     */
    private ExamVO getExamVOById(Long id) {
        // 直接用 mapper 查询所有考试然后过滤（此处简化实现）
        Page<ExamVO> singlePage = new Page<>(1, 1000);
        IPage<ExamVO> examVOPage = examMapper.selectExamList(singlePage, null, null, null);
        for (ExamVO vo : examVOPage.getRecords()) {
            if (vo.getId().equals(id)) {
                return vo;
            }
        }
        return null;
    }
}
