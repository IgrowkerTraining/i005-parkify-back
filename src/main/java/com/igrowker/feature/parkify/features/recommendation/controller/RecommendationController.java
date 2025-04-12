package com.igrowker.feature.parkify.features.recommendation.controller;

import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendationResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedParkingsResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedZonesResponse;
import com.igrowker.feature.parkify.features.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService; // Нужен сервис

    // #28
    @GetMapping("/high-availability")
    public ResponseEntity<List<RecommendationResponse>> getHighAvailabilityRecommendations() {
        List<RecommendationResponse> recommendations = recommendationService.generateRecommendations();
        return ResponseEntity.ok(recommendations);
    }

    @GetMapping("/zones")
    public ResponseEntity<RecommendedZonesResponse> getRecommendedZones(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        RecommendedZonesResponse response = recommendationService.findRecommendedZones(latitude, longitude, limit);
        return ResponseEntity.ok(response);
    }

    // #29
    @GetMapping("/parkings")
    public ResponseEntity<RecommendedParkingsResponse> getRecommendedParkings(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "2000") Integer radius,
            @RequestParam(required = false) String timeOfDay,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        RecommendedParkingsResponse response = recommendationService.findRecommendedParkings(latitude, longitude, radius, timeOfDay, limit);
        return ResponseEntity.ok(response);
    }
}
