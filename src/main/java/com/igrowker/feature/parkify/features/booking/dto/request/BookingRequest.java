package com.igrowker.feature.parkify.features.booking.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public record BookingRequest(
        @NotBlank(message = "Parking ID cannot be blank")
        String parkingId,
        @FutureOrPresent(message = "Booking time must be in the present or future")
        OffsetDateTime bookingTime) {
}
