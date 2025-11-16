package com.tiny.oauthserver.sys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.RoleCreateUpdateDto;
import com.tiny.oauthserver.sys.model.RoleRequestDto;
import com.tiny.oauthserver.sys.model.RoleResponseDto;
import com.tiny.oauthserver.sys.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

@MockBean
    private RoleServiceImpl roleService;

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
}


