package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParkingResponse {
    private Long id;
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
