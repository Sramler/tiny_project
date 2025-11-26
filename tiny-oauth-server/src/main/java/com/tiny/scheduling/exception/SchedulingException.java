package com.tiny.scheduling.exception;

/**
 * 调度模块统一业务异常。
 */
public class SchedulingException extends RuntimeException {

    private final SchedulingErrorCode errorCode;

    public SchedulingException(SchedulingErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SchedulingException(SchedulingErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public SchedulingErrorCode getErrorCode() {
        return errorCode;
    }
}

