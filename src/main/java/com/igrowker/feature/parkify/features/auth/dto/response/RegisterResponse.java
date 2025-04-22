package com.igrowker.feature.parkify.features.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(description = "Response containing user registration details")
@Data
@AllArgsConstructor
public class RegisterResponse {
    @Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String token;
    private String email;
}
