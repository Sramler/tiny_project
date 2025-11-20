package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.HttpRequestLog;
import com.tiny.oauthserver.sys.repository.HttpRequestLogRepository;
import com.tiny.oauthserver.sys.service.HttpRequestLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HttpRequestLogServiceImpl implements HttpRequestLogService {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLogServiceImpl.class);

    private final HttpRequestLogRepository repository;

    public HttpRequestLogServiceImpl(HttpRequestLogRepository repository) {
        this.repository = repository;
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(HttpRequestLog requestLog) {
        try {
            repository.save(requestLog);
            if (log.isInfoEnabled()) {
                log.info("REQ_LOG service={} env={} method={} path={} status={} duration={}ms user={} traceId={} requestId={}",
                        requestLog.getServiceName(),
                        requestLog.getEnv(),
                        requestLog.getMethod(),
                        requestLog.getPathTemplate(),
                        requestLog.getStatus(),
                        requestLog.getDurationMs(),
                        requestLog.getUserId(),
                        requestLog.getTraceId(),
                        requestLog.getRequestId());
            }
        } catch (Exception ex) {
            log.warn("保存 HTTP 请求日志失败: {}", ex.getMessage(), ex);
        }
    }
}


