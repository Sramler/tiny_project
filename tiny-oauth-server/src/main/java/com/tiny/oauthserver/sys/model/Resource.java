package com.tiny.oauthserver.sys.model;

import com.tiny.oauthserver.sys.enums.ResourceType;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "resource")
public class Resource implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; //后端内部识别名（权限资源名）

    @Column(length = 200)
    private String path; // 前端路由路径，如 /user/list
    
    @Column(length = 200)
    private String uri; // 后端 API 路径，如 /api/user/add

    @Column(length = 10)
    private String method; // GET, POST, PUT, DELETE

    @Column(length = 200)
    private String icon; // 图标，如 UserOutlined

    @Column(name = "show_icon",nullable = false)
    private boolean showIcon; // 是否显示图标

    @Column(name = "sort", nullable = false)
    private Integer sort = 0; // 菜单排序权重，越小越靠前

    @Column(length = 200)
    private String component; // 对应 Vue 路由的组件路径，如 "views/user/UserList.vue"

    @Column(length = 200)
    private String redirect; // 需要重定向的路由地址（用于父菜单）

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false; // 是否在侧边栏隐藏（如详情页不显示）

    @Column(name = "keep_alive", nullable = false)
    private boolean keepAlive = false; // 是否需要缓存页面

    @Column(length = 100)
    private String title; // 前端展示名（菜单显示标题）

    @Column(length = 100)
    private String permission; // 权限标识，用于前端 v-permission 控制


    @Convert(converter = ResourceTypeConverter.class)
    @Column(name = "type", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private ResourceType type = ResourceType.MENU; // MENU, BUTTON, API

    @Column(name = "parent_id")
    private Long parentId; // 菜单结构层级

    @Transient
    private Set<Resource> children = new HashSet<>();

    @ManyToMany(mappedBy = "resources", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();


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

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public Set<Resource> getChildren() {
        return children;
    }

    public void setChildren(Set<Resource> children) {
        this.children = children;
    }
}