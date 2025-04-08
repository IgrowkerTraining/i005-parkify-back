package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.request.UpdateAvailabilityRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.UpdateAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
public class ParkingAvailabilityController {
    private final ParkingAvailabilityService parkingAvailabilityService;

    @PutMapping("/update-availability")
    public ResponseEntity<UpdateAvailabilityResponse> updateAvailability(
            @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(parkingAvailabilityService.updateAvailability(request));
    }
}
