package com.tiny.core.dict.exception;

/**
 * 字典不存在异常
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictNotFoundException extends DictException {
    public DictNotFoundException(String message) {
        super(message);
    }

    public DictNotFoundException(String dictCode) {
        super("Dictionary not found: " + dictCode);
    }
}

