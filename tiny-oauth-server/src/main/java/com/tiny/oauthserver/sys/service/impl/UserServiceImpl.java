package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.model.UserCreateUpdateDto;
import com.tiny.oauthserver.sys.repository.UserRepository;
import com.tiny.oauthserver.sys.repository.RoleRepository;
import com.tiny.oauthserver.sys.repository.UserAuthenticationMethodRepository;
import com.tiny.oauthserver.sys.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.HashSet;
import java.util.HashMap;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserAuthenticationMethodRepository authenticationMethodRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          RoleRepository roleRepository, UserAuthenticationMethodRepository authenticationMethodRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationMethodRepository = authenticationMethodRepository;
    }


    @Override
    public Page<UserResponseDto> users(UserRequestDto query, Pageable pageable) {
        Page<User> users = userRepository.findAll((Specification<User>) (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(query.getUsername())) {
                predicates.add(cb.like(root.get("username"), "%" + query.getUsername() + "%"));
            }
            if (StringUtils.hasText(query.getNickname())) {
                predicates.add(cb.like(root.get("nickname"), "%" + query.getNickname() + "%")); // ⚠️ 字段名错误？可能你原本写了 phone？
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);

        // ⚠️ 用 Page.map() 转换为 Page<UserResponseDto>
        return users.map(this::toDto);
    }

    private UserResponseDto toDto(User user) {
        return new UserResponseDto(user.getId(),user.getUsername(),user.getNickname(),user.isEnabled(),
                user.isAccountNonExpired(),user.isAccountNonLocked(),user.isCredentialsNonExpired(),user.getLastLoginAt());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        return userRepository.findById(id)
            .map(existing -> {
                existing.setUsername(user.getUsername());
                // 不再更新 user.password，密码已迁移到 user_authentication_method 表
                existing.setNickname(user.getNickname());
                existing.setEnabled(user.isEnabled());
                existing.setLastLoginAt(user.getLastLoginAt());
                return userRepository.save(existing);
            })
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User createFromDto(UserCreateUpdateDto userDto) {
        // 检查用户名是否已存在
        if (userRepository.findUserByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户（不设置密码）
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setNickname(userDto.getNickname());
        user.setEnabled(userDto.getEnabled());
        user.setAccountNonExpired(userDto.getAccountNonExpired());
        user.setAccountNonLocked(userDto.getAccountNonLocked());
        user.setCredentialsNonExpired(userDto.getCredentialsNonExpired());
        
        // 处理角色
        if (userDto.getRoleIds() != null && !userDto.getRoleIds().isEmpty()) {
            var roles = roleRepository.findAllById(userDto.getRoleIds());
            if (roles.size() != userDto.getRoleIds().size()) {
                throw new RuntimeException("部分角色不存在");
            }
            user.setRoles(new HashSet<>(roles));
        }
        
        // 保存用户，获取用户ID
        user = userRepository.save(user);
        
        // 创建认证方法（将密码存储在 user_authentication_method 表中）
        createPasswordAuthenticationMethod(user.getId(), userDto.getPassword());
        
        return user;
    }

    @Override
    @Transactional
    public User updateFromDto(UserCreateUpdateDto userDto) {
        User existingUser = userRepository.findById(userDto.getId())
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查用户名是否已被其他用户使用
        Optional<User> userWithSameUsername = userRepository.findUserByUsername(userDto.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(userDto.getId())) {
            throw new RuntimeException("用户名已被其他用户使用");
        }
        
        // 更新基本信息
        existingUser.setUsername(userDto.getUsername());
        existingUser.setNickname(userDto.getNickname());
        existingUser.setEnabled(userDto.getEnabled());
        existingUser.setAccountNonExpired(userDto.getAccountNonExpired());
        existingUser.setAccountNonLocked(userDto.getAccountNonLocked());
        existingUser.setCredentialsNonExpired(userDto.getCredentialsNonExpired());
        
        // 如果提供了新密码，则更新认证方法表中的密码
        if (userDto.needUpdatePassword()) {
            updatePasswordAuthenticationMethod(userDto.getId(), userDto.getPassword());
        }
        
        // 处理角色
        if (userDto.getRoleIds() != null) {
            var roles = roleRepository.findAllById(userDto.getRoleIds());
            if (roles.size() != userDto.getRoleIds().size()) {
                throw new RuntimeException("部分角色不存在");
            }
            existingUser.setRoles(new HashSet<>(roles));
        }
        
        return userRepository.save(existingUser);
    }
    
    /**
     * 创建密码认证方法
     * @param userId 用户ID
     * @param plainPassword 明文密码
     */
    private void createPasswordAuthenticationMethod(Long userId, String plainPassword) {
        // 加密密码（DelegatingPasswordEncoder 会自动添加 {bcrypt} 前缀）
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // 创建认证配置
        Map<String, Object> config = new HashMap<>();
        config.put("password", encodedPassword);
        
        // 创建认证方法
        UserAuthenticationMethod method = new UserAuthenticationMethod();
        method.setUserId(userId);
        method.setAuthenticationProvider("LOCAL");
        method.setAuthenticationType("PASSWORD");
        method.setAuthenticationConfiguration(config);
        method.setIsPrimaryMethod(true);
        method.setIsMethodEnabled(true);
        method.setAuthenticationPriority(0);
        method.setCreatedAt(LocalDateTime.now());
        method.setUpdatedAt(LocalDateTime.now());
        
        // 保存认证方法
        authenticationMethodRepository.save(method);
    }
    
    /**
     * 更新密码认证方法
     * @param userId 用户ID
     * @param plainPassword 新明文密码
     */
    private void updatePasswordAuthenticationMethod(Long userId, String plainPassword) {
        // 查找现有的认证方法
        Optional<UserAuthenticationMethod> existingMethod = authenticationMethodRepository
                .findByUserIdAndAuthenticationProviderAndAuthenticationType(userId, "LOCAL", "PASSWORD");
        
        if (existingMethod.isPresent()) {
            // 更新现有认证方法
            UserAuthenticationMethod method = existingMethod.get();
            Map<String, Object> config = method.getAuthenticationConfiguration();
            if (config == null) {
                config = new HashMap<>();
            }
            // 加密新密码（DelegatingPasswordEncoder 会自动添加 {bcrypt} 前缀）
            String encodedPassword = passwordEncoder.encode(plainPassword);
            config.put("password", encodedPassword);
            method.setAuthenticationConfiguration(config);
            method.setUpdatedAt(LocalDateTime.now());
            authenticationMethodRepository.save(method);
        } else {
            // 如果不存在，则创建新的认证方法
            createPasswordAuthenticationMethod(userId, plainPassword);
        }
    }

    @Override
    @Transactional
    public void batchEnable(List<Long> ids) {
        // 批量启用用户，使用事务确保一致性
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new RuntimeException("部分用户不存在");
        }
        
        for (User user : users) {
            user.setEnabled(true);
        }
        userRepository.saveAll(users);
    }
    
    @Override
    @Transactional
    public void batchDisable(List<Long> ids) {
        // 批量禁用用户，使用事务确保一致性
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new RuntimeException("部分用户不存在");
        }
        
        for (User user : users) {
            user.setEnabled(false);
        }
        userRepository.saveAll(users);
    }
    
    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        // 批量删除用户，使用事务确保一致性
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new RuntimeException("部分用户不存在");
        }
        
        userRepository.deleteAll(users);
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));
        // 查询所有角色
        List<com.tiny.oauthserver.sys.model.Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new RuntimeException("部分角色不存在");
        }
        user.setRoles(new java.util.HashSet<>(roles));
        userRepository.save(user);
    }
}