package com.igrowker.feature.parkify.features.booking.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateBookingStatusRequest(
        @NotBlank(message = "Status cannot be blank")
        String status) {
}
