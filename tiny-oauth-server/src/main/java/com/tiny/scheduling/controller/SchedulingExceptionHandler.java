package com.tiny.scheduling.controller;

import com.tiny.scheduling.exception.SchedulingErrorCode;
import com.tiny.scheduling.exception.SchedulingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * 调度模块统一异常处理。
 */
@RestControllerAdvice(basePackages = "com.tiny.scheduling")
public class SchedulingExceptionHandler {

    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<ErrorResponse> handleSchedulingException(SchedulingException ex, HttpServletRequest request) {
        SchedulingErrorCode code = ex.getErrorCode();
        HttpStatus status = code.getHttpStatus();
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code.name(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(SchedulingErrorCode.VALIDATION_ERROR.name(), message, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(SchedulingErrorCode.SYSTEM_ERROR.name(), ex.getMessage(), request.getRequestURI()));
    }

    public record ErrorResponse(String code, String message, String path, Instant timestamp) {
        public static ErrorResponse of(String code, String message, String path) {
            return new ErrorResponse(code, message, path, Instant.now());
        }
    }
}

