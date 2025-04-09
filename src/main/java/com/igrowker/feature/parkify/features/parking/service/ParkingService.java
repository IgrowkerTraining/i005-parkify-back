package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ParkingService {
    private final ParkingRepository parkingRepository;

    public ParkingResponse createParking(ParkingRequest request) {
        Parking parking = new Parking();
        parking.setName(request.getName());
        parking.setAddress(request.getAddress());
        parking.setLatitude(request.getLatitude());
        parking.setLongitude(request.getLongitude());
        parking.setRateHour(request.getRateHour());
        parking.setAvailable(request.getAvailable());
        parking.setWhatsapp(request.getWhatsapp());
        parking.setOwnerId(request.getOwnerId());

        Parking saved = parkingRepository.save(parking);

        return ParkingResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .rateHour(saved.getRateHour())
                .available(saved.getAvailable())
                .whatsapp(saved.getWhatsapp())
                .ownerId(saved.getOwnerId())
                .build();
    }

    public ParkingResponse updateAvailability(ParkingRequest request) {
        Parking parking = parkingRepository.findById(request.getParkingId())
                .orElseThrow(() -> new RuntimeException("Parking not found"));

        if (request.getAvailableSpots() < 0) {
            throw new IllegalArgumentException("Available spots cannot be negative");
        }

        parking.setAvailableSpots(request.getAvailableSpots());
        parkingRepository.save(parking);

        return ParkingResponse.builder()
                .parkingId(parking.getId())
                .availableSpots(parking.getAvailableSpots())
                .build();
    }
}
