package com.igrowker.feature.parkify.features.auth.service;

import com.igrowker.feature.parkify.exception.EmailAlreadyExistsException;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.UpdateEmailRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.UserResponse;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.entities.Role;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        final UserDetails userDetails = (UserDetails) authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                )
                .getPrincipal();
        final String email = userDetails.getUsername();
        final String token = jwtService.generateToken(userDetails);
        return new LoginResponse(token, email);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (authUserRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado.");
        }

        AuthUser newUser = new AuthUser();

        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));

        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.OWNER);
        newUser.setUsername(request.username() != null ? request.username() : "Owner");
        newUser.setContactPhone(request.contactPhone() != null ? request.contactPhone() : "");

        authUserRepository.save(newUser);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                newUser.getEmail(),
                newUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + newUser.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        return new RegisterResponse(token);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserDetails(String email) {
        final AuthUser authUser = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Authenticated user not found with email: " + email)
                );
        return UserResponse.builder()
                .id(String.valueOf(authUser.getId()))
                .name(authUser.getUsername())
                .email(authUser.getEmail())
                .role(authUser.getRole().name())
                .contactPhone(authUser.getContactPhone())
                .build();
    }

    @Override
    @Transactional
    public void updateEmail(String currentEmail, String newEmail) {
        AuthUser user = authUserRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        if (authUserRepository.findByEmail(newEmail).isPresent()) {
            throw new EmailAlreadyExistsException("El email ya está en uso");
        }

        user.setEmail(newEmail);
        authUserRepository.save(user);
    }

}