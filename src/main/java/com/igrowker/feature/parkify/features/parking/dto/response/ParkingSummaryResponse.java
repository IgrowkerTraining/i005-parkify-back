package com.igrowker.feature.parkify.features.parking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.igrowker.feature.parkify.features.parking.dto.LocationDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParkingSummaryResponse {
    private String id;
    private String name;
    private String address;
    private LocationDto location;
    private Double distance;
    private Integer currentAvailability;
    private Double hourlyRate;

    public ParkingSummaryResponse(Long id, String name, String address, Double latitude, Double longitude, @NotNull @PositiveOrZero Double hourlyRate) {
    }
}
