package com.igrowker.feature.parkify.features.parking.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateAvailabilityRequest(
        @Schema(
                description = "The new number of available spots",
                example = "15",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Available spots cannot be null")
        @PositiveOrZero(message = "Available spots must be zero or positive")
        Integer availableSpots
) {
}
