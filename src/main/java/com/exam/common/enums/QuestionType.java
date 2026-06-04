package com.exam.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 题目类型枚举
 */
@Getter
@AllArgsConstructor
public enum QuestionType {

    SINGLE_CHOICE(1, "单选题"),
    MULTI_CHOICE(2, "多选题"),
    TRUE_FALSE(3, "判断题"),
    FILL_BLANK(4, "填空题"),
    SHORT_ANSWER(5, "简答题");

    /** 类型编码 */
    private final Integer code;

    /** 类型描述 */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 类型编码
     * @return 题目类型枚举
     */
    public static QuestionType of(Integer code) {
        if (code == null) {
            return null;
        }
        for (QuestionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
