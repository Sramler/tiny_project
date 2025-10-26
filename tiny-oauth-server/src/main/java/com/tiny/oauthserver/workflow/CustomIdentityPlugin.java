package com.tiny.oauthserver.workflow;

import com.tiny.oauthserver.sys.service.RoleService;
import com.tiny.oauthserver.sys.service.UserService;
import com.tiny.oauthserver.workflow.camunda.CamundaIdentityProviderSessionFactory;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;

public class CustomIdentityPlugin implements ProcessEnginePlugin {

    private final UserService userService;
    private final RoleService roleService;

    public CustomIdentityPlugin(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
        // 不使用数据库身份表
        processEngineConfiguration.setDbIdentityUsed(false);
        // 注册只读身份提供者工厂
        var factories = processEngineConfiguration.getCustomSessionFactories();
        if (factories == null) {
            factories = new java.util.ArrayList<>();
            processEngineConfiguration.setCustomSessionFactories(factories);
        }
        factories.add(new CamundaIdentityProviderSessionFactory(userService, roleService));
    }

    @Override
    public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {}

    @Override
    public void postProcessEngineBuild(org.camunda.bpm.engine.ProcessEngine processEngine) {}
}