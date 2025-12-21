package com.tiny.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

    /**
     * 配置资源服务器过滤链，仅保护 /api/** 路径
     * 其他如 /login、/oauth2/token 不受此影响
     */
    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**") // 仅对 API 路径启用资源保护
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated() // 所有请求都需认证
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // 从授权服务器获取 JWK，用于校验 access_token
                    .jwkSetUri("http://localhost:8080/.well-known/jwks.json")
                )
            );
        return http.build();
    }
}