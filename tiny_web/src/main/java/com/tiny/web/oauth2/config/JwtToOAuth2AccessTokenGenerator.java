package com.tiny.web.oauth2.config;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

public class JwtToOAuth2AccessTokenGenerator implements OAuth2TokenGenerator<OAuth2AccessToken> {

    private final JwtGenerator jwtGenerator;

    public JwtToOAuth2AccessTokenGenerator(JwtGenerator jwtGenerator) {
        this.jwtGenerator = jwtGenerator;
    }

    @Override
    public OAuth2AccessToken generate(OAuth2TokenContext context) {
        // ✅ 推荐加上这段判断，确保仅在自包含 Token 情况下生成 JWT
        if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType()) ||
                !OAuth2TokenFormat.SELF_CONTAINED.equals(context.getRegisteredClient().getTokenSettings().getAccessTokenFormat())) {
            return null;
        }

        Jwt jwt = jwtGenerator.generate(context);
        if (jwt == null) {
            return null;
        }


        return new OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER,
            jwt.getTokenValue(),
            jwt.getIssuedAt(),
            jwt.getExpiresAt(),
            context.getAuthorizedScopes()
        );
    }
}