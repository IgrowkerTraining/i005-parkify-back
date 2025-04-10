package com.igrowker.feature.parkify.features.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private String uuid;
    private String email;
    private String username;
    private String role;
}
