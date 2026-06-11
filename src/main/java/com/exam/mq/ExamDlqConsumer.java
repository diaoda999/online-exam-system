package com.exam.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 死信队列(DLQ)消费者
 * 监听自动提交失败的消息，记录日志并支持后续人工处理
 */
@Slf4j
@Component
public class ExamDlqConsumer {

    /**
     * 监听死信队列
     * 消费失败的消息会被路由到这里，不丢失
     */
    @RabbitListener(queues = "#{@gradingDeadLetterQueue.name}")
    public void onDeadLetterMessage(Map<String, Object> message) {
        try {
            log.error("==================== DLQ 死信消息 ====================");
            log.error("消息时间: {}", LocalDateTime.now());
            log.error("消息内容: {}", message);

            Object examId = message.get("examId");
            Object userId = message.get("userId");

            log.error("失败详情: examId={}, userId={}", examId, userId);
            log.error("处理建议: 请检查该考生的考试记录和答题进度，手动处理提交");
            log.error("========================================================");

        } catch (Exception e) {
            log.error("DLQ 消费者异常（死信消息无法二次处理）: {}", message, e);
            // 不重新抛出，避免死循环
        }
    }
}
