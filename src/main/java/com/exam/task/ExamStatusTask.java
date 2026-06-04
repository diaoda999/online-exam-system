package com.exam.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.constant.ExamStatusConstant;
import com.exam.common.constant.RabbitMQConstant;
import com.exam.model.entity.Exam;
import com.exam.model.entity.ExamRecord;
import com.exam.model.mapper.ExamMapper;
import com.exam.model.mapper.ExamRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考试状态定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamStatusTask {

    private final ExamMapper examMapper;
    private final ExamRecordMapper examRecordMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 每分钟检查一次考试状态
     * - NOT_STARTED 且 startTime <= now → 自动开始
     * - IN_PROGRESS 且 endTime <= now → 自动结束
     */
    @Scheduled(fixedRate = 60000)
    public void checkExamStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 扫描需要自动开始的考试
        List<Exam> toStart = examMapper.selectList(
                new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getStatus, ExamStatusConstant.EXAM_NOT_STARTED)
                        .le(Exam::getStartTime, now)
        );

        for (Exam exam : toStart) {
            try {
                exam.setStatus(ExamStatusConstant.EXAM_IN_PROGRESS);
                examMapper.updateById(exam);

                // Redis 更新状态
                String statusKey = "exam:status:" + exam.getId();
                try {
                    redisTemplate.opsForValue().set(statusKey, ExamStatusConstant.EXAM_IN_PROGRESS,
                            Duration.ofHours(24));
                } catch (Exception e) {
                    log.error("Redis更新考试状态失败: examId={}", exam.getId(), e);
                }

                log.info("考试自动开始: examId={}", exam.getId());
            } catch (Exception e) {
                log.error("考试自动开始失败: examId={}", exam.getId(), e);
            }
        }

        // 扫描需要自动结束的考试
        List<Exam> toEnd = examMapper.selectList(
                new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getStatus, ExamStatusConstant.EXAM_IN_PROGRESS)
                        .le(Exam::getEndTime, now)
        );

        for (Exam exam : toEnd) {
            try {
                exam.setStatus(ExamStatusConstant.EXAM_ENDED);
                examMapper.updateById(exam);

                // Redis 更新状态
                String statusKey = "exam:status:" + exam.getId();
                try {
                    redisTemplate.opsForValue().set(statusKey, ExamStatusConstant.EXAM_ENDED,
                            Duration.ofHours(24));
                } catch (Exception e) {
                    log.error("Redis更新考试状态失败: examId={}", exam.getId(), e);
                }

                // 对未提交的学生发送 MQ 消息触发自动提交
                List<ExamRecord> unsubmitted = examRecordMapper.selectList(
                        new LambdaQueryWrapper<ExamRecord>()
                                .eq(ExamRecord::getExamId, exam.getId())
                                .ne(ExamRecord::getStatus, ExamStatusConstant.RECORD_SUBMITTED)
                                .ne(ExamRecord::getStatus, ExamStatusConstant.RECORD_GRADED)
                );

                for (ExamRecord record : unsubmitted) {
                    try {
                        Map<String, Object> message = new HashMap<>();
                        message.put("examId", exam.getId());
                        message.put("userId", record.getUserId());
                        rabbitTemplate.convertAndSend(
                                RabbitMQConstant.EXAM_EXCHANGE,
                                RabbitMQConstant.GRADING_ROUTING_KEY,
                                message
                        );
                    } catch (Exception e) {
                        log.error("发送自动提交MQ消息失败: examId={}, userId={}",
                                exam.getId(), record.getUserId(), e);
                    }
                }

                log.info("考试自动结束: examId={}, autoSubmitCount={}", exam.getId(), unsubmitted.size());
            } catch (Exception e) {
                log.error("考试自动结束失败: examId={}", exam.getId(), e);
            }
        }
    }
}
