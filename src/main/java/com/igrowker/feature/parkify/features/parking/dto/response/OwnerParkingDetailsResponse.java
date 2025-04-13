package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerParkingDetailsResponse {
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private ParkingResponse parking;
}
