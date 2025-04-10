package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ParkingService {

    ParkingResponse createParking(ParkingRequest request);

    ParkingResponse updateAvailability(ParkingRequest request);

    ParkingAvailabilityResponse getParkingAvailability(Long parkingId);

    /**
     * Retrieves detailed information about a specific parking facility.
     * @param parkingId The ID of the parking to retrieve.
     * @return A DTO containing the full details of the parking.
     * @throws ParkingNotFoundException if no parking is found with the given ID.
     */
    ParkingDetailsResponse getParkingDetails(Long parkingId);
    ParkingResponse createMyParking(CreateMyParkingRequest request, String ownerEmail);
    PaginatedParkingResponse findNearbyParkings(
            Double latitude, Double longitude, Integer radius,
            Double maxPrice, Integer minAvailability, List<String> featureSlugs,
            int limit, int offset, Pageable pageable
    );

    ParkingAvailabilityResponse updateMyParkingAvailability(
            String ownerEmail,
            @NotNull(message = "Available spots cannot be null")
            @PositiveOrZero(message = "Available spots must be zero or positive")
            Integer availableSpots
    );

    ParkingDetailsResponse getMyParkingDetails(String ownerEmail);

    void associateFeature(String ownerEmail, Long parkingId, String featureSlug);

    void disassociateFeature(String ownerEmail, Long parkingId, String featureSlug);
}



