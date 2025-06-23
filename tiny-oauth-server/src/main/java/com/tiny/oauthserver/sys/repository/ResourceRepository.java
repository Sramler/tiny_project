package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}