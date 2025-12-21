package com.tiny.common.exception.util;

import com.tiny.common.exception.code.ErrorCode;
import com.tiny.common.exception.response.ErrorResponse;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 响应工具类
 * 
 * <p>提供便捷方法构建统一格式的错误响应</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class ResponseUtils {
    
    /**
     * 构建错误响应
     * 
     * @param errorCode 错误码
     * @param detail 错误详情
     * @param request HTTP 请求（可选，用于获取路径）
     * @return 错误响应
     */
    public static ResponseEntity<ErrorResponse> error(ErrorCode errorCode, String detail, HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .detail(detail)
            .status(errorCode.getStatusValue())
            .path(request != null ? request.getRequestURI() : null)
            .build();
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
    
    /**
     * 构建错误响应（不包含路径）
     */
    public static ResponseEntity<ErrorResponse> error(ErrorCode errorCode, String detail) {
        return error(errorCode, detail, null);
    }
    
    /**
     * 构建错误响应（使用异常消息作为详情）
     */
    public static ResponseEntity<ErrorResponse> error(ErrorCode errorCode, Exception ex, HttpServletRequest request) {
        return error(errorCode, ExceptionUtils.getExceptionDetail(ex), request);
    }
    
    /**
     * 未授权错误（401）
     */
    public static ResponseEntity<ErrorResponse> unauthorized(String detail) {
        return error(ErrorCode.UNAUTHORIZED, detail);
    }
    
    /**
     * 未授权错误（401）- 未登录
     */
    public static ResponseEntity<ErrorResponse> unauthorized() {
        return unauthorized("未登录");
    }
    
    /**
     * 参数验证错误（400）
     */
    public static ResponseEntity<ErrorResponse> badRequest(String detail) {
        return error(ErrorCode.VALIDATION_ERROR, detail);
    }
    
    /**
     * 缺少参数错误（400）
     */
    public static ResponseEntity<ErrorResponse> missingParameter(String parameterName) {
        return error(ErrorCode.MISSING_PARAMETER, "缺少参数: " + parameterName);
    }
    
    /**
     * 无效参数错误（400）
     */
    public static ResponseEntity<ErrorResponse> invalidParameter(String detail) {
        return error(ErrorCode.INVALID_PARAMETER, detail);
    }
    
    /**
     * 资源不存在错误（404）
     */
    public static ResponseEntity<ErrorResponse> notFound(String resourceName) {
        return error(ErrorCode.NOT_FOUND, resourceName + " 不存在");
    }
    
    /**
     * 权限不足错误（403）
     */
    public static ResponseEntity<ErrorResponse> forbidden(String detail) {
        return error(ErrorCode.FORBIDDEN, detail);
    }
    
    /**
     * 业务错误（409）
     */
    public static ResponseEntity<ErrorResponse> businessError(String detail) {
        return error(ErrorCode.BUSINESS_ERROR, detail);
    }
    
    /**
     * 服务器内部错误（500）
     */
    public static ResponseEntity<ErrorResponse> internalError(String detail) {
        return error(ErrorCode.INTERNAL_ERROR, detail);
    }
    
    /**
     * 服务器内部错误（500）- 使用异常
     */
    public static ResponseEntity<ErrorResponse> internalError(Exception ex) {
        return error(ErrorCode.INTERNAL_ERROR, ExceptionUtils.getExceptionDetail(ex));
    }
}

