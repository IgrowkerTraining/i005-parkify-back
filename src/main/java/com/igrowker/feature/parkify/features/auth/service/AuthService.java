package com.igrowker.feature.parkify.features.auth.service;


import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;

public interface AuthService {
    String login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
}
