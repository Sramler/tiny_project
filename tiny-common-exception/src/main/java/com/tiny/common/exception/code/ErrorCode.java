package com.tiny.common.exception.code;

import org.springframework.http.HttpStatus;

/**
 * 通用错误码枚举
 * 
 * <p>定义统一的错误码规范，各项目可以扩展自己的错误码</p>
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public enum ErrorCode {
    
    // ==================== 成功 ====================
    SUCCESS(200, "操作成功", HttpStatus.OK),
    
    // ==================== 客户端错误 (400-499) ====================
    VALIDATION_ERROR(1001, "参数校验失败", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1002, "缺少参数", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(1003, "无效的参数", HttpStatus.BAD_REQUEST),
    METHOD_NOT_SUPPORTED(1004, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED(1005, "媒体类型不支持", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_FOUND(1006, "资源不存在", HttpStatus.NOT_FOUND),
    
    // ==================== 认证错误 (401) ====================
    UNAUTHORIZED(2001, "未授权", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(2002, "令牌已过期", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(2003, "无效的令牌", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING(2004, "缺少令牌", HttpStatus.UNAUTHORIZED),
    
    // ==================== 权限错误 (403) ====================
    ACCESS_DENIED(3001, "拒绝访问", HttpStatus.FORBIDDEN),
    FORBIDDEN(3002, "没有权限", HttpStatus.FORBIDDEN),
    
    // ==================== 业务错误 (409) ====================
    IDEMPOTENT_CONFLICT(4001, "请勿重复提交", HttpStatus.CONFLICT),
    BUSINESS_ERROR(4002, "业务处理失败", HttpStatus.CONFLICT),
    
    // ==================== 服务器错误 (500-599) ====================
    INTERNAL_ERROR(5001, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(5002, "服务不可用", HttpStatus.SERVICE_UNAVAILABLE),
    UNKNOWN_ERROR(5003, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final int code;
    private final String message;
    private final HttpStatus status;
    
    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public HttpStatus getStatus() {
        return status;
    }
    
    public int getStatusValue() {
        return status.value();
    }
}

