package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.exception.ParkingNotFoundException;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;

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
    ParkingResponse getParkingDetails(Long parkingId);
}



