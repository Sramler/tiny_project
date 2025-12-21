package com.tiny.common.exception.exception;

import com.tiny.common.exception.code.ErrorCode;

/**
 * 业务异常
 * 
 * <p>用于业务逻辑中的异常情况，会被 GlobalExceptionHandler 统一处理</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

