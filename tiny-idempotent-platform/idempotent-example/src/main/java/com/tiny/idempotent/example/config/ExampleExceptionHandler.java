package com.tiny.idempotent.example.config;

import com.tiny.common.exception.base.BaseExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 示例应用异常处理器
 * 
 * <p>继承 BaseExceptionHandler，复用通用异常处理逻辑</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@RestControllerAdvice
public class ExampleExceptionHandler extends BaseExceptionHandler {
    // 继承 BaseExceptionHandler 的通用异常处理
    // 如需处理特定异常，可以在这里添加 @ExceptionHandler 方法
}

