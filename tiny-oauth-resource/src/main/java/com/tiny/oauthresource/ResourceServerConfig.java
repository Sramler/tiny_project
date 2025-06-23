package com.tiny.oauthresource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true)
public class ResourceServerConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {
		http.authorizeHttpRequests(authorize -> authorize
                        //所有的访问都需要通过身份认证
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
								.jwt(Customizer.withDefaults())

				);
		return http.build();

	}
}