package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sys/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserResponseDto>> getUsers(
            @Valid UserRequestDto query,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(new PageResponse<>(userService.users(query, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody UserCreateUpdateDto userDto) {
        User user = userService.createFromDto(userDto);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable("id") Long id, @Valid @RequestBody UserCreateUpdateDto userDto) {
        // 设置ID
        userDto.setId(id);
        
        User user = userService.updateFromDto(userDto);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 批量启用用户
     */
    @PostMapping("/batch/enable")
    public ResponseEntity<Map<String, Object>> batchEnable(@RequestBody List<Long> ids) {
        try {
            userService.batchEnable(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量启用成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 批量禁用用户
     */
    @PostMapping("/batch/disable")
    public ResponseEntity<Map<String, Object>> batchDisable(@RequestBody List<Long> ids) {
        try {
            userService.batchDisable(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量禁用成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 批量删除用户
     */
    @PostMapping("/batch/delete")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        try {
            userService.batchDelete(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 查询指定用户已绑定的角色ID列表
     */
    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Long>> getUserRoles(@PathVariable("id") Long id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(user.getRoles().stream().map(role -> role.getId()).toList()))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 保存用户角色绑定
     */
    @PostMapping("/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable("id") Long id, @RequestBody List<Long> roleIds) {
        try {
            userService.updateUserRoles(id, roleIds);
            return ResponseEntity.ok(Map.of("success", true, "message", "用户角色已更新"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}