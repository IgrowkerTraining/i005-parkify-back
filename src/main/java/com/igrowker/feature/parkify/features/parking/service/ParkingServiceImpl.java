package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import com.igrowker.feature.parkify.features.auth.repository.AuthUserRepository;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParkingServiceImpl implements ParkingService {

    private final ParkingRepository parkingRepository;
    private final AuthUserRepository authUserRepository;

    @Override
    public ParkingResponse createParking(ParkingRequest request) {
        final Parking parking = Parking.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .hourlyRate(request.getRateHour())
                .ownerId(request.getOwnerId())
                .build();
        final Parking saved = parkingRepository.save(parking);
        return ParkingResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .hourlyRate(saved.getHourlyRate())
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
                .id(parking.getId())
                .currentAvailability(parking.getAvailableSpots())
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

    @Transactional(readOnly = true)
    @Override
    public ParkingResponse getParkingDetails(Long parkingId) {
        final Parking parking = parkingRepository.findById(parkingId)
                .orElseThrow(() -> new ParkingNotFoundException("Parking not found with id: " + parkingId));
        final AuthUser owner = authUserRepository.findById(parking.getOwnerId())
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Owner not found with id: "
                                + parking.getOwnerId()
                                + " for parking id: "
                                + parkingId
                ));
        return mapToFlatParkingResponse(parking, owner);
    }

    @Override
    @Transactional // Важно для консистентности
    public ParkingResponse createMyParking(CreateMyParkingRequest request, String ownerEmail) {
        final AuthUser owner = authUserRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new OwnerNotFoundException(
                        "Authenticated owner not found with email: " + ownerEmail
                ));
        final Parking parking = Parking.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .hourlyRate(request.getHourlyRate())
                .workingHours(request.getWorkingHours())
                .features(request.getFeatures())
                .ownerId(owner.getId())
                .availableSpots(request.getCapacity())
                .build();
        final Parking savedParking = parkingRepository.save(parking);
        return mapToFlatParkingResponse(savedParking, owner);
    }

    private ParkingResponse mapToFlatParkingResponse(Parking parking, AuthUser owner) {
        final List<String> featureList = !StringUtils.hasText(parking.getFeatures())
                ? Collections.emptyList()
                : Arrays.stream(parking.getFeatures().split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        return ParkingResponse.builder()
                .id(parking.getId())
                .name(parking.getName())
                .address(parking.getAddress())
                .latitude(parking.getLatitude())
                .longitude(parking.getLongitude())
                .description(parking.getDescription())
                .capacity(parking.getCapacity())
                .currentAvailability(Optional.ofNullable(parking.getAvailableSpots()).orElse(0))
                .hourlyRate(parking.getHourlyRate())
                .workingHours(parking.getWorkingHours())
                .features(featureList)
                .ownerId(owner.getId())
                .build();
    }
}
