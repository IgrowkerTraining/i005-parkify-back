package com.igrowker.feature.parkify.features.auth.controller;

import com.github.dockerjava.api.model.AuthResponse;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.service.AuthService;
import org.h2.engine.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private final String testToken = "mockJwtToken123";
    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("kris");
        registerRequest.setEmail("kris@example.com");
        registerRequest.setPassword("Password");

        registerResponse = new RegisterResponse(
                "Token123",
                "krisel@example.com",
                "kris",
                "OWNER"
        );
    }

    @Test
    void login_Success_ShouldReturnOkWithToken() {
        when(authService.login(any(LoginRequest.class))).thenReturn(testToken);

        final ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(testToken, response.getBody().getToken())
        );
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void login_ServiceThrowsException_ShouldPropagateException() {
        final RuntimeException expectedException = new RuntimeException("Authentication failed");
        when(authService.login(any(LoginRequest.class))).thenThrow(expectedException);

        final RuntimeException thrownException = assertThrows(
                RuntimeException.class, () -> authController.login(loginRequest)
        );

        assertEquals(expectedException, thrownException);
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void register_Success_ShouldReturnOkWithRegisterResponse() {
        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        final ResponseEntity<RegisterResponse> response = authController.register(registerRequest);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(registerResponse.getToken(), response.getBody().getToken()),
                () -> assertEquals(registerResponse.getEmail(), response.getBody().getEmail()),
                () -> assertEquals(registerResponse.getUsername(), response.getBody().getUsername()),
                () -> assertEquals(registerResponse.getRole(), response.getBody().getRole())
        );

        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void register_ServiceThrowsException_ShouldPropagateException() {
        RuntimeException expectedException = new RuntimeException("Register failed");
        when(authService.register(any(RegisterRequest.class))).thenThrow(expectedException);

        RuntimeException thrownException = assertThrows(
                RuntimeException.class,
                () -> authController.register(registerRequest)
        );

        assertEquals(expectedException, thrownException);
        verify(authService, times(1)).register(registerRequest);
    }

}
