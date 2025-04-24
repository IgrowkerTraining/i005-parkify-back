package com.igrowker.feature.parkify.features.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to update the user's email")
public record UpdateEmailRequest(
        @Schema(description = "New email address", example = "newemail@example.com")
        @NotBlank(message = "New email cannot be blank")
        @Email(message = "Invalid email format")
        String newEmail
) {}
