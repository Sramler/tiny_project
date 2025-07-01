package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.impl.RoleServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sys/roles")
public class RoleController {
    private final RoleServiceImpl roleService;
    public RoleController(RoleServiceImpl roleService) { this.roleService = roleService; }

    @GetMapping
    public ResponseEntity<PageResponse<RoleResponseDto>> list(RoleRequestDto query, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(new PageResponse<>(roleService.roles(query, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> get(@PathVariable("id") Long id) {
        return roleService.findById(id)
            .map(role -> ResponseEntity.ok(new RoleResponseDto(
                role.getId(), 
                role.getName(), 
                role.getCode(), 
                role.getDescription(), 
                role.isBuiltin(), 
                role.isEnabled(), 
                role.getCreatedAt(), 
                role.getUpdatedAt()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoleResponseDto> create(@RequestBody RoleCreateUpdateDto dto) {
        return ResponseEntity.ok(roleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponseDto> update(@PathVariable("id") Long id, @RequestBody RoleCreateUpdateDto dto) {
        return ResponseEntity.ok(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 获取所有角色列表（不分页，适用于a-transfer）
     */
    @GetMapping("/all")
    public ResponseEntity<List<RoleResponseDto>> getAllRoles() {
        // 直接查全部
        return ResponseEntity.ok(roleService.roles(new RoleRequestDto(), Pageable.unpaged()).getContent());
    }

    /**
     * 获取该角色下所有已分配用户ID列表
     */
    @GetMapping("/{id}/users")
    public ResponseEntity<List<Long>> getRoleUsers(@PathVariable("id") Long id) {
        return ResponseEntity.ok(roleService.getUserIdsByRoleId(id));
    }

    /**
     * 保存角色与用户的分配关系
     */
    @PostMapping("/{id}/users")
    public ResponseEntity<?> updateRoleUsers(@PathVariable("id") Long id, @RequestBody List<Long> userIds) {
        roleService.updateRoleUsers(id, userIds);
        return ResponseEntity.ok().build();
    }
} 