package com.tiny.oauthserver.config.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * SecurityUser 的 userId 字段反序列化器。
 * <p>
 * 将 String 类型的 userId 反序列化为 Long，支持：
 * 1. 从 String 格式反序列化（由 {@link SecurityUserLongSerializer} 序列化）
 * 2. 从 Number 格式反序列化（兼容旧数据）
 * <p>
 * 这是符合官方指南的扩展方式，通过自定义反序列化器而不是修改框架内部实现。
 *
 * @since 1.0.0
 */
public class SecurityUserLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 支持从 String 或 Number 反序列化
        com.fasterxml.jackson.core.JsonToken token = p.getCurrentToken();
        if (token == null) {
            token = p.nextToken();
        }
        
        if (token == com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT 
            || token == com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT) {
            // 如果是数字，直接读取为 Long
            return p.getLongValue();
        } else if (token == com.fasterxml.jackson.core.JsonToken.VALUE_STRING) {
            // 如果是字符串，解析为 Long
            String value = p.getText();
            if (value == null || value.isEmpty()) {
                return null;
            }
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                throw new IOException("无法将字符串 '" + value + "' 解析为 Long", e);
            }
        } else if (token == com.fasterxml.jackson.core.JsonToken.VALUE_NULL) {
            return null;
        } else {
            throw new IOException("无法反序列化 userId：期望 String 或 Number，但得到 " + token);
        }
    }
}

