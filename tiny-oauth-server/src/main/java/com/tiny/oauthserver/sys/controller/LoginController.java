package com.tiny.oauthserver.sys.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录页面控制器
 */
@Controller
public class LoginController {

    /**
     * 返回自定义登录页面
     * 替代 Spring Security 默认的登录页面
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
