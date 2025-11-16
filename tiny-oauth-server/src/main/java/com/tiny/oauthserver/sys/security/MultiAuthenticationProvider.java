package com.tiny.oauthserver.sys.security;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.tiny.oauthserver.config.CustomWebAuthenticationDetailsSource;
import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserAuthenticationMethod;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.repository.UserAuthenticationMethodRepository;
import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * 多认证方式支持
 * 根据 CustomWebAuthenticationDetails 中的 authenticationProvider 和 authenticationType
 * 去数据库中查找对应的认证方法进行验证
 */
@Component
public class MultiAuthenticationProvider implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(MultiAuthenticationProvider.class);

    private final UserRepository userRepository;
    private final UserAuthenticationMethodRepository authenticationMethodRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public MultiAuthenticationProvider(UserRepository userRepository,
                                       UserAuthenticationMethodRepository authenticationMethodRepository,
                                       PasswordEncoder passwordEncoder,
                                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.authenticationMethodRepository = authenticationMethodRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username;
        Object credentials;
        String provider = null;
        String type = null;

        // 支持两种 Authentication 类型：
        // 1. MultiFactorAuthenticationToken（自定义，更清晰，易于扩展）
        // 2. UsernamePasswordAuthenticationToken（标准，向后兼容）
        if (authentication instanceof MultiFactorAuthenticationToken multiFactorToken) {
            // 使用自定义 Token，直接从 Token 中获取信息
            username = multiFactorToken.getUsername();
            credentials = multiFactorToken.getCredentials();
            provider = multiFactorToken.getAuthenticationProvider();
            type = multiFactorToken.getAuthenticationType();
            logger.debug("使用 MultiFactorAuthenticationToken: provider={}, type={}", provider, type);
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // 使用标准 Token，从 Details 中获取额外信息
            username = authentication.getName();
            credentials = authentication.getCredentials();

        if (authentication.getDetails() instanceof CustomWebAuthenticationDetailsSource.CustomWebAuthenticationDetails details) {
            provider = details.getAuthenticationProvider();
            type = details.getAuthenticationType();
                logger.debug("从 CustomWebAuthenticationDetails 获取认证参数: provider={}, type={}", provider, type);
            }
        } else {
            // 不支持的 Authentication 类型
            logger.error("不支持的 Authentication 类型: {}", authentication.getClass().getName());
            throw new BadCredentialsException("不支持的认证类型");
        }

        // 查找用户（先查找用户，以便后续查询用户的认证方法）
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 验证参数：如果参数为空，尝试智能回退
        if (provider == null || provider.trim().isEmpty() || type == null || type.trim().isEmpty()) {
            logger.warn("用户 {} 的认证参数不完整，provider={}, type={}，尝试智能回退", username, provider, type);
            
            // 智能回退：查询用户的所有启用的认证方法
            java.util.List<UserAuthenticationMethod> enabledMethods = authenticationMethodRepository
                    .findEnabledMethodsByUserId(user.getId());
            
            if (enabledMethods.isEmpty()) {
                logger.error("用户 {} 未配置任何启用的认证方法", username);
                throw new BadCredentialsException("该用户未配置任何认证方式，请联系管理员");
            }
            
            if (enabledMethods.size() == 1) {
                // 如果只有一个启用的认证方法，使用它
                UserAuthenticationMethod method = enabledMethods.get(0);
                provider = method.getAuthenticationProvider();
                type = method.getAuthenticationType();
                logger.info("用户 {} 只有一个启用的认证方法，自动选择: provider={}, type={}", 
                        username, provider, type);
            } else {
                // 如果有多个认证方法，必须明确指定
                logger.error("用户 {} 有多个启用的认证方法（{} 个），必须明确指定认证方式", 
                        username, enabledMethods.size());
                StringBuilder errorMsg = new StringBuilder("该用户有多个认证方式，请明确指定认证方式。可用方式：");
                for (UserAuthenticationMethod method : enabledMethods) {
                    errorMsg.append(String.format(" %s+%s", 
                            method.getAuthenticationProvider(), 
                            method.getAuthenticationType()));
                }
                throw new BadCredentialsException(errorMsg.toString());
            }
        }

        // 验证参数格式（防止 SQL 注入等安全问题）
        provider = provider.trim();
        type = type.trim();
        
        // 基本格式验证
        if (provider.length() > 50 || type.length() > 50) {
            logger.error("认证参数长度超出限制: provider={}, type={}", provider, type);
            throw new BadCredentialsException("认证参数格式错误");
        }
        
        // 验证参数值是否包含非法字符（防止 SQL 注入、XSS 等）
        if (!isValidParameterFormat(provider) || !isValidParameterFormat(type)) {
            logger.error("认证参数包含非法字符: provider={}, type={}", provider, type);
            throw new BadCredentialsException("认证参数格式错误：包含非法字符");
        }
        
        // 转换为大写以便统一处理
        provider = provider.toUpperCase();
        type = type.toUpperCase();

        // 根据 provider 和 type 去数据库查找认证方法
        Optional<UserAuthenticationMethod> methodOpt = authenticationMethodRepository
                .findByUserIdAndAuthenticationProviderAndAuthenticationType(user.getId(), provider, type);

        if (!methodOpt.isPresent()) {
            logger.warn("用户 {} 未配置 {} + {} 认证方法", username, provider, type);
            throw new BadCredentialsException("该用户未配置此认证方式");
        }

        UserAuthenticationMethod method = methodOpt.get();

        // 检查认证方法是否启用
        if (!Boolean.TRUE.equals(method.getIsMethodEnabled())) {
            logger.warn("用户的 {} + {} 认证方法已禁用，用户ID: {}", provider, type, user.getId());
            throw new BadCredentialsException("该认证方式已被禁用");
        }

        // 支持多因素认证（MFA）：检查用户是否配置了多个认证方法
        // 例如：PASSWORD + TOTP, EMAIL + TOTP 等
        java.util.List<UserAuthenticationMethod> enabledMethods = authenticationMethodRepository
                .findEnabledMethodsByUserId(user.getId());
        
        // 检查是否需要多步骤认证
        java.util.List<UserAuthenticationMethod> mfaMethods = enabledMethods.stream()
                .filter(m -> "LOCAL".equals(m.getAuthenticationProvider()))
                .filter(m -> "PASSWORD".equals(m.getAuthenticationType()) || "TOTP".equals(m.getAuthenticationType()))
                .sorted((a, b) -> {
                    // 排序：PASSWORD 优先，然后是 TOTP
                    if ("PASSWORD".equals(a.getAuthenticationType()) && "TOTP".equals(b.getAuthenticationType())) {
                        return -1;
                    }
                    if ("TOTP".equals(a.getAuthenticationType()) && "PASSWORD".equals(b.getAuthenticationType())) {
                        return 1;
                    }
                    return 0;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 如果用户配置了多个认证方法（如 PASSWORD + TOTP），进行多步骤认证
        if (mfaMethods.size() > 1) {
            return handleMultiFactorAuthentication(user, credentials, mfaMethods, provider, type);
        }
        
        // 单一认证方法：直接验证
        String credentialsString = credentials != null ? credentials.toString() : null;
        switch (type) {
            case "PASSWORD":
                return authenticatePassword(user, credentialsString, method, provider, type);
            case "TOTP":
                return authenticateTotp(user, credentialsString, method, provider, type);
            // 可以添加更多认证类型的处理
            default:
                throw new BadCredentialsException("不支持的认证类型: " + type);
        }
    }
    
    /**
     * 处理多因素认证（MFA）
     * 支持组合认证方式，如 PASSWORD + TOTP, EMAIL + TOTP 等
     * 
     * 认证流程：
     * 1. 如果用户同时提供了所有凭证（如 Map 包含 password 和 totpCode），则一次性验证所有因子
     * 2. 如果用户只提供了部分凭证（如只有 password），则先验证第一个因子，返回部分认证 Token，要求提供第二个因子
     * 
     * @param user 用户对象
     * @param credentials 凭证（可以是单个凭证或包含多个凭证的 Map，如 {"password": "xxx", "totpCode": "123456"}）
     * @param mfaMethods 需要验证的认证方法列表（已按优先级排序）
     * @param provider 认证提供者
     * @param requestedType 当前请求的认证类型（如果 credentials 是单个值）
     * @return 认证结果
     */
    private Authentication handleMultiFactorAuthentication(
            User user,
            Object credentials,
            java.util.List<UserAuthenticationMethod> mfaMethods,
            String provider,
            String requestedType) {
        
        // 解析凭证：支持单个凭证或 Map<String, String>（包含多个凭证）
        java.util.Map<String, String> credentialMap = new java.util.HashMap<>();
        if (credentials instanceof java.util.Map) {
            // 凭证是 Map，可能包含多个认证因子
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) credentials;
            map.forEach((k, v) -> {
                if (v != null) {
                    credentialMap.put(k.toLowerCase(), v.toString());
                }
            });
            logger.debug("用户 {} 提供了多因子凭证: {}", user.getUsername(), credentialMap.keySet());
        } else if (credentials != null) {
            // 单一凭证：根据请求类型添加到 Map
            credentialMap.put(requestedType.toLowerCase(), credentials.toString());
            logger.debug("用户 {} 提供了单因子凭证: {} = {}", user.getUsername(), requestedType, "***");
        }
        
        java.util.Set<String> completedFactors = new java.util.HashSet<>();
        java.util.List<UserAuthenticationMethod> remainingMethods = new java.util.ArrayList<>(mfaMethods);
        
        // 依次验证每个认证方法
        for (UserAuthenticationMethod method : mfaMethods) {
            String methodType = method.getAuthenticationType();
            
            // 获取对应的凭证（支持多种键名）
            String methodCredential = credentialMap.get(methodType.toLowerCase());
            if (methodCredential == null) {
                methodCredential = credentialMap.get(methodType); // 尝试大写键名
            }
            if (methodCredential == null && "TOTP".equals(methodType)) {
                methodCredential = credentialMap.get("totpcode"); // 尝试 totpCode
            }
            
            if (methodCredential == null || methodCredential.isEmpty()) {
                // 凭证不存在，说明需要分步验证
                logger.debug("用户 {} 的 {} 认证步骤缺少凭证，需要分步验证", user.getUsername(), methodType);
                
                // 如果已经有完成的因子，说明这是第二步，返回部分认证 Token
                if (!completedFactors.isEmpty()) {
                    logger.info("用户 {} 已完成 {} 个因子，还需要验证: {}", 
                            user.getUsername(), completedFactors.size(), methodType);
                    return createPartialAuthenticationToken(user, provider, completedFactors, remainingMethods);
                } else {
                    // 这是第一步，但缺少凭证
                    // 在多因素认证中，用户可能先提供第一个因子（如 PASSWORD），然后提供第二个因子（如 TOTP）
                    // 所以，如果第一个因子缺少凭证，应该继续检查下一个因子
                    // 如果所有因子都缺少凭证，才抛出异常
                    logger.debug("用户 {} 的 {} 认证步骤缺少凭证，跳过此步骤", user.getUsername(), methodType);
                    continue; // 跳过这个因子，继续检查下一个
                }
            }
            
            // 验证当前步骤
            logger.debug("用户 {} 开始验证 {} 因子", user.getUsername(), methodType);
            Authentication stepResult = authenticateFactor(user, methodCredential, method, methodType);
            if (!stepResult.isAuthenticated()) {
                logger.warn("用户 {} 的 {} 认证步骤验证失败", user.getUsername(), methodType);
                throw new BadCredentialsException(methodType + " 验证失败");
            }
            
            // 验证成功，添加到已完成列表
            completedFactors.add(methodType);
            remainingMethods.remove(method);
            logger.info("用户 {} 完成 {} 认证步骤，已完成: {}", user.getUsername(), methodType, completedFactors);
        }
        
        // 检查是否有完成的因子
        if (completedFactors.isEmpty()) {
            // 没有任何因子被验证，说明用户没有提供任何有效的凭证
            logger.warn("用户 {} 配置了多因素认证，但未提供任何有效的凭证", user.getUsername());
            throw new BadCredentialsException("多因素认证需要提供至少一个认证因子");
        }
        
        // 检查是否所有因子都已完成
        if (completedFactors.size() < mfaMethods.size()) {
            // 还有未完成的因子
            // 注意：Spring Security 只有在 authentication.isAuthenticated() == true 时才会调用 successHandler
            // 所以，为了能够触发 onAuthenticationSuccess 并跳转到 TOTP 验证页面，
            // 我们需要返回一个完全认证的 Token，但标记需要 TOTP 验证
            // 在 CustomLoginSuccessHandler 中检查用户是否已绑定 TOTP，如果已绑定但未提供 TOTP 码，则跳转到 TOTP 验证页面
            
            logger.info("用户 {} 完成了 {} 个因子，还有 {} 个因子需要验证", 
                    user.getUsername(), completedFactors.size(), mfaMethods.size() - completedFactors.size());
            
            // 检查是否已完成 PASSWORD 验证，但还需要 TOTP 验证
            if (completedFactors.contains("PASSWORD") && remainingMethods.stream().anyMatch(m -> "TOTP".equals(m.getAuthenticationType()))) {
                // 返回一个完全认证的 Token（这样会触发 successHandler），但标记需要 TOTP 验证
                // 在 CustomLoginSuccessHandler 中检查用户是否已绑定 TOTP，如果已绑定，则跳转到 TOTP 验证页面
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                MultiFactorAuthenticationToken authenticatedToken = new MultiFactorAuthenticationToken(
                        user.getUsername(),
                        null,
                        provider,
                        "MFA",
                        completedFactors,
                        userDetails.getAuthorities()
                );
                authenticatedToken.setAuthenticated(true); // 标记为已认证，以便触发 successHandler
                logger.info("用户 {} 已完成密码验证，返回完全认证 Token（需要 TOTP 验证），将在 successHandler 中处理", user.getUsername());
                return authenticatedToken;
            }
            
            // 其他情况，返回部分认证 Token（但这种情况不应该触发 successHandler）
            return createPartialAuthenticationToken(user, provider, completedFactors, remainingMethods);
        }
        
        // 所有步骤完成
        logger.info("用户 {} 完成所有 MFA 认证步骤: {}", user.getUsername(), completedFactors);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return new MultiFactorAuthenticationToken(
                user.getUsername(),
                null,
                provider,
                "MFA",
                completedFactors,
                userDetails.getAuthorities()
        );
    }
    
    /**
     * 验证单个认证因子
     */
    private Authentication authenticateFactor(User user, String credential, UserAuthenticationMethod method, String factorType) {
        switch (factorType) {
            case "PASSWORD":
                return authenticatePassword(user, credential, method, method.getAuthenticationProvider(), factorType);
            case "TOTP":
                return authenticateTotp(user, credential, method, method.getAuthenticationProvider(), factorType);
            default:
                throw new BadCredentialsException("不支持的认证因子类型: " + factorType);
        }
    }
    
    /**
     * 创建部分认证的 Token（用于多步骤认证）
     * 
     * @param user 用户对象
     * @param provider 认证提供者
     * @param completedFactors 已完成的认证因子
     * @param remainingMethods 还需要验证的认证方法列表
     * @return 部分认证的 Token（未完全认证，需要继续验证）
     */
    private Authentication createPartialAuthenticationToken(
            User user, 
            String provider, 
            java.util.Set<String> completedFactors,
            java.util.List<UserAuthenticationMethod> remainingMethods) {
        // 返回一个部分认证的 Token，标记为未完全认证（setAuthenticated(false)）
        // 这样可以要求用户继续提供下一个认证因子
        MultiFactorAuthenticationToken partialToken = new MultiFactorAuthenticationToken(
                user.getUsername(),
                null,
                provider,
                "MFA",
                completedFactors,
                java.util.Collections.emptyList()
        );
        partialToken.setAuthenticated(false); // 标记为未完全认证
        logger.info("用户 {} 部分认证完成，已完成因子: {}，剩余因子: {}", 
                user.getUsername(), 
                completedFactors,
                remainingMethods.stream()
                    .map(UserAuthenticationMethod::getAuthenticationType)
                    .collect(java.util.stream.Collectors.toList()));
        return partialToken;
    }


    /**
     * 邮箱验证码认证（示例，需要实现）
     * TODO: 实现邮箱验证码验证逻辑
     */
    private Authentication authenticateEmail(User user, String emailCode, UserAuthenticationMethod method, String provider, String type) {
        // TODO: 实现邮箱验证码验证逻辑
        logger.warn("邮箱验证码认证暂未实现，用户: {}", user.getUsername());
        throw new BadCredentialsException("邮箱验证码认证暂未实现");
    }

    /**
     * 密码认证
     * 
     * @param user 用户对象
     * @param password 密码
     * @param method 认证方法配置
     * @param provider 认证提供者
     * @param type 认证类型
     * @return 认证成功的 Authentication 对象
     */
    private Authentication authenticatePassword(User user, String password, UserAuthenticationMethod method, String provider, String type) {
        Map<String, Object> config = method.getAuthenticationConfiguration();
        
        // 添加详细的调试日志
        logger.debug("用户 {} 的认证配置内容: {}", user.getUsername(), config);
        logger.debug("用户 {} 的认证配置键: {}", user.getUsername(), config != null ? config.keySet() : "null");
        
        if (config == null || config.isEmpty()) {
            logger.error("用户 {} 的认证配置为空。UserAuthenticationMethod ID: {}", user.getUsername(), method.getId());
            throw new BadCredentialsException("认证配置错误：配置为空");
        }
        
        // 获取编码后的密码
        // 在 authentication_configuration 上下文中，使用 "password" 键名
        // 编码后的密码可能包含编码器前缀（如 {bcrypt}...）或者只是纯哈希（如 $2a$10$...）
        if (!config.containsKey("password")) {
            logger.error("用户 {} 的认证配置中不包含 password 字段。可用的键: {}, 配置内容: {}", 
                    user.getUsername(), config.keySet(), config);
            throw new BadCredentialsException("认证配置错误：缺少 password 字段");
        }
        
        Object passwordValue = config.get("password");
        if (passwordValue == null) {
            logger.error("用户 {} 的 password 字段值为 null。配置内容: {}", user.getUsername(), config);
            throw new BadCredentialsException("认证配置错误：password 字段值为 null");
        }
        
        String encodedPassword;
        if (passwordValue instanceof String) {
            encodedPassword = (String) passwordValue;
        } else {
            logger.warn("用户 {} 的 password 字段不是字符串类型: {}，尝试转换为字符串", 
                    user.getUsername(), passwordValue.getClass().getName());
            encodedPassword = passwordValue.toString();
        }

        if (encodedPassword.isEmpty()) {
            logger.error("用户 {} 的 password 字段值为空字符串。配置内容: {}", user.getUsername(), config);
            throw new BadCredentialsException("认证配置错误：password 字段值为空");
        }

        logger.debug("用户 {} 成功读取密码，密码长度: {}, 密码前缀: {}", 
                user.getUsername(), 
                encodedPassword.length(),
                encodedPassword.length() > 10 ? encodedPassword.substring(0, Math.min(10, encodedPassword.length())) : encodedPassword);
        
        // 验证密码（数据库中的密码应该已经包含前缀，如 {bcrypt}...）
        logger.debug("用户 {} 密码验证开始", user.getUsername());
        boolean passwordMatches = passwordEncoder.matches(password, encodedPassword);
        logger.debug("用户 {} 密码验证结果: {}", user.getUsername(), passwordMatches ? "成功" : "失败");
        
        if (!passwordMatches) {
            logger.warn("用户 {} 密码验证失败", user.getUsername());
            throw new BadCredentialsException("密码错误");
        }

        // 通过 UserDetailsService 加载用户详情（包括权限）
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // 创建认证成功的结果
        // 优先使用 MultiFactorAuthenticationToken（如果原始请求使用了它）
        // 否则使用标准的 UsernamePasswordAuthenticationToken（向后兼容）
        // 注意：credentials 使用 null 清空敏感信息（密码），符合 Spring Security 最佳实践
        if (provider != null && type != null) {
            // 使用 MultiFactorAuthenticationToken，保留认证方式信息
            return new MultiFactorAuthenticationToken(
                    user.getUsername(),
                    null, // 清空敏感信息
                    provider,
                    type,
                    userDetails.getAuthorities()
            );
        } else {
            // 使用标准 Token（向后兼容）
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    }

    /**
     * TOTP 认证
     * 使用 RFC 6238 标准的 TOTP 算法验证动态验证码
     * 
     * @param user 用户对象
     * @param totpCode TOTP 验证码
     * @param method 认证方法配置
     * @param provider 认证提供者
     * @param type 认证类型
     * @return 认证成功的 Authentication 对象
     */
    private Authentication authenticateTotp(User user, String totpCode, UserAuthenticationMethod method, String provider, String type) {
        Map<String, Object> config = method.getAuthenticationConfiguration();
        if (config == null) {
            logger.error("用户 {} 的 TOTP 认证配置为空", user.getUsername());
            throw new BadCredentialsException("TOTP 认证配置错误");
        }

        // 获取 TOTP 密钥（支持多种键名）
        String secret = (String) config.get("secretKey");
        if (secret == null || secret.isEmpty() || "null".equals(secret)) {
            secret = (String) config.get("secret");
        }
        
        if (secret == null || secret.isEmpty() || "null".equals(secret)) {
            logger.error("用户 {} 的 TOTP 密钥未找到，配置键: {}", user.getUsername(), config.keySet());
            throw new BadCredentialsException("TOTP 认证配置错误：未找到密钥");
        }

        // 验证 TOTP 验证码
        if (totpCode == null || totpCode.isEmpty()) {
            logger.warn("用户 {} 未提供 TOTP 验证码", user.getUsername());
            throw new BadCredentialsException("请输入 TOTP 验证码");
        }

        if (!validateTotpCode(secret, totpCode)) {
            logger.warn("用户 {} TOTP 验证码错误", user.getUsername());
            throw new BadCredentialsException("TOTP 验证码错误");
        }

        logger.debug("用户 {} TOTP 验证成功", user.getUsername());

        // 通过 UserDetailsService 加载用户详情
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // 创建认证成功的结果
        // 优先使用 MultiFactorAuthenticationToken（如果原始请求使用了它）
        // 否则使用标准的 UsernamePasswordAuthenticationToken（向后兼容）
        if (provider != null && type != null) {
            // 使用 MultiFactorAuthenticationToken，保留认证方式信息
            return new MultiFactorAuthenticationToken(
                    user.getUsername(),
                    null, // 清空敏感信息
                    provider,
                    type,
                    userDetails.getAuthorities()
            );
        } else {
            // 使用标准 Token（向后兼容）
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }
    }
    

    /**
     * 验证 TOTP 验证码
     * 实现 RFC 6238 标准的 TOTP 算法，兼容 ±1 时间步长的偏移（允许时钟偏差）
     * 
     * @param secret Base32 编码的 TOTP 密钥
     * @param submittedCode 用户提交的 6 位验证码
     * @return 验证是否通过
     */
    private boolean validateTotpCode(String secret, String submittedCode) {
        try {
            // Base32 解码密钥
            Base32 base32 = new Base32();
            byte[] keyBytes = base32.decode(secret);
            Key key = new SecretKeySpec(keyBytes, "HmacSHA1");

            // 创建 TOTP 生成器（6 位数字，30 秒时间步长）
            TimeBasedOneTimePasswordGenerator generator = new TimeBasedOneTimePasswordGenerator();

            // 获取当前时间
            long now = System.currentTimeMillis();

            // 验证当前时间步长及前后各一个时间步长（允许 ±30 秒的时钟偏差）
            // 这样可以处理网络延迟、设备时钟不同步等情况
            for (int step = -1; step <= 1; step++) {
                long time = now + step * generator.getTimeStep().toMillis();
                String code = String.format("%06d", 
                    generator.generateOneTimePassword(key, Instant.ofEpochMilli(time)));
                
                if (code.equals(submittedCode)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("TOTP 验证过程中发生异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证参数格式是否合法（防止 SQL 注入、XSS 等安全问题）
     * 只允许字母、数字、下划线和连字符
     * 
     * 注意：我们不再硬编码允许的 provider 和 type 列表，而是：
     * 1. 验证参数格式安全（防止注入攻击）
     * 2. 通过数据库查询验证用户是否实际配置了该认证方法
     * 3. 这样可以动态支持新的认证方式，无需修改代码
     */
    private boolean isValidParameterFormat(String param) {
        if (param == null || param.isEmpty()) {
            return false;
        }
        // 只允许字母、数字、下划线和连字符（防止 SQL 注入、XSS 等）
        return param.matches("^[A-Za-z0-9_-]+$");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 支持两种 Authentication 类型：
        // 1. MultiFactorAuthenticationToken（自定义，支持多因素认证，包括部分认证状态）
        // 2. UsernamePasswordAuthenticationToken（标准，向后兼容）
        // 
        // 注意：MultiFactorAuthenticationToken 可以表示部分认证状态（isAuthenticated() = false），
        // 用于多步骤认证流程（如 PASSWORD + TOTP）
        return MultiFactorAuthenticationToken.class.isAssignableFrom(authentication) ||
               UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

