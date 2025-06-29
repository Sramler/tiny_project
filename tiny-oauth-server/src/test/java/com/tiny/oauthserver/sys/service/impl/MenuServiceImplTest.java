package com.tiny.oauthserver.sys.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * MenuServiceImpl 测试类
 * 主要测试类型转换功能
 */
class MenuServiceImplTest {

    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
        // 由于 MenuServiceImpl 依赖 ResourceRepository 和 EntityManager，
        // 这里我们只测试类型转换方法，不进行完整的集成测试
        menuService = new MenuServiceImpl(null);
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - Byte 类型转换")
    void testSafeToIntegerWithByte() {
        // 使用反射调用私有方法
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试 Byte 类型
            Byte byteValue = 1;
            Integer result = (Integer) method.invoke(menuService, byteValue);
            assertEquals(1, result);
            
            // 测试 null
            result = (Integer) method.invoke(menuService, (Object) null);
            assertNull(result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - Integer 类型转换")
    void testSafeToIntegerWithInteger() {
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试 Integer 类型
            Integer intValue = 100;
            Integer result = (Integer) method.invoke(menuService, intValue);
            assertEquals(100, result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - Short 类型转换")
    void testSafeToIntegerWithShort() {
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试 Short 类型
            Short shortValue = 50;
            Integer result = (Integer) method.invoke(menuService, shortValue);
            assertEquals(50, result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - Long 类型转换")
    void testSafeToIntegerWithLong() {
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试 Long 类型
            Long longValue = 200L;
            Integer result = (Integer) method.invoke(menuService, longValue);
            assertEquals(200, result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - String 类型转换")
    void testSafeToIntegerWithString() {
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试有效的字符串
            String validString = "300";
            Integer result = (Integer) method.invoke(menuService, validString);
            assertEquals(300, result);
            
            // 测试无效的字符串
            String invalidString = "abc";
            result = (Integer) method.invoke(menuService, invalidString);
            assertNull(result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 safeToInteger 方法 - 其他类型")
    void testSafeToIntegerWithOtherTypes() {
        try {
            java.lang.reflect.Method method = MenuServiceImpl.class.getDeclaredMethod("safeToInteger", Object.class);
            method.setAccessible(true);
            
            // 测试 Double 类型
            Double doubleValue = 400.0;
            Integer result = (Integer) method.invoke(menuService, doubleValue);
            assertEquals(400, result);
            
            // 测试 Float 类型
            Float floatValue = 500.0f;
            result = (Integer) method.invoke(menuService, floatValue);
            assertEquals(500, result);
            
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }
} 