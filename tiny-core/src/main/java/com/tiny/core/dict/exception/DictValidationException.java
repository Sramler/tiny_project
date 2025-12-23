package com.tiny.core.dict.exception;

/**
 * 字典校验异常
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictValidationException extends DictException {
    public DictValidationException(String message) {
        super(message);
    }

    public DictValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

