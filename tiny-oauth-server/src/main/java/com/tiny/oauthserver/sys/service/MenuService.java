package com.tiny.oauthserver.sys.service;

import com.tiny.oauthserver.sys.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 菜单服务接口
 * 定义菜单管理的业务方法
 */
public interface MenuService {
    
    /**
     * 分页查询菜单
     * @param query 查询条件
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<MenuResponseDto> menus(MenuRequestDto query, Pageable pageable);
    
    /**
     * 根据ID查找菜单
     * @param id 菜单ID
     * @return 菜单对象
     */
    Optional<Menu> findById(Long id);
    
    /**
     * 创建菜单
     * @param menu 菜单对象
     * @return 创建的菜单
     */
    Menu create(Menu menu);
    
    /**
     * 更新菜单
     * @param id 菜单ID
     * @param menu 菜单对象
     * @return 更新后的菜单
     */
    Menu update(Long id, Menu menu);
    
    /**
     * 从DTO创建菜单
     * @param menuDto 菜单DTO
     * @return 创建的菜单
     */
    Menu createFromDto(MenuCreateUpdateDto menuDto);
    
    /**
     * 从DTO更新菜单
     * @param menuDto 菜单DTO
     * @return 更新后的菜单
     */
    Menu updateFromDto(MenuCreateUpdateDto menuDto);
    
    /**
     * 删除菜单
     * @param id 菜单ID
     */
    void delete(Long id);
    
    /**
     * 批量删除菜单
     * @param ids 菜单ID列表
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 根据父级ID查找子菜单
     * @param parentId 父级菜单ID
     * @return 子菜单列表
     */
    List<Menu> findByParentId(Long parentId);
    
    /**
     * 查找顶级菜单
     * @return 顶级菜单列表
     */
    List<Menu> findTopLevel();
    
    /**
     * 构建菜单树结构
     * @param menus 菜单列表
     * @return 树形结构的菜单列表
     */
    List<MenuResponseDto> buildMenuTree(List<Menu> menus);
    
    /**
     * 根据名称查找菜单
     * @param name 菜单名称
     * @return 菜单对象
     */
    Optional<Menu> findByName(String name);
    
    /**
     * 根据路径查找菜单
     * @param path 前端路径
     * @return 菜单对象
     */
    Optional<Menu> findByPath(String path);
    
    /**
     * 根据权限标识查找菜单
     * @param permission 权限标识
     * @return 菜单列表
     */
    List<Menu> findByPermission(String permission);
    
    /**
     * 根据是否隐藏查找菜单
     * @param hidden 是否隐藏
     * @return 菜单列表
     */
    List<Menu> findByHidden(boolean hidden);
    
    /**
     * 检查菜单名称是否存在
     * @param name 菜单名称
     * @param excludeId 要排除的菜单ID
     * @return 是否存在
     */
    boolean existsByName(String name, Long excludeId);
    
    /**
     * 检查菜单路径是否存在
     * @param path 前端路径
     * @param excludeId 要排除的菜单ID
     * @return 是否存在
     */
    boolean existsByPath(String path, Long excludeId);
    
    /**
     * 更新菜单排序
     * @param id 菜单ID
     * @param sort 新的排序值
     * @return 更新后的菜单
     */
    Menu updateSort(Long id, Integer sort);
    
    /**
     * 批量更新菜单排序
     * @param sortData 排序数据列表
     */
    void batchUpdateSort(List<MenuSortDto> sortData);
    
    /**
     * 获取所有菜单（树形结构）
     * @return 树形结构的菜单列表
     */
    List<MenuResponseDto> getAllMenuTree();
    
    /**
     * 根据角色ID查找菜单
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<Menu> findByRoleId(Long roleId);
    
    /**
     * 根据用户ID查找菜单
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<Menu> findByUserId(Long userId);
    
    /**
     * 获取用户可见的菜单树
     * @param userId 用户ID
     * @return 用户可见的菜单树
     */
    List<MenuResponseDto> getUserMenuTree(Long userId);
} 