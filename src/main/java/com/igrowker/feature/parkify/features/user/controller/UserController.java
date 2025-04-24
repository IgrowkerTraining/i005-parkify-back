package com.igrowker.feature.parkify.features.user.controller;

import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import com.igrowker.feature.parkify.features.user.service.UserService; // Нужен сервис
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User-related operations")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // #19
    @Operation(
            summary = "Update current user's location (DEPRECATED)",
            description = "DEPRECATED: This endpoint is no longer recommended for use in the primary driver flow. Location is typically passed as query parameters in parking search requests.",
            deprecated = true
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PutMapping("/me/location")
    @Deprecated(since = "2024-04-23", forRemoval = true)
    public ResponseEntity<Void> updateMyLocation(
            @Valid @RequestBody LocationUpdateRequest request, Authentication authentication
    ) {
        userService.updateUserLocation(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    // part of #16 and #23
    @GetMapping("/{userId}")
    public ResponseEntity<PublicUserResponse> getUserById(@PathVariable String userId) {
        PublicUserResponse userResponse = userService.getPublicUserById(userId);
        return ResponseEntity.ok(userResponse);
    }
}
