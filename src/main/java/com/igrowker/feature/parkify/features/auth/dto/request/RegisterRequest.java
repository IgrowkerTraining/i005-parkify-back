package com.igrowker.feature.parkify.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Username cannot be blank")
        String username,

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Role cannot be blank")
        @Pattern(regexp = "^(DRIVER|OWNER)$", message = "Role must be either DRIVER or OWNER")
        String role,

        @NotBlank(message = "Contact phone cannot be blank")
        String contactPhone) {
}
