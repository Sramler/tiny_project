package com.tiny.web.oauth2.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.tiny.web.oauth2.password.OAuth2PasswordAuthenticationConverter;
import com.tiny.web.oauth2.password.OAuth2PasswordAuthenticationProvider;
import com.tiny.web.util.PemUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class OAuth2AuthorizationServerConfig {

    @Autowired
    private ClientProperties authProperties;

    @Autowired
    private AuthenticationManager authenticationManager;
    /**
     * 授权信息（如 access_token、refresh_token）存储到数据库
     */
    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        // 设置 jwtCustomizer ...
        JwtToOAuth2AccessTokenGenerator accessTokenWrapper = new JwtToOAuth2AccessTokenGenerator(jwtGenerator);
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                accessTokenWrapper,
                refreshTokenGenerator
        );


    }


    /**
     * 配置授权服务器的安全过滤器链
     * 包括端点如：/oauth2/token、/.well-known/jwks.json
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
                                                                      AuthenticationManager authenticationManager,
                                                                      OAuth2AuthorizationService authorizationService,
                                                                      OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) throws Exception {

        // 应用 Spring Authorization Server 默认配置（已初始化内部 matcher）
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // 手动扩展 password 模式支持
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(new OAuth2PasswordAuthenticationConverter())
                        .authenticationProvider(
                                new OAuth2PasswordAuthenticationProvider(
                                        authenticationManager,
                                        authorizationService,
                                        tokenGenerator
                                )
                        )
                )
                .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                        .consentPage(null)  // 禁用授权同意页面
                );

        // 可选配置：关闭 CSRF（仅用于开发）
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
//    @Bean
//    @Order(1)
//    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
//                                                                      OAuth2AuthorizationService authorizationService,
//                                                                      OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) throws Exception {
//
//        // ✅ 应用默认安全配置，注册标准端点
//        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
//        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
//
//        authorizationServerConfigurer
//                .tokenEndpoint(tokenEndpoint ->
//                        tokenEndpoint
//                                .accessTokenRequestConverter(new OAuth2PasswordAuthenticationConverter())
//                                .authenticationProvider(
//                                        new OAuth2PasswordAuthenticationProvider(
//                                                authenticationManager,
//                                                authorizationService,
//                                                tokenGenerator
//                                        )
//                                )
//                );
//        http
//                .apply(authorizationServerConfigurer)
//                .and()
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }

    /**
     * 注册客户端信息（如 client_id、client_secret、授权模式等），并从数据库中加载
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }



    /**
     * 授权同意信息（authorization_code 授权场景中才用），持久化到数据库
     */
    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate,
            RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    /**
     * JWK 密钥对（用于签发 JWT），加载本地 PEM 格式密钥
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        RSAPublicKey publicKey = PemUtils.readPublicKey(authProperties.getJwt().getPublicKeyPath());
        RSAPrivateKey privateKey = PemUtils.readPrivateKey(authProperties.getJwt().getPrivateKeyPath());

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-server-key")
                .build();

        return (selector, context) -> selector.select(new JWKSet(rsaKey));
    }

    /**
     * 设置授权服务器的公开地址（issuer），用于 JWT 校验
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:8080") // 替代 ProviderSettings
                .build();
    }
}