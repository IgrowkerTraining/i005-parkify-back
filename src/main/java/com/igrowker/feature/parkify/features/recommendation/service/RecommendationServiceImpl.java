package com.igrowker.feature.parkify.features.recommendation.service;

import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendationResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedParkingsResponse;
import com.igrowker.feature.parkify.features.recommendation.dto.response.RecommendedZonesResponse;
import com.igrowker.feature.parkify.features.recommendation.entities.OccupancyHistory;
import com.igrowker.feature.parkify.features.recommendation.repository.OccupancyHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final ParkingRepository parkingRepository;
    private final OccupancyHistoryRepository occupancyHistoryRepository;

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

    @Override
    public List<RecommendationResponse> generateRecommendations() {
        List<OccupancyHistory> history = occupancyHistoryRepository.findRecentHistory(LocalDateTime.now().minusDays(7));

        List<Parking> highAvailabilityParkings = parkingRepository.findAll().stream()
                .filter(parking -> isHighAvailability(parking, history))
                .collect(Collectors.toList());

        return highAvailabilityParkings.stream()
                .map(parking -> RecommendationResponse.builder()
                        .parkingId(parking.getId())
                        .name(parking.getName())
                        .address(parking.getAddress())
                        .build())
                .collect(Collectors.toList());
    }

    private boolean isHighAvailability(Parking parking, List<OccupancyHistory> history) {
        double avgOccupancy = calculateAverageOccupancy(parking, history);
        return avgOccupancy < 0.5;
    }

    private double calculateAverageOccupancy(Parking parking, List<OccupancyHistory> history) {
        List<OccupancyHistory> relevant = history.stream()
                .filter(h -> h.getParkingId().equals(parking.getId()))
                .toList();

        return relevant.isEmpty() ? 0.0 :
                relevant.stream().mapToDouble(OccupancyHistory::getOccupancyRate).average().orElse(0.0);
    }
}
