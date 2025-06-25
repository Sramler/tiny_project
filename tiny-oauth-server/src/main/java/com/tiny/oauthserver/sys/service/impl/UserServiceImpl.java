package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserRequestDto;
import com.tiny.oauthserver.sys.model.UserResponseDto;
import com.tiny.oauthserver.sys.model.UserCreateUpdateDto;
import com.tiny.oauthserver.sys.repository.UserRepository;
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
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        // Long id, String username, String password, String nickname, boolean enabled, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired, LocalDateTime lastLoginAt)
        return new UserResponseDto(user.getId(),user.getUsername(),"",user.getNickname(),user.isEnabled(),
                user.isAccountNonExpired(),user.isAccountNonLocked(),user.isCredentialsNonExpired(),user.getLastLoginAt());
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
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
                existing.setPassword(user.getPassword());
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
    public User createFromDto(UserCreateUpdateDto userDto) {
        // 检查用户名是否已存在
        if (userRepository.findUserByUsername(userDto.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setNickname(userDto.getNickname());
        user.setEnabled(userDto.getEnabled());
        user.setAccountNonExpired(userDto.getAccountNonExpired());
        user.setAccountNonLocked(userDto.getAccountNonLocked());
        user.setCredentialsNonExpired(userDto.getCredentialsNonExpired());
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        
        return userRepository.save(user);
    }

    @Override
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
        
        // 如果提供了新密码，则更新密码
        if (userDto.needUpdatePassword()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        return userRepository.save(existingUser);
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
}