package com.tiny.oauthserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@Order(2)
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class DefaultSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    private final UserDetailsService userDetailsService;

    public DefaultSecurityConfig(@Qualifier("corsConfigurationSource")CorsConfigurationSource corsConfigurationSource, UserDetailsService userDetailsService) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/sys/users/test/**").permitAll() // 允许测试接口不需要认证
                        .requestMatchers("/sys/users/batch/**").permitAll() // 临时允许批量操作接口不需要认证，用于测试
                        .requestMatchers("/sys/roles/**").permitAll() // 临时允许角色管理接口不需要认证，用于测试
                        .requestMatchers("/sys/resources/**").permitAll() // 临时允许资源管理接口不需要认证，用于测试
                        .requestMatchers("/process/**").permitAll() // 临时允许流程管理接口不需要认证，用于测试
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // 自定义登录页面，保持不变
                        .loginProcessingUrl("/login") // 登录处理 URL，Spring Security 会自动处理
                        .defaultSuccessUrl("/", true) // 登录成功后的默认跳转
                        .failureUrl("/login?error") // 登录失败后的跳转
                        .permitAll()
                )
                //.cors(Customizer.withDefaults()) // 开启 CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // 启用并设置 CORS
                .csrf(csrf -> csrf.disable()) // 前后端分离建议关闭 CSRF，或使用 Token 保护
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .userDetailsService(userDetailsService);
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder支持多种加密算法，默认bcrypt
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
