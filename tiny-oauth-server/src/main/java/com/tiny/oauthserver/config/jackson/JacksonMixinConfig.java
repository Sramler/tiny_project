package com.tiny.oauthserver.config.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.SecurityUser;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonMixinConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer addSecurityUserMixin() {
        return builder -> builder.mixIn(SecurityUser.class, SecurityUserMixin.class);
    }
}