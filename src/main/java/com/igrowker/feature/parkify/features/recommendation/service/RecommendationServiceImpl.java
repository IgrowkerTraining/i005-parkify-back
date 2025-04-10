package com.igrowker.feature.parkify.features.recommendation.service;

import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedParkingsResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedZonesResponse;
import org.springframework.stereotype.Service;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    @Override
    public RecommendedZonesResponse findRecommendedZones(Double latitude, Double longitude, int limit) {
        return null;
    }

    @Override
    public RecommendedParkingsResponse findRecommendedParkings(
            Double latitude, Double longitude, Integer radius, String timeOfDay, int limit
    ) {
        return null;
    }
}
