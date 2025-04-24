package com.igrowker.feature.parkify.features.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.igrowker.feature.parkify.common.service.UriBuilderService;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import com.igrowker.feature.parkify.features.auth.security.SecurityConfig;
import com.igrowker.feature.parkify.features.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtService.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "jwt.secret=TXlUZXN0U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaDEyMw=="
})
class AuthControllerWebLayerTest {

    private final String testToken = "webLayerTestJwtToken789";
    private final String testEmail = "test.web@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    private UriBuilderService uriBuilderService;

    private LoginRequest loginRequest;
    private LoginResponse expectedLoginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest(testEmail, "goodpassword");
        expectedLoginResponse = new LoginResponse(testToken, testEmail);
    }

    @Test
    @DisplayName("POST /login with valid credentials should return OK, Token and Email")
    void login_ValidCredentials_ShouldReturnOkAndTokenAndEmail() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(expectedLoginResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is(testToken)))
                .andExpect(jsonPath("$.email", is(testEmail)));

        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    @DisplayName("POST /login with invalid credentials should return Unauthorized")
    void login_InvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials provided"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login(loginRequest);
    }

    @ParameterizedTest(name = "[{index}] POST /login with invalid body (email={0}, password={1}) should return Bad Request")
    @MethodSource("invalidLoginArgumentsProvider")
    @DisplayName("POST /login with various invalid request bodies should return Bad Request")
    void login_InvalidRequestBody_ShouldReturnBadRequest(String email, String password) throws Exception {
        final LoginRequest invalidRequest = new LoginRequest(email, password);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(0)).login(any(LoginRequest.class));
    }

    private static Stream<Arguments> invalidLoginArgumentsProvider() {
        final String validEmail = "test@example.com";
        final String validPassword = "password123";

        return Stream.of(
                Arguments.of(null, validPassword),
                Arguments.of("", validPassword),
                Arguments.of("invalid-email", validPassword),
                Arguments.of(validEmail, null),
                Arguments.of(validEmail, "")
        );
    }
}