package com.tiny.oauthserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.tiny.oauthserver.sys.security.MultiFactorAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MultiFactorAuthenticationToken 的 Jackson 反序列化器
 * 使用反射直接设置 final 字段，避免调用有参构造器
 */
public class MultiFactorAuthenticationTokenJacksonDeserializer
        extends JsonDeserializer<MultiFactorAuthenticationToken> {

    @Override
    public MultiFactorAuthenticationToken deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        
        String username = jsonNode.has("username") ? jsonNode.get("username").asText() : null;
        Object credentials = jsonNode.has("credentials") && !jsonNode.get("credentials").isNull() 
            ? jsonNode.get("credentials").asText() : null;
        String providerStr = jsonNode.has("provider") 
            ? jsonNode.get("provider").asText() : (jsonNode.has("authenticationProvider") 
                ? jsonNode.get("authenticationProvider").asText() : null);
        
        // 解析 completedFactors（支持 Factor 枚举或字符串）
        Set<MultiFactorAuthenticationToken.AuthenticationFactorType> completedFactors = new HashSet<>();
        if (jsonNode.has("completedFactors") && jsonNode.get("completedFactors").isArray()) {
            for (JsonNode factor : jsonNode.get("completedFactors")) {
                if (factor.isTextual()) {
                    MultiFactorAuthenticationToken.AuthenticationFactorType f = MultiFactorAuthenticationToken.AuthenticationFactorType.from(factor.asText());
                    if (f != MultiFactorAuthenticationToken.AuthenticationFactorType.UNKNOWN) {
                        completedFactors.add(f);
                    }
                }
            }
        }
        
        // 如果没有 completedFactors，尝试从 authenticationType 获取初始因子
        String initialFactorStr = null;
        if (completedFactors.isEmpty() && jsonNode.has("authenticationType")) {
            initialFactorStr = jsonNode.get("authenticationType").asText();
        }
        
        // 解析 authorities
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (jsonNode.has("authorities") && jsonNode.get("authorities").isArray()) {
            for (JsonNode authority : jsonNode.get("authorities")) {
                if (authority.has("authority")) {
                    authorities.add(new SimpleGrantedAuthority(authority.get("authority").asText()));
                } else if (authority.isTextual()) {
                    authorities.add(new SimpleGrantedAuthority(authority.asText()));
                }
            }
        }
        
        boolean authenticated = jsonNode.has("authenticated") && jsonNode.get("authenticated").asBoolean(false);
        
        try {
            // 使用合适的构造函数创建实例
            MultiFactorAuthenticationToken token;
            if (authenticated && !authorities.isEmpty()) {
                // 使用已认证的构造器
                token = new MultiFactorAuthenticationToken(
                    username,
                    credentials,
                    MultiFactorAuthenticationToken.AuthenticationProviderType.from(providerStr),
                    completedFactors.isEmpty() ? null : completedFactors,
                    authorities
                );
            } else {
                // 使用未认证的构造器
                MultiFactorAuthenticationToken.AuthenticationFactorType initialFactor = null;
                if (!completedFactors.isEmpty()) {
                    initialFactor = completedFactors.iterator().next();
                } else if (initialFactorStr != null) {
                    initialFactor = MultiFactorAuthenticationToken.AuthenticationFactorType.from(initialFactorStr);
                }
                token = new MultiFactorAuthenticationToken(
                    username,
                    credentials,
                    MultiFactorAuthenticationToken.AuthenticationProviderType.from(providerStr),
                    initialFactor
                );
                // 如果还有其他因子，添加进去
                if (completedFactors.size() > 1) {
                    for (MultiFactorAuthenticationToken.AuthenticationFactorType f : completedFactors) {
                        if (f != initialFactor) {
                            token.addCompletedFactor(f);
                        }
                    }
                }
            }
            
            // 如果 authenticated 状态与构造器不一致，使用反射修改
            if (token.isAuthenticated() != authenticated) {
                java.lang.reflect.Method setAuthenticatedMethod = 
                    org.springframework.security.authentication.AbstractAuthenticationToken.class
                        .getDeclaredMethod("setAuthenticated", boolean.class);
                setAuthenticatedMethod.setAccessible(true);
                setAuthenticatedMethod.invoke(token, authenticated);
            }
            
            return token;
        } catch (Exception e) {
            throw new IOException("Failed to deserialize MultiFactorAuthenticationToken", e);
        }
    }
}

