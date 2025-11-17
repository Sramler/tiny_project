package com.tiny.oauthserver.config;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.security.MultiFactorAuthenticationToken;
import com.tiny.oauthserver.sys.security.MultiFactorAuthenticationSessionManager;
import com.tiny.oauthserver.sys.service.SecurityService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final FrontendProperties frontendProperties;
    private final MultiFactorAuthenticationSessionManager sessionManager;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomLoginSuccessHandler(SecurityService securityService, 
                                    UserRepository userRepository,
                                    FrontendProperties frontendProperties,
                                    MultiFactorAuthenticationSessionManager sessionManager) {
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.frontendProperties = frontendProperties;
        this.sessionManager = sessionManager;
    }

    @Override
    /**
     * 登录成功后处理流程：
     * 1. 获取当前登录用户名，查对应用户实例。
     * 2. 检查是否是部分认证（已完成 PASSWORD，但还需要 TOTP）
     * 3. 拉取用户当前二步认证状态（是否已绑定、激活、系统是否要求强制二次认证）。
     * 4. 解析当前用户原意图跳转URL（优先SavedRequest, 其次redirect参数，兜底首页）。
     * 5. 跳转策略：
     *    - 完全关闭MFA (enforce=off)：直接跳原意图页面。
     *    - 部分认证（已完成 PASSWORD，但还需要 TOTP）：跳转到 TOTP 验证页面。
     *    - 未激活（无论是否已创建记录）：统一跳转至 totp-bind 继续绑定流程（用户可继续扫码/输入验证码激活）。
     *    - 已激活且完全认证：直接跳回原意图页面。
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userRepository.findUserByUsername(username).orElse(null);
        if (user == null) {
            response.sendRedirect("/");
            return;
        }
        
        Map<String, Object> status = securityService.getSecurityStatus(user);
        boolean totpBound = Boolean.TRUE.equals(status.get("totpBound"));
        boolean totpActivated = Boolean.TRUE.equals(status.get("totpActivated"));
        boolean disableMfa = Boolean.TRUE.equals(status.get("disableMfa"));
        boolean skipMfaRemind = Boolean.TRUE.equals(status.get("skipMfaRemind"));
        boolean forceMfa = Boolean.TRUE.equals(status.get("forceMfa"));

        // 解析原意图跳转 URL
        String intendedUrl = extractIntendedUrl(request, response);
        if (intendedUrl == null || intendedUrl.isBlank()) intendedUrl = "/";
        if ("/login".equals(intendedUrl) || intendedUrl.startsWith("/login?")) {
            intendedUrl = "/";
        }
        String encodedUrl = URLEncoder.encode(intendedUrl, StandardCharsets.UTF_8);

        // 1️⃣ 完全关闭 MFA，直接跳转
        if (disableMfa) {
            logger.debug("用户 {} 已关闭 MFA，直接跳转 {}", user.getUsername(), intendedUrl);
            sessionManager.promoteToFullyAuthenticated(user, request, response);
            response.sendRedirect(intendedUrl);
            return;
        }

        // 2️⃣ 检查是否是 MultiFactorAuthenticationToken（部分认证状态）
        if (authentication instanceof MultiFactorAuthenticationToken mfaToken) {
            if (mfaToken.hasCompletedFactor(MultiFactorAuthenticationToken.AuthenticationFactorType.PASSWORD) &&
                !mfaToken.hasCompletedFactor(MultiFactorAuthenticationToken.AuthenticationFactorType.TOTP) &&
                totpActivated) {
                logger.info("用户 {} 已完成密码验证，但还需 TOTP 验证，跳转 TOTP 验证页", user.getUsername());
                String totpVerifyUrl = buildFrontendUrl(frontendProperties.getTotpVerifyUrl(), request, "redirect", encodedUrl);
                redirectToFrontend(totpVerifyUrl, request, response);
                return;
            }
        }

        // 3️⃣ 已绑定但未激活 TOTP → 跳转绑定页面
        if (totpBound && !totpActivated) {
            logger.info("用户 {} 已绑定 TOTP 但未激活，跳转 TOTP 绑定页", user.getUsername());
            String totpBindUrl = buildFrontendUrl(frontendProperties.getTotpBindUrl(), request, "redirect", encodedUrl);
            redirectToFrontend(totpBindUrl, request, response);
            return;
        }

        // 4️⃣ 未绑定 TOTP 且未跳过或系统强制 MFA → 跳转绑定页面
        if (!totpBound && !disableMfa && (forceMfa || !skipMfaRemind)) {
            logger.info("用户 {} 未绑定 TOTP，跳转 TOTP 绑定页 (forceMfa={}, skipMfaRemind={})",
                    user.getUsername(), forceMfa, skipMfaRemind);
            String totpBindUrl = buildFrontendUrl(frontendProperties.getTotpBindUrl(), request, "redirect", encodedUrl);
            redirectToFrontend(totpBindUrl, request, response);
            return;
        }

        // 5️⃣ 已完成所有认证或无需 MFA → 升级为完全认证并跳转目标页面
        sessionManager.promoteToFullyAuthenticated(user, request, response);
        
        if (intendedUrl.startsWith("/") && !intendedUrl.startsWith("/api/") && !intendedUrl.startsWith("/oauth2/")) {
            // 前端路由处理
            String loginUrl = frontendProperties.getLoginUrl();
            if (loginUrl.startsWith("redirect:")) {
                String baseUrl = loginUrl.substring("redirect:".length());
                String devServerBase = baseUrl.substring(0, baseUrl.indexOf("/", baseUrl.indexOf("://") + 3));
                String separator = intendedUrl.contains("?") ? "&" : "?";
                String redirectUrl = devServerBase + intendedUrl + separator + "formLogin=true";
                logger.info("开发环境重定向前端路由: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
                return;
            } else {
                try {
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/dist/index.html");
                    dispatcher.forward(request, response);
                    return;
                } catch (Exception e) {
                    logger.warn("前端页面转发失败，使用重定向: {}", e.getMessage());
                }
            }
        }

        // 默认重定向
        response.sendRedirect(intendedUrl);
    }

    private String extractIntendedUrl(HttpServletRequest request, HttpServletResponse response) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null && savedRequest.getRedirectUrl() != null) return savedRequest.getRedirectUrl();
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) return redirect;
        return null;
    }

    private String buildFrontendUrl(String configuredUrl, HttpServletRequest request, String paramName, String paramValue) {
        if (configuredUrl.startsWith("redirect:")) {
            StringBuilder url = new StringBuilder(configuredUrl);
            url.append(configuredUrl.contains("?") ? "&" : "?");
            url.append(paramName).append("=").append(paramValue);
            return url.toString();
        }
        return configuredUrl; // forward 默认返回
    }

    private void redirectToFrontend(String url, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (url.startsWith("redirect:")) {
            response.sendRedirect(url.substring("redirect:".length()));
        } else if (url.startsWith("forward:")) {
            RequestDispatcher dispatcher = request.getRequestDispatcher(url.substring("forward:".length()));
            dispatcher.forward(request, response);
        } else {
            response.sendRedirect(url);
        }
    }
}
