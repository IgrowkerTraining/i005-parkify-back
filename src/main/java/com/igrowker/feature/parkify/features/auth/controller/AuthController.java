package com.igrowker.feature.parkify.features.auth.controller;

import com.igrowker.feature.parkify.common.service.UriBuilderService;
import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.features.auth.dto.request.LoginRequest;
import com.igrowker.feature.parkify.features.auth.dto.request.RegisterRequest;
import com.igrowker.feature.parkify.features.auth.dto.response.LoginResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.RegisterResponse;
import com.igrowker.feature.parkify.features.auth.dto.response.UserResponse;
import com.igrowker.feature.parkify.features.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Authentication", description = "Endpoints related to user authentication and registration")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UriBuilderService uriBuilderService;

    // #16
    @Operation(
            summary = "Login User (#16)",
            description = "Authenticates a user based on email and password, returning a JWT token upon success."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody( // Явное описание RequestBody
            description = "User credentials for login",
            required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class))
    )
    @ApiResponse(responseCode = "200", description = "Authentication successful",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request body (e.g., missing fields, invalid email format)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Authentication failed (Invalid credentials)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        final String token = authService.login(request);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // #14, #15
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided email and password. "
                    + "Optional fields such as username and contact phone may also be provided."
    )
    @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Validation error or email already in use",
            content = @Content(mediaType = "application/json")
    )
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request){
        final RegisterResponse response = authService.register(request);
        final URI location = uriBuilderService.buildUserLocationUri(response.getToken());
        return ResponseEntity.created(location)
                .body(response);
    }

    // #12, #17, #18
    @Operation(
            summary = "Get Current User Details",
            description = "Retrieves details of the currently authenticated user based on the JWT token. Related to tasks #17, #18."
    )
    @ApiResponse(responseCode = "200", description = "User details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT token)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        final UserResponse userResponse = authService
                .getCurrentUserDetails(authentication.getName());
        return ResponseEntity.ok(userResponse);
    }
}
