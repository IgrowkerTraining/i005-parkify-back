package com.igrowker.feature.parkify.features.auth.service;

import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
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
    public String login(LoginRequest request) {
        final UserDetails userDetails = (UserDetails) authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                )
                .getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (authUserRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email is already in use");
        }

        AuthUser newUser = new AuthUser();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(Role.OWNER);
        newUser.setContactPhone(request.contactPhone());

        authUserRepository.save(newUser);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                newUser.getEmail(),
                newUser.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + newUser.getRole().name()))
        );

        String token = jwtService.generateToken(userDetails);

        return new RegisterResponse(
                token,
                newUser.getEmail(),
                newUser.getUsername(),
                newUser.getRole().name()
        );
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
}