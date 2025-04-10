package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.NearbyParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "")
@RestController
@RequestMapping("/api/v1/parkings")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

    @GetMapping("/{parkingId}/availability")
    public ResponseEntity<ParkingAvailabilityResponse> getParkingAvailability
            (@PathVariable Long parkingId
            ) {
        ParkingAvailabilityResponse response = parkingService.getParkingAvailability(parkingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<ParkingResponse> createParking(@RequestBody ParkingRequest request) {
        ParkingResponse response = parkingService.createParking(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-availability")
    public ResponseEntity<ParkingResponse> updateAvailability(
            @RequestBody ParkingRequest request) {
        return ResponseEntity.ok(parkingService.updateAvailability(request));
    }

    @GetMapping("/nearby")
    public ResponseEntity<Page<NearbyParkingResponse>> getNearbyParkings(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(parkingService.getNearbyParkings(lat, lon, pageable));
    }
}
