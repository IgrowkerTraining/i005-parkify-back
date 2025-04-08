package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.features.parking.dto.request.CreateParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.CreateParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.CreateParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/parkings")
@RequiredArgsConstructor
public class CreateParkingController {
    private final CreateParkingService createParkingService;

    @PostMapping("/create")
    public ResponseEntity<CreateParkingResponse> createParking(@RequestBody CreateParkingRequest request) {
        CreateParkingResponse response = createParkingService.createParking(request);
        return ResponseEntity.ok(response);
    }
}
