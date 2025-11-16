package com.tiny.oauthserver.sys.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    static class DummyController {
        @GetMapping("/dummy/valid")
        public String valid(@Valid DummyReq req) {
            return "ok";
        }
    }

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    static class DummyReq {
        @NotBlank(message = "name不能为空")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Test
    @DisplayName("参数校验失败返回400")
    void validationError_returns400() throws Exception {
        mockMvc.perform(get("/dummy/valid").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isBadRequest());
    }
}


