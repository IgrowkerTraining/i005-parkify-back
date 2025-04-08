package com.igrowker.feature.parkify.features.config.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlagsDto {
    private Boolean recommendationsEnabled;
    private Boolean onlineBookingEnabled;
}
