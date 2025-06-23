package com.tiny.web.oauth2.password;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collections;
import java.util.Set;

/**
 * 自定义 OAuth2 Password 模式认证令牌。
 * 封装了客户端信息、用户名、密码、scope。
 */
public class OAuth2PasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final Authentication clientPrincipal;
    private final String username;
    private final String password;
    private final Set<String> scopes;

    public OAuth2PasswordAuthenticationToken(Authentication clientPrincipal, String username, String password, Set<String> scopes) {
        super(Collections.emptyList());
        this.clientPrincipal = clientPrincipal;
        this.username = username;
        this.password = password;
        this.scopes = scopes;
        setAuthenticated(false); // 不标记已认证
    }

    @Override
    public Object getPrincipal() {
        return this.clientPrincipal;
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Set<String> getScopes() {
        return this.scopes;
    }
}