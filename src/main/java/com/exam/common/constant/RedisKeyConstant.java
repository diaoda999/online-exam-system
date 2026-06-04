package com.exam.common.constant;

/**
 * Redis Key 常量类
 */
public final class RedisKeyConstant {

    private RedisKeyConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /** 考试进度 Key: exam:progress:{examId}:{userId} */
    public static final String EXAM_PROGRESS = "exam:progress:%d:%d";

    /** 考试倒计时 Key: exam:timer:{examId}:{userId} */
    public static final String EXAM_TIMER = "exam:timer:%d:%d";

    /** 考试 Token Key: exam:token:{examId}:{userId} */
    public static final String EXAM_TOKEN = "exam:token:%d:%d";

    /** 考试状态 Key: exam:status:{examId}:{userId} */
    public static final String EXAM_STATUS = "exam:status:%d:%d";

    /** 考试统计 Key: exam:stats:{examId} */
    public static final String EXAM_STATS = "exam:stats:%d";

    /** 考试状态 TTL（小时） */
    public static final int EXAM_STATE_TTL_HOURS = 24;

    /** 考试 Token 额外有效时间（分钟） */
    public static final int EXAM_TOKEN_EXTRA_MINUTES = 5;
}
