package com.tiny.core.governance.tenant;

import com.tiny.core.governance.model.TenantPolicy;
import com.tiny.core.governance.repository.TenantPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 租户策略服务
 *
 * <p>用于判断租户是否启用了某些治理能力，例如 DICT_STRICT_MODE 等。
 */
@Service
public class TenantPolicyService {

    private final TenantPolicyRepository repository;

    public TenantPolicyService(TenantPolicyRepository repository) {
        this.repository = repository;
    }

    /**
     * 检查租户是否启用了指定策略
     *
     * @param tenantId   租户ID
     * @param policyCode 策略编码
     * @return 是否启用
     */
    public boolean isPolicyEnabled(Long tenantId, String policyCode) {
        Optional<TenantPolicy> policy = repository.findByTenantIdAndPolicyCode(tenantId, policyCode);
        return policy.map(TenantPolicy::getEnabled).orElse(false);
    }

    /**
     * 启用或禁用某个租户策略
     */
    public TenantPolicy setPolicy(Long tenantId, String policyCode, boolean enabled) {
        TenantPolicy policy = repository.findByTenantIdAndPolicyCode(tenantId, policyCode)
                .orElseGet(() -> {
                    TenantPolicy p = new TenantPolicy();
                    p.setTenantId(tenantId);
                    p.setPolicyCode(policyCode);
                    p.setCreatedAt(LocalDateTime.now());
                    return p;
                });
        policy.setEnabled(enabled);
        policy.setUpdatedAt(LocalDateTime.now());
        return repository.save(policy);
    }
}
