package com.igrowker.feature.parkify.features.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull(message = "Latitude cannot be null")
        Double latitude,

        @NotNull(message = "Longitude cannot be null")
        Double longitude) {
}
