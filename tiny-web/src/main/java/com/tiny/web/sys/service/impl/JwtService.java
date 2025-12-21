//package com.tiny.web.sys.service.impl;
//
//import com.nimbusds.jose.*;
//import com.nimbusds.jose.crypto.MACSigner;
//import com.nimbusds.jose.crypto.MACVerifier;
//import com.nimbusds.jwt.JWTClaimsSet;
//import com.nimbusds.jwt.SignedJWT;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.Base64;
//import java.util.Date;
//import java.util.Map;
//@Component
//public class JwtService {
//
//    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
//    private static final String ISSUER = "tiny-system";
//    private final byte[] secret;
//    private final JWSSigner signer;
//    private final JWSVerifier verifier;
//
//    /**
//     * 构造函数，接收 Base64 编码的密钥字符串
//     */
//    public JwtService(@Value("${jwt.secret}") String base64Secret) {
//        try {
//            this.secret = Base64.getDecoder().decode(base64Secret);
//
//            if (secret.length < 32) {
//                throw new IllegalArgumentException("JWT 密钥长度必须至少为 256 位（32 字节）");
//            }
//
//            this.signer = new MACSigner(secret);
//            this.verifier = new MACVerifier(secret);
//
//        } catch (IllegalArgumentException | JOSEException e) {
//            throw new IllegalStateException("初始化 JwtService 失败：" + e.getMessage(), e);
//        }
//    }
//
//    public String generateAccessToken(UserDetails user) {
//        return generateToken(user, Map.of(
//                "type", "access",
//                "roles", user.getAuthorities().stream().map(Object::toString).toList()
//        ), 15 * 60); // 30分钟
//    }
//
//    public String generateRefreshToken(UserDetails user) {
//        return generateToken(user, Map.of(
//                "type", "refresh"
//        ), 7 * 24 * 60 * 60); // 7天
//    }
//
//    private String generateToken(UserDetails user, Map<String, Object> claims, long expiresInSeconds) {
//        try {
//            Instant now = Instant.now();
//            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
//                    .subject(user.getUsername())
//                    .issuer(ISSUER)
//                    .issueTime(Date.from(now))
//                    .expirationTime(Date.from(now.plusSeconds(expiresInSeconds)))
//                    .jwtID(java.util.UUID.randomUUID().toString());
//
//            if (claims != null) {
//                claims.forEach(builder::claim);
//            }
//
//            SignedJWT signedJWT = new SignedJWT(
//                    new JWSHeader.Builder(ALGORITHM).type(JOSEObjectType.JWT).build(),
//                    builder.build()
//            );
//
//            signedJWT.sign(signer);
//            return signedJWT.serialize();
//
//        } catch (JOSEException e) {
//            throw new RuntimeException("生成 JWT 失败：" + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 生成 JWT
//     */
//    public String generateToken(String subject, Map<String, Object> claims, long expiresInSeconds) {
//        try {
//            Instant now = Instant.now();
//            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
//                    .subject(subject)
//                    .issueTime(Date.from(now))
//                    .expirationTime(Date.from(now.plusSeconds(expiresInSeconds)));
//
//            if (claims != null) {
//                claims.forEach(builder::claim);
//            }
//
//            SignedJWT signedJWT = new SignedJWT(
//                    new JWSHeader.Builder(ALGORITHM).type(JOSEObjectType.JWT).build(),
//                    builder.build()
//            );
//
//            signedJWT.sign(signer);
//
//            return signedJWT.serialize();
//        } catch (JOSEException e) {
//            throw new RuntimeException("生成 JWT 失败：" + e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 校验 JWT 是否有效（签名正确 + 未过期）
//     */
//    public boolean validateToken(String token) {
//        try {
//            SignedJWT signedJWT = SignedJWT.parse(token);
//            return signedJWT.verify(verifier) &&
//                   signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    /**
//     * 提取 JWT Claims，需保证验证通过
//     */
//    public JWTClaimsSet parseToken(String token) {
//        try {
//            SignedJWT signedJWT = SignedJWT.parse(token);
//            if (!signedJWT.verify(verifier)) {
//                throw new JOSEException("JWT 签名验证失败");
//            }
//            return signedJWT.getJWTClaimsSet();
//        } catch (Exception e) {
//            throw new RuntimeException("解析 JWT 失败：" + e.getMessage(), e);
//        }
//    }
//}