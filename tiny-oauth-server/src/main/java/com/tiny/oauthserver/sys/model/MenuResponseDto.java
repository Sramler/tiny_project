package com.tiny.oauthserver.sys.model;

import java.util.Set;

/**
 * 菜单响应DTO
 * 用于返回给前端的数据
 */
public class MenuResponseDto {
    
    /**
     * 菜单ID
     */
    private Long id;
    
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
     * 图标名称
     */
    private String icon;
    
    /**
     * 是否显示图标
     */
    private boolean showIcon;
    
    /**
     * 排序权重
     */
    private Integer sort;
    
    /**
     * Vue组件路径
     */
    private String component;
    
    /**
     * 重定向地址
     */
    private String redirect;
    
    /**
     * 是否隐藏
     */
    private boolean hidden;
    
    /**
     * 是否缓存页面
     */
    private boolean keepAlive;
    
    /**
     * 权限标识
     */
    private String permission;
    
    /**
     * 父级菜单ID
     */
    private Long parentId;
    
    /**
     * 子菜单列表
     */
    private Set<MenuResponseDto> children;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public boolean isShowIcon() {
        return showIcon;
    }
    
    public void setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort;
    }
    
    public String getComponent() {
        return component;
    }
    
    public void setComponent(String component) {
        this.component = component;
    }
    
    public String getRedirect() {
        return redirect;
    }
    
    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
    
    public boolean isHidden() {
        return hidden;
    }
    
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    
    public boolean isKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
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
    
    public Set<MenuResponseDto> getChildren() {
        return children;
    }
    
    public void setChildren(Set<MenuResponseDto> children) {
        this.children = children;
    }
} 