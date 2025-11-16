package com.tiny.oauthserver.sys.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResourceControllerAllTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ResourceService resourceService;
    private ResourceController resourceController;

    @BeforeEach
    void setup() {
        resourceService = mock(ResourceService.class);
        resourceController = new ResourceController(resourceService);
        mockMvc = MockMvcBuilders.standaloneSetup(resourceController).build();
    }

    @Test
    @DisplayName("GET /sys/resources 分页 200")
    void listResources_ok() throws Exception {
        ResourceResponseDto dto = new ResourceResponseDto();
        dto.setId(1L); dto.setName("menu_home");
        Page<ResourceResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        Mockito.when(resourceService.resources(any(ResourceRequestDto.class), any())).thenReturn(page);
        mockMvc.perform(get("/sys/resources")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /sys/resources/{id} 404")
    void getResource_notFound() throws Exception {
        Mockito.when(resourceService.findById(99L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/sys/resources/99")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST/PUT/DELETE 资源创建/更新/删除")
    void create_update_delete() throws Exception {
        Resource body = new Resource(); body.setName("api_user");
        Resource saved = new Resource(); saved.setId(5L); saved.setName("api_user");
        Mockito.when(resourceService.create(any(Resource.class))).thenReturn(saved);
        mockMvc.perform(post("/sys/resources").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        ResourceCreateUpdateDto dto = new ResourceCreateUpdateDto(); dto.setName("api_users");
        Resource updated = new Resource(); updated.setId(1L); updated.setName("api_users");
        Mockito.when(resourceService.updateFromDto(any(ResourceCreateUpdateDto.class))).thenReturn(updated);
        mockMvc.perform(put("/sys/resources/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        mockMvc.perform(delete("/sys/resources/9")).andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("菜单相关接口：分页、树、排序")
    void menu_endpoints() throws Exception {
        ResourceResponseDto dto = new ResourceResponseDto(); dto.setId(2L);
        Page<ResourceResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
        Mockito.when(resourceService.resources(any(ResourceRequestDto.class), any())).thenReturn(page);
        mockMvc.perform(get("/sys/resources/menus")).andExpect(status().isOk());

        Mockito.when(resourceService.findByTypeIn(anyList())).thenReturn(List.of());
        Mockito.when(resourceService.buildResourceTree(anyList())).thenReturn(List.of());
        mockMvc.perform(get("/sys/resources/menus/tree")).andExpect(status().isOk());

        Resource r = new Resource(); r.setId(3L); r.setSort(5);
        Mockito.when(resourceService.updateSort(3L, 5)).thenReturn(r);
        mockMvc.perform(put("/sys/resources/menus/3/sort?sort=5")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("exist 校验接口")
    void exists_checks() throws Exception {
        Mockito.when(resourceService.existsByName(eq("n"), isNull())).thenReturn(true);
        Mockito.when(resourceService.existsByUrl(eq("u"), isNull())).thenReturn(false);
        Mockito.when(resourceService.existsByUri(eq("r"), isNull())).thenReturn(true);
        mockMvc.perform(get("/sys/resources/check-name?name=n")).andExpect(status().isOk());
        mockMvc.perform(get("/sys/resources/check-url?url=u")).andExpect(status().isOk());
        mockMvc.perform(get("/sys/resources/check-uri?uri=r")).andExpect(status().isOk());
    }
}


