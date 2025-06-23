package com.tiny.web;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        String BCryptPassword = new BCryptPasswordEncoder().encode("admin");
        System.out.println("加密结果：" + BCryptPassword);
    }
}