package com.igrowker.feature.parkify.features.user.controller;

import com.igrowker.feature.parkify.features.user.dto.request.LocationUpdateRequest;
import com.igrowker.feature.parkify.features.user.dto.response.PublicUserResponse;
import com.igrowker.feature.parkify.features.user.service.UserService; // Нужен сервис
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // #19
    @PutMapping("/me/location")
    public ResponseEntity<Void> updateMyLocation(
            @Valid @RequestBody LocationUpdateRequest request, Authentication authentication
    ) {
        userService.updateUserLocation(authentication.getName(), request);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // part of #16 and #23
    @GetMapping("/{userId}")
    public ResponseEntity<PublicUserResponse> getUserById(@PathVariable String userId) {
        PublicUserResponse userResponse = userService.getPublicUserById(userId);
        return ResponseEntity.ok(userResponse);
    }
}
