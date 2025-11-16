package com.tiny.oauthserver.sys.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

/**
 * 部分认证授权管理器
 * 允许部分认证的 MultiFactorAuthenticationToken（有已完成认证因子的）访问特定端点
 * 
 * 使用场景：
 * - 用户完成了 PASSWORD 验证，但还需要完成 TOTP 验证
 * - 此时 Token 的 authenticated=false，但用户需要访问 TOTP 绑定页面来完成第二个因子验证
 * - 这个授权管理器允许部分认证的 Token 访问 TOTP 相关的端点
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class PartialAuthenticationAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(PartialAuthenticationAuthorizationManager.class);
    
    /**
     * 允许部分认证访问的路径模式
     */
    private static final String[] ALLOWED_PATHS = {
        "/self/security/totp-bind",
        "/self/security/totp-verify",
        "/self/security/totp/bind",
        "/self/security/totp/bind-form",
        "/self/security/totp/pre-bind",
        "/self/security/totp/check",
        "/self/security/totp/check-form",
        "/self/security/skip-mfa-remind",
        "/self/security/status"
    };
    
    /**
     * 检查请求路径是否在允许列表中
     */
    private boolean isAllowedPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        for (String allowedPath : ALLOWED_PATHS) {
            if (requestPath.equals(allowedPath) || requestPath.startsWith(allowedPath + "/")) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        
        // 如果没有认证信息，拒绝访问
        if (auth == null) {
            logger.debug("拒绝访问: 无认证信息");
            return new AuthorizationDecision(false);
        }
        
        String requestPath = context.getRequest().getRequestURI();
        
        // 检查是否是部分认证的 MultiFactorAuthenticationToken
        if (auth instanceof MultiFactorAuthenticationToken mfaToken) {
            // 检查是否有已完成的认证因子
            if (!mfaToken.getCompletedFactors().isEmpty()) {
                // 如果请求的是允许的路径，则允许访问（即使 authenticated=false）
                if (isAllowedPath(requestPath)) {
                    logger.debug("允许部分认证的 Token 访问: {} (已完成因子: {}, authenticated: {})", 
                            requestPath, mfaToken.getCompletedFactors(), mfaToken.isAuthenticated());
                    return new AuthorizationDecision(true);
                }
            }
            
            // 如果是完全认证的 MultiFactorAuthenticationToken，允许访问所有端点
            if (mfaToken.isAuthenticated()) {
                logger.debug("允许完全认证的 Token 访问: {}", requestPath);
                return new AuthorizationDecision(true);
            }
        }
        
        // 检查是否是其他类型的已认证 Token（向后兼容）
        if (auth.isAuthenticated()) {
            logger.debug("允许已认证的 Token 访问: {}", requestPath);
            return new AuthorizationDecision(true);
        }
        
        // 部分认证的 Token 访问了不允许的路径，或未认证，拒绝访问
        logger.debug("拒绝访问: 路径={}, 认证状态={}", requestPath, 
                auth instanceof MultiFactorAuthenticationToken mfaToken2 
                    ? "部分认证(" + mfaToken2.getCompletedFactors() + ")" 
                    : "未认证");
        return new AuthorizationDecision(false);
    }
}

