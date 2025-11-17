package com.tiny.oauthserver.sys.security;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserDetailsServiceImplTest {

    private UserRepository userRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void shouldLoadUserWithRolesAndResources() {
        // 准备资源
        Resource resource1 = new Resource();
        resource1.setId(1L);
        resource1.setName("PERM_READ");

        Resource resource2 = new Resource();
        resource2.setId(2L);
        resource2.setName("PERM_WRITE");

        // 准备角色
        Role role = new Role();
        role.setId(1L);
        role.setName("ADMIN");
        role.setResources(Set.of(resource1, resource2));

        // 准备用户
        User user = new User();
        user.setId(100L);
        user.setUsername("testuser");
        user.setRoles(Set.of(role));
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setEnabled(true);
        user.setLastLoginAt(LocalDateTime.now());

        when(userRepository.findUserByUsername("testuser"))
                .thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // 验证
        assertThat(userDetails).isInstanceOf(SecurityUser.class);
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEmpty();

        // 角色和资源权限
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ADMIN", "PERM_READ", "PERM_WRITE");

        // 验证 userRepository.save 被调用（记录 lastLoginAt）
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findUserByUsername("nonexistent"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("用户不存在");
    }
}