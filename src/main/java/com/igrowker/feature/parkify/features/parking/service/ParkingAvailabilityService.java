package com.igrowker.feature.parkify.features.parking.service;


import com.igrowker.feature.parkify.features.parking.dto.request.UpdateAvailabilityRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.UpdateAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingAvailabilityService {
    private final ParkingRepository parkingRepository;

    public UpdateAvailabilityResponse updateAvailability(UpdateAvailabilityRequest request) {
        Parking parking = parkingRepository.findById(request.getParkingId())
                .orElseThrow(() -> new RuntimeException("Parking not found"));

        if (request.getAvailableSpots() < 0) {
            throw new IllegalArgumentException("Available spots cannot be negative");
        }

        parking.setAvailableSpots(request.getAvailableSpots());
        parkingRepository.save(parking);

        return UpdateAvailabilityResponse.builder()
                .parkingId(parking.getId())
                .availableSpots(parking.getAvailableSpots())
                .build();
    }
}
