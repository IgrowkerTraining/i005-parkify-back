package com.igrowker.feature.parkify.features.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "jwt.secret=TXlUZXN0U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaDEyMw==",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Sql(statements = {
        "DELETE FROM users WHERE email = 'test.owner.it@example.com';", // Оставляем очистку
        "INSERT INTO users (username, email, password, role, contact_phone) " +
                "VALUES ('testuser', 'test.owner.it@example.com', " +
                "'$2a$10$PK8hd/HO2R/mvqqhdyhS7.QjRF5AKbCMeclnVm.yV6tchnT/CkN6K', " +
                "'OWNER', " +
                "'1234567890' );" // <-- Добавлено значение для contact_phone
})
class AuthControllerIT {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15")
    );
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test.owner.it@example.com", "password");
    }

    @Test
    void login_ValidCredentials_ShouldReturnOkAndToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        final LoginRequest loginRequest1 = new LoginRequest(loginRequest.email(), "wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound_ShouldReturnUnauthorized() throws Exception {
        final LoginRequest loginRequest1 = new LoginRequest("nonexistent.user@example.com", loginRequest.password());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest1)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_InvalidRequestBody_MissingEmail_ShouldReturnBadRequest() throws Exception {
        final LoginRequest invalidRequest = new LoginRequest("", "password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_InvalidRequestBody_MissingPassword_ShouldReturnBadRequest() throws Exception {
        final LoginRequest invalidRequest = new LoginRequest("test.owner.it@example.com", "");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}