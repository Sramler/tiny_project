package com.tiny.oauthserver.sys.security;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 部分认证令牌（多因素认证的中间状态）
 * 
 * 用于多步骤认证流程：
 * 1. 第一步：验证第一个因子（如密码）→ 返回 PartialAuthenticationToken
 * 2. 第二步：验证第二个因子（如 TOTP）→ 返回完整的 MultiFactorAuthenticationToken
 * 
 * 示例场景：
 * - password + totp：先验证密码，再验证 TOTP
 * - email + totp：先验证邮箱验证码，再验证 TOTP
 * 
 * @author Auto Generated
 * @since 1.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public class PartialAuthenticationToken extends AbstractAuthenticationToken {

    private final String username;
    private final UserDetails userDetails;
    private final String completedFactorProvider;  // 已完成的认证提供者
    private final String completedFactorType;      // 已完成的认证类型
    private final List<String> remainingFactors;   // 剩余需要验证的因子列表，格式：["LOCAL+TOTP", "EMAIL+TOTP"]
    private final Object credentials;              // 第二个因子的凭证（TOTP码等）

    /**
     * 创建部分认证令牌（第一步认证通过后）
     * 
     * @param username 用户名
     * @param userDetails 用户详情（第一步认证通过后加载）
     * @param completedFactorProvider 已完成的认证提供者（如 "LOCAL"）
     * @param completedFactorType 已完成的认证类型（如 "PASSWORD"）
     * @param remainingFactors 剩余需要验证的因子列表
     */
    public PartialAuthenticationToken(String username,
                                     UserDetails userDetails,
                                     String completedFactorProvider,
                                     String completedFactorType,
                                     List<String> remainingFactors) {
        super(userDetails.getAuthorities());
        this.username = username;
        this.userDetails = userDetails;
        this.completedFactorProvider = completedFactorProvider != null ? completedFactorProvider.toUpperCase() : null;
        this.completedFactorType = completedFactorType != null ? completedFactorType.toUpperCase() : null;
        this.remainingFactors = remainingFactors != null ? remainingFactors : List.of();
        this.credentials = null; // 第二步的凭证还未提供
        setAuthenticated(false); // 部分认证，未完全认证
    }

    /**
     * 创建部分认证令牌（用于第二步认证）
     * 
     * @param username 用户名
     * @param userDetails 用户详情
     * @param completedFactorProvider 已完成的认证提供者
     * @param completedFactorType 已完成的认证类型
     * @param remainingFactors 剩余需要验证的因子列表
     * @param credentials 第二步的凭证（TOTP码等）
     */
    public PartialAuthenticationToken(String username,
                                     UserDetails userDetails,
                                     String completedFactorProvider,
                                     String completedFactorType,
                                     List<String> remainingFactors,
                                     Object credentials) {
        super(userDetails.getAuthorities());
        this.username = username;
        this.userDetails = userDetails;
        this.completedFactorProvider = completedFactorProvider != null ? completedFactorProvider.toUpperCase() : null;
        this.completedFactorType = completedFactorType != null ? completedFactorType.toUpperCase() : null;
        this.remainingFactors = remainingFactors != null ? remainingFactors : List.of();
        this.credentials = credentials;
        setAuthenticated(false); // 部分认证，未完全认证
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails != null ? this.userDetails : this.username;
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
     * 获取用户详情
     */
    public UserDetails getUserDetails() {
        return this.userDetails;
    }

    /**
     * 获取已完成的认证提供者
     */
    public String getCompletedFactorProvider() {
        return this.completedFactorProvider;
    }

    /**
     * 获取已完成的认证类型
     */
    public String getCompletedFactorType() {
        return this.completedFactorType;
    }

    /**
     * 获取剩余需要验证的因子列表
     * 格式：["LOCAL+TOTP", "EMAIL+TOTP"]
     */
    public List<String> getRemainingFactors() {
        return this.remainingFactors != null ? this.remainingFactors : List.of();
    }

    /**
     * 检查是否还有剩余的因子需要验证
     */
    public boolean hasRemainingFactors() {
        return this.remainingFactors != null && !this.remainingFactors.isEmpty();
    }

    /**
     * 获取下一个需要验证的因子
     * 格式：["LOCAL", "TOTP"] 或 null（如果没有剩余因子）
     */
    public String[] getNextFactor() {
        if (!hasRemainingFactors()) {
            return null;
        }
        String nextFactor = this.remainingFactors.get(0);
        String[] parts = nextFactor.split("\\+");
        if (parts.length == 2) {
            return new String[]{parts[0], parts[1]};
        }
        return null;
    }

    @Override
    public String toString() {
        return "PartialAuthenticationToken{" +
                "username='" + username + '\'' +
                ", completedFactorProvider='" + completedFactorProvider + '\'' +
                ", completedFactorType='" + completedFactorType + '\'' +
                ", remainingFactors=" + remainingFactors +
                ", authenticated=" + isAuthenticated() +
                '}';
    }
}

