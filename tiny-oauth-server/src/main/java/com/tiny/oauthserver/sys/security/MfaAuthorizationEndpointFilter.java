package com.tiny.oauthserver.sys.security;

import com.tiny.oauthserver.config.FrontendProperties;
import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.service.SecurityService;
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
 * 针对 OIDC 授权端点的 MFA 强制过滤器。
 * <p>
 * 职责：
 * <ul>
 *   <li>拦截对 {@code /oauth2/authorize} 的请求</li>
 *   <li>根据 {@code security.mfa.mode} 和用户 TOTP 绑定状态判断本次会话是否必须完成 TOTP</li>
 *   <li>如果本次需要 TOTP 且当前 Authentication 未完成 TOTP，则重定向到 TOTP 验证页</li>
 *   <li>只有当本次所需因子全部完成时，才允许进入授权端点，确保发出的 Access/ID Token 中的 {@code amr} 能准确反映认证因子</li>
 * </ul>
 *
 * 这属于“思路 A”在 OAuth2/OIDC 授权层的落地：先完成本地 MFA，再进入授权码流程。
 */
public class MfaAuthorizationEndpointFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MfaAuthorizationEndpointFilter.class);

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final FrontendProperties frontendProperties;

    public MfaAuthorizationEndpointFilter(SecurityService securityService,
                                          UserRepository userRepository,
                                          FrontendProperties frontendProperties) {
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.frontendProperties = frontendProperties;
    }

    @Override
    protected boolean shouldNotFilter(@org.springframework.lang.NonNull jakarta.servlet.http.HttpServletRequest request) {
        String uri = request.getRequestURI();
        // 仅拦截 /oauth2/authorize（授权端点），其他请求不处理
        return uri == null || !uri.startsWith("/oauth2/authorize");
    }

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull jakarta.servlet.http.HttpServletRequest request,
                                    @org.springframework.lang.NonNull jakarta.servlet.http.HttpServletResponse response,
                                    @org.springframework.lang.NonNull jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // 没有登录，由 Spring Security 走默认登录流程
            filterChain.doFilter(request, response);
            return;
        }

        // 解析当前用户
        String username = authentication.getName();
        if (username == null || username.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findUserByUsername(username).orElse(null);
        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 读取安全状态，判断本次会话是否必须 TOTP
        Map<String, Object> status = securityService.getSecurityStatus(user);
        boolean requireTotp = Boolean.TRUE.equals(status.get("requireTotp"));
        if (!requireTotp) {
            // 本次会话不要求 TOTP，直接进入授权端点
            filterChain.doFilter(request, response);
            return;
        }

        // 需要 TOTP：检查当前 Authentication 是否已经完成 TOTP 因子
        boolean totpCompleted = false;
        if (authentication instanceof MultiFactorAuthenticationToken mfaToken) {
            totpCompleted = mfaToken.hasCompletedFactor(MultiFactorAuthenticationToken.AuthenticationFactorType.TOTP);
        }

        if (totpCompleted) {
            // 已完成 TOTP，允许进入授权端点
            filterChain.doFilter(request, response);
            return;
        }

        // 本次会话需要 TOTP，但当前认证未完成 TOTP ⇒ 重定向到前端 TOTP 验证页
        String originalUrl = buildOriginalUrl(request);
        String encodedRedirect = URLEncoder.encode(originalUrl, StandardCharsets.UTF_8);

        String totpVerifyUrl = frontendProperties.getTotpVerifyUrl();
        if (totpVerifyUrl == null || totpVerifyUrl.isBlank()) {
            // 兜底：使用服务器端 TOTP 验证页
            totpVerifyUrl = "/self/security/totp-verify";
        }

        String redirectUrl;
        if (totpVerifyUrl.startsWith("redirect:")) {
            String base = totpVerifyUrl.substring("redirect:".length());
            String separator = base.contains("?") ? "&" : "?";
            redirectUrl = base + separator + "redirect=" + encodedRedirect;
        } else {
            String separator = totpVerifyUrl.contains("?") ? "&" : "?";
            redirectUrl = totpVerifyUrl + separator + "redirect=" + encodedRedirect;
        }

        log.info("用户 {} 本次会话需要 TOTP，但当前未完成，拦截 /oauth2/authorize，重定向到 {}", username, redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private String buildOriginalUrl(jakarta.servlet.http.HttpServletRequest request) {
        StringBuilder url = new StringBuilder(request.getRequestURI());
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            url.append('?').append(query);
        }
        return url.toString();
    }
}


