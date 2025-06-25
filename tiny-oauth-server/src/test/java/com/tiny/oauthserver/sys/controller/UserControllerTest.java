package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.model.User;
import com.tiny.oauthserver.sys.model.UserRequestDto;
import com.tiny.oauthserver.sys.model.UserResponseDto;
import com.tiny.oauthserver.sys.model.UserCreateUpdateDto;
import com.tiny.oauthserver.sys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("test");
        mockUser.setPassword("pwd");
        mockUser.setNickname("nick");
        mockUser.setEnabled(true);
        mockUser.setAccountNonExpired(true);
        mockUser.setAccountNonLocked(true);
        mockUser.setCredentialsNonExpired(true);
        mockUser.setLastLoginAt(LocalDateTime.now());
    }

    @Test
    void testGetUsers() {
        Page<UserResponseDto> page = new PageImpl<>(List.of());
        when(userService.users(any(), any())).thenReturn(page);

        ResponseEntity<Page<UserResponseDto>> resp = userController.getUsers(new UserRequestDto(), Pageable.unpaged());
        assertEquals(page, resp.getBody());
        verify(userService).users(any(), any());
    }

    @Test
    void testGetUser() {
        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));
        ResponseEntity<User> resp = userController.getUser(1L);
        assertEquals(mockUser, resp.getBody());
    }

    @Test
    void testCreate() {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        when(userService.createFromDto(any(UserCreateUpdateDto.class))).thenReturn(mockUser);
        ResponseEntity<User> resp = userController.create(dto);
        assertEquals(mockUser, resp.getBody());
    }

    @Test
    void testUpdate() {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        when(userService.updateFromDto(any(UserCreateUpdateDto.class))).thenReturn(mockUser);
        ResponseEntity<User> resp = userController.update(1L, dto);
        assertEquals(mockUser, resp.getBody());
        assertEquals(1L, dto.getId());
    }

    @Test
    void testDelete() {
        doNothing().when(userService).delete(1L);
        ResponseEntity<Void> resp = userController.delete(1L);
        assertEquals(204, resp.getStatusCode().value());
    }

    @Test
    void testBatchEnable() {
        doNothing().when(userService).batchEnable(anyList());
        ResponseEntity<Map<String, Object>> resp = userController.batchEnable(List.of(1L, 2L));
        assertTrue((Boolean) resp.getBody().get("success"));
    }

    @Test
    void testBatchDisable() {
        doNothing().when(userService).batchDisable(anyList());
        ResponseEntity<Map<String, Object>> resp = userController.batchDisable(List.of(1L, 2L));
        assertTrue((Boolean) resp.getBody().get("success"));
    }

    @Test
    void testBatchDelete() {
        doNothing().when(userService).batchDelete(anyList());
        ResponseEntity<Map<String, Object>> resp = userController.batchDelete(List.of(1L, 2L));
        assertTrue((Boolean) resp.getBody().get("success"));
    }

    @Test
    void testTestUser() {
        when(userService.findById(1L)).thenReturn(Optional.of(mockUser));
        ResponseEntity<User> resp = userController.testUser(1L);
        assertEquals(mockUser, resp.getBody());
    }
}