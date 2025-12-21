package com.tiny.common.exception.exception;

import com.tiny.common.exception.code.ErrorCode;

/**
 * 资源不存在异常（404）
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {
    
    public NotFoundException(String resourceName) {
        super(ErrorCode.NOT_FOUND, resourceName + " 不存在");
    }
    
    public NotFoundException(String message, Throwable cause) {
        super(ErrorCode.NOT_FOUND, message, cause);
    }
}

