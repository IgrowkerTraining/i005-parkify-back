package com.igrowker.feature.parkify.features.auth.controller;

import com.igrowker.feature.parkify.common.service.UriBuilderService;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private UriBuilderService uriBuilderService;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private final String testToken = "mockJwtToken123";
    private String expectedUuid = "Token123";
    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password");

        registerRequest = new RegisterRequest(
                "kris", "kris@example.com", "Password",
                "role", "0123456789"
                );

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
    void register_Success_ShouldReturnCreatedWithRegisterResponse() { // Переименовали для ясности
        URI mockLocation = URI.create("http://mock-location/api/v1/users/" + expectedUuid);
        // Arrange: Мокируем сервис, чтобы он вернул ожидаемый ответ
        when(uriBuilderService.buildUserLocationUri(expectedUuid)).thenReturn(mockLocation);
        when(authService.register(any(RegisterRequest.class))).thenReturn(registerResponse);

        // Act: Вызываем метод контроллера напрямую
        final ResponseEntity<RegisterResponse> responseEntity = authController.register(registerRequest);

        // Assert: Проверяем статус-код и тело ответа
        assertAll(
                () -> assertNotNull(responseEntity),
                // Проверяем, что статус CREATED (201), т.к. используется ResponseEntity.created()
                () -> assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode()),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(registerResponse.getUuid(), responseEntity.getBody().getUuid()),
                () -> assertEquals(registerResponse.getEmail(), responseEntity.getBody().getEmail()),
                () -> assertEquals(registerResponse.getUsername(), responseEntity.getBody().getUsername()),
                () -> assertEquals(registerResponse.getRole(), responseEntity.getBody().getRole())
                // Заголовок Location здесь не проверяем!
        );

        // Verify: Убеждаемся, что сервис был вызван
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
