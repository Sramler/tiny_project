package com.tiny.oauthserver.sys.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 多因素认证令牌
 * 封装了用户名、凭证（密码/TOTP码等）、认证提供者和认证类型
 * 支持多步骤认证流程（如 PASSWORD + TOTP）
 * 
 * 优势：
 * 1. 类型安全：直接在 Token 中包含 provider 和 type，无需通过 Details 获取
 * 2. 易于扩展：可以轻松添加新的认证方式相关字段
 * 3. 清晰的设计：明确表达多认证方式的意图
 * 4. 更好的可维护性：减少类型转换和空值检查
 * 5. 支持多步骤认证：可以标记已完成的认证步骤，支持双因子认证
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class MultiFactorAuthenticationToken extends AbstractAuthenticationToken {

    private final String username;
    private final Object credentials;
    private final String authenticationProvider;
    private final String authenticationType;
    
    /**
     * 已完成的认证步骤（用于多步骤认证）
     * 例如：["PASSWORD"] 表示已完成密码验证，等待 TOTP 验证
     * 例如：["PASSWORD", "TOTP"] 表示已完成所有认证步骤
     */
    private final java.util.Set<String> completedFactors;

    /**
     * 创建未认证的 MultiFactorAuthenticationToken
     * 
     * @param username 用户名
     * @param credentials 凭证（密码、TOTP码等，可以是多个凭证的组合，如 Map<String, String>）
     * @param authenticationProvider 认证提供者（如 LOCAL, GITHUB, GOOGLE 等）
     * @param authenticationType 认证类型（如 PASSWORD, TOTP, OAUTH2 等）
     */
    public MultiFactorAuthenticationToken(String username, 
                                         Object credentials,
                                         String authenticationProvider,
                                         String authenticationType) {
        super(Collections.emptyList());
        this.username = username;
        this.credentials = credentials;
        this.authenticationProvider = authenticationProvider != null ? authenticationProvider.toUpperCase() : null;
        this.authenticationType = authenticationType != null ? authenticationType.toUpperCase() : null;
        this.completedFactors = new java.util.HashSet<>();
        setAuthenticated(false); // 初始状态为未认证
    }
    
    /**
     * 创建未认证的 MultiFactorAuthenticationToken（支持多步骤认证）
     * 
     * @param username 用户名
     * @param credentials 凭证（可以是单个凭证或多个凭证的组合）
     * @param authenticationProvider 认证提供者
     * @param authenticationType 认证类型
     * @param completedFactors 已完成的认证步骤（用于多步骤认证）
     */
    public MultiFactorAuthenticationToken(String username,
                                         Object credentials,
                                         String authenticationProvider,
                                         String authenticationType,
                                         java.util.Set<String> completedFactors) {
        super(Collections.emptyList());
        this.username = username;
        this.credentials = credentials;
        this.authenticationProvider = authenticationProvider != null ? authenticationProvider.toUpperCase() : null;
        this.authenticationType = authenticationType != null ? authenticationType.toUpperCase() : null;
        this.completedFactors = completedFactors != null ? new java.util.HashSet<>(completedFactors) : new java.util.HashSet<>();
        setAuthenticated(false);
    }

    /**
     * 创建已认证的 MultiFactorAuthenticationToken
     * 
     * @param username 用户名
     * @param credentials 凭证（通常为 null，认证成功后清空敏感信息）
     * @param authenticationProvider 认证提供者
     * @param authenticationType 认证类型
     * @param authorities 用户权限
     */
    public MultiFactorAuthenticationToken(String username,
                                         Object credentials,
                                         String authenticationProvider,
                                         String authenticationType,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.credentials = credentials;
        this.authenticationProvider = authenticationProvider != null ? authenticationProvider.toUpperCase() : null;
        this.authenticationType = authenticationType != null ? authenticationType.toUpperCase() : null;
        this.completedFactors = new java.util.HashSet<>();
        setAuthenticated(true); // 标记为已认证
    }
    
    /**
     * 创建已认证的 MultiFactorAuthenticationToken（支持多步骤认证）
     * 
     * @param username 用户名
     * @param credentials 凭证（通常为 null，认证成功后清空敏感信息）
     * @param authenticationProvider 认证提供者
     * @param authenticationType 认证类型
     * @param completedFactors 已完成的认证步骤
     * @param authorities 用户权限
     */
    public MultiFactorAuthenticationToken(String username,
                                         Object credentials,
                                         String authenticationProvider,
                                         String authenticationType,
                                         java.util.Set<String> completedFactors,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.username = username;
        this.credentials = credentials;
        this.authenticationProvider = authenticationProvider != null ? authenticationProvider.toUpperCase() : null;
        this.authenticationType = authenticationType != null ? authenticationType.toUpperCase() : null;
        this.completedFactors = completedFactors != null ? new java.util.HashSet<>(completedFactors) : new java.util.HashSet<>();
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return this.username;
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    /**
     * 获取用户名
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * 获取认证提供者
     */
    public String getAuthenticationProvider() {
        return this.authenticationProvider;
    }

    /**
     * 获取认证类型
     */
    public String getAuthenticationType() {
        return this.authenticationType;
    }
    
    /**
     * 获取已完成的认证步骤
     */
    public java.util.Set<String> getCompletedFactors() {
        return java.util.Collections.unmodifiableSet(this.completedFactors);
    }
    
    /**
     * 检查是否已完成指定的认证步骤
     */
    public boolean hasCompletedFactor(String factor) {
        return this.completedFactors.contains(factor != null ? factor.toUpperCase() : null);
    }
    
    /**
     * 检查是否已完成所有必需的认证步骤
     * @param requiredFactors 必需的认证步骤列表
     */
    public boolean hasCompletedAllFactors(java.util.Set<String> requiredFactors) {
        if (requiredFactors == null || requiredFactors.isEmpty()) {
            return true;
        }
        return this.completedFactors.containsAll(
            requiredFactors.stream()
                .map(f -> f != null ? f.toUpperCase() : null)
                .collect(java.util.stream.Collectors.toSet())
        );
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        // 注意：credentials 是 final 的，无法直接修改
        // Spring Security 的最佳实践是在创建已认证的 Token 时传入 null 作为 credentials
        // 这样可以防止敏感信息（如密码）在内存中保留过长时间
    }

    @Override
    public String toString() {
        return "MultiFactorAuthenticationToken{" +
                "username='" + username + '\'' +
                ", authenticationProvider='" + authenticationProvider + '\'' +
                ", authenticationType='" + authenticationType + '\'' +
                ", completedFactors=" + completedFactors +
                ", authenticated=" + isAuthenticated() +
                ", authorities=" + getAuthorities() +
                '}';
    }
}

