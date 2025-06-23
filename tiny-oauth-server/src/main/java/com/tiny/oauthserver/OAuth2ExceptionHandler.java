package com.tiny.oauthserver;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OAuth2ExceptionHandler {
    @ExceptionHandler(OAuth2AuthorizationException.class)
    public ResponseEntity<String> handle(OAuth2AuthorizationException e) {
        return ResponseEntity.badRequest().body("OAuth2 Error: " + e.getError().getDescription());
    }
}