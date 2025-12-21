package com.tiny.oauthserver.sys.controller;

import com.tiny.common.exception.base.BaseExceptionHandler;
import com.tiny.common.exception.code.ErrorCode;
import com.tiny.common.exception.response.ErrorResponse;
import com.tiny.idempotent.sdk.exception.IdempotentException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 
 * <p>继承 BaseExceptionHandler，复用通用异常处理逻辑</p>
 * <p>处理项目特定的异常（如 IdempotentException）</p>
 * 
 * <p>所有异常都会返回统一的 ErrorResponse 格式：</p>
 * <pre>
 * {
 *   "code": 1002,
 *   "message": "缺少参数",
 *   "detail": "具体错误信息",
 *   "status": 400,
 *   "path": "/api/users",
 *   "timestamp": "2024-12-21T18:40:00"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    /**
     * 处理幂等性异常（项目特定异常）
     */
    @ExceptionHandler(IdempotentException.class)
    public ResponseEntity<ErrorResponse> handleIdempotentException(IdempotentException ex) {
        log.warn("幂等性检查失败: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.IDEMPOTENT_CONFLICT, ex);
    }
} 