package com.tiny.oauthserver.sys.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class MessagesControllerSecurityTest {

    private final MessagesController controller = new MessagesController();

    @Test
    @DisplayName("messages1 返回纯文本")
    void messages1_returnsPlainText() {
        assertEquals(" hello Message 1", controller.getMessages1());
    }

    @Test
    @DisplayName("messages2 声明 SCOPE_profile 权限")
    void messages2_requiresScopeProfile() throws Exception {
        Method method = MessagesController.class.getDeclaredMethod("getMessages2");
        PreAuthorize authorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(authorize, "getMessages2 应声明 PreAuthorize");
        assertEquals("hasAuthority('SCOPE_profile')", authorize.value());
        assertEquals(" hello Message 2", controller.getMessages2());
    }

    @Test
    @DisplayName("messages3 声明 SCOPE_Message 权限")
    void messages3_requiresScopeMessage() throws Exception {
        Method method = MessagesController.class.getDeclaredMethod("getMessages3");
        PreAuthorize authorize = method.getAnnotation(PreAuthorize.class);
        assertNotNull(authorize, "getMessages3 应声明 PreAuthorize");
        assertEquals("hasAuthority('SCOPE_Message')", authorize.value());
        assertEquals(" hello Message 3", controller.getMessages3());
    }
}

