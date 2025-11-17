package com.tiny.web.util;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码哈希生成工具类
 * 用于生成 BCrypt 密码哈希
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        
        // 生成 123456 的 BCrypt 哈希
        String password = "123456";
        String encodedPassword = passwordEncoder.encode(password);
        
        System.out.println("============================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希: " + encodedPassword);
        System.out.println("============================================");
        System.out.println();
        System.out.println("SQL 更新语句：");
        System.out.println("UPDATE user_authentication_method");
        System.out.println("SET authentication_configuration = JSON_SET(");
        System.out.println("    authentication_configuration,");
        System.out.println("    '$.password',");
        System.out.println("    '" + encodedPassword + "'");
        System.out.println(")");
        System.out.println("WHERE authentication_provider = 'LOCAL'");
        System.out.println("  AND authentication_type = 'PASSWORD'");
        System.out.println("  AND JSON_EXTRACT(authentication_configuration, '$.password') IS NOT NULL;");
        System.out.println();
        System.out.println("验证密码：");
        System.out.println("passwordEncoder.matches(\"" + password + "\", \"" + encodedPassword + "\") = " 
                + passwordEncoder.matches(password, encodedPassword));
    }
}

