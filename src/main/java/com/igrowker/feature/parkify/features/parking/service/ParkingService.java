package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;

public interface ParkingService {

    ParkingResponse createParking(ParkingRequest request);

    ParkingResponse updateAvailability(ParkingRequest request);

    OwnerParkingDetailsResponse getOwnerWithParking(String ownerEmail);

    ParkingAvailabilityResponse getParkingAvailability(Long parkingId);

    /**
     * Retrieves detailed information about a specific parking facility.
     *
     * @param parkingId The ID of the parking to retrieve.
     * @return A DTO containing the full details of the parking.
     * @throws ParkingNotFoundException if no parking is found with the given ID.
     */
    ParkingDetailsResponse getParkingDetails(Long parkingId);

    ParkingResponse createMyParking(CreateMyParkingRequest request, String ownerEmail);

    PaginatedParkingResponse findNearbyParkings(
            Double latitude, Double longitude, Integer radius,
            Double maxPrice, Integer minAvailability,
            int limit, int offset, Pageable pageable
    );

    ParkingAvailabilityResponse updateMyParkingAvailability(
            String ownerEmail,
            @NotNull(message = "Available spots cannot be null")
            @PositiveOrZero(message = "Available spots must be zero or positive")
            Integer availableSpots
    );

    /**
     * Deletes the parking facility associated with the given owner.
     * Assumes an owner has at most one parking for simplicity in MVP.
     *
     * @param ownerEmail The email of the authenticated owner.
     * @throws OwnerNotFoundException if the owner is not found.
     * @throws ParkingNotFoundException if the owner has no associated parking to delete.
     */
    void deleteMyParking(String ownerEmail);

    ParkingDetailsResponse getMyParkingDetails(String ownerEmail);

}



