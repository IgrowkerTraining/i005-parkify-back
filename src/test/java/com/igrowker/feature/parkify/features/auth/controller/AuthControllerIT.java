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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {
        "jwt.secret=TXlUZXN0U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaDEyMw==",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Sql(statements = {
        "DELETE FROM users WHERE email = 'test.owner.it@example.com';",
        "INSERT INTO users (username, email, password, role) " +
                "VALUES ('testuser', 'test.owner.it@example.com', " +
                "'$2a$10$PK8hd/HO2R/mvqqhdyhS7.QjRF5AKbCMeclnVm.yV6tchnT/CkN6K', " +
                "'OWNER'" +
                ");"
})
class AuthControllerIT {

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(
            DockerImageName.parse("mysql:8.0")
    );
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
    }

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test.owner.it@example.com");
        loginRequest.setPassword("password");
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
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception { // 401 для неверных данных
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_UserNotFound_ShouldReturnUnauthorized() throws Exception {
        loginRequest.setEmail("nonexistent.user@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void login_InvalidRequestBody_MissingEmail_ShouldReturnBadRequest() throws Exception {
        final LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setPassword("password");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_InvalidRequestBody_MissingPassword_ShouldReturnBadRequest() throws Exception {
        final LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("test.owner.it@example.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}