package com.igrowker.feature.parkify.features.parking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingDetailsResponse {
    private String id;
    private String name;
    private String address;
    private LocationDto location;
    private String description;
    private Integer capacity;
    private Integer currentAvailability;
    private Double hourlyRate;
    private String workingHours;
    private String ownerId;
}
