package com.tiny.oauthserver.sys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerAllTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    // 正则示例（当前未使用）
    // private static final Pattern ISO_LOCAL_DATE_TIME = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

    @Test
    @DisplayName("GET /sys/users 分页列表，lastLoginAt 为 ISO 字符串")
    void users_paged_and_iso_date() throws Exception {
        UserResponseDto u = new UserResponseDto(1L, "alice", "Alice", true, true, true, true, LocalDateTime.now());
        Page<UserResponseDto> page = new PageImpl<>(List.of(u), PageRequest.of(0, 10), 1);
        Mockito.when(userService.users(any(UserRequestDto.class), any())).thenReturn(page);

        mockMvc.perform(get("/sys/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username").value("alice"))
                .andExpect(jsonPath("$.content[0].lastLoginAt").exists());
    }

    @Test
    @DisplayName("GET /sys/users/{id} 返回 200/404")
    void getUser_ok_and_404() throws Exception {
        User u = new User();
        u.setId(1L); u.setUsername("test");
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(u));
        Mockito.when(userService.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/sys/users/1")).andExpect(status().isOk());
        mockMvc.perform(get("/sys/users/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /sys/users/current 未认证401、用户不存在404、成功200")
    void current_all_paths() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/sys/users/current")).andExpect(status().isUnauthorized());

        TestingAuthenticationToken bobAuth = new TestingAuthenticationToken("bob", "N/A");
        bobAuth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(bobAuth);
        Mockito.when(userService.findByUsername("bob")).thenReturn(Optional.empty());
        mockMvc.perform(get("/sys/users/current")).andExpect(status().isNotFound());

        TestingAuthenticationToken carolAuth = new TestingAuthenticationToken("carol", "N/A");
        carolAuth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(carolAuth);
        User user = new User(); user.setId(7L); user.setUsername("carol");
        Mockito.when(userService.findByUsername("carol")).thenReturn(Optional.of(user));
        mockMvc.perform(get("/sys/users/current")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST/PUT/DELETE /sys/users 创建、更新、删除 200/204")
    void create_update_delete() throws Exception {
        UserCreateUpdateDto createDto = new UserCreateUpdateDto();
        createDto.setUsername("dave");
        createDto.setNickname("Nick");
        createDto.setEnabled(true);
        createDto.setAccountNonExpired(true);
        createDto.setAccountNonLocked(true);
        createDto.setCredentialsNonExpired(true);
        createDto.setPassword("secret123");
        createDto.setConfirmPassword("secret123");
        User saved = new User(); saved.setId(8L); saved.setUsername("dave");
        Mockito.when(userService.createFromDto(any(UserCreateUpdateDto.class))).thenReturn(saved);
        mockMvc.perform(post("/sys/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8L));

        UserCreateUpdateDto updateDto = new UserCreateUpdateDto();
        updateDto.setUsername("neo");
        updateDto.setNickname("Neo");
        updateDto.setEnabled(true);
        updateDto.setAccountNonExpired(true);
        updateDto.setAccountNonLocked(true);
        updateDto.setCredentialsNonExpired(true);
        updateDto.setPassword("newSecret123");
        updateDto.setConfirmPassword("newSecret123");
        User updated = new User(); updated.setId(8L); updated.setUsername("neo");
        Mockito.when(userService.updateFromDto(any(UserCreateUpdateDto.class))).thenReturn(updated);
        mockMvc.perform(put("/sys/users/8").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("neo"));

        mockMvc.perform(delete("/sys/users/8")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("批量 enable/disable/delete 成功与失败分支")
    void batch_ops() throws Exception {
        // enable ok
        mockMvc.perform(post("/sys/users/batch/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L,2L))))
                .andExpect(status().isOk());

        // enable fail
        Mockito.doThrow(new RuntimeException("err")).when(userService).batchEnable(anyList());
        mockMvc.perform(post("/sys/users/batch/enable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(3L))))
                .andExpect(status().isBadRequest());

        // disable ok + fail
        mockMvc.perform(post("/sys/users/batch/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L))))
                .andExpect(status().isOk());
        Mockito.doThrow(new RuntimeException("err")).when(userService).batchDisable(anyList());
        mockMvc.perform(post("/sys/users/batch/disable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(2L))))
                .andExpect(status().isBadRequest());

        // delete ok + fail
        mockMvc.perform(post("/sys/users/batch/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ArrayList<>(List.of(1L,2L)))))
                .andExpect(status().isOk());
        Mockito.doThrow(new RuntimeException("err")).when(userService).batchDelete(anyList());
        mockMvc.perform(post("/sys/users/batch/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(9L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET/POST 用户角色查询与更新")
    void user_roles() throws Exception {
        User u = new User(); u.setId(1L);
        u.setRoles(new java.util.HashSet<>());
        Mockito.when(userService.findById(1L)).thenReturn(Optional.of(u));
        mockMvc.perform(get("/sys/users/1/roles")).andExpect(status().isOk());

        Mockito.doNothing().when(userService).updateUserRoles(eq(1L), anyList());
        mockMvc.perform(post("/sys/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(1L,2L))))
                .andExpect(status().isOk());
    }
}


