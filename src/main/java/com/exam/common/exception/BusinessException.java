package com.exam.common.exception;

import com.exam.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final int code;

    /**
     * 使用 ResultCode 构造
     *
     * @param resultCode 响应状态码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用 ResultCode 和自定义消息构造
     *
     * @param resultCode 响应状态码枚举
     * @param message    自定义错误消息
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 使用自定义状态码和消息构造
     *
     * @param code    自定义状态码
     * @param message 自定义错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
