package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.NearbyParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingRepository parkingRepository;

    @Override
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

    @Override
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

    @Transactional(readOnly = true)
    @Override
    public ParkingAvailabilityResponse getParkingAvailability(Long parkingId) {
        final Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ParkingNotFoundException(
                        "Parking not found with id: " + parkingId
                ));
        final int availability = Optional.ofNullable(parking.getAvailableSpots())
                .orElse(0);
        return new ParkingAvailabilityResponse(parking.getId(), availability);
    }

    @Override
    public Page<NearbyParkingResponse> getNearbyParkings(double lat, double lon, Pageable pageable) {
        Page<Object[]> results = parkingRepository.findNearbyParkings(lat, lon, pageable);
        return results.map(row -> {
            Parking parking = (Parking) row[0];
            Double distance = (Double) row[1];

            return NearbyParkingResponse.builder()
                    .id(parking.getId())
                    .name(parking.getName())
                    .address(parking.getAddress())
                    .rateHour(parking.getRateHour())
                    .availableSpots(parking.getAvailableSpots())
                    .distanceInKm(Math.round(distance * 100.0) / 100.0)
                    .build();
        });
    }

    @Override
    public Page<Parking> getParkingDetailsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return parkingRepository.findAll(pageable);
    }
}
