package com.igrowker.feature.parkify.features.auth.service;


import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.UserResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
    UserResponse getCurrentUserDetails(String email);
}
