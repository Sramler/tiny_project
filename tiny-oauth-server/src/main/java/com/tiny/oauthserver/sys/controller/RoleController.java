package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/roles")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @GetMapping
    public ResponseEntity<Page<RoleResponseDto>> list(RoleRequestDto query, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(roleService.roles(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDto> get(@PathVariable("id") Long id) {
        return roleService.findById(id)
            .map(role -> ResponseEntity.ok(new RoleResponseDto(role.getId(), role.getName(), role.getDescription())))
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
} 