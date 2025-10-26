package com.tiny.oauthserver.sys.security;

import com.tiny.oauthserver.sys.model.SecurityUser;
import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserAuthenticationMethod;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.repository.UserAuthenticationMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;
    private final UserAuthenticationMethodRepository authenticationMethodRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, 
                                   UserAuthenticationMethodRepository authenticationMethodRepository) {
        this.userRepository = userRepository;
        this.authenticationMethodRepository = authenticationMethodRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // 从 user_authentication_method 表获取 LOCAL + PASSWORD 认证方法的密码
        String password = getPasswordFromAuthenticationMethod(user.getId());
        
        // 如果 user_authentication_method 表中没有密码，则尝试从 user 表获取（兼容旧数据）
        if (password == null || password.isEmpty()) {
            logger.warn("用户 {} 在 user_authentication_method 表中没有找到密码，使用 user 表中的密码", username);
            password = user.getPassword();
        }

        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 创建 SecurityUser，使用从认证方法表获取的密码
        return new SecurityUser(user, password);
    }

    /**
     * 从 user_authentication_method 表获取用户的密码
     */
    private String getPasswordFromAuthenticationMethod(Long userId) {
        Optional<UserAuthenticationMethod> methodOpt = authenticationMethodRepository
                .findByUserIdAndAuthenticationProviderAndAuthenticationType(userId, "LOCAL", "PASSWORD");

        if (methodOpt.isPresent()) {
            UserAuthenticationMethod method = methodOpt.get();
            if (!Boolean.TRUE.equals(method.getIsMethodEnabled())) {
                logger.warn("用户的 LOCAL + PASSWORD 认证方法已禁用，用户ID: {}", userId);
                return null;
            }

            Map<String, Object> config = method.getAuthenticationConfiguration();
            if (config != null && config.containsKey("passwordHash")) {
                return (String) config.get("passwordHash");
            }
        }

        return null;
    }
}