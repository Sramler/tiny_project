package com.tiny.oauthserver.workflow.camunda;

import com.tiny.oauthserver.sys.service.RoleService;
import com.tiny.oauthserver.sys.service.UserService;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;

/**
 * Provides Camunda with a ReadOnlyIdentityProvider backed by the project's
 * own user/role tables instead of ACT_ID_*.
 */
public class CamundaIdentityProviderSessionFactory implements SessionFactory {

    private final UserService userService;
    private final RoleService roleService;

    public CamundaIdentityProviderSessionFactory(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public Class<?> getSessionType() {
        return ReadOnlyIdentityProvider.class;
    }

    @Override
    public Session openSession() {
        return new CamundaIdentityProvider(userService, roleService);
    }
}


