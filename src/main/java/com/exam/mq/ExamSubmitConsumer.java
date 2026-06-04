package com.exam.mq;

import com.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 考试自动提交 MQ 消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmitConsumer {

    private final ExamService examService;

    /**
     * 监听自动提交队列
     * 消息格式：{"examId": 1, "userId": 2}
     */
    @RabbitListener(queues = "#{@gradingQueue.name}")
    public void onMessage(Map<String, Object> message) {
        try {
            Long examId = toLong(message.get("examId"));
            Long userId = toLong(message.get("userId"));

            if (examId == null || userId == null) {
                log.error("自动提交消息格式错误: {}", message);
                return;
            }

            log.info("收到自动提交消息: examId={}, userId={}", examId, userId);
            examService.autoSubmitExam(examId, userId);

        } catch (Exception e) {
            log.error("自动提交考试失败，消息将进入DLQ: {}", message, e);
            throw e; // 重新抛出，让消息进入死信队列
        }
    }

    /**
     * 安全转换为 Long
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
