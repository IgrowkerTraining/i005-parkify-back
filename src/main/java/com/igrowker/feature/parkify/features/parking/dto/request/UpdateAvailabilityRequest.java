package com.igrowker.feature.parkify.features.parking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvailabilityRequest {
    private Long parkingId;
    private int availableSpots;

}
