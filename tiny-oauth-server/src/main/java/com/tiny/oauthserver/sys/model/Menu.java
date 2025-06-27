package com.tiny.oauthserver.sys.model;

import com.tiny.oauthserver.sys.enums.ResourceType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 菜单实体类
 * 继承自Resource，专门用于菜单管理
 */
@Entity
@Table(name = "menu")
public class Menu implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 菜单名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 菜单标题
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 前端路由路径
     */
    @Column(length = 200)
    private String path;

    /**
     * 图标名称
     */
    @Column(length = 200)
    private String icon;

    /**
     * 是否显示图标
     */
    @Column(name = "show_icon", nullable = false)
    private boolean showIcon = true;

    /**
     * 排序权重
     */
    @Column(name = "sort", nullable = false)
    private Integer sort = 0;

    /**
     * Vue组件路径
     */
    @Column(length = 200)
    private String component;

    /**
     * 重定向地址
     */
    @Column(length = 200)
    private String redirect;

    /**
     * 是否隐藏
     */
    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    /**
     * 是否缓存页面
     */
    @Column(name = "keep_alive", nullable = false)
    private boolean keepAlive = false;

    /**
     * 权限标识
     */
    @Column(length = 100)
    private String permission;

    /**
     * 父级菜单ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 子菜单列表
     */
    @Transient
    private Set<Menu> children = new HashSet<>();

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

    public Set<Menu> getChildren() {
        return children;
    }

    public void setChildren(Set<Menu> children) {
        this.children = children;
    }
} 