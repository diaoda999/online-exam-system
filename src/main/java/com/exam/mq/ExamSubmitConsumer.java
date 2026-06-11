package com.exam.mq;

import com.exam.common.exception.BusinessException;
import com.exam.service.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * 考试自动提交 MQ 消费者
 *
 * 异常处理策略：
 * - 不可重试异常（消息格式错误、业务异常）：直接抛出让消息进入 DLQ
 * - 可重试异常（数据库超时、连接异常）：指数退避重试 3 次，全部失败后进入 DLQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExamSubmitConsumer {

    private final ExamService examService;

    /** 最大重试次数（可重试异常） */
    private static final int MAX_RETRIES = 3;
    /** 基础退避时间（毫秒） */
    private static final long BASE_BACKOFF_MS = 1000;

    @RabbitListener(queues = "#{@gradingQueue.name}")
    public void onMessage(Map<String, Object> message) {
        Long examId = toLong(message.get("examId"));
        Long userId = toLong(message.get("userId"));

        // ---- 不可重试：消息格式错误，直接 DLQ ----
        if (examId == null || userId == null) {
            log.error("[不可重试] 消息格式错误，直接进入DLQ — examId={}, userId={}, rawMessage={}",
                    examId, userId, message);
            throw new IllegalArgumentException(
                    String.format("消息格式错误: examId=%s, userId=%s", examId, userId));
        }

        // ---- 执行自动提交（带重试） ----
        executeWithRetry(examId, userId, message);
    }

    /**
     * 带重试策略的执行方法
     * - 可重试异常（DB超时/连接异常）：指数退避重试最多 MAX_RETRIES 次
     * - 不可重试异常（业务异常）：立即抛出进 DLQ
     */
    private void executeWithRetry(Long examId, Long userId, Map<String, Object> message) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < MAX_RETRIES) {
            attempt++;
            try {
                log.info("执行自动提交: examId={}, userId={}, attempt={}/{}", examId, userId, attempt, MAX_RETRIES);
                examService.autoSubmitExam(examId, userId);
                log.info("自动提交成功: examId={}, userId={}", examId, userId);
                return; // 成功，退出

            } catch (BusinessException e) {
                // 业务异常（如"已提交"、"考试不存在"）——不可重试，直接 DLQ
                log.error("[不可重试] 业务异常 — examId={}, userId={}, error={}", examId, userId, e.getMessage());
                throw e;

            } catch (Exception e) {
                lastException = e;
                if (isRetryable(e)) {
                    if (attempt < MAX_RETRIES) {
                        long delay = BASE_BACKOFF_MS * (1L << (attempt - 1)); // 1s → 2s → 4s
                        log.warn("[可重试] 自动提交失败，{}ms后重试 {} — examId={}, userId={}, error={}",
                                delay, attempt + 1, examId, userId, e.getMessage());
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("重试等待被中断: examId={}, userId={}", examId, userId);
                            throw new RuntimeException("重试被中断", ie);
                        }
                    } else {
                        log.error("[可重试] 自动提交重试{}次全部失败 — examId={}, userId={}, lastError={}",
                                MAX_RETRIES, examId, userId, lastException.getMessage());
                        throw new RuntimeException(
                                String.format("自动提交失败(已重试%d次): examId=%d, userId=%d",
                                        MAX_RETRIES, examId, userId), lastException);
                    }
                } else {
                    // 其他未分类异常，保守起见直接 DLQ
                    log.error("[不可重试] 未分类异常 — examId={}, userId={}, error={}", examId, userId, e.getMessage());
                    throw new RuntimeException("未分类异常，直接进入DLQ: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 判断异常是否可重试
     * 可重试：数据库连接超时、SQL执行超时、网络超时、Spring事务超时
     * 不可重试：业务异常（已在catch块处理）、格式错误（已在入口处理）
     */
    private boolean isRetryable(Exception e) {
        // 数据库访问异常（连接超时、死锁等）
        if (e instanceof DataAccessException) return true;
        // SQL异常（超时、连接断开）
        if (e instanceof SQLException) return true;
        // 网络/IO超时
        if (e instanceof TimeoutException) return true;
        // 检查 cause 链中是否包含可重试异常
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof DataAccessException
                    || cause instanceof SQLException
                    || cause instanceof TimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        // 默认不可重试（保守策略：宁可进 DLQ 也不漏掉）
        return false;
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
