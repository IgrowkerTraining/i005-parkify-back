package com.igrowker.feature.parkify.features.auth.service;

import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.entities.Role;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                                request.getEmail(),
                                request.getPassword()
                        )
                )
                .getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        if (authUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email is already in use");
        }

        AuthUser newUser = new AuthUser();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.OWNER);

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
}