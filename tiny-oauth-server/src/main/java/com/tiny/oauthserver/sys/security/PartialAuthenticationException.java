package com.tiny.oauthserver.sys.security;

import org.springframework.security.core.AuthenticationException;

/**
 * 部分认证异常
 * 当用户完成了部分认证步骤（如 PASSWORD），但还需要完成其他步骤（如 TOTP）时抛出
 * 
 * 这个异常不会被当作认证失败处理，而是会触发跳转到下一个认证步骤的页面
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class PartialAuthenticationException extends AuthenticationException {
    
    private final MultiFactorAuthenticationToken partialToken;
    private final String nextStepUrl;
    
    public PartialAuthenticationException(String msg, MultiFactorAuthenticationToken partialToken, String nextStepUrl) {
        super(msg);
        this.partialToken = partialToken;
        this.nextStepUrl = nextStepUrl;
    }
    
    public MultiFactorAuthenticationToken getPartialToken() {
        return partialToken;
    }
    
    public String getNextStepUrl() {
        return nextStepUrl;
    }
}

