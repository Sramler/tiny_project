package com.tiny.scheduling.repository;

import com.tiny.scheduling.model.SchedulingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchedulingTaskRepository extends JpaRepository<SchedulingTask, Long>, JpaSpecificationExecutor<SchedulingTask> {
    Optional<SchedulingTask> findByTenantIdAndCode(Long tenantId, String code);
}


