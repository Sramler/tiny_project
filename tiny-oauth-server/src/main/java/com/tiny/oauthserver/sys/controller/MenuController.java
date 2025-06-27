package com.tiny.oauthserver.sys.controller;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.service.MenuService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 菜单管理控制器
 * 提供菜单管理的REST API接口
 */
@RestController
@RequestMapping("/sys/menus")
public class MenuController {
    
    private final MenuService menuService;
    
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 分页查询菜单
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    @GetMapping
    public ResponseEntity<PageResponse<MenuResponseDto>> getMenus(
            @Valid MenuRequestDto query,
            @PageableDefault(size = 10, sort = "sort", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(new PageResponse<>(menuService.menus(query, pageable)));
    }

    /**
     * 根据ID获取菜单详情
     * @param id 菜单ID
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Menu> getMenu(@PathVariable("id") Long id) {
        return menuService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建菜单
     * @param menuDto 菜单创建DTO
     * @return 创建的菜单
     */
    @PostMapping
    public ResponseEntity<Menu> create(@Valid @RequestBody MenuCreateUpdateDto menuDto) {
        Menu menu = menuService.createFromDto(menuDto);
        return ResponseEntity.ok(menu);
    }

    /**
     * 更新菜单
     * @param id 菜单ID
     * @param menuDto 菜单更新DTO
     * @return 更新后的菜单
     */
    @PutMapping("/{id}")
    public ResponseEntity<Menu> update(@PathVariable("id") Long id, @Valid @RequestBody MenuCreateUpdateDto menuDto) {
        // 设置ID
        menuDto.setId(id);
        
        Menu menu = menuService.updateFromDto(menuDto);
        return ResponseEntity.ok(menu);
    }

    /**
     * 删除菜单
     * @param id 菜单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 批量删除菜单
     * @param ids 菜单ID列表
     * @return 删除结果
     */
    @PostMapping("/batch/delete")
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody List<Long> ids) {
        try {
            menuService.batchDelete(ids);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 根据父级ID获取子菜单列表
     * @param parentId 父级菜单ID
     * @return 子菜单列表
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<Menu>> getMenusByParentId(@PathVariable("parentId") Long parentId) {
        List<Menu> menus = menuService.findByParentId(parentId);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 获取顶级菜单列表
     * @return 顶级菜单列表
     */
    @GetMapping("/top-level")
    public ResponseEntity<List<Menu>> getTopLevelMenus() {
        List<Menu> menus = menuService.findTopLevel();
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 获取菜单树结构
     * @return 菜单树
     */
    @GetMapping("/tree")
    public ResponseEntity<List<MenuResponseDto>> getMenuTree() {
        List<MenuResponseDto> tree = menuService.getAllMenuTree();
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 获取用户可见的菜单树
     * @param userId 用户ID
     * @return 用户可见的菜单树
     */
    @GetMapping("/user/{userId}/tree")
    public ResponseEntity<List<MenuResponseDto>> getUserMenuTree(@PathVariable("userId") Long userId) {
        List<MenuResponseDto> tree = menuService.getUserMenuTree(userId);
        return ResponseEntity.ok(tree);
    }
    
    /**
     * 更新菜单排序
     * @param id 菜单ID
     * @param sort 新的排序值
     * @return 更新后的菜单
     */
    @PutMapping("/{id}/sort")
    public ResponseEntity<Menu> updateSort(@PathVariable("id") Long id, @RequestParam("sort") Integer sort) {
        Menu menu = menuService.updateSort(id, sort);
        return ResponseEntity.ok(menu);
    }
    
    /**
     * 批量更新菜单排序
     * @param sortData 排序数据列表
     * @return 更新结果
     */
    @PutMapping("/batch/sort")
    public ResponseEntity<Map<String, Object>> batchUpdateSort(@RequestBody List<MenuSortDto> sortData) {
        try {
            menuService.batchUpdateSort(sortData);
            return ResponseEntity.ok(Map.of("success", true, "message", "批量更新排序成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * 根据权限标识获取菜单列表
     * @param permission 权限标识
     * @return 菜单列表
     */
    @GetMapping("/permission/{permission}")
    public ResponseEntity<List<Menu>> getMenusByPermission(@PathVariable("permission") String permission) {
        List<Menu> menus = menuService.findByPermission(permission);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 根据是否隐藏获取菜单列表
     * @param hidden 是否隐藏
     * @return 菜单列表
     */
    @GetMapping("/hidden/{hidden}")
    public ResponseEntity<List<Menu>> getMenusByHidden(@PathVariable("hidden") boolean hidden) {
        List<Menu> menus = menuService.findByHidden(hidden);
        return ResponseEntity.ok(menus);
    }
    
    /**
     * 检查菜单名称是否存在
     * @param name 菜单名称
     * @param excludeId 要排除的菜单ID
     * @return 是否存在
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Boolean>> checkNameExists(
            @RequestParam("name") String name,
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        boolean exists = menuService.existsByName(name, excludeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
    /**
     * 检查菜单路径是否存在
     * @param path 菜单路径
     * @param excludeId 要排除的菜单ID
     * @return 是否存在
     */
    @GetMapping("/check-path")
    public ResponseEntity<Map<String, Boolean>> checkPathExists(
            @RequestParam("path") String path,
            @RequestParam(value = "excludeId", required = false) Long excludeId) {
        boolean exists = menuService.existsByPath(path, excludeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }
    
    /**
     * 获取菜单选项列表（用于下拉选择）
     * @return 菜单选项列表
     */
    @GetMapping("/options")
    public ResponseEntity<List<Map<String, Object>>> getMenuOptions() {
        List<Menu> allMenus = menuService.findTopLevel();
        List<Map<String, Object>> options = buildMenuOptions(allMenus);
        return ResponseEntity.ok(options);
    }
    
    /**
     * 构建菜单选项列表
     * @param menus 菜单列表
     * @return 选项列表
     */
    private List<Map<String, Object>> buildMenuOptions(List<Menu> menus) {
        List<Map<String, Object>> options = new java.util.ArrayList<>();
        for (Menu menu : menus) {
            Map<String, Object> option = Map.of(
                "value", menu.getId(),
                "label", menu.getTitle(),
                "children", buildMenuOptions(menuService.findByParentId(menu.getId()))
            );
            options.add(option);
        }
        return options;
    }
} 