package com.sadid.university.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test 2: the admin seeded at startup can log in and gets the ADMIN role
@SpringBootTest
@AutoConfigureMockMvc
class AdminLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_withSeededAdmin_returnsAdminRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@uni.edu", "password": "admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@uni.edu"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
