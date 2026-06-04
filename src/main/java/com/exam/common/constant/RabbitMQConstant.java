package com.exam.common.constant;

/**
 * RabbitMQ 常量类
 */
public final class RabbitMQConstant {

    private RabbitMQConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /** 主交换机 */
    public static final String EXAM_EXCHANGE = "exam.exchange";

    /** 自动阅卷队列 */
    public static final String GRADING_QUEUE = "exam.grading.queue";

    /** 自动阅卷路由键 */
    public static final String GRADING_ROUTING_KEY = "exam.grading";

    /** 死信交换机 */
    public static final String DLX_EXCHANGE = "exam.dlx.exchange";

    /** 死信队列 */
    public static final String GRADING_DLQ = "exam.grading.dlq";

    /** 死信路由键 */
    public static final String GRADING_DLQ_ROUTING_KEY = "exam.grading.failed";
}
