package com.tiny.web;

import com.tiny.web.core.GlobalResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.zalando.problem.Problem;

@ControllerAdvice
/**
 *  负责统一格式化 Problem 响应，剥离type/title，添加timestamp,uri,traceId 等字段
 */
public class ProblemFormatAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;  // 或限制为 Problem 类型
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body instanceof Problem problem) {
            int status = problem.getStatus() != null ? problem.getStatus().getStatusCode() : 500;
            int code = problem.getParameters().containsKey("code")
                    ? (Integer) problem.getParameters().get("code")
                    : status;
            String message = problem.getParameters().containsKey("message")
                    ? (String) problem.getParameters().get("message")
                    : (problem.getDetail() != null ? problem.getDetail() : problem.getTitle());

            String path = request.getURI().getPath();

//            String traceId = MDC.get("traceId");
//            if (traceId == null) {
//                traceId = java.util.UUID.randomUUID().toString().substring(0, 16);
//            }

            return GlobalResponse.error(code, message, status);
        }

        return body;
    }
}