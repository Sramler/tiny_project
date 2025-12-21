package com.tiny.common.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一错误响应格式
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * 错误码
     */
    private int code;
    
    /**
     * 错误消息
     */
    private String message;
    
    /**
     * 错误详情（可选）
     */
    private String detail;
    
    /**
     * HTTP 状态码
     */
    private Integer status;
    
    /**
     * 请求路径（可选）
     */
    private String path;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 扩展字段（可选）
     */
    private Object data;
    
    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(int code, String message) {
        this();
        this.code = code;
        this.message = message;
    }
    
    public ErrorResponse(int code, String message, Integer status) {
        this(code, message);
        this.status = status;
    }
    
    // Getters and Setters
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getDetail() {
        return detail;
    }
    
    public void setDetail(String detail) {
        this.detail = detail;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * 创建 ErrorResponse 的 Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ErrorResponse response = new ErrorResponse();
        
        public Builder code(int code) {
            response.setCode(code);
            return this;
        }
        
        public Builder message(String message) {
            response.setMessage(message);
            return this;
        }
        
        public Builder detail(String detail) {
            response.setDetail(detail);
            return this;
        }
        
        public Builder status(int status) {
            response.setStatus(status);
            return this;
        }
        
        public Builder path(String path) {
            response.setPath(path);
            return this;
        }
        
        public Builder data(Object data) {
            response.setData(data);
            return this;
        }
        
        public ErrorResponse build() {
            return response;
        }
    }
}

