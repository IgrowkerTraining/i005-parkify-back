package com.igrowker.feature.parkify.features.parking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMyParkingRequest {

    @NotBlank(message = "Parking name cannot be blank")
    private String name;
    @NotBlank(message = "Parking address cannot be blank")
    private String address;
    @NotNull(message = "Latitude cannot be null")
    private Double latitude;
    @NotNull(message = "Longitude cannot be null")
    private Double longitude;
    private String description;
    @NotNull(message = "Capacity cannot be null")
    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;
    @NotNull(message = "Hourly rate cannot be null")
    @PositiveOrZero(message = "Hourly rate must be zero or positive")
    private Double hourlyRate;
    private String workingHours;
}
