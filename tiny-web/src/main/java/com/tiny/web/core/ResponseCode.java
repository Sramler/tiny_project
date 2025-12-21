package com.tiny.web.core;

import org.springframework.http.HttpStatus;

public enum ResponseCode {

    // 成功响应
    SUCCESS(200, "操作成功", HttpStatus.OK),
    LOGIN_SUCCESS(2001, "登录成功", HttpStatus.OK),
    LOGOUT_SUCCESS(2002, "登出成功", HttpStatus.OK),
    TOKEN_REFRESHED(2003, "令牌刷新成功", HttpStatus.OK),
    USER_INFO_SUCCESS(2004, "用户信息获取成功", HttpStatus.OK),

    // 通用错误
    VALIDATION_ERROR(1003, "参数校验失败", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1004, "缺少参数", HttpStatus.BAD_REQUEST),
    INVALID_PARAMETER(1005, "无效的参数", HttpStatus.BAD_REQUEST),
    METHOD_NOT_SUPPORTED(1006, "请求方法不支持", HttpStatus.METHOD_NOT_ALLOWED),
    MEDIA_TYPE_NOT_SUPPORTED(1007, "媒体类型不支持", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_FOUND(1008, "资源不存在", HttpStatus.NOT_FOUND),

    // 鉴权相关
    UNAUTHORIZED(1002, "未授权", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1011, "令牌已过期", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1012, "无效的令牌", HttpStatus.UNAUTHORIZED),
    TOKEN_MISSING(1013, "缺少令牌", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED(1014, "登录失败", HttpStatus.UNAUTHORIZED),
    LOGOUT_FAILED(1015, "登出失败", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_AUTHENTICATION(1016, "认证信息不足", HttpStatus.UNAUTHORIZED),

    // 权限相关
    ACCESS_DENIED(1101, "拒绝访问", HttpStatus.FORBIDDEN),
    FORBIDDEN(1102, "没有权限", HttpStatus.FORBIDDEN),

    // 业务错误
    USER_NOT_FOUND(1001, "用户不存在", HttpStatus.NOT_FOUND),

    // 系统异常
    INTERNAL_ERROR(5000, "服务器内部错误", HttpStatus.INTERNAL_SERVER_ERROR),
    UNKNOWN_ERROR(5001, "未知错误", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(5002, "服务不可用", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ResponseCode(int code, String message, HttpStatus status) {
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

    public static ResponseCode fromCode(int code) {
        for (ResponseCode value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return UNKNOWN_ERROR;
    }
}