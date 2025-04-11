package com.igrowker.feature.parkify.features.auth.service;

import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "NewUser", "newuser@example.com", "123456",
                "OWNER", "0123456789"
        );
    }

    @Test
    void register_Success_ShouldReturnRegisterResponse() {
        when(authUserRepository.findByEmail(registerRequest.email()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password()))
                .thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class)))
                .thenReturn("mocked-jwt-token");

        final RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertAll(
                () -> assertEquals("mocked-jwt-token", response.getUuid()),
                () -> assertEquals("newuser@example.com", response.getEmail()),
                () -> assertEquals("NewUser", response.getUsername()),
                () -> assertEquals("OWNER", response.getRole())
        );
        verify(authUserRepository, times(1)).save(any(AuthUser.class));
    }

    @Test
    void register_EmailAlreadyExists_ShouldThrowException() {
        when(authUserRepository.findByEmail(registerRequest.email()))
                .thenReturn(Optional.of(new AuthUser()));

        final RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest)
        );

        assertEquals("Email is already in use", exception.getMessage());
        verify(authUserRepository, never()).save(any());
    }
}