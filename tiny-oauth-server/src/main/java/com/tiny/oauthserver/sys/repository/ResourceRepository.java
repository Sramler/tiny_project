package com.tiny.oauthserver.sys.repository;

import com.tiny.oauthserver.sys.enums.ResourceType;
import com.tiny.oauthserver.sys.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 资源数据访问层
 * 提供资源的CRUD操作和自定义查询方法
 */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    /**
     * 根据资源类型查找资源
     * @param type 资源类型
     * @return 资源列表
     */
    List<Resource> findByTypeOrderBySortAsc(ResourceType type);
    
    /**
     * 根据父级ID查找子资源
     * @param parentId 父级资源ID
     * @return 子资源列表
     */
    List<Resource> findByParentIdOrderBySortAsc(Long parentId);
    
    /**
     * 查找顶级资源（parentId为null）
     * @return 顶级资源列表
     */
    List<Resource> findByParentIdIsNullOrderBySortAsc();
    
    /**
     * 根据名称查找资源
     * @param name 资源名称
     * @return 资源对象
     */
    Optional<Resource> findByName(String name);
    
    /**
     * 根据路径查找资源
     * @param path 前端路径
     * @return 资源对象
     */
    Optional<Resource> findByPath(String path);
    
    /**
     * 根据URI查找资源
     * @param uri 后端API路径
     * @return 资源对象
     */
    Optional<Resource> findByUri(String uri);
    
    /**
     * 根据权限标识查找资源
     * @param permission 权限标识
     * @return 资源列表
     */
    List<Resource> findByPermission(String permission);
    
    /**
     * 根据HTTP方法查找资源
     * @param method HTTP方法
     * @return 资源列表
     */
    List<Resource> findByMethod(String method);
    
    /**
     * 根据是否隐藏查找资源
     * @param hidden 是否隐藏
     * @return 资源列表
     */
    List<Resource> findByHiddenOrderBySortAsc(boolean hidden);
    
    /**
     * 根据名称模糊查询资源
     * @param name 资源名称（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByNameContainingIgnoreCaseOrderBySortAsc(String name, Pageable pageable);
    
    /**
     * 根据标题模糊查询资源
     * @param title 资源标题（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByTitleContainingIgnoreCaseOrderBySortAsc(String title, Pageable pageable);
    
    /**
     * 根据路径模糊查询资源
     * @param path 前端路径（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByPathContainingIgnoreCaseOrderBySortAsc(String path, Pageable pageable);
    
    /**
     * 根据URI模糊查询资源
     * @param uri 后端API路径（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByUriContainingIgnoreCaseOrderBySortAsc(String uri, Pageable pageable);
    
    /**
     * 根据权限标识模糊查询资源
     * @param permission 权限标识（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByPermissionContainingIgnoreCaseOrderBySortAsc(String permission, Pageable pageable);
    
    /**
     * 根据资源类型分页查询
     * @param type 资源类型
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByTypeOrderBySortAsc(ResourceType type, Pageable pageable);
    
    /**
     * 根据父级ID分页查询
     * @param parentId 父级资源ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Resource> findByParentIdOrderBySortAsc(Long parentId, Pageable pageable);
    
    /**
     * 检查是否存在指定名称的资源（排除指定ID）
     * @param name 资源名称
     * @param id 要排除的资源ID
     * @return 是否存在
     */
    boolean existsByNameAndIdNot(String name, Long id);
    
    /**
     * 检查是否存在指定路径的资源（排除指定ID）
     * @param path 前端路径
     * @param id 要排除的资源ID
     * @return 是否存在
     */
    boolean existsByPathAndIdNot(String path, Long id);
    
    /**
     * 检查是否存在指定URI的资源（排除指定ID）
     * @param uri 后端API路径
     * @param id 要排除的资源ID
     * @return 是否存在
     */
    boolean existsByUriAndIdNot(String uri, Long id);
    
    /**
     * 获取最大排序值
     * @return 最大排序值
     */
    @Query("SELECT COALESCE(MAX(r.sort), 0) FROM Resource r")
    Integer findMaxSort();
    
    /**
     * 根据父级ID获取最大排序值
     * @param parentId 父级资源ID
     * @return 最大排序值
     */
    @Query("SELECT COALESCE(MAX(r.sort), 0) FROM Resource r WHERE r.parentId = :parentId")
    Integer findMaxSortByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据资源类型获取最大排序值
     * @param type 资源类型
     * @return 最大排序值
     */
    @Query("SELECT COALESCE(MAX(r.sort), 0) FROM Resource r WHERE r.type = :type")
    Integer findMaxSortByType(@Param("type") ResourceType type);
    
    /**
     * 复合查询：根据多个条件查询资源
     * @param name 资源名称（可选）
     * @param path 前端路径（可选）
     * @param uri 后端API路径（可选）
     * @param permission 权限标识（可选）
     * @param title 显示标题（可选）
     * @param type 资源类型（可选）
     * @param parentId 父级资源ID（可选）
     * @param hidden 是否隐藏（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT r FROM Resource r WHERE " +
           "(:name IS NULL OR r.name LIKE %:name%) AND " +
           "(:path IS NULL OR r.path LIKE %:path%) AND " +
           "(:uri IS NULL OR r.uri LIKE %:uri%) AND " +
           "(:permission IS NULL OR r.permission LIKE %:permission%) AND " +
           "(:title IS NULL OR r.title LIKE %:title%) AND " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:parentId IS NULL OR r.parentId = :parentId) AND " +
           "(:hidden IS NULL OR r.hidden = :hidden) " +
           "ORDER BY r.sort ASC")
    Page<Resource> findByConditions(
            @Param("name") String name,
            @Param("path") String path,
            @Param("uri") String uri,
            @Param("permission") String permission,
            @Param("title") String title,
            @Param("type") ResourceType type,
            @Param("parentId") Long parentId,
            @Param("hidden") Boolean hidden,
            Pageable pageable
    );
}