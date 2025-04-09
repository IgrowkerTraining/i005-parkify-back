package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "")
@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

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

}
