package com.tiny.scheduling.exception;

import org.springframework.http.HttpStatus;

/**
 * 标准化的调度模块错误码。
 */
public enum SchedulingErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    CONFLICT(HttpStatus.CONFLICT),
    OPERATION_NOT_ALLOWED(HttpStatus.FORBIDDEN),
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    SchedulingErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

