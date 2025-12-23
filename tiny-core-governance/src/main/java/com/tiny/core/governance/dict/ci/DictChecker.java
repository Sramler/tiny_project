package com.tiny.core.governance.dict.ci;

import java.util.ArrayList;
import java.util.List;

/**
 * dict-checker：字典静态校验工具
 * 
 * <p>用于 CI/CD 流程中校验字典使用是否符合规范。
 * 
 * @author Tiny Platform
 * @version 1.0.0
 */
public class DictChecker {
    
    /**
     * 校验规则：
     * 1. 检查代码中硬编码的字典值
     * 2. 检查字典值是否在字典定义中存在
     * 3. 检查字典编码命名规范
     * 4. 检查字典使用是否传入 tenantId
     */
    
    /**
     * 校验源代码
     * 
     * @param sourceCodePath 源代码路径
     * @return 校验结果
     */
    public DictCheckResult check(String sourceCodePath) {
        // 1. 扫描代码中的字典使用
        List<DictUsage> usages = scanDictUsage(sourceCodePath);
        
        // 2. 校验字典值是否存在
        List<DictViolation> violations = validateDictValues(usages);
        
        // 3. 校验字典编码规范
        violations.addAll(validateDictCodeFormat(usages));
        
        // 4. 校验多租户使用
        violations.addAll(validateTenantUsage(usages));
        
        return new DictCheckResult(violations);
    }
    
    /**
     * 扫描代码中的字典使用
     */
    private List<DictUsage> scanDictUsage(String sourceCodePath) {
        // TODO: 实现代码扫描逻辑
        // 可以使用 AST 解析器（如 JavaParser）扫描代码
        return new ArrayList<>();
    }
    
    /**
     * 校验字典值是否存在
     */
    private List<DictViolation> validateDictValues(List<DictUsage> usages) {
        // TODO: 实现字典值校验逻辑
        // 需要连接数据库或字典服务，检查字典值是否存在
        return new ArrayList<>();
    }
    
    /**
     * 校验字典编码规范
     */
    private List<DictViolation> validateDictCodeFormat(List<DictUsage> usages) {
        // TODO: 实现字典编码格式校验
        // 检查是否符合规范：大写字母开头，3-64字符
        return new ArrayList<>();
    }
    
    /**
     * 校验多租户使用
     */
    private List<DictViolation> validateTenantUsage(List<DictUsage> usages) {
        // TODO: 实现租户ID校验
        // 检查是否所有字典使用都传入了 tenantId
        return new ArrayList<>();
    }
    
    /**
     * 字典使用信息
     */
    public static class DictUsage {
        private String filePath;
        private int lineNumber;
        private String dictCode;
        private String value;
        private boolean hasTenantId;
        
        // Getters and Setters
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
        
        public String getDictCode() {
            return dictCode;
        }
        
        public void setDictCode(String dictCode) {
            this.dictCode = dictCode;
        }
        
        public String getValue() {
            return value;
        }
        
        public void setValue(String value) {
            this.value = value;
        }
        
        public boolean isHasTenantId() {
            return hasTenantId;
        }
        
        public void setHasTenantId(boolean hasTenantId) {
            this.hasTenantId = hasTenantId;
        }
    }
    
    /**
     * 校验违规信息
     */
    public static class DictViolation {
        private String ruleName;
        private String message;
        private String filePath;
        private int lineNumber;
        
        // Getters and Setters
        public String getRuleName() {
            return ruleName;
        }
        
        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
        
        public int getLineNumber() {
            return lineNumber;
        }
        
        public void setLineNumber(int lineNumber) {
            this.lineNumber = lineNumber;
        }
    }
    
    /**
     * 校验结果
     */
    public static class DictCheckResult {
        private final List<DictViolation> violations;
        
        public DictCheckResult(List<DictViolation> violations) {
            this.violations = violations;
        }
        
        public List<DictViolation> getViolations() {
            return violations;
        }
        
        public boolean hasViolations() {
            return !violations.isEmpty();
        }
    }
}

