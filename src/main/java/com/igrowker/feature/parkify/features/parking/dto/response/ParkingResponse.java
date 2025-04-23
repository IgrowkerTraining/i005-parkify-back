package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkingResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    private Integer capacity;
    private Integer currentAvailability;
    private Double hourlyRate;
    private String workingHours;
    private Long ownerId;
    private String parkingPhone;
    private String parkingImageUrl;
}
