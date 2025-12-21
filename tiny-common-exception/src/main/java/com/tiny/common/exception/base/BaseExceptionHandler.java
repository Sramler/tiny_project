package com.tiny.common.exception.base;

import com.tiny.common.exception.code.ErrorCode;
import com.tiny.common.exception.exception.BusinessException;
import com.tiny.common.exception.response.ErrorResponse;
import com.tiny.common.exception.util.ExceptionUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础异常处理器
 * 
 * <p>提供通用的异常处理逻辑，各项目可以继承此类并添加项目特定的异常处理</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@RestControllerAdvice
public abstract class BaseExceptionHandler {
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 处理参数验证异常（final，不允许子类覆盖）
     * 
     * @param ex 参数验证异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        BindingResult bindingResult = ex.getBindingResult();
        List<String> errors = bindingResult.getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        String detail = "参数验证失败: " + String.join(", ", errors);
        
        log.warn("参数验证失败: {}", detail);
        
        return buildErrorResponse(
            ErrorCode.VALIDATION_ERROR,
            detail,
            getRequestPath()
        );
    }
    
    /**
     * 处理业务异常
     * 
     * @param ex 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ExceptionUtils.getExceptionDetail(ex), getRequestPath());
    }
    
    /**
     * 处理运行时异常（final，不允许子类覆盖）
     * 
     * <p>注意：子类可以通过 @ExceptionHandler 处理特定的异常类型，
     * 这些特定处理器会优先匹配</p>
     * 
     * @param ex 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex) {
        
        // 如果不是业务异常，使用默认处理
        log.error("运行时异常: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
            ErrorCode.INTERNAL_ERROR,
            ExceptionUtils.isBusinessException(ex) 
                ? ExceptionUtils.getExceptionDetail(ex)
                : "操作失败: " + ExceptionUtils.getExceptionDetail(ex),
            getRequestPath()
        );
    }
    
    /**
     * 处理通用异常（final，不允许子类覆盖）
     * 
     * <p>注意：子类可以通过 @ExceptionHandler 处理特定的异常类型，
     * 这些特定处理器会优先匹配</p>
     * 
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorResponse> handleException(Exception ex) {
        
        log.error("系统异常: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
            ErrorCode.INTERNAL_ERROR,
            "服务器内部错误: " + ExceptionUtils.getExceptionDetail(ex),
            getRequestPath()
        );
    }
    
    
    /**
     * 构建错误响应（钩子方法，子类可以覆盖）
     * 
     * @param errorCode 错误码
     * @param detail 错误详情
     * @param path 请求路径
     * @return 错误响应
     */
    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode, String detail, String path) {
        
        ErrorResponse response = ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .detail(detail)
            .status(errorCode.getStatusValue())
            .path(path)
            .build();
        
        return ResponseEntity
            .status(errorCode.getStatus())
            .body(response);
    }
    
    /**
     * 构建错误响应（便捷方法）
     * 
     * @param errorCode 错误码
     * @param detail 错误详情
     * @return 错误响应
     */
    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode, String detail) {
        return buildErrorResponse(errorCode, detail, getRequestPath());
    }
    
    /**
     * 构建错误响应（便捷方法，使用异常消息作为详情）
     * 
     * @param errorCode 错误码
     * @param ex 异常
     * @return 错误响应
     */
    protected ResponseEntity<ErrorResponse> buildErrorResponse(
            ErrorCode errorCode, Exception ex) {
        return buildErrorResponse(
            errorCode,
            ExceptionUtils.getExceptionDetail(ex),
            getRequestPath()
        );
    }
    
    /**
     * 获取当前请求路径
     * 
     * @return 请求路径，如果获取失败返回 null
     */
    protected String getRequestPath() {
        try {
            ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request != null ? request.getRequestURI() : null;
            }
        } catch (Exception e) {
            log.debug("获取请求路径失败", e);
        }
        return null;
    }
}

