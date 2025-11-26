package com.tiny.scheduling.repository;

import com.tiny.scheduling.model.SchedulingAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SchedulingAuditRepository extends JpaRepository<SchedulingAudit, Long>, JpaSpecificationExecutor<SchedulingAudit> {
}


