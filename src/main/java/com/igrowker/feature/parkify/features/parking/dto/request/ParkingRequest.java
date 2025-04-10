package com.igrowker.feature.parkify.features.parking.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingRequest {
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double rateHour;
    private int available;
    private String whatsapp;
    private Long ownerId;
    private Long parkingId;
    private int availableSpots;
}
