package com.tiny.web.sys.controller;

import com.tiny.web.core.ResponseUtil;
import com.tiny.web.sys.dto.LoginRequest;
import com.tiny.web.sys.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/authentication")
public class AuthenticationController {

//    @Autowired
//    JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails user = (UserDetails) authentication.getPrincipal();

//        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user);

        String accessToken = "";
        String refreshToken = "";
        String sessionId = req.getSession().getId();

        // ✅ 设置 accessToken Cookie
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(30 * 60)
                .secure(true)
                .sameSite("Lax")
                .build();

        // ✅ 设置 refreshToken Cookie（可选，也可以返回到 body）
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/authentication/refresh") // 限定刷新接口使用
                .maxAge(7 * 24 * 60 * 60)
                .secure(true)
                .sameSite("Strict")
                .build();

//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
//                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//                .body(new LoginResponse(accessToken, refreshToken, sessionId));
        return ResponseUtil.ok(new LoginResponse(accessToken, refreshToken, sessionId));

    }



}
