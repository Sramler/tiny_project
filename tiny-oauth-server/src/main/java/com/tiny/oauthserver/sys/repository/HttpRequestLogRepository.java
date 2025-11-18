package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.HttpRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HttpRequestLogRepository extends JpaRepository<HttpRequestLog, Long> {
}


