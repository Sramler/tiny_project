package com.tiny.oauthserver.config.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public abstract class SecurityUserMixin {

    @JsonCreator
    public SecurityUserMixin(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("userId") Long userId,
            @JsonProperty("authorities") Collection<? extends GrantedAuthority> authorities
    ) {}
}