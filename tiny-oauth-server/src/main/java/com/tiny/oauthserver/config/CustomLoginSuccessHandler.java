package com.tiny.oauthserver.config;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.service.SecurityService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSuccessHandler.class);

    private final SecurityService securityService;
    private final UserRepository userRepository;
    private final FrontendProperties frontendProperties;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomLoginSuccessHandler(SecurityService securityService, 
                                    UserRepository userRepository,
                                    FrontendProperties frontendProperties) {
        this.securityService = securityService;
        this.userRepository = userRepository;
        this.frontendProperties = frontendProperties;
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

        String intendedUrl = extractIntendedUrl(request, response);
        if (intendedUrl == null || intendedUrl.isBlank()) intendedUrl = "/";
        // 如果 intendedUrl 是登录页面，登录成功后应该跳转到首页，避免循环重定向
        if ("/login".equals(intendedUrl) || intendedUrl.startsWith("/login?")) {
            intendedUrl = "/";
        }
        String encoded = URLEncoder.encode(intendedUrl, StandardCharsets.UTF_8);

        // 完全关闭MFA时直接跳转，不弹任何页面
        if (disableMfa) {
            response.sendRedirect(intendedUrl);
            return;
        }
        
        // 检查是否是多因素认证 Token，且只完成了 PASSWORD 验证（还需要 TOTP 验证）
        if (authentication instanceof com.tiny.oauthserver.sys.security.MultiFactorAuthenticationToken mfaToken) {
            java.util.Set<String> completedFactors = mfaToken.getCompletedFactors();
            // 如果只完成了 PASSWORD 验证，且用户已绑定并激活 TOTP，需要跳转到 TOTP 验证页面
            if (completedFactors.contains("PASSWORD") && !completedFactors.contains("TOTP") && totpActivated) {
                logger.info("用户 {} 已完成密码验证，但还需要 TOTP 验证，跳转到 TOTP 验证页面", user.getUsername());
                String totpVerifyUrl = buildFrontendUrl(frontendProperties.getTotpVerifyUrl(), request, "redirect", encoded);
                redirectToFrontend(totpVerifyUrl, request, response);
                return;
            }
        }
        
        // 如果用户已绑定 TOTP 但未激活，跳转到 TOTP 绑定页面
        if (totpBound && !totpActivated) {
            logger.info("用户 {} 已绑定 TOTP 但未激活，跳转到 TOTP 绑定页面", user.getUsername());
            String totpBindUrl = buildFrontendUrl(frontendProperties.getTotpBindUrl(), request, "redirect", encoded);
            redirectToFrontend(totpBindUrl, request, response);
            return;
        }

        // 如果用户未绑定 TOTP 且未选择跳过提醒，或系统强制 MFA，跳转到绑定页面
        if (!totpBound && !disableMfa && (forceMfa || !skipMfaRemind)) {
            logger.info("用户 {} 未绑定 TOTP，跳转到 TOTP 绑定页面( forceMfa={}, skipMfaRemind={} )",
                    user.getUsername(), forceMfa, skipMfaRemind);
            String totpBindUrl = buildFrontendUrl(frontendProperties.getTotpBindUrl(), request, "redirect", encoded);
            redirectToFrontend(totpBindUrl, request, response);
            return;
        }
        
        // 用户未绑定 TOTP 或已激活且完成所有认证：直接进入业务
        // 对于 intendedUrl，如果是前端路由，需要根据环境使用不同的处理方式
        if (intendedUrl.startsWith("/") && !intendedUrl.startsWith("/api/") && !intendedUrl.startsWith("/oauth2/")) {
            // 可能是前端路由
            String loginUrl = frontendProperties.getLoginUrl();
            if (loginUrl.startsWith("redirect:")) {
                // 开发环境：重定向到 Vite dev server
                String baseUrl = loginUrl.substring("redirect:".length());
                // 提取基础 URL（去掉路径部分）
                String devServerBase = baseUrl.substring(0, baseUrl.indexOf("/", baseUrl.indexOf("://") + 3));
                // 添加查询参数标识这是表单登录后的重定向
                String separator = intendedUrl.contains("?") ? "&" : "?";
                String redirectUrl = devServerBase + intendedUrl + separator + "formLogin=true";
                logger.info("开发环境：重定向到前端路由: {}", redirectUrl);
                response.sendRedirect(redirectUrl);
                return;
            } else {
                // 生产环境：forward 到打包后的静态文件
                try {
                    RequestDispatcher dispatcher = request.getRequestDispatcher("/dist/index.html");
                    dispatcher.forward(request, response);
                    return;
                } catch (Exception e) {
                    logger.warn("无法转发到前端页面，使用重定向: {}", e.getMessage());
                }
            }
        }
        response.sendRedirect(intendedUrl);
    }

    private String extractIntendedUrl(HttpServletRequest request, HttpServletResponse response) {
        // 1) SavedRequest by Spring Security
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null && savedRequest.getRedirectUrl() != null) {
            return savedRequest.getRedirectUrl();
        }
        // 2) explicit redirect param if any
        String redirect = request.getParameter("redirect");
        if (redirect != null && !redirect.isBlank()) return redirect;
        return null;
    }

    /**
     * 构建前端 URL，支持查询参数传递
     * - 如果是 redirect: 前缀，将查询参数附加到重定向 URL
     * - 如果是 forward: 前缀，直接返回（forward 会自动保留查询参数）
     */
    private String buildFrontendUrl(String configuredUrl, HttpServletRequest request, String paramName, String paramValue) {
        if (configuredUrl.startsWith("redirect:")) {
            // 开发环境：重定向到 Vite dev server，需要附加查询参数
            String baseUrl = configuredUrl.substring("redirect:".length());
            String queryString = request.getQueryString();
            StringBuilder url = new StringBuilder(configuredUrl);
            
            // 添加新的查询参数
            if (baseUrl.contains("?")) {
                url.append("&");
            } else {
                url.append("?");
            }
            url.append(paramName).append("=").append(paramValue);
            
            // 保留原有的查询参数（如果有）
            if (queryString != null && !queryString.isEmpty()) {
                url.append("&").append(queryString);
            }
            
            return url.toString();
        } else {
            // 生产环境：forward 会自动保留查询参数，但我们需要手动添加
            // 由于 forward 会保留原始请求的查询参数，我们需要通过 request.setAttribute 传递
            // 或者直接返回 forward URL，让前端路由处理
            return configuredUrl;
        }
    }

    /**
     * 重定向或转发到前端页面
     * - redirect: 前缀使用 response.sendRedirect
     * - forward: 前缀使用 RequestDispatcher.forward
     */
    private void redirectToFrontend(String url, HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        if (url.startsWith("redirect:")) {
            // 开发环境：重定向到 Vite dev server
            String redirectUrl = url.substring("redirect:".length());
            response.sendRedirect(redirectUrl);
        } else if (url.startsWith("forward:")) {
            // 生产环境：转发到打包后的静态文件
            String forwardPath = url.substring("forward:".length());
            RequestDispatcher dispatcher = request.getRequestDispatcher(forwardPath);
            dispatcher.forward(request, response);
        } else {
            // 默认使用重定向
            response.sendRedirect(url);
        }
    }
}
