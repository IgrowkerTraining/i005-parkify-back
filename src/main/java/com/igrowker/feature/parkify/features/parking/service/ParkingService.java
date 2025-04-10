package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.NearbyParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkingService {

    ParkingResponse createParking(ParkingRequest request);

    ParkingResponse updateAvailability(ParkingRequest request);

    ParkingAvailabilityResponse getParkingAvailability(Long parkingId);

    Page<NearbyParkingResponse> getNearbyParkings(double lat, double lon, Pageable pageable);

    Page<Parking> getParkingDetailsWithPagination(int page, int size);
}



