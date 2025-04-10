package com.igrowker.feature.parkify.features.parking.dto.response;

public record ParkingAvailabilityResponse(
        Long parkingId,
        Integer availableSpots) {
}
