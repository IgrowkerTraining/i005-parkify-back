package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;

public interface ParkingService {

    ParkingResponse createParking(ParkingRequest request);

    ParkingResponse updateAvailability(ParkingRequest request);

    ParkingAvailabilityResponse getParkingAvailability(Long parkingId);
}



