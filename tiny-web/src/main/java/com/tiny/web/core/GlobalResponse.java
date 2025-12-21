package com.tiny.web.core;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {

    private int code;
    private String message;

    // 成功时返回
    private T data;

    // 错误时返回（如 HttpStatus.UNAUTHORIZED 等）
    private Integer status;

    public static <T> GlobalResponse<T> ok(T data) {
        GlobalResponse<T> r = new GlobalResponse<>();
        r.setCode(ResponseCode.SUCCESS.getCode());
        r.setMessage(ResponseCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    public static GlobalResponse<Void> error(ResponseCode rc) {
        GlobalResponse<Void> r = new GlobalResponse<>();
        r.setCode(rc.getCode());
        r.setMessage(rc.getMessage());
        r.setStatus(rc.getStatus().value());
        return r;
    }

    public static GlobalResponse<Void> error(int code, String message, int status) {
        GlobalResponse<Void> r = new GlobalResponse<>();
        r.setCode(code);
        r.setMessage(message);
        r.setStatus(status);
        return r;
    }

    // --- Getters & Setters ---

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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}