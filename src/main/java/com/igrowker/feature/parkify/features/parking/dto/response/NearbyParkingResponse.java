package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyParkingResponse {
    private Long id;
    private String name;
    private String address;
    private Double rateHour;
    private int availableSpots;
    private double distanceInKm;
}
