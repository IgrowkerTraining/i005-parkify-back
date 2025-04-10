package com.igrowker.feature.parkify.features.config.dto;

public record FeatureFlagsDto(
        Boolean recommendationsEnabled,
        Boolean onlineBookingEnabled) {
}
