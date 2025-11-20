package com.tiny.oauthserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * OAuth2 异常处理器
 * 处理 OAuth2 相关的异常，提供更友好的错误信息
 */
@RestControllerAdvice
public class OAuth2ExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(OAuth2ExceptionHandler.class);

    @ExceptionHandler(OAuth2AuthorizationException.class)
    public ResponseEntity<String> handle(OAuth2AuthorizationException e) {
        OAuth2Error error = e.getError();
        String errorCode = error.getErrorCode();
        String description = error.getDescription();
        
        logger.error("OAuth2 错误: code={}, description={}, uri={}", 
                     errorCode, description, error.getUri(), e);
        
        String errorMessage = String.format("OAuth2 Error [%s]: %s", errorCode, description);
        return ResponseEntity.badRequest().body(errorMessage);
    }
}