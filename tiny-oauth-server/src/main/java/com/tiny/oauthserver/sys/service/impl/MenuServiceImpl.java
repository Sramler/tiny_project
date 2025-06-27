package com.tiny.oauthserver.sys.service.impl;

import com.tiny.oauthserver.sys.model.*;
import com.tiny.oauthserver.sys.repository.MenuRepository;
import com.tiny.oauthserver.sys.service.MenuService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;

    public MenuServiceImpl(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Override
    public Page<MenuResponseDto> menus(MenuRequestDto query, Pageable pageable) {
        // 构建查询条件
        String name = StringUtils.hasText(query.getName()) ? query.getName() : null;
        String title = StringUtils.hasText(query.getTitle()) ? query.getTitle() : null;
        String path = StringUtils.hasText(query.getPath()) ? query.getPath() : null;
        String permission = StringUtils.hasText(query.getPermission()) ? query.getPermission() : null;
        Long parentId = query.getParentId();
        Boolean hidden = query.getHidden();

        // 根据条件查询
        Page<Menu> menus;
        if (name != null) {
            menus = menuRepository.findByNameContainingIgnoreCaseOrderBySortAsc(name, pageable);
        } else if (title != null) {
            menus = menuRepository.findByTitleContainingIgnoreCaseOrderBySortAsc(title, pageable);
        } else if (path != null) {
            menus = menuRepository.findByPathContainingIgnoreCaseOrderBySortAsc(path, pageable);
        } else if (permission != null) {
            menus = menuRepository.findByPermissionContainingIgnoreCaseOrderBySortAsc(permission, pageable);
        } else if (parentId != null) {
            menus = menuRepository.findByParentIdOrderBySortAsc(parentId, pageable);
        } else if (hidden != null) {
            // 由于findByHiddenOrderBySortAsc返回List，需要手动分页
            List<Menu> allMenus = menuRepository.findByHiddenOrderBySortAsc(hidden);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allMenus.size());
            List<Menu> pageContent = allMenus.subList(start, end);
            menus = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allMenus.size());
        } else {
            // 默认查询所有菜单
            menus = menuRepository.findAll(pageable);
        }

        // 转换为DTO
        return menus.map(this::toDto);
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    @Override
    public Menu create(Menu menu) {
        return menuRepository.save(menu);
    }

    @Override
    public Menu update(Long id, Menu menu) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
        
        // 更新字段
        existingMenu.setName(menu.getName());
        existingMenu.setTitle(menu.getTitle());
        existingMenu.setPath(menu.getPath());
        existingMenu.setIcon(menu.getIcon());
        existingMenu.setShowIcon(menu.isShowIcon());
        existingMenu.setSort(menu.getSort());
        existingMenu.setComponent(menu.getComponent());
        existingMenu.setRedirect(menu.getRedirect());
        existingMenu.setHidden(menu.isHidden());
        existingMenu.setKeepAlive(menu.isKeepAlive());
        existingMenu.setPermission(menu.getPermission());
        existingMenu.setParentId(menu.getParentId());
        
        return menuRepository.save(existingMenu);
    }

    @Override
    public Menu createFromDto(MenuCreateUpdateDto menuDto) {
        // 检查名称是否已存在
        if (menuRepository.findByName(menuDto.getName()).isPresent()) {
            throw new RuntimeException("菜单名称已存在");
        }
        
        // 检查路径是否已存在（如果提供了路径）
        if (StringUtils.hasText(menuDto.getPath()) && 
            menuRepository.findByPath(menuDto.getPath()).isPresent()) {
            throw new RuntimeException("菜单路径已存在");
        }
        
        // 创建菜单对象
        Menu menu = new Menu();
        menu.setName(menuDto.getName());
        menu.setTitle(menuDto.getTitle());
        menu.setPath(menuDto.getPath());
        menu.setIcon(menuDto.getIcon());
        menu.setShowIcon(menuDto.isShowIcon());
        menu.setSort(menuDto.getSort());
        menu.setComponent(menuDto.getComponent());
        menu.setRedirect(menuDto.getRedirect());
        menu.setHidden(menuDto.isHidden());
        menu.setKeepAlive(menuDto.isKeepAlive());
        menu.setPermission(menuDto.getPermission());
        menu.setParentId(menuDto.getParentId());
        
        return menuRepository.save(menu);
    }

    @Override
    public Menu updateFromDto(MenuCreateUpdateDto menuDto) {
        Menu existingMenu = menuRepository.findById(menuDto.getId())
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
        
        // 检查名称是否已被其他菜单使用
        Optional<Menu> menuWithSameName = menuRepository.findByName(menuDto.getName());
        if (menuWithSameName.isPresent() && !menuWithSameName.get().getId().equals(menuDto.getId())) {
            throw new RuntimeException("菜单名称已被其他菜单使用");
        }
        
        // 检查路径是否已被其他菜单使用（如果提供了路径）
        if (StringUtils.hasText(menuDto.getPath())) {
            Optional<Menu> menuWithSamePath = menuRepository.findByPath(menuDto.getPath());
            if (menuWithSamePath.isPresent() && !menuWithSamePath.get().getId().equals(menuDto.getId())) {
                throw new RuntimeException("菜单路径已被其他菜单使用");
            }
        }
        
        // 更新字段
        existingMenu.setName(menuDto.getName());
        existingMenu.setTitle(menuDto.getTitle());
        existingMenu.setPath(menuDto.getPath());
        existingMenu.setIcon(menuDto.getIcon());
        existingMenu.setShowIcon(menuDto.isShowIcon());
        existingMenu.setSort(menuDto.getSort());
        existingMenu.setComponent(menuDto.getComponent());
        existingMenu.setRedirect(menuDto.getRedirect());
        existingMenu.setHidden(menuDto.isHidden());
        existingMenu.setKeepAlive(menuDto.isKeepAlive());
        existingMenu.setPermission(menuDto.getPermission());
        existingMenu.setParentId(menuDto.getParentId());
        
        return menuRepository.save(existingMenu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查是否有子菜单
        List<Menu> children = menuRepository.findByParentIdOrderBySortAsc(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("无法删除有子菜单的菜单，请先删除子菜单");
        }
        
        menuRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        // 检查是否有菜单包含子菜单
        for (Long id : ids) {
            List<Menu> children = menuRepository.findByParentIdOrderBySortAsc(id);
            if (!children.isEmpty()) {
                throw new RuntimeException("无法删除有子菜单的菜单，请先删除子菜单");
            }
        }
        
        menuRepository.deleteAllById(ids);
    }

    @Override
    public List<Menu> findByParentId(Long parentId) {
        return menuRepository.findByParentIdOrderBySortAsc(parentId);
    }

    @Override
    public List<Menu> findTopLevel() {
        return menuRepository.findByParentIdIsNullOrderBySortAsc();
    }

    @Override
    public List<MenuResponseDto> buildMenuTree(List<Menu> menus) {
        // 构建ID到菜单的映射
        Map<Long, MenuResponseDto> menuMap = new HashMap<>();
        List<MenuResponseDto> rootMenus = new ArrayList<>();
        
        // 转换为DTO并建立映射
        for (Menu menu : menus) {
            MenuResponseDto dto = toDto(menu);
            menuMap.put(menu.getId(), dto);
        }
        
        // 构建树形结构
        for (Menu menu : menus) {
            MenuResponseDto dto = menuMap.get(menu.getId());
            if (menu.getParentId() == null) {
                // 顶级菜单
                rootMenus.add(dto);
            } else {
                // 子菜单
                MenuResponseDto parent = menuMap.get(menu.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new HashSet<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }
        
        return rootMenus;
    }

    @Override
    public Optional<Menu> findByName(String name) {
        return menuRepository.findByName(name);
    }

    @Override
    public Optional<Menu> findByPath(String path) {
        return menuRepository.findByPath(path);
    }

    @Override
    public List<Menu> findByPermission(String permission) {
        return menuRepository.findByPermission(permission);
    }

    @Override
    public List<Menu> findByHidden(boolean hidden) {
        return menuRepository.findByHiddenOrderBySortAsc(hidden);
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        if (excludeId == null) {
            return menuRepository.findByName(name).isPresent();
        }
        return menuRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public boolean existsByPath(String path, Long excludeId) {
        if (excludeId == null) {
            return menuRepository.findByPath(path).isPresent();
        }
        return menuRepository.existsByPathAndIdNot(path, excludeId);
    }

    @Override
    public Menu updateSort(Long id, Integer sort) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
        menu.setSort(sort);
        return menuRepository.save(menu);
    }

    @Override
    @Transactional
    public void batchUpdateSort(List<MenuSortDto> sortData) {
        for (MenuSortDto sortDto : sortData) {
            Menu menu = menuRepository.findById(sortDto.getId())
                    .orElseThrow(() -> new RuntimeException("菜单不存在: " + sortDto.getId()));
            menu.setSort(sortDto.getSort());
            if (sortDto.getParentId() != null) {
                menu.setParentId(sortDto.getParentId());
            }
            menuRepository.save(menu);
        }
    }

    @Override
    public List<MenuResponseDto> getAllMenuTree() {
        List<Menu> allMenus = menuRepository.findAll();
        return buildMenuTree(allMenus);
    }

    @Override
    public List<Menu> findByRoleId(Long roleId) {
        // TODO: 实现根据角色ID查找菜单的逻辑
        // 这需要关联查询用户-角色-菜单表
        return new ArrayList<>();
    }

    @Override
    public List<Menu> findByUserId(Long userId) {
        // TODO: 实现根据用户ID查找菜单的逻辑
        // 这需要关联查询用户-角色-菜单表
        return new ArrayList<>();
    }

    @Override
    public List<MenuResponseDto> getUserMenuTree(Long userId) {
        // TODO: 实现获取用户可见菜单树的逻辑
        // 这需要关联查询用户-角色-菜单表，并过滤掉隐藏的菜单
        return new ArrayList<>();
    }

    /**
     * 将Menu实体转换为MenuResponseDto
     */
    private MenuResponseDto toDto(Menu menu) {
        MenuResponseDto dto = new MenuResponseDto();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setTitle(menu.getTitle());
        dto.setPath(menu.getPath());
        dto.setIcon(menu.getIcon());
        dto.setShowIcon(menu.isShowIcon());
        dto.setSort(menu.getSort());
        dto.setComponent(menu.getComponent());
        dto.setRedirect(menu.getRedirect());
        dto.setHidden(menu.isHidden());
        dto.setKeepAlive(menu.isKeepAlive());
        dto.setPermission(menu.getPermission());
        dto.setParentId(menu.getParentId());
        dto.setChildren(new HashSet<>());
        return dto;
    }
} 