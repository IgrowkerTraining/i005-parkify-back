package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.OwnerNotFoundException;
import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.UpdateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface ParkingService {

    ParkingResponse createParking(ParkingRequest request);

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

    @Deprecated(since = "2045-04-24")
    ParkingResponse updateAvailability(ParkingRequest request);

    /**
     * Updates the available spots for the parking associated with the given owner.
     *
     * @param ownerEmail     The email of the authenticated owner.
     * @param availableSpots The new number of available spots. Must be not null and non-negative.
     * @return A DTO containing the updated availability information.
     * @throws OwnerNotFoundException   if the owner is not found.
     * @throws ParkingNotFoundException if the owner has no associated parking.
     * @throws IllegalArgumentException if availableSpots is negative or exceeds capacity (optional check).
     */
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
     * @throws OwnerNotFoundException   if the owner is not found.
     * @throws ParkingNotFoundException if the owner has no associated parking to delete.
     */
    void deleteMyParking(String ownerEmail);

    /**
     * Retrieves the current availability for a list of parking facilities.
     *
     * @param parkingIds A list of parking IDs to query. Must not be empty.
     * @return A list of DTOs containing availability information for the found parkings.
     * Parkings not found for the given IDs will be omitted from the result.
     */
    List<ParkingAvailabilityResponse> getParkingsAvailability(
            @NotEmpty(message = "List of parking IDs cannot be empty")
            List<Long> parkingIds
    );

    ParkingDetailsResponse getMyParkingDetails(String ownerEmail);

    /**
     * Retrieves summaries for all parkings owned by the specified owner.
     *
     * @param ownerEmail The email of the authenticated owner.
     * @return A list of parking summaries, or an empty list if the owner has no parkings.
     * @throws OwnerNotFoundException if the owner is not found.
     */
    List<ParkingSummaryResponse> getMyParkingSummaries(String ownerEmail);

    ParkingAvailabilityResponse updateSpecificParkingAvailability(
            String ownerEmail,
            Long parkingId,
            @NotNull(message = "Available spots cannot be null")
            @PositiveOrZero(message = "Available spots must be zero or positive")
            Integer integer
    );

    /**
     * Updates the details of a specific parking facility owned by the authenticated user.
     *
     * @param ownerEmail The email of the authenticated owner.
     * @param parkingId  The ID of the parking to update.
     * @param request    The DTO containing the updated parking details.
     * @return A DTO representing the updated parking.
     * @throws OwnerNotFoundException   if the owner is not found.
     * @throws ParkingNotFoundException if the parking with the given ID is not found.
     * @throws AccessDeniedException    if the authenticated user is not the owner of the parking.
     * @throws IllegalArgumentException if validation fails (e.g., capacity < available spots).
     */
    ParkingResponse updateMyParking(String ownerEmail, Long parkingId, @Valid UpdateMyParkingRequest request);
}



