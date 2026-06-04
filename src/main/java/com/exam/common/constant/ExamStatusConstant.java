package com.exam.common.constant;

/**
 * 考试状态常量类
 */
public final class ExamStatusConstant {

    private ExamStatusConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /** 考试未开始 */
    public static final String EXAM_NOT_STARTED = "NOT_STARTED";

    /** 考试进行中 */
    public static final String EXAM_IN_PROGRESS = "IN_PROGRESS";

    /** 考试已结束 */
    public static final String EXAM_ENDED = "ENDED";

    /** 考试记录-已开始 */
    public static final String RECORD_STARTED = "STARTED";

    /** 考试记录-已提交 */
    public static final String RECORD_SUBMITTED = "SUBMITTED";

    /** 考试记录-已阅卷 */
    public static final String RECORD_GRADED = "GRADED";
}
