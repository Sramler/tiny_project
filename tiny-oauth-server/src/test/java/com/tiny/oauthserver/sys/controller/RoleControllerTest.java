package com.tiny.oauthserver.sys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tiny.oauthserver.sys.model.RoleCreateUpdateDto;
import com.tiny.oauthserver.sys.model.RoleRequestDto;
import com.tiny.oauthserver.sys.model.RoleResponseDto;
import com.tiny.oauthserver.sys.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleServiceImpl roleService;

    @InjectMocks
    private RoleController roleController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /sys/roles 返回分页结果 200")
    void listRoles_ok() throws Exception {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(1L);
        dto.setName("Admin");
        Page<RoleResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        Mockito.when(roleService.roles(any(RoleRequestDto.class), any())).thenReturn(page);

        mockMvc.perform(get("/sys/roles").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Admin"));
    }

    @Test
    @DisplayName("GET /sys/roles/{id} 未找到返回 404")
    void getRole_notFound() throws Exception {
        Mockito.when(roleService.findById(eq(9L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/sys/roles/9").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /sys/roles 创建成功返回 200")
    void createRole_ok() throws Exception {
        RoleCreateUpdateDto createDto = new RoleCreateUpdateDto();
        createDto.setName("User");
        RoleResponseDto resp = new RoleResponseDto();
        resp.setId(2L);
        resp.setName("User");
        Mockito.when(roleService.create(any(RoleCreateUpdateDto.class))).thenReturn(resp);

        mockMvc.perform(post("/sys/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("User"));
    }

    @Test
    @DisplayName("PUT /sys/roles/{id} 更新成功返回 200")
    void updateRole_ok() throws Exception {
        RoleCreateUpdateDto updateDto = new RoleCreateUpdateDto();
        updateDto.setName("Manager");
        RoleResponseDto resp = new RoleResponseDto();
        resp.setId(3L);
        resp.setName("Manager");
        when(roleService.update(eq(3L), any(RoleCreateUpdateDto.class))).thenReturn(resp);

        mockMvc.perform(put("/sys/roles/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.name").value("Manager"));
    }

    @Test
    @DisplayName("DELETE /sys/roles/{id} 删除成功返回 204")
    void deleteRole_noContent() throws Exception {
        mockMvc.perform(delete("/sys/roles/8"))
                .andExpect(status().isNoContent());

        verify(roleService).delete(8L);
    }

    @Test
    @DisplayName("GET /sys/roles/all 返回全部列表")
    void getAllRoles_ok() throws Exception {
        RoleResponseDto dto = new RoleResponseDto();
        dto.setId(6L);
        dto.setName("All");
        Page<RoleResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 1), 1);
        when(roleService.roles(any(RoleRequestDto.class), any())).thenReturn(page);

        mockMvc.perform(get("/sys/roles/all").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(6L));
    }

    @Test
    @DisplayName("GET /sys/roles/{id}/users 返回用户列表")
    void getRoleUsers_ok() throws Exception {
        when(roleService.getUserIdsByRoleId(5L)).thenReturn(List.of(1L, 2L));

        mockMvc.perform(get("/sys/roles/5/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1]").value(2L));
    }

    @Test
    @DisplayName("POST /sys/roles/{id}/users 更新角色用户")
    void updateRoleUsers_ok() throws Exception {
        List<Long> userIds = List.of(10L, 11L);

        mockMvc.perform(post("/sys/roles/7/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userIds)))
                .andExpect(status().isOk());

        verify(roleService).updateRoleUsers(7L, userIds);
    }

    @Test
    @DisplayName("GET /sys/roles/{id}/resources 返回资源列表")
    void getRoleResources_ok() throws Exception {
        when(roleService.getResourceIdsByRoleId(4L)).thenReturn(List.of(20L, 21L));

        mockMvc.perform(get("/sys/roles/4/resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(20L));
    }

    @Test
    @DisplayName("POST /sys/roles/{id}/resources 更新角色资源")
    void updateRoleResources_ok() throws Exception {
        List<Long> resourceIds = List.of(100L, 101L);

        mockMvc.perform(post("/sys/roles/9/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceIds)))
                .andExpect(status().isOk());

        verify(roleService).updateRoleResources(9L, resourceIds);
    }
}


