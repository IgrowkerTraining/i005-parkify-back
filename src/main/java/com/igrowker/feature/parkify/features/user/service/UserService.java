package com.igrowker.feature.parkify.features.user.service;

import com.igrowker.feature.parkify.features.user.dto.RegisterRequest;
import com.igrowker.feature.parkify.features.user.entities.User;
import com.igrowker.feature.parkify.features.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (!"driver".equalsIgnoreCase(request.getRole())) {
            throw new IllegalArgumentException("Rol must be 'driver'.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("driver");

        userRepository.save(user);
    }
}

