package com.tiny.oauthserver.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * SecurityUser 的 userId 字段序列化器。
 * <p>
 * 将 Long 类型的 userId 序列化为 String，避免：
 * 1. Spring Security allowlist 检查失败（Long 不在默认 allowlist 中）
 * 2. JavaScript 精度丢失（JavaScript Number 类型只能安全表示 -2^53 到 2^53 的整数）
 * <p>
 * 这是符合官方指南的扩展方式，通过自定义序列化器而不是修改框架内部实现。
 *
 * @since 1.0.0
 */
public class SecurityUserLongSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            // 将 Long 序列化为 String
            gen.writeString(String.valueOf(value));
        }
    }
}

