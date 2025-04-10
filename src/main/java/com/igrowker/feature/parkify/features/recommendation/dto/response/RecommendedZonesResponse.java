package com.igrowker.feature.parkify.features.recommendation.dto.response;

import java.util.List;

public record RecommendedZonesResponse(
        List<ZoneRecommendationDto> zoneRecommendations) {
}