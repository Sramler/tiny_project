package com.tiny.oauthserver.sys.model;

import jakarta.validation.constraints.*;

/**
 * 菜单创建和更新DTO
 * 用于接收前端创建和更新菜单的请求数据
 */
public class MenuCreateUpdateDto {
    
    /**
     * 菜单ID（更新时使用）
     */
    private Long id;
    
    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 100, message = "菜单名称长度不能超过100个字符")
    private String name;
    
    /**
     * 菜单标题
     */
    @NotBlank(message = "菜单标题不能为空")
    @Size(max = 100, message = "菜单标题长度不能超过100个字符")
    private String title;
    
    /**
     * 前端路由路径
     */
    @Size(max = 200, message = "前端路径长度不能超过200个字符")
    private String path;
    
    /**
     * 图标名称
     */
    @Size(max = 200, message = "图标名称长度不能超过200个字符")
    private String icon;
    
    /**
     * 是否显示图标
     */
    private boolean showIcon = true;
    
    /**
     * 排序权重
     */
    @Min(value = 0, message = "排序权重不能小于0")
    @Max(value = 9999, message = "排序权重不能大于9999")
    private Integer sort = 0;
    
    /**
     * Vue组件路径
     */
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;
    
    /**
     * 重定向地址
     */
    @Size(max = 200, message = "重定向地址长度不能超过200个字符")
    private String redirect;
    
    /**
     * 是否隐藏
     */
    private boolean hidden = false;
    
    /**
     * 是否缓存页面
     */
    private boolean keepAlive = false;
    
    /**
     * 权限标识
     */
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permission;
    
    /**
     * 父级菜单ID
     */
    private Long parentId;
    
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
} 