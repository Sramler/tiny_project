package com.tiny.oauthserver.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.SecurityUser;
import com.tiny.oauthserver.sys.security.MultiFactorAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 * <b>关键修复</b>：反序列化时恢复 details 字段（包含 SecurityUser），
 * 以便在 JWT Token 生成时能够获取 userId。
 */
public class MultiFactorAuthenticationTokenJacksonDeserializer
        extends JsonDeserializer<MultiFactorAuthenticationToken> {

    private static final Logger log = LoggerFactory.getLogger(MultiFactorAuthenticationTokenJacksonDeserializer.class);

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
            
            // ========== 关键修复：反序列化 details 字段 ==========
            // details 可能包含 SecurityUser，需要正确恢复以便在 JWT Token 生成时获取 userId
            if (jsonNode.has("details") && !jsonNode.get("details").isNull()) {
                try {
                    JsonNode detailsNode = jsonNode.get("details");
                    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
                    
                    // 检查 details 是否是 SecurityUser
                    if (detailsNode.has("@type") && "securityUser".equals(detailsNode.get("@type").asText())) {
                        // 反序列化为 SecurityUser
                        SecurityUser securityUser = mapper.treeToValue(detailsNode, SecurityUser.class);
                        token.setDetails(securityUser);
                        log.debug("[MultiFactorAuthenticationTokenJacksonDeserializer] 成功恢复 SecurityUser 到 details (userId: {})", 
                                securityUser != null ? securityUser.getUserId() : "null");
                    } else {
                        // 尝试直接反序列化（可能是其他类型的 details）
                        Object details = mapper.treeToValue(detailsNode, Object.class);
                        token.setDetails(details);
                        log.debug("[MultiFactorAuthenticationTokenJacksonDeserializer] 恢复 details: {}", 
                                details != null ? details.getClass().getName() : "null");
                    }
                } catch (Exception e) {
                    log.warn("[MultiFactorAuthenticationTokenJacksonDeserializer] 无法反序列化 details 字段: {}", e.getMessage());
                    // 不抛出异常，允许 token 在没有 details 的情况下继续使用
                }
            } else {
                log.debug("[MultiFactorAuthenticationTokenJacksonDeserializer] JSON 中没有 details 字段或为 null");
            }
            
            return token;
        } catch (Exception e) {
            throw new IOException("Failed to deserialize MultiFactorAuthenticationToken", e);
        }
    }
}

