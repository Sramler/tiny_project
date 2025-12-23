package com.tiny.core.governance.dict.validation;

import com.tiny.core.dict.runtime.DictRuntime;
import com.tiny.core.governance.tenant.TenantPolicyService;
import org.springframework.stereotype.Service;

/**
 * Level1：字典值严格校验服务
 *
 * <p>租户可选择启用，增强数据一致性。
 */
@Service
public class DictValidationService {

    public static final String POLICY_STRICT_MODE = "DICT_STRICT_MODE";

    private final DictRuntime dictRuntime;
    private final TenantPolicyService tenantPolicyService;

    public DictValidationService(DictRuntime dictRuntime, TenantPolicyService tenantPolicyService) {
        this.dictRuntime = dictRuntime;
        this.tenantPolicyService = tenantPolicyService;
    }

    /**
     * 校验字典值是否合法
     *
     * @param dictCode 字典编码
     * @param value    字典值
     * @param tenantId 租户ID
     * @throws IllegalArgumentException 如果值不合法且租户启用了严格模式
     */
    public void validateValue(String dictCode, String value, Long tenantId) {
        boolean strictEnabled = tenantPolicyService.isPolicyEnabled(tenantId, POLICY_STRICT_MODE);
        if (!strictEnabled) {
            return;
        }
        String label = dictRuntime.getLabel(dictCode, value, tenantId);
        if (label == null || label.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("非法字典值: dictCode=%s, value=%s, tenantId=%d",
                            dictCode, value, tenantId)
            );
        }
    }
}
