package com.tiny.web.sys.controller;

import java.time.Instant;

public class ErrorResponse {
    private final int code;
    private final String message;
    private final int status;
    private final String timestamp;
    private final String path;
    private final String traceId;

    public ErrorResponse(int code, String message, int status, String path, String traceId) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now().toString();
        this.path = path;
        this.traceId = traceId;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public String getTraceId() { return traceId; }
}