package com.tiny.web.oauth2.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(ClientProperties.class)
public class RegisteredClientConfig {

    @Bean
    public CommandLineRunner registerClients(
            ClientProperties properties,
            RegisteredClientRepository repository) {
        return args -> {
            for (ClientProperties.Client config : properties.getClients()) {
                if (repository.findByClientId(config.getClientId()) != null) continue;

                RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(config.getClientId())
                        .clientSecret("{noop}" + config.getClientSecret())
                        .clientName(config.getClientId())
                        .clientSettings(ClientSettings.builder()
                                .requireAuthorizationConsent(false)
                                .build())
                        .tokenSettings(TokenSettings.builder()
                                .accessTokenTimeToLive(Duration.ofHours(1))
                                .refreshTokenTimeToLive(Duration.ofHours(12))
                                .reuseRefreshTokens(true)
                                .accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED) // ✅ 显式指定为 JWT 格式
                                .build());

                config.getAuthenticationMethods().forEach(method ->
                        builder.clientAuthenticationMethod(new ClientAuthenticationMethod(method)));
                config.getGrantTypes().forEach(grantType ->
                        builder.authorizationGrantType(new AuthorizationGrantType(grantType)));
                if (config.getScopes() != null) config.getScopes().forEach(builder::scope);
                if (config.getRedirectUris() != null) config.getRedirectUris().forEach(builder::redirectUri);
                if (config.getPostLogoutRedirectUris() != null) config.getPostLogoutRedirectUris()
                        .forEach(uri -> builder.postLogoutRedirectUri(uri));

                repository.save(builder.build());
            }
        };
    }
}