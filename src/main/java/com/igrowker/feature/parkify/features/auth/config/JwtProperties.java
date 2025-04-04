package com.igrowker.feature.parkify.features.auth.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "jwt")
@NoArgsConstructor
@AllArgsConstructor
public class JwtProperties {
    @NotBlank(message = "JWT secret cannot be blank")
    private String secret;
    @NotNull(message = "JWT expiration cannot be null")
    private Duration expiration = Duration.ofHours(10);
}