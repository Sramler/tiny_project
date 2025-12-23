package com.tiny.core.governance.repository;

import com.tiny.core.governance.model.TenantPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 租户策略 Repository
 */
@Repository
public interface TenantPolicyRepository extends JpaRepository<TenantPolicy, Long> {

    Optional<TenantPolicy> findByTenantIdAndPolicyCode(Long tenantId, String policyCode);
}
