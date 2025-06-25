package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserCreateUpdateDto;
import com.tiny.oauthserver.sys.model.UserRequestDto;
import com.tiny.oauthserver.sys.model.UserResponseDto;
import com.tiny.oauthserver.sys.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("pwd");
        user.setNickname("nick");
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        user.setLastLoginAt(LocalDateTime.now());
    }

    @Test
    void testUsers() {
        UserRequestDto query = new UserRequestDto();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<UserResponseDto> result = userService.users(query, pageable);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testFindById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void testCreate() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userService.create(user);
        assertEquals(user, result);
    }

    @Test
    void testUpdate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updated = new User();
        updated.setId(1L);
        updated.setUsername("new");
        updated.setPassword("newpwd");
        updated.setNickname("newnick");
        updated.setEnabled(false);
        updated.setLastLoginAt(LocalDateTime.now());
        User result = userService.update(1L, updated);
        assertEquals(user, result);
    }

    @Test
    void testDelete() {
        doNothing().when(userRepository).deleteById(1L);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testCreateFromDto() {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        dto.setUsername("test");
        dto.setPassword("pwd");
        dto.setNickname("nick");
        dto.setEnabled(true);
        dto.setAccountNonExpired(true);
        dto.setAccountNonLocked(true);
        dto.setCredentialsNonExpired(true);

        when(userRepository.findUserByUsername("test")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pwd")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createFromDto(dto);
        assertEquals(user, result);
    }

    @Test
    void testUpdateFromDto() {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        dto.setId(1L);
        dto.setUsername("test");
        dto.setNickname("nick");
        dto.setEnabled(true);
        dto.setAccountNonExpired(true);
        dto.setAccountNonLocked(true);
        dto.setCredentialsNonExpired(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findUserByUsername("test")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateFromDto(dto);
        assertEquals(user, result);
    }

    @Test
    void testBatchEnable() {
        List<Long> ids = List.of(1L, 2L);
        User user2 = new User();
        user2.setId(2L);
        user2.setEnabled(false);
        when(userRepository.findAllById(ids)).thenReturn(List.of(user, user2));
        when(userRepository.saveAll(anyList())).thenReturn(List.of(user, user2));
        userService.batchEnable(ids);
        assertTrue(user.isEnabled());
        assertTrue(user2.isEnabled());
    }

    @Test
    void testBatchDisable() {
        List<Long> ids = List.of(1L, 2L);
        User user2 = new User();
        user2.setId(2L);
        user2.setEnabled(true);
        when(userRepository.findAllById(ids)).thenReturn(List.of(user, user2));
        when(userRepository.saveAll(anyList())).thenReturn(List.of(user, user2));
        userService.batchDisable(ids);
        assertFalse(user.isEnabled());
        assertFalse(user2.isEnabled());
    }

    @Test
    void testBatchDelete() {
        List<Long> ids = List.of(1L, 2L);
        User user2 = new User();
        user2.setId(2L);
        when(userRepository.findAllById(ids)).thenReturn(List.of(user, user2));
        doNothing().when(userRepository).deleteAll(anyList());
        userService.batchDelete(ids);
        verify(userRepository).deleteAll(anyList());
    }
}