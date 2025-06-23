package com.tiny.oauthserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.tiny.oauthserver.util.PemUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
public class AuthorizationServerConfig {

    private final ClientProperties clientProperties;


    private final CorsConfigurationSource corsConfigurationSource;

    public AuthorizationServerConfig(ClientProperties authProperties,@Qualifier("corsConfigurationSource")CorsConfigurationSource corsConfigurationSource) {
        this.clientProperties = authProperties;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * Spring Authorization Server 相关配置
     * 此处方法与下面defaultSecurityFilterChain都是SecurityFilterChain配置，配置的内容有点区别，
     * 因为Spring Authorization Server是建立在Spring Security 基础上的，defaultSecurityFilterChain方法主要
     * 配置Spring Security相关的东西，而此处authorizationServerSecurityFilterChain方法主要配置OAuth 2.1和OpenID Connect 1.0相关的东西
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                //开启OpenID Connect 1.0（其中oidc为OpenID Connect的缩写）。
                .oidc(Customizer.withDefaults());
        http
                //将需要认证的请求，重定向到login页面行登录认证。
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                //.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // 👈 添加这行

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // 启用并设置 CORS
                .csrf(csrf -> csrf.disable()) // 前后端分离建议关闭 CSRF，或使用 Token 保护
                // 使用jwt处理接收到的access token
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(Customizer.withDefaults()));


        return http.build();
    }





    /**
     *设置用户信息，校验用户名、密码
     * 这里或许有人会有疑问，不是说OAuth 2.1已经移除了密码模式了码？怎么这里还有用户名、密码登录？
     * 例如：某平台app支持微信登录，用户想使用微信账号登录登录该平台app，则用户需先登录微信app，
     * 此处代码的操作就类似于某平台app跳到微信登录界面让用户先登录微信，然后微信校验用户提交的用户名、密码，
     * 登录了微信才对某平台app进行授权，对于微信平台来说，某平台的app就是OAuth 2.1中的客户端。
     * 其实，这一步是Spring Security的操作，纯碎是认证平台的操作，是脱离客户端（第三方平台）的。
     */
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//        //基于内存的用户数据校验
//        return new InMemoryUserDetailsManager(userDetails);
//    }



//    /**
//     * 注册客户端信息
//     */
//    @Bean
//    public RegisteredClientRepository registeredClientRepository() {
//        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
//                .clientId("oidc-client")
//                //{noop}开头，表示"secret"以明文存储
//                .clientSecret("{noop}secret")
//                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
//                //.redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
//                //将上面的redirectUri地址注释掉，改成下面的地址，是因为我们暂时还没有客户端服务，以免重定向跳转错误导致接收不到授权码
//                //.redirectUri("http://www.baidu.com")
//                .redirectUri("http://localhost:9000/")
//                //退出操作，重定向地址，暂时也没遇到
//                .postLogoutRedirectUri("http://127.0.0.1:8080/")
//                //设置客户端权限范围
//                .scope(OidcScopes.OPENID)
//                .scope(OidcScopes.PROFILE)
//                //客户端设置用户需要确认授权
//                .clientSettings(ClientSettings.builder()
//                        //.requireAuthorizationConsent(true)
//                        .requireAuthorizationConsent(false) // 👈 自动授权，跳过 consent 页面
//                        .build()
//                )
//                .build();
//        //配置基于内存的客户端信息
//        return new InMemoryRegisteredClientRepository(oidcClient);
//    }


    @Bean
    public JWKSource<SecurityContext> jwkSource() throws Exception {
        RSAPublicKey publicKey = PemUtils.readPublicKey(clientProperties.getJwt().getPublicKeyPath());
        RSAPrivateKey privateKey = PemUtils.readPrivateKey(clientProperties.getJwt().getPrivateKeyPath());

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-server-key")
                .build();

        return (selector, context) -> selector.select(new JWKSet(rsaKey));
    }

    /**
     * 配置jwt解析器
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    /**
     *配置认证服务器请求地址
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        //什么都不配置，则使用默认地址
        return AuthorizationServerSettings.builder().build();
    }


    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                accessTokenGenerator,
                refreshTokenGenerator,
                jwtGenerator // 注意：不叫 OidcIdTokenGenerator，JwtGenerator 会负责处理 id_token 和 access_token
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

}