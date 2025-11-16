package com.tiny.oauthserver.sys.security;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.service.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 部分认证过滤器
 * 检查用户是否处于部分认证状态（已完成 PASSWORD 验证，但还需要 TOTP 验证）
 * 如果用户已绑定 TOTP，但还没有完成 TOTP 验证，则跳转到 TOTP 验证页面
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
public class PartialAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(PartialAuthenticationFilter.class);
    
    private final SecurityService securityService;
    private final UserRepository userRepository;
    
    // 排除的路径（不需要检查部分认证）
    private static final String[] EXCLUDED_PATHS = {
        "/login",
        "/self/security/totp-bind",
        "/self/security/totp-verify",
        "/self/security/totp/bind",
        "/self/security/totp/bind-form",
        "/self/security/totp/pre-bind",
        "/self/security/totp/check",
        "/self/security/totp/check-form",
        "/self/security/skip-mfa-remind",
        "/self/security/status",
        "/favicon.ico",
        "/error",
        "/webjars/**",
        "/assets/**",
        "/css/**",
        "/js/**",
        "/sys/users/**"
    };
    
    public PartialAuthenticationFilter(SecurityService securityService, UserRepository userRepository) {
        this.securityService = securityService;
        this.userRepository = userRepository;
    }
    
    private boolean isExcludedPath(String requestPath) {
        if (requestPath == null) {
            return false;
        }
        for (String excludedPath : EXCLUDED_PATHS) {
            if (excludedPath.endsWith("/**")) {
                String prefix = excludedPath.substring(0, excludedPath.length() - 3);
                if (requestPath.startsWith(prefix)) {
                    return true;
                }
            } else if (requestPath.equals(excludedPath)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // 排除的路径直接放行
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // 检查是否是部分认证的 MultiFactorAuthenticationToken
        if (authentication instanceof MultiFactorAuthenticationToken mfaToken) {
            // 检查是否是部分认证（有已完成因子，但 authenticated=false）
            if (!mfaToken.isAuthenticated() && !mfaToken.getCompletedFactors().isEmpty()) {
                String username = mfaToken.getUsername();
                User user = userRepository.findUserByUsername(username).orElse(null);
                
                if (user != null) {
                    Map<String, Object> status = securityService.getSecurityStatus(user);
                    boolean totpActivated = Boolean.TRUE.equals(status.get("totpActivated"));
                    
                    // 如果用户已绑定并激活 TOTP，但还没有完成 TOTP 验证，跳转到 TOTP 验证页面
                    if (totpActivated && mfaToken.getCompletedFactors().contains("PASSWORD")) {
                        logger.info("用户 {} 已完成密码验证，但还需要 TOTP 验证，跳转到 TOTP 验证页面", username);
                        
                        // 获取原始请求 URL
                        String intendedUrl = requestPath;
                        if (request.getQueryString() != null) {
                            intendedUrl += "?" + request.getQueryString();
                        }
                        String encoded = URLEncoder.encode(intendedUrl, StandardCharsets.UTF_8);
                        
                        // 跳转到 TOTP 验证页面
                        response.sendRedirect("/self/security/totp-verify?redirect=" + encoded);
                        return;
                    }
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

