package com.tiny.oauthserver.sys.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;

/**
 * SecurityUser 是 Spring Security 的认证用户对象，仅用于安全框架内部，
 * 避免将包含 ORM 懒加载字段的实体类（如 User）直接存入 Session。
 */
@JsonTypeName("securityUser")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
public class SecurityUser implements UserDetails, Serializable {

    private final Long userId;

    // 用户名
    private final String username;

    // 加密后的密码
    private final String password;

    // 权限列表（通常来源于角色）
    private final Collection<? extends GrantedAuthority> authorities;

    // 账号是否未过期
    private final boolean accountNonExpired;

    // 账号是否未锁定
    private final boolean accountNonLocked;

    // 凭证（密码）是否未过期
    private final boolean credentialsNonExpired;

    // 是否启用
    private final boolean enabled;

    /**
     * 构造函数，基于数据库中查询出的 User 实体构建出安全框架使用的对象。
     * 这样可以避免将 User 实体（含懒加载字段）直接放入 Session。
     */
    public SecurityUser(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
//        this.authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName()))
//                .collect(Collectors.toSet()); // 将角色名转为权限


        this.authorities = user.getRoles().stream()
                .flatMap(role -> {
                    // 1. 将 roleName 作为权限（如 "ROLE_ADMIN"）
                    // 2. 将 resource.code 也作为权限（如 "PERM_READ"）
                    return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of(
                                    new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName())
                            ),
                            role.getResources().stream()
                                    .map(resource -> new org.springframework.security.core.authority.SimpleGrantedAuthority(resource.getName()))
                    );
                })
                .collect(java.util.stream.Collectors.toSet());
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.isEnabled();
    }

    /**
     * 构造函数，基于数据库中查询出的 User 实体和自定义密码构建出安全框架使用的对象。
     * 用于从 user_authentication_method 表获取密码的场景。
     */
    public SecurityUser(User user, String password) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = password;
        this.authorities = user.getRoles().stream()
                .flatMap(role -> {
                    // 1. 将 roleName 作为权限（如 "ROLE_ADMIN"）
                    // 2. 将 resource.code 也作为权限（如 "PERM_READ"）
                    return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of(
                                    new org.springframework.security.core.authority.SimpleGrantedAuthority(role.getName())
                            ),
                            role.getResources().stream()
                                    .map(resource -> new org.springframework.security.core.authority.SimpleGrantedAuthority(resource.getName()))
                    );
                })
                .collect(java.util.stream.Collectors.toSet());
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.isEnabled();
    }

    @JsonCreator
    public SecurityUser(
            @JsonProperty("userId") Long userId,
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities,
            @JsonProperty("accountNonExpired") boolean accountNonExpired,
            @JsonProperty("accountNonLocked") boolean accountNonLocked,
            @JsonProperty("credentialsNonExpired") boolean credentialsNonExpired,
            @JsonProperty("enabled") boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
        this.enabled = enabled;
    }

    /**
     * 获取当前用户的权限集合（角色转换而来）
     * 此方法是认证和授权的关键点
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities; // 此处原来写成 List.of() 是错误的，需返回真实权限
    }

    public Long getUserId() {
        return userId;
    }

    /**
     * 返回用户密码（已加密）
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 返回用户名
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 账号是否未过期（true 表示有效）
     */
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    /**
     * 账号是否未锁定（true 表示未锁定）
     */
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    /**
     * 密码是否未过期（true 表示有效）
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    /**
     * 用户是否启用（true 表示启用）
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}