package com.igrowker.feature.parkify.features.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull(message = "Latitude cannot be null")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
        Double latitude,

        @NotNull(message = "Longitude cannot be null")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
        Double longitude
) {
}
