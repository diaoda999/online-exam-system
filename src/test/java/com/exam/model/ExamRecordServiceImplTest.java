package com.exam.model;

import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.enums.QuestionType;
import com.exam.model.entity.*;
import com.exam.model.mapper.*;
import com.exam.service.impl.ExamRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExamRecordServiceImpl 单元测试
 * 覆盖客观题自动批改核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class ExamRecordServiceImplTest {

    @Mock
    private ExamRecordMapper examRecordMapper;

    @Mock
    private ExamAnswerMapper examAnswerMapper;

    @Mock
    private ExamMapper examMapper;

    @Mock
    private PaperQuestionMapper paperQuestionMapper;

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private ExamRecordServiceImpl examRecordService;

    private Exam mockExam;
    private ExamRecord mockRecord;
    private PaperQuestion mockPaperQuestion;
    private Question singleChoiceQuestion;
    private Question multiChoiceQuestion;
    private Question trueFalseQuestion;
    private Question fillBlankQuestion;
    private Question shortAnswerQuestion;

    @BeforeEach
    void setUp() {
        mockExam = Exam.builder()
                .id(1L)
                .examName("期中考试")
                .paperId(1L)
                .classId(1L)
                .status(ExamStatusConstant.EXAM_ENDED)
                .build();

        mockRecord = ExamRecord.builder()
                .id(1L)
                .examId(1L)
                .userId(1L)
                .status(ExamStatusConstant.RECORD_SUBMITTED)
                .totalScore(-1)
                .objectiveScore(-1)
                .subjectiveScore(-1)
                .build();

        mockPaperQuestion = PaperQuestion.builder()
                .id(1L)
                .paperId(1L)
                .questionId(1L)
                .score(5)
                .sortOrder(0)
                .build();

        // 单选题
        singleChoiceQuestion = Question.builder()
                .id(1L)
                .content("1+1=?")
                .questionType(QuestionType.SINGLE_CHOICE.getCode())
                .answer("B")
                .build();

        // 多选题
        multiChoiceQuestion = Question.builder()
                .id(2L)
                .content("以下哪些是编程语言？")
                .questionType(QuestionType.MULTI_CHOICE.getCode())
                .answer("AB")
                .build();

        // 判断题
        trueFalseQuestion = Question.builder()
                .id(3L)
                .content("地球是平的")
                .questionType(QuestionType.TRUE_FALSE.getCode())
                .answer("F")
                .build();

        // 填空题
        fillBlankQuestion = Question.builder()
                .id(4L)
                .content("中国的首都是___")
                .questionType(QuestionType.FILL_BLANK.getCode())
                .answer("北京")
                .build();

        // 简答题
        shortAnswerQuestion = Question.builder()
                .id(5L)
                .content("请简述Java的特性")
                .questionType(QuestionType.SHORT_ANSWER.getCode())
                .answer("面向对象、跨平台...")
                .build();
    }

    // ==================== 客观题批改测试 ====================

    @Nested
    @DisplayName("客观题自动批改测试")
    class GradeObjectiveTests {

        @Test
        @DisplayName("批改成功 - 单选题答对得满分")
        void gradeObjectiveSingleChoiceCorrect() {
            ExamAnswer correctAnswer = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer("B").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(mockPaperQuestion));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(correctAnswer));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // 验证答案被标记为正确
            assertEquals(1, correctAnswer.getIsCorrect());
            assertEquals(5, correctAnswer.getScore());
            // 验证客观题得分
            assertEquals(5, mockRecord.getObjectiveScore());
            verify(examAnswerMapper).updateById(correctAnswer);
            verify(examRecordMapper).updateById(mockRecord);
        }

        @Test
        @DisplayName("批改成功 - 单选题答错得0分")
        void gradeObjectiveSingleChoiceWrong() {
            ExamAnswer wrongAnswer = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer("A").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(mockPaperQuestion));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(wrongAnswer));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            assertEquals(0, wrongAnswer.getIsCorrect());
            assertEquals(0, wrongAnswer.getScore());
            assertEquals(0, mockRecord.getObjectiveScore());
        }

        @Test
        @DisplayName("批改成功 - 多选题完全匹配判分")
        void gradeObjectiveMultiChoiceCorrect() {
            PaperQuestion pq2 = PaperQuestion.builder()
                    .id(2L).paperId(1L).questionId(2L).score(10).sortOrder(1).build();
            ExamAnswer correctAnswer = ExamAnswer.builder()
                    .id(2L).recordId(1L).questionId(2L).answer("AB").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq2));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(correctAnswer));
            when(questionMapper.selectById(2L)).thenReturn(multiChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            assertEquals(1, correctAnswer.getIsCorrect());
            assertEquals(10, correctAnswer.getScore());
        }

        @Test
        @DisplayName("批改成功 - 多选题部分正确得0分")
        void gradeObjectiveMultiChoicePartialWrong() {
            PaperQuestion pq2 = PaperQuestion.builder()
                    .id(2L).paperId(1L).questionId(2L).score(10).sortOrder(1).build();
            ExamAnswer partialAnswer = ExamAnswer.builder()
                    .id(2L).recordId(1L).questionId(2L).answer("A").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq2));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(partialAnswer));
            when(questionMapper.selectById(2L)).thenReturn(multiChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // 完全匹配判分：部分正确等于错误
            assertEquals(0, partialAnswer.getIsCorrect());
            assertEquals(0, partialAnswer.getScore());
        }

        @Test
        @DisplayName("批改成功 - 判断题正确判分")
        void gradeObjectiveTrueFalseCorrect() {
            PaperQuestion pq3 = PaperQuestion.builder()
                    .id(3L).paperId(1L).questionId(3L).score(5).sortOrder(2).build();
            ExamAnswer correctAnswer = ExamAnswer.builder()
                    .id(3L).recordId(1L).questionId(3L).answer("F").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq3));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(correctAnswer));
            when(questionMapper.selectById(3L)).thenReturn(trueFalseQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            assertEquals(1, correctAnswer.getIsCorrect());
            assertEquals(5, correctAnswer.getScore());
        }

        @Test
        @DisplayName("批改成功 - 填空题不自动批改，保留score=-1")
        void gradeObjectiveFillBlankNotAutoGraded() {
            PaperQuestion pq4 = PaperQuestion.builder()
                    .id(4L).paperId(1L).questionId(4L).score(10).sortOrder(3).build();
            ExamAnswer fillAnswer = ExamAnswer.builder()
                    .id(4L).recordId(1L).questionId(4L).answer("北京").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq4));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(fillAnswer));
            when(questionMapper.selectById(4L)).thenReturn(fillBlankQuestion);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // 填空题不自动批改
            assertEquals(-1, fillAnswer.getScore());
            verify(examAnswerMapper, never()).updateById(fillAnswer);
        }

        @Test
        @DisplayName("批改成功 - 简答题不自动批改，保留score=-1")
        void gradeObjectiveShortAnswerNotAutoGraded() {
            PaperQuestion pq5 = PaperQuestion.builder()
                    .id(5L).paperId(1L).questionId(5L).score(20).sortOrder(4).build();
            ExamAnswer shortAnswer = ExamAnswer.builder()
                    .id(5L).recordId(1L).questionId(5L).answer("面向对象...").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq5));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(shortAnswer));
            when(questionMapper.selectById(5L)).thenReturn(shortAnswerQuestion);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            assertEquals(-1, shortAnswer.getScore());
            verify(examAnswerMapper, never()).updateById(shortAnswer);
        }

        @Test
        @DisplayName("批改成功 - 混合题型：客观题自动批改，主观题保留")
        void gradeObjectiveMixedQuestionTypes() {
            PaperQuestion pq1 = PaperQuestion.builder()
                    .id(1L).paperId(1L).questionId(1L).score(5).sortOrder(0).build();
            PaperQuestion pq4 = PaperQuestion.builder()
                    .id(4L).paperId(1L).questionId(4L).score(10).sortOrder(3).build();
            PaperQuestion pq5 = PaperQuestion.builder()
                    .id(5L).paperId(1L).questionId(5L).score(20).sortOrder(4).build();

            ExamAnswer answer1 = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer("B").score(-1).build();
            ExamAnswer answer4 = ExamAnswer.builder()
                    .id(4L).recordId(1L).questionId(4L).answer("北京").score(-1).build();
            ExamAnswer answer5 = ExamAnswer.builder()
                    .id(5L).recordId(1L).questionId(5L).answer("面向对象...").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(pq1, pq4, pq5));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(answer1, answer4, answer5));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(questionMapper.selectById(4L)).thenReturn(fillBlankQuestion);
            when(questionMapper.selectById(5L)).thenReturn(shortAnswerQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // 客观题被批改
            assertEquals(5, answer1.getScore());
            assertEquals(1, answer1.getIsCorrect());
            // 主观题未批改
            assertEquals(-1, answer4.getScore());
            assertEquals(-1, answer5.getScore());
            // 客观题得分=5，总分=5+0=5
            assertEquals(5, mockRecord.getObjectiveScore());
            assertEquals(5, mockRecord.getTotalScore());
        }

        @Test
        @DisplayName("批改失败 - 考试不存在")
        void gradeObjectiveFailExamNotFound() {
            when(examMapper.selectById(999L)).thenReturn(null);

            assertThrows(Exception.class, () -> examRecordService.gradeObjective(999L));
        }

        @Test
        @DisplayName("批改成功 - 无已提交记录时不处理")
        void gradeObjectiveNoSubmittedRecords() {
            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(paperQuestionMapper.selectList(any())).thenReturn(Collections.emptyList());

            examRecordService.gradeObjective(1L);

            verify(examAnswerMapper, never()).updateById(any());
            verify(examRecordMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("批改成功 - 答案为null时判为错误")
        void gradeObjectiveNullAnswerTreatedAsWrong() {
            ExamAnswer nullAnswer = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer(null).score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(mockPaperQuestion));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(nullAnswer));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            assertEquals(0, nullAnswer.getIsCorrect());
            assertEquals(0, nullAnswer.getScore());
        }

        @Test
        @DisplayName("批改成功 - 答案大小写/空格trim后匹配")
        void gradeObjectiveAnswerTrimComparison() {
            ExamAnswer answerWithSpaces = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer(" B ").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(mockPaperQuestion));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(answerWithSpaces));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // trim后的"B"应与正确答案"B"匹配
            assertEquals(1, answerWithSpaces.getIsCorrect());
            assertEquals(5, answerWithSpaces.getScore());
        }

        @Test
        @DisplayName("批改成功 - 所有客观题正确且无主观题时状态变为GRADED")
        void gradeObjectiveAllGradedStatusChange() {
            ExamAnswer correctAnswer = ExamAnswer.builder()
                    .id(1L).recordId(1L).questionId(1L).answer("B").score(-1).build();

            when(examMapper.selectById(1L)).thenReturn(mockExam);
            when(examRecordMapper.selectList(any())).thenReturn(List.of(mockRecord));
            when(paperQuestionMapper.selectList(any())).thenReturn(List.of(mockPaperQuestion));
            when(examAnswerMapper.selectList(any())).thenReturn(List.of(correctAnswer));
            when(questionMapper.selectById(1L)).thenReturn(singleChoiceQuestion);
            when(examAnswerMapper.updateById(any())).thenReturn(1);
            when(examRecordMapper.updateById(any())).thenReturn(1);

            examRecordService.gradeObjective(1L);

            // 只有客观题且全部批改完成 → 状态应变为GRADED
            assertEquals(ExamStatusConstant.RECORD_GRADED, mockRecord.getStatus());
        }
    }
}
