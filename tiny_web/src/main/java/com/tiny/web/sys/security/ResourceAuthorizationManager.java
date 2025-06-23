package com.tiny.web.sys.security;

import com.tiny.web.sys.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ResourceAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    @Autowired
    private ResourceService resourceService; // 你可以从数据库加载权限信息

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        String path = context.getRequest().getRequestURI();
        String method = context.getRequest().getMethod();
        Authentication auth = authentication.get();

        // 举例：判断当前用户的角色是否能访问该资源
        boolean hasPermission = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> resourceService.hasAccess(role, path, method)); // 自定义匹配逻辑

        return new AuthorizationDecision(hasPermission);
    }
}