package com.igrowker.feature.parkify.features.parking.dto.request;

import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateParkingRequest {
    @NotBlank(message = "Parking name cannot be blank")
    private String name;
    @NotBlank(message = "Parking address cannot be blank")
    private String address;
    @NotNull(message = "Location cannot be null")
    @Valid
    private LocationDto location;
    private String description;
    @NotNull(message = "Capacity cannot be null")
    @PositiveOrZero(message = "Capacity must be zero or positive")
    private Integer capacity;
    @NotNull(message = "Hourly rate cannot be null")
    @PositiveOrZero(message = "Hourly rate must be zero or positive")
    private Double hourlyRate;
    private String workingHours;
}