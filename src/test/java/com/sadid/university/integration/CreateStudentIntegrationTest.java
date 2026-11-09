package com.sadid.university.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration test 3: admin creates a student, reads it back from the database,
// and a duplicate create returns 409 Conflict
@SpringBootTest
@AutoConfigureMockMvc
class CreateStudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void admin_createsStudent_thenFetchesItFromDatabase() throws Exception {
        // Log in as the seeded admin; the session keeps the login for the next requests
        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "admin@uni.edu", "password": "admin123"}
                                """))
                .andExpect(status().isOk());

        String body = """
                {
                  "studentId": "IT-1001",
                  "name": "Karim",
                  "email": "karim@uni.edu",
                  "password": "secret123",
                  "department": "CSE",
                  "semester": "3-2",
                  "phone": "01712345678"
                }
                """;

        MvcResult created = mockMvc.perform(post("/api/students")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.studentId").value("IT-1001"))
                .andReturn();

        String location = created.getResponse().getHeader("Location");

        mockMvc.perform(get(location).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Karim"))
                .andExpect(jsonPath("$.email").value("karim@uni.edu"));

        // Same request again -> duplicate email/student ID -> 409 Conflict
        mockMvc.perform(post("/api/students")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
