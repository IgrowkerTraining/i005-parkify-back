package com.igrowker.feature.parkify.features.config.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "parkify.config.initial")
@Data
@Validated
public class InitialConfigProperties {

    @NotNull(message = "Theme colors properties cannot be null")
    @Valid
    private ThemeColorsProperties themeColors = new ThemeColorsProperties();

    @NotNull(message = "Feature flags properties cannot be null")
    @Valid
    private FeatureFlagsProperties featureFlags = new FeatureFlagsProperties();

    @Data
    @Validated
    public static class ThemeColorsProperties {
        @NotBlank(message = "Primary theme color cannot be blank")
        private String primary;
        @NotBlank(message = "Secondary theme color cannot be blank")
        private String secondary;
    }

    @Data
    @Validated
    public static class FeatureFlagsProperties {
        @NotNull(message = "Recommendations enabled flag cannot be null")
        private Boolean recommendationsEnabled;
        @NotNull(message = "Online booking enabled flag cannot be null")
        private Boolean onlineBookingEnabled;
    }
}
