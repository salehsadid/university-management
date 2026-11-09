package com.sadid.university.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test 1: a protected endpoint without login returns 401 Unauthorized
@SpringBootTest
@AutoConfigureMockMvc
class UnauthorizedAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpoint_withoutLogin_returns401() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isUnauthorized());
    }
}
