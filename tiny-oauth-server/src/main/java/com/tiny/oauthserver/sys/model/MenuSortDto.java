package com.tiny.oauthserver.sys.model;

/**
 * 菜单排序DTO
 * 用于批量更新菜单排序
 */
public class MenuSortDto {
    
    /**
     * 菜单ID
     */
    private Long id;
    
    /**
     * 排序值
     */
    private Integer sort;
    
    /**
     * 父级菜单ID
     */
    private Long parentId;
    
    // 构造函数
    public MenuSortDto() {}
    
    public MenuSortDto(Long id, Integer sort) {
        this.id = id;
        this.sort = sort;
    }
    
    public MenuSortDto(Long id, Integer sort, Long parentId) {
        this.id = id;
        this.sort = sort;
        this.parentId = parentId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
} 