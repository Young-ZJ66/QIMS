package com.young.common;

/**
 * 业务异常
 * <p>
 * 用于在 Service / Controller 中显式抛出的可预期业务错误，
 * 由 {@link GlobalExceptionHandler} 统一捕获并返回给前端。
 * </p>
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
