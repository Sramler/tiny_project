package com.tiny.oauthserver.sys.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 菜单查询请求DTO
 * 用于接收前端查询参数
 */
public class MenuRequestDto {
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 菜单标题
     */
    private String title;
    
    /**
     * 前端路由路径
     */
    private String path;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 父级菜单ID
     */
    private Long parentId;
    
    /**
     * 是否隐藏
     */
    private Boolean hidden;
    
    /**
     * 页码（从0开始）
     */
    @Min(value = 0, message = "页码不能小于0")
    private Integer page = 0;
    
    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 100, message = "每页大小不能大于100")
    private Integer size = 10;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Boolean getHidden() {
        return hidden;
    }
    
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
} 