package com.tiny.oauthserver.sys.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 极简安全集成测试：仅用于验证基于注解的授权分支。
 * 说明：该文件刻意使用 @WebMvcTest 与 @Autowired，其他控制器测试保持纯 mock。
 */
@WebMvcTest(MessagesController.class)
class MessagesControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/messages2 未授权 403")
    void messages2_forbidden() throws Exception {
        mockMvc.perform(get("/messages2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/messages2 授权后 200")
    @WithMockUser(authorities = {"SCOPE_profile"})
    void messages2_authorized() throws Exception {
        mockMvc.perform(get("/messages2"))
                .andExpect(status().isOk());
    }
}


