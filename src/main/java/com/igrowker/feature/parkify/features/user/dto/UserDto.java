package com.igrowker.feature.parkify.features.user.dto;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;



    public class RegisterRequest {

        @NotBlank
        private String name;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        @Size(min = 8)
        private String password;

        @NotBlank
        private String role; // debe ser "driver"


    }