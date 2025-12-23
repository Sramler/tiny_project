package com.tiny.core.dict.exception;

/**
 * 字典异常基类
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictException extends RuntimeException {
    public DictException(String message) {
        super(message);
    }

    public DictException(String message, Throwable cause) {
        super(message, cause);
    }
}

