package com.tiny.web.oauth2.config;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Map;

public class PasswordTokenGranter implements OAuth2TokenCustomizer<OAuth2TokenContext> {

    private final AuthenticationManager authenticationManager;
    private final OAuth2TokenGenerator<?> tokenGenerator;

    public PasswordTokenGranter(AuthenticationManager authenticationManager,
                                OAuth2TokenGenerator<?> tokenGenerator) {
        this.authenticationManager = authenticationManager;
        this.tokenGenerator = tokenGenerator;
    }

    public OAuth2AccessTokenAuthenticationToken grant(String grantType, Map<String, Object> parameters, RegisteredClient registeredClient) {
        if (!"password".equals(grantType)) {
            return null;
        }

        String username = (String) parameters.get("userName");
        String password = (String) parameters.get("password");
        if (username == null || password == null) {
            throw new IllegalArgumentException("userName and password must be provided");
        }

        Authentication userAuth = new UsernamePasswordAuthenticationToken(username, password);
        try {
            Authentication authentication = authenticationManager.authenticate(userAuth);
            if (!authentication.isAuthenticated()) {
                throw new RuntimeException("Invalid username or password");
            }

            // 这里构造 TokenContext 等调用 tokenGenerator，保存授权信息等
            // 具体细节较复杂，这里简化示例
            // TODO: 实现 OAuth2AccessTokenAuthenticationToken 的创建和保存
            return null; // 需要自己实现完整逻辑

        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }

    @Override
    public void customize(OAuth2TokenContext context) {

    }
}