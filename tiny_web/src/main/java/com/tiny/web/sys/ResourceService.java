package com.tiny.web.sys;

public interface ResourceService {
    /**
     * 判断某个角色是否有访问某个资源的权限
     */
    boolean hasAccess(String role, String path, String method);
}