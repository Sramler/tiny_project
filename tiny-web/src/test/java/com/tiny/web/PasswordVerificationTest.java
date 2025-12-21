package com.tiny.web;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码验证测试工具
 * 用于验证 BCrypt 密码哈希是否正确
 */
public class PasswordVerificationTest {
    public static void main(String[] args) {
        // 使用 DelegatingPasswordEncoder（与 Spring Security 配置一致）
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        
        // 数据库中的密码哈希值（123456）
        String storedPassword = "{bcrypt}$2a$10$4EIhU0zLHczmflOEv4FmwePyA3GDH04Jo/UIYbbiz/XH6IE173DEu";
        
        // 用户输入的密码
        String rawPassword = "123456";
        
        System.out.println("============================================");
        System.out.println("密码验证测试");
        System.out.println("============================================");
        System.out.println("存储的密码哈希: " + storedPassword);
        System.out.println("用户输入的密码: " + rawPassword);
        System.out.println();
        
        // 测试 1: 使用 DelegatingPasswordEncoder 验证
        boolean matches1 = passwordEncoder.matches(rawPassword, storedPassword);
        System.out.println("测试 1: 使用 DelegatingPasswordEncoder 验证");
        System.out.println("结果: " + (matches1 ? "✅ 密码正确" : "❌ 密码错误"));
        System.out.println();
        
        // 测试 2: 使用 BCryptPasswordEncoder 直接验证（不带前缀）
        BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();
        String hashWithoutPrefix = storedPassword.replace("{bcrypt}", "");
        boolean matches2 = bcryptEncoder.matches(rawPassword, hashWithoutPrefix);
        System.out.println("测试 2: 使用 BCryptPasswordEncoder 直接验证（不带前缀）");
        System.out.println("哈希值（不带前缀）: " + hashWithoutPrefix);
        System.out.println("结果: " + (matches2 ? "✅ 密码正确" : "❌ 密码错误"));
        System.out.println();
        
        // 测试 3: 生成新的 123456 密码哈希并验证
        String newHash = passwordEncoder.encode(rawPassword);
        boolean matches3 = passwordEncoder.matches(rawPassword, newHash);
        System.out.println("测试 3: 生成新的 123456 密码哈希并验证");
        System.out.println("新生成的哈希: " + newHash);
        System.out.println("验证结果: " + (matches3 ? "✅ 密码正确" : "❌ 密码错误"));
        System.out.println();
        
        // 测试 4: 规范化密码哈希（模拟 PasswordAuthenticationProvider 的逻辑）
        String normalizedPassword = normalizePasswordHash(storedPassword);
        boolean matches4 = passwordEncoder.matches(rawPassword, normalizedPassword);
        System.out.println("测试 4: 规范化密码哈希后验证");
        System.out.println("规范化后的密码: " + normalizedPassword);
        System.out.println("验证结果: " + (matches4 ? "✅ 密码正确" : "❌ 密码错误"));
        System.out.println();
        
        // 测试 5: 测试不带前缀的哈希（模拟数据库中没有前缀的情况）
        String hashWithoutPrefix2 = hashWithoutPrefix;
        String normalizedPassword2 = normalizePasswordHash(hashWithoutPrefix2);
        boolean matches5 = passwordEncoder.matches(rawPassword, normalizedPassword2);
        System.out.println("测试 5: 测试不带前缀的哈希（模拟数据库中没有前缀的情况）");
        System.out.println("原始哈希（不带前缀）: " + hashWithoutPrefix2);
        System.out.println("规范化后的密码: " + normalizedPassword2);
        System.out.println("验证结果: " + (matches5 ? "✅ 密码正确" : "❌ 密码错误"));
        System.out.println();
        
        System.out.println("============================================");
        System.out.println("总结");
        System.out.println("============================================");
        System.out.println("所有测试是否通过: " + (matches1 && matches2 && matches3 && matches4 && matches5 ? "✅ 是" : "❌ 否"));
    }
    
    /**
     * 规范化密码哈希（与 PasswordAuthenticationProvider 中的逻辑一致）
     */
    private static String normalizePasswordHash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return hash;
        }
        // 如果已经有前缀（以 { 开头），直接返回
        if (hash.startsWith("{")) {
            return hash;
        }
        // 如果是 BCrypt 格式（$2a$, $2b$, $2y$ 开头），添加 {bcrypt} 前缀
        if (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$")) {
            return "{bcrypt}" + hash;
        }
        // 其他格式，直接返回
        return hash;
    }
}

