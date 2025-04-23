package com.igrowker.feature.parkify.features.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successful login, containing JWT token, user email, and role")
public record LoginResponse(

        @Schema(
                description = "JWT token for authenticated session",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        String token,

        @Schema(
                description = "Email of the logged-in user",
                example = "owner@example.com"
        )
        String email
) {}
