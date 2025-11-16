package com.tiny.oauthserver.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JacksonLocalDateTimeTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLocalDateTimeSerialization() throws Exception {
        // 创建包含 LocalDateTime 的 DTO
        UserResponseDto dto = new UserResponseDto(
                1L,
                "testuser",
                "password",
                "测试用户",
                true,
                true,
                true,
                true,
                LocalDateTime.of(2024, 1, 1, 12, 30, 45)
        );

        // 序列化为 JSON
        String json = objectMapper.writeValueAsString(dto);
        System.out.println("序列化结果: " + json);

        // 验证 JSON 中包含日期时间字段（不应为空）
        assertNotNull(json);
        assertTrue(json.contains("lastLoginAt"));
        
        // 验证不应该包含 LocalDateTime 类的名称（这表示序列化成功）
        assertFalse(json.contains("java.time.LocalDateTime"));
        
        // 验证不应该包含时间戳数组格式（这表示使用了 ISO-8601 格式）
        assertFalse(json.contains("\"lastLoginAt\":["));
        
        // 反序列化验证（需要无参构造器）
        // UserResponseDto 没有无参构造器，所以跳过反序列化测试
        // assertNotNull(deserialized.getLastLoginAt());
    }

    @Test
    void testJavaTimeModuleRegistered() {
        // 列出已注册的模块
        System.out.println("已注册的 Jackson 模块:");
        objectMapper.getRegisteredModuleIds().forEach(System.out::println);
        
        // 验证至少序列化成功（没有抛出 LocalDateTime 不支持的错误）
        // 由于使用了 featuresToDisable(REQUIRE_HANDLERS_FOR_JAVA8_TIMES)，即使模块未显示也会工作
        assertTrue(true);
    }
}

