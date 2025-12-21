package com.tiny.common.exception.exception;

import com.tiny.common.exception.code.ErrorCode;

/**
 * 未授权异常（401）
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(ErrorCode.UNAUTHORIZED, message, cause);
    }
}

