package com.igrowker.feature.parkify.features.recommendation.dto.response;

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
public class ZoneRecommendationDto {
    private String zoneId;
    private LocationDto center;
    private String estimatedAvailability;
    private Double averageRate;
}
