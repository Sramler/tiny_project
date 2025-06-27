package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.model.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 菜单数据访问层
 * 提供菜单的CRUD操作和自定义查询方法
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    /**
     * 根据父级ID查找子菜单
     * @param parentId 父级菜单ID
     * @return 子菜单列表
     */
    List<Menu> findByParentIdOrderBySortAsc(Long parentId);
    
    /**
     * 根据父级ID查找子菜单（分页）
     * @param parentId 父级菜单ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Menu> findByParentIdOrderBySortAsc(Long parentId, Pageable pageable);
    
    /**
     * 查找顶级菜单（parentId为null）
     * @return 顶级菜单列表
     */
    List<Menu> findByParentIdIsNullOrderBySortAsc();
    
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
    List<Menu> findByHiddenOrderBySortAsc(boolean hidden);
    
    /**
     * 根据名称模糊查询菜单
     * @param name 菜单名称（模糊匹配）
     * @return 菜单列表
     */
    List<Menu> findByNameContainingIgnoreCaseOrderBySortAsc(String name);
    
    /**
     * 根据名称模糊查询菜单（分页）
     * @param name 菜单名称（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Menu> findByNameContainingIgnoreCaseOrderBySortAsc(String name, Pageable pageable);
    
    /**
     * 根据标题模糊查询菜单
     * @param title 菜单标题（模糊匹配）
     * @return 菜单列表
     */
    List<Menu> findByTitleContainingIgnoreCaseOrderBySortAsc(String title);
    
    /**
     * 根据标题模糊查询菜单（分页）
     * @param title 菜单标题（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Menu> findByTitleContainingIgnoreCaseOrderBySortAsc(String title, Pageable pageable);
    
    /**
     * 根据路径模糊查询菜单
     * @param path 前端路径（模糊匹配）
     * @return 菜单列表
     */
    List<Menu> findByPathContainingIgnoreCaseOrderBySortAsc(String path);
    
    /**
     * 根据路径模糊查询菜单（分页）
     * @param path 前端路径（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Menu> findByPathContainingIgnoreCaseOrderBySortAsc(String path, Pageable pageable);
    
    /**
     * 根据权限标识模糊查询菜单
     * @param permission 权限标识（模糊匹配）
     * @return 菜单列表
     */
    List<Menu> findByPermissionContainingIgnoreCaseOrderBySortAsc(String permission);
    
    /**
     * 根据权限标识模糊查询菜单（分页）
     * @param permission 权限标识（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Menu> findByPermissionContainingIgnoreCaseOrderBySortAsc(String permission, Pageable pageable);
    
    /**
     * 检查是否存在指定名称的菜单（排除指定ID）
     * @param name 菜单名称
     * @param id 要排除的菜单ID
     * @return 是否存在
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * 检查是否存在指定路径的菜单（排除指定ID）
     * @param path 前端路径
     * @param id 要排除的菜单ID
     * @return 是否存在
     */
    boolean existsByPathAndIdNot(String path, Long id);
    
    /**
     * 获取最大排序值
     * @return 最大排序值
     */
    @Query("SELECT COALESCE(MAX(m.sort), 0) FROM Menu m")
    Integer findMaxSort();
    
    /**
     * 根据父级ID获取最大排序值
     * @param parentId 父级菜单ID
     * @return 最大排序值
     */
    @Query("SELECT COALESCE(MAX(m.sort), 0) FROM Menu m WHERE m.parentId = :parentId")
    Integer findMaxSortByParentId(@Param("parentId") Long parentId);
} 