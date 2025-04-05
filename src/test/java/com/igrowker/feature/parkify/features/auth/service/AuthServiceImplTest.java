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
import org.testcontainers.shaded.com.trilead.ssh2.auth.AuthenticationManager;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

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
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setUsername("NewUser");
        registerRequest.setPassword("123456");
    }

    @Test
    void register_Success_ShouldReturnRegisterResponse() {
        // Arrange
        when(authUserRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(registerRequest.getPassword()))
                .thenReturn("encodedPassword");

        when(jwtService.generateToken(any(User.class)))
                .thenReturn("mocked-jwt-token");

        // Act
        RegisterResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("NewUser", response.getUsername());
        assertEquals("OWNER", response.getRole());

        verify(authUserRepository, times(1)).save(any(AuthUser.class));
    }

    @Test
    void register_EmailAlreadyExists_ShouldThrowException() {
        when(authUserRepository.findByEmail(registerRequest.getEmail()))
                .thenReturn(Optional.of(new AuthUser()));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authService.register(registerRequest)
        );

        assertEquals("Email is already in use", exception.getMessage());
        verify(authUserRepository, never()).save(any());
    }
}