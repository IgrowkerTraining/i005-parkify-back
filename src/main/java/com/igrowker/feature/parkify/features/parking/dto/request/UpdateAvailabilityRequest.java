package com.igrowker.feature.parkify.features.parking.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAvailabilityRequest(
        @NotNull(message = "Available spots cannot be null")
        @PositiveOrZero(message = "Available spots must be zero or positive")
        Integer availableSpots) {
}
