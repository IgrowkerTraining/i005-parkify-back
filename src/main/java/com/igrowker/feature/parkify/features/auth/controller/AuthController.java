package com.igrowker.feature.parkify.features.auth.controller;

import com.igrowker.feature.parkify.common.service.UriBuilderService;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.UserResponse;
import com.igrowker.feature.parkify.features.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*") // TODO: Configurar CORS correctamente despues
public class AuthController {

    private final AuthService authService;
    private final UriBuilderService uriBuilderService;

    // #16
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        final String token = authService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // #14, #15
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        final RegisterResponse response = authService.register(request);
        final URI location = uriBuilderService.buildUserLocationUri(response.getUuid());
        return ResponseEntity.created(location)
                .body(response);
    }

    // #12, #17, #18
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        final UserResponse userResponse = authService
                .getCurrentUserDetails(authentication.getName());
        return ResponseEntity.ok(userResponse);
    }
}
