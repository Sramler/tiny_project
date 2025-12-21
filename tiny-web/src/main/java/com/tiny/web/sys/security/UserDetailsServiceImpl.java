package com.tiny.web.sys.security;

import com.tiny.web.sys.model.User;
import com.tiny.web.sys.model.UserAuthenticationMethod;
import com.tiny.web.sys.repository.UserRepository;
import com.tiny.web.sys.repository.UserAuthenticationMethodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserAuthenticationMethodRepository authenticationMethodRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        // 从 user_authentication_method 表读取密码（如果存在）
        // 为了向后兼容，如果 UserAuthenticationMethod 中有密码，设置到 User 对象中
        // 这样 User.getPassword() 仍然可以返回密码，Spring Security 可以正常验证
        Optional<UserAuthenticationMethod> methodOpt = authenticationMethodRepository
                .findByUserIdAndAuthenticationProviderAndAuthenticationType(user.getId(), "LOCAL", "PASSWORD");
        
        if (methodOpt.isPresent()) {
            UserAuthenticationMethod method = methodOpt.get();
            Map<String, Object> config = method.getAuthenticationConfiguration();
            if (config != null && !config.isEmpty()) {
                // 获取编码后的密码
                // 在 authentication_configuration 上下文中，使用 "password" 键名
                if (config.containsKey("password")) {
                    Object passwordValue = config.get("password");
                    if (passwordValue instanceof String) {
                        String encodedPassword = (String) passwordValue;
                        if (!encodedPassword.isEmpty()) {
                            // 设置编码后的密码到 User 对象（临时方案，向后兼容）
                            // 注意：数据库中的密码应该已经包含编码器前缀，如 {bcrypt}...
                            user.setPassword(encodedPassword);
                            logger.debug("从 UserAuthenticationMethod 表加载编码密码到 User 对象，用户: {}, 密码前缀: {}", 
                                    username, encodedPassword.length() > 10 
                                            ? encodedPassword.substring(0, Math.min(10, encodedPassword.length())) 
                                            : "null");
                        } else {
                            logger.warn("用户 {} 的 password 字段值为空字符串", username);
                        }
                    } else {
                        logger.warn("用户 {} 的 password 字段不是字符串类型: {}", 
                                username, passwordValue != null ? passwordValue.getClass().getName() : "null");
                    }
                } else {
                    logger.warn("用户 {} 的认证配置中不包含 password 字段。可用的键: {}", username, config.keySet());
                }
            } else {
                logger.warn("用户 {} 的认证配置为空", username);
            }
        } else {
            // 如果 UserAuthenticationMethod 中没有密码，检查 user.password 字段（向后兼容）
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                logger.warn("用户 {} 既没有 UserAuthenticationMethod 密码，也没有 user.password 字段", username);
                // 不抛出异常，允许继续（可能用户使用其他认证方式）
            }
        }
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return user;
    }
}