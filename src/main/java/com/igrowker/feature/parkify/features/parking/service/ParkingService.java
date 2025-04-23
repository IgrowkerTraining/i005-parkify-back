package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
     * Retrieves the current availability for a list of parking facilities.
     *
     * @param parkingIds A list of parking IDs to query. Must not be empty.
     * @return A list of DTOs containing availability information for the found parkings.
     *         Parkings not found for the given IDs will be omitted from the result.
     */
    List<ParkingAvailabilityResponse> getParkingsAvailability(
            @NotEmpty(message = "List of parking IDs cannot be empty")
            List<Long> parkingIds
    );

    ParkingDetailsResponse getMyParkingDetails(String ownerEmail);

}



