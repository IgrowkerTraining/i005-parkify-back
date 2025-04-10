package com.igrowker.feature.parkify.features.recommendation.service;

import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedParkingsResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedZonesResponse;

public interface RecommendationService {
    RecommendedZonesResponse findRecommendedZones(Double latitude, Double longitude, int limit);

    RecommendedParkingsResponse findRecommendedParkings(
            Double latitude, Double longitude, Integer radius, String timeOfDay, int limit
    );
}
