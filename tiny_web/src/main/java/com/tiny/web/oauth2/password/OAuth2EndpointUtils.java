package com.tiny.web.oauth2.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import java.util.*;

/**
 * 自定义的 Endpoint 工具类，提取客户端信息和请求参数。
 */
public final class OAuth2EndpointUtils {

    private OAuth2EndpointUtils() {
    }

    /**
     * 获取已认证的客户端，如果不存在则抛出异常
     */
    public static Authentication getAuthenticatedClientElseThrowInvalidClient(HttpServletRequest request) {
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        if (clientPrincipal == null || !clientPrincipal.isAuthenticated()) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, "客户端未认证");
        }
        return clientPrincipal;
    }

    /**
     * 获取请求中某个参数的值列表
     */
    public static List<String> getParameterValues(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return values != null ? Arrays.asList(values) : Collections.emptyList();
    }

    /**
     * 获取请求中某个参数的首个值
     */
    public static String getParameter(HttpServletRequest request, String name) {
        String[] values = request.getParameterValues(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    private static void throwError(String errorCode, String description) {
        OAuth2Error error = new OAuth2Error(errorCode, description, null);
        throw new OAuth2AuthenticationException(error);
    }
}