package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.user.dto.RegisterRequest;
import com.igrowker.feature.parkify.features.user.entities.User;
import com.igrowker.feature.parkify.features.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

public class AuthService {

    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (!"driver".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("El rol debe ser 'driver'.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("driver");

        userRepository.save(user);
    }
}

