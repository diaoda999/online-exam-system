package com.exam.model;

import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.constant.RedisKeyConstant;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ResultCode;
import com.exam.model.dto.exam.AnswerItem;
import com.exam.model.dto.exam.ExamSubmitRequest;
import com.exam.model.entity.*;
import com.exam.model.mapper.*;
import com.exam.model.vo.exam.ExamEnterVO;
import com.exam.service.PaperService;
import com.exam.service.impl.ExamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExamServiceImpl 单元测试
 * 覆盖进入考试和提交考试核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {

    @Mock
    private ExamMapper examMapper;

    @Mock
    private ExamRecordMapper examRecordMapper;

    @Mock
    private ExamAnswerMapper examAnswerMapper;

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private PaperQuestionMapper paperQuestionMapper;

    @Mock
    private PaperRuleMapper paperRuleMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private QuestionBankItemMapper questionBankItemMapper;

    @Mock
    private ClassMapper classMapper;

    @Mock
    private ClassStudentMapper classStudentMapper;

    @Mock
    private PaperService paperService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam mockExam;
    private ExamRecord mockRecord;
    private Paper mockPaper;
    private Question mockQuestion;
    private ClassStudent mockClassStudent;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        mockExam = Exam.builder()
                .id(1L)
                .examName("期中考试")
                .paperId(1L)
                .classId(1L)
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(1))
                .duration(60)
                .status(ExamStatusConstant.EXAM_IN_PROGRESS)
                .creatorId(1L)
                .build();

        mockRecord = ExamRecord.builder()
                .id(1L)
                .examId(1L)
                .userId(1L)
                .status(ExamStatusConstant.EXAM_NOT_STARTED)
                .totalScore(-1)
                .objectiveScore(-1)
                .subjectiveScore(-1)
                .build();

        mockPaper = Paper.builder()
                .id(1L)
                .paperName("数学试卷")
                .paperType(1)
                .totalScore(100)
                .passScore(60)
                .duration(60)
                .creatorId(1L)
                .build();

        mockQuestion = Question.builder()
                .id(1L)
                .content("1+1=?")
                .questionType(1) // 单选题
                .optionA("1")
                .optionB("2")
                .optionC("3")
                .optionD("4")
                .answer("B")
                .difficulty(1)
                .build();

        mockClassStudent = ClassStudent.builder()
                .id(1L)
                .classId(1L)
                .studentId(1L)
                .build();
    }

    // ==================== 进入考试测试 ====================

    @Nested
    @DisplayName("进入考试测试")
    class EnterExamTests {

        @Test
        @DisplayName("进入考试成功 - 首次进入")
        void enterExamSuccess() {
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(1L);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(paperQuestionMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExamEnterVO result = examService.enterExam(1L, 1L);

            assertNotNull(result);
            assertNotNull(result.getExamToken());
            assertEquals("期中考试", result.getExamName());
            assertEquals(60, result.getDuration());

            // 验证record状态被更新为STARTED
            assertEquals(ExamStatusConstant.RECORD_STARTED, mockRecord.getStatus());
            assertNotNull(mockRecord.getStartTime());

            verify(redisTemplate.opsForValue(), atLeast(2)).set(anyString(), any(), any());
        }

        @Test
        @DisplayName("进入考试成功 - 已开始记录不重复更新startTime")
        void enterExamAlreadyStartedRecord() {
            mockRecord.setStatus(ExamStatusConstant.RECORD_STARTED);
            mockRecord.setStartTime(LocalDateTime.now().minusMinutes(10));

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(1L);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(paperQuestionMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExamEnterVO result = examService.enterExam(1L, 1L);

            assertNotNull(result);
            // RECORD_STARTED状态的记录不应再更新状态
            verify(examRecordMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("进入考试失败 - 考试不存在")
        void enterExamFailExamNotFound() {
            when(examMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.enterExam(999L, 1L));
            assertEquals(ResultCode.NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("进入考试失败 - 考试尚未开始")
        void enterExamFailNotStarted() {
            mockExam.setStatus(ExamStatusConstant.EXAM_NOT_STARTED);

            when(examMapper.selectById(1L)).thenReturn(mockExam);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.enterExam(1L, 1L));
            assertEquals(ResultCode.CONFLICT.getCode(), exception.getCode());
            assertEquals("考试尚未开始", exception.getMessage());
        }

        @Test
        @DisplayName("进入考试失败 - 考试已结束")
        void enterExamFailEnded() {
            mockExam.setStatus(ExamStatusConstant.EXAM_ENDED);

            when(examMapper.selectById(1L)).thenReturn(mockExam);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.enterExam(1L, 1L));
            assertEquals(ResultCode.CONFLICT.getCode(), exception.getCode());
            assertEquals("考试已结束", exception.getMessage());
        }

        @Test
        @DisplayName("进入考试失败 - 学生不在班级中")
        void enterExamFailNotInClass() {
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(0L);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.enterExam(1L, 1L));
            assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
            assertEquals("您不在此考试的班级中", exception.getMessage());
        }

        @Test
        @DisplayName("进入考试失败 - 已提交过此考试")
        void enterExamFailAlreadySubmitted() {
            mockRecord.setStatus(ExamStatusConstant.RECORD_SUBMITTED);

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(1L);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.enterExam(1L, 1L));
            assertEquals(ResultCode.CONFLICT.getCode(), exception.getCode());
            assertEquals("您已提交过此考试", exception.getMessage());
        }

        @Test
        @DisplayName("进入考试成功 - 返回的题目不含答案和解析")
        void enterExamSuccessQuestionsNoAnswer() {
            PaperQuestion pq = PaperQuestion.builder()
                    .id(1L).paperId(1L).questionId(1L).score(5).sortOrder(0).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(1L);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq));
            when(questionMapper.selectById(1L)).thenReturn(mockQuestion);

            ExamEnterVO result = examService.enterExam(1L, 1L);

            assertNotNull(result);
            assertEquals(1, result.getQuestions().size());
            // 验证返回的题目信息包含content但不暴露答案
            assertEquals("1+1=?", result.getQuestions().get(0).getContent());
            assertEquals(1, result.getQuestions().get(0).getQuestionType());
        }

        @Test
        @DisplayName("进入考试成功 - examToken为UUID格式（32位无连字符）")
        void enterExamSuccessTokenFormat() {
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(classStudentMapper.selectCount(any())).thenReturn(1L);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(paperQuestionMapper.selectList(any())).thenReturn(Collections.emptyList());

            ExamEnterVO result = examService.enterExam(1L, 1L);

            assertNotNull(result.getExamToken());
            // UUID去掉连字符后应为32位hex字符串
            assertTrue(result.getExamToken().matches("^[0-9a-f]{32}$"),
                    "examToken应为32位hex字符串，实际: " + result.getExamToken());
        }
    }

    // ==================== 提交考试测试 ====================

    @Nested
    @DisplayName("提交考试测试")
    class SubmitExamTests {

        @Test
        @DisplayName("提交考试成功 - 合并Redis缓存和请求答案")
        void submitExamSuccessMergeAnswers() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .answers(List.of(
                            AnswerItem.builder().questionId(2L).answer("C").build()
                    ))
                    .build();

            // Redis缓存中存在questionId=1的答案
            Map<Object, Object> cachedAnswers = new HashMap<>();
            cachedAnswers.put("1", "B");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(anyString())).thenReturn(cachedAnswers);
            when(examAnswerMapper.insert(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            examService.submitExam(request, 1L);

            // 验证答案合并：Redis中1题 + 请求中2题 = 2条ExamAnswer
            verify(examAnswerMapper, times(2)).insert(any(ExamAnswer.class));
            // 验证记录状态更新为SUBMITTED
            assertEquals(ExamStatusConstant.RECORD_SUBMITTED, mockRecord.getStatus());
            assertNotNull(mockRecord.getSubmitTime());
            // 验证Redis清理
            verify(redisTemplate, atLeastOnce()).delete(anyString());
        }

        @Test
        @DisplayName("提交考试成功 - 请求答案覆盖Redis缓存答案")
        void submitExamRequestOverridesCache() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .answers(List.of(
                            AnswerItem.builder().questionId(1L).answer("D").build()  // 覆盖Redis中的答案
                    ))
                    .build();

            Map<Object, Object> cachedAnswers = new HashMap<>();
            cachedAnswers.put("1", "B");  // Redis中1题答案为B，但请求中为D
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(anyString())).thenReturn(cachedAnswers);
            when(examAnswerMapper.insert(any())).thenAnswer(invocation -> {
                ExamAnswer answer = invocation.getArgument(0);
                if (answer.getQuestionId().equals(1L)) {
                    assertEquals("D", answer.getAnswer(), "请求答案应覆盖Redis缓存答案");
                }
                return 1;
            });
            when(examRecordMapper.updateById(any())).thenReturn(1);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            examService.submitExam(request, 1L);
        }

        @Test
        @DisplayName("提交考试失败 - examToken无效")
        void submitExamFailInvalidToken() {
            String examToken = "invalidtoken1234567890123456789";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.submitExam(request, 1L));
            assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
            assertEquals("考试Token无效或已过期", exception.getMessage());
        }

        @Test
        @DisplayName("提交考试失败 - 考试不存在")
        void submitExamFailExamNotFound() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("999:1");
            when(examMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.submitExam(request, 1L));
            assertEquals(ResultCode.NOT_FOUND.getCode(), exception.getCode());
        }

        @Test
        @DisplayName("提交考试失败 - 考试记录不存在")
        void submitExamFailRecordNotFound() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.submitExam(request, 1L));
            assertEquals(ResultCode.NOT_FOUND.getCode(), exception.getCode());
            assertEquals("考试记录不存在", exception.getMessage());
        }

        @Test
        @DisplayName("提交考试失败 - 已提交过此考试")
        void submitExamFailAlreadySubmitted() {
            String examToken = "abc123def456abc123def456abc12345";
            mockRecord.setStatus(ExamStatusConstant.RECORD_SUBMITTED);
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);

            BusinessException exception = assertThrows(BusinessException.class,
                    () -> examService.submitExam(request, 1L));
            assertEquals(ResultCode.CONFLICT.getCode(), exception.getCode());
            assertEquals("已提交过此考试", exception.getMessage());

            // 不应插入任何答案
            verify(examAnswerMapper, never()).insert(any());
        }

        @Test
        @DisplayName("提交考试成功 - 只有请求答案，Redis无缓存")
        void submitExamSuccessOnlyRequestAnswers() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .answers(List.of(
                            AnswerItem.builder().questionId(1L).answer("B").build(),
                            AnswerItem.builder().questionId(2L).answer("C").build()
                    ))
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(anyString())).thenReturn(Collections.emptyMap());
            when(examAnswerMapper.insert(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            examService.submitExam(request, 1L);

            verify(examAnswerMapper, times(2)).insert(any(ExamAnswer.class));
        }

        @Test
        @DisplayName("提交考试成功 - 只有Redis缓存答案，请求无答案")
        void submitExamSuccessOnlyCachedAnswers() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .answers(null)  // 请求中无答案
                    .build();

            Map<Object, Object> cachedAnswers = new HashMap<>();
            cachedAnswers.put("1", "B");
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(anyString())).thenReturn(cachedAnswers);
            when(examAnswerMapper.insert(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            examService.submitExam(request, 1L);

            verify(examAnswerMapper, times(1)).insert(any(ExamAnswer.class));
        }

        @Test
        @DisplayName("提交考试成功 - Redis清理包含progress/timer/token和反查key")
        void submitExamSuccessRedisCleanupComplete() {
            String examToken = "abc123def456abc123def456abc12345";
            ExamSubmitRequest request = ExamSubmitRequest.builder()
                    .examToken(examToken)
                    .answers(List.of(AnswerItem.builder().questionId(1L).answer("B").build()))
                    .build();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("exam:token:value:" + examToken)).thenReturn("1:1");
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectOne(any())).thenReturn(mockRecord);
            when(redisTemplate.opsForHash()).thenReturn(hashOperations);
            when(hashOperations.entries(anyString())).thenReturn(Collections.emptyMap());
            when(examAnswerMapper.insert(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);
            when(redisTemplate.delete(anyString())).thenReturn(true);

            examService.submitExam(request, 1L);

            // 验证删除了4个Redis key：progress、timer、token、tokenValue反查
            verify(redisTemplate, times(4)).delete(anyString());
        }
    }
}
