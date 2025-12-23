package com.tiny.core.governance.dict.approval;

import org.springframework.stereotype.Service;

/**
 * Level2：字典变更审批服务（骨架实现）
 *
 * <p>实际业务中可以集成工作流/审批流，这里先保留服务接口和简单占位。
 */
@Service
public class DictApprovalService {

    /**
     * 提交字典变更审批申请
     */
    public void submitApprovalRequest(String dictCode, String value, Long tenantId, String changeSummary) {
        // TODO: 集成实际审批流程，如发送到工作流引擎
    }

    /**
     * 审批通过回调
     */
    public void approve(String approvalId) {
        // TODO: 审批通过后实际执行变更（可调用 DictForceService）
    }

    /**
     * 审批驳回回调
     */
    public void reject(String approvalId, String reason) {
        // TODO: 记录驳回原因
    }
}
