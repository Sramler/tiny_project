package com.tiny.web;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成 admin 的 BCrypt 哈希
        String adminPassword = encoder.encode("admin");
        System.out.println("admin 密码加密结果：" + adminPassword);
        System.out.println("admin 密码（带前缀）：{bcrypt}" + adminPassword);
        System.out.println();
        
        // 生成 123456 的 BCrypt 哈希
        String password123456 = encoder.encode("123456");
        System.out.println("123456 密码加密结果：" + password123456);
        System.out.println("123456 密码（带前缀）：{bcrypt}" + password123456);
        System.out.println();
        
        // 验证密码
        System.out.println("验证 admin 密码：" + encoder.matches("admin", adminPassword));
        System.out.println("验证 123456 密码：" + encoder.matches("123456", password123456));
        System.out.println();
        
        // 生成 SQL 更新语句
        System.out.println("============================================");
        System.out.println("SQL 更新语句（将密码更新为 123456）：");
        System.out.println("============================================");
        System.out.println("UPDATE user_authentication_method");
        System.out.println("SET authentication_configuration = JSON_SET(");
        System.out.println("    authentication_configuration,");
        System.out.println("    '$.password',");
        System.out.println("    '{bcrypt}" + password123456 + "'");
        System.out.println(")");
        System.out.println("WHERE authentication_provider = 'LOCAL'");
        System.out.println("  AND authentication_type = 'PASSWORD'");
        System.out.println("  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL;");
    }
}