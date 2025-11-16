package com.tiny.oauthserver.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.security.PartialAuthenticationAuthorizationManager;
import com.tiny.oauthserver.sys.security.PartialAuthenticationFilter;
import com.tiny.oauthserver.sys.service.SecurityService;

@Configuration
@Order(2)
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class DefaultSecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;

    private final UserDetailsService userDetailsService;

    public DefaultSecurityConfig(@Qualifier("corsConfigurationSource")CorsConfigurationSource corsConfigurationSource,
                                UserDetailsService userDetailsService) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          AuthenticationProvider authenticationProvider,
                                                          CustomLoginSuccessHandler customLoginSuccessHandler,
                                                          PartialAuthenticationAuthorizationManager partialAuthManager)
            throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/favicon.ico",
                                "/error",
                                "/webjars/**",
                                "/assets/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()
                        .requestMatchers("/sys/users/**").permitAll()
                        // 允许部分认证的 Token 访问 TOTP 相关端点
                        .requestMatchers("/self/security/totp-bind", 
                                        "/self/security/totp-verify",
                                        "/self/security/totp/bind",
                                        "/self/security/totp/bind-form",
                                        "/self/security/totp/pre-bind",
                                        "/self/security/totp/check",
                                        "/self/security/totp/check-form",
                                        "/self/security/skip-mfa-remind",
                                        "/self/security/status")
                                .access(partialAuthManager)
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .authenticationDetailsSource(customAuthenticationDetailsSource())
                        .permitAll()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults()))
                .authenticationProvider(authenticationProvider)
                .userDetailsService(userDetailsService);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationDetailsSource<HttpServletRequest, WebAuthenticationDetails> customAuthenticationDetailsSource() {
        return new CustomWebAuthenticationDetailsSource();
    }

    @Bean
    public CustomLoginSuccessHandler customLoginSuccessHandler(SecurityService securityService,
                                                               UserRepository userRepository,
                                                               FrontendProperties frontendProperties) {
        return new CustomLoginSuccessHandler(securityService, userRepository, frontendProperties);
    }
    
    @Bean
    public PartialAuthenticationAuthorizationManager partialAuthenticationAuthorizationManager() {
        return new PartialAuthenticationAuthorizationManager();
    }
    
    @Bean
    public PartialAuthenticationFilter partialAuthenticationFilter(SecurityService securityService,
                                                                  UserRepository userRepository) {
        return new PartialAuthenticationFilter(securityService, userRepository);
    }
}
