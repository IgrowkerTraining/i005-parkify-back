package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingAvailabilityResponse {
    private Long parkingId;
    private Integer availableSpots;
}
