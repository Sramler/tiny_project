package com.tiny.oauthserver.sys.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MessagesControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new MessagesController()).build();
    }

    @Test
    @DisplayName("/messages1 无需授权 200")
    void messages1_ok() throws Exception {
        mockMvc.perform(get("/messages1"))
                .andExpect(status().isOk());
    }

    // 注意：standaloneSetup 不启用 Spring Security 过滤链，无法在此验证 403/授权分支
}


