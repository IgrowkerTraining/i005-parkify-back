package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.CreateParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.CreateParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.CreateParkingService;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "")
@RestController
@NoArgsConstructor
@RequestMapping("/api/parking")
public class CreateParkingController {
    private final CreateParkingService createParkingService;

    @PostMapping("/create")
    public ResponseEntity<CreateParkingResponse> createParking(@RequestBody CreateParkingRequest request) {
        CreateParkingResponse response = createParkingService.createParking(request);
        return ResponseEntity.ok(response);
    }
}
