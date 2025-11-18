package com.tiny.oauthserver.sys.filter;

import com.tiny.oauthserver.config.HttpRequestLoggingProperties;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 负责包装请求/响应并生成 trace/request 信息的过滤器
 * 日志写入在 {@link com.tiny.oauthserver.sys.interceptor.HttpRequestLoggingInterceptor} 中完成
 */
@Component
@Order(Integer.MAX_VALUE - 10)
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    public static final String ATTR_START_TIME = HttpRequestLoggingFilter.class.getName() + ".START";
    public static final String ATTR_TRACE_ID = HttpRequestLoggingFilter.class.getName() + ".TRACE_ID";
    public static final String ATTR_SPAN_ID = HttpRequestLoggingFilter.class.getName() + ".SPAN_ID";
    public static final String ATTR_REQUEST_ID = HttpRequestLoggingFilter.class.getName() + ".REQUEST_ID";
    public static final String ATTR_ENV = HttpRequestLoggingFilter.class.getName() + ".ENV";
    public static final String ATTR_SERVICE = HttpRequestLoggingFilter.class.getName() + ".SERVICE";
    public static final String ATTR_USER_ID = HttpRequestLoggingFilter.class.getName() + ".USER_ID";

    private static final List<String> TRACE_ID_HEADERS = List.of(
            "traceparent",
            "x-b3-traceid",
            "x-trace-id",
            "trace-id",
            "x-request-id"
    );

    private static final List<String> SPAN_ID_HEADERS = List.of(
            "x-b3-spanid",
            "span-id"
    );

    private final HttpRequestLoggingProperties properties;
    private final String serviceName;
    private final String env;

    public HttpRequestLoggingFilter(HttpRequestLoggingProperties properties,
                                    Environment environment,
                                    @Value("${spring.application.name:oauth-server}") String serviceName) {
        this.properties = properties;
        this.serviceName = serviceName;
        this.env = resolveEnv(environment);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String uri = request.getRequestURI();
        if (!StringUtils.hasText(uri)) {
            return false;
        }
        return properties.getExcludedPathPrefixes().stream()
                .filter(StringUtils::hasText)
                .anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = wrapRequest(request);
        ContentCachingResponseWrapper responseWrapper = wrapResponse(response);

        long start = System.currentTimeMillis();
        requestWrapper.setAttribute(ATTR_START_TIME, start);
        requestWrapper.setAttribute(ATTR_SERVICE, serviceName);
        requestWrapper.setAttribute(ATTR_ENV, env);

        String requestId = getOrCreateRequestId(requestWrapper, responseWrapper);
        String traceId = resolveTraceId(requestWrapper, requestId);
        String spanId = resolveSpanId(requestWrapper);

        requestWrapper.setAttribute(ATTR_REQUEST_ID, requestId);
        requestWrapper.setAttribute(ATTR_TRACE_ID, traceId);
        requestWrapper.setAttribute(ATTR_SPAN_ID, spanId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            if (requestWrapper.isAsyncStarted()) {
                requestWrapper.getAsyncContext().addListener(new AsyncListener() {
                    @Override
                    public void onComplete(AsyncEvent event) throws IOException {
                        responseWrapper.copyBodyToResponse();
                    }

                    @Override
                    public void onTimeout(AsyncEvent event) {
                    }

                    @Override
                    public void onError(AsyncEvent event) {
                    }

                    @Override
                    public void onStartAsync(AsyncEvent event) {
                    }
                });
            } else {
                responseWrapper.copyBodyToResponse();
            }
        }
    }

    private ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper existing) {
            return existing;
        }
        return new ContentCachingRequestWrapper(request, properties.getMaxBodyLength());
    }

    private ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper existing) {
            return existing;
        }
        return new ContentCachingResponseWrapper(response);
    }

    private String getOrCreateRequestId(HttpServletRequest request, HttpServletResponse response) {
        String requestId = request.getHeader("X-Request-Id");
        if (!StringUtils.hasText(requestId)) {
            requestId = randomHex();
        }
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "X-Request-Id");
        response.setHeader("X-Request-Id", requestId);
        return sanitizeId(requestId);
    }

    private String resolveTraceId(HttpServletRequest request, String fallback) {
        for (String header : TRACE_ID_HEADERS) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value)) {
                return sanitizeId(value);
            }
        }
        return sanitizeId(fallback);
    }

    private String resolveSpanId(HttpServletRequest request) {
        for (String header : SPAN_ID_HEADERS) {
            String value = request.getHeader(header);
            if (StringUtils.hasText(value)) {
                return sanitizeId(value);
            }
        }
        return null;
    }

    private String sanitizeId(String value) {
        if (!StringUtils.hasText(value)) {
            return randomHex();
        }
        String normalized = value.trim().replace("-", "").toLowerCase(Locale.ROOT);
        if (normalized.length() > 64) {
            normalized = normalized.substring(0, 64);
        }
        return normalized;
    }

    private String randomHex() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveEnv(Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length > 0) {
            return activeProfiles[0];
        }
        String[] defaultProfiles = environment.getDefaultProfiles();
        return defaultProfiles.length > 0 ? defaultProfiles[0] : "default";
    }
}


