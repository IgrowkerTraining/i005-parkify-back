package com.igrowker.feature.parkify.features.auth.service;


import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;

public interface AuthService {
    String login(LoginRequest request);
}
